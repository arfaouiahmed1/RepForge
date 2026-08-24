"""Train and gate RepForge's compact success-probability model."""

from __future__ import annotations

import argparse
import pickle
from dataclasses import dataclass
from pathlib import Path
from typing import Final

import numpy as np
import polars as pl
from sklearn.ensemble import HistGradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

from training.artifacts import MetricsJson, PlattCalibration, write_metrics, write_mobile_logistic
from training.data import (
    FEATURE_COLUMNS,
    DatasetSplits,
    feature_matrix,
    generate_synthetic_data,
    load_snapshot,
    split_history,
)
from training.evaluation import (
    ProbabilityMetrics,
    classification_metrics,
    relative_brier_regression,
)

SIZE_LIMIT_BYTES: Final = 5 * 1024 * 1024
REGRESSION_LIMIT: Final = 0.10


Estimator = Pipeline | HistGradientBoostingClassifier


@dataclass(frozen=True, slots=True)
class RunConfig:
    output_dir: Path
    snapshot: Path | None
    seed: int = 20260823
    user_count: int = 120
    records_per_user: int = 48


@dataclass(frozen=True, slots=True)
class TrainingResult:
    mobile_artifact: Path
    metrics_artifact: Path
    mobile_size_bytes: int
    feature_count: int
    selected_model: str


@dataclass(frozen=True, slots=True)
class FittedModel:
    name: str
    estimator: Estimator
    calibration: PlattCalibration

    def predict(self, features: np.ndarray) -> np.ndarray:
        raw_probabilities = np.asarray(self.estimator.predict_proba(features), dtype=np.float64)
        raw = np.clip(raw_probabilities[:, 1], 1e-6, 1.0 - 1e-6)
        logits = np.log(raw / (1.0 - raw))
        calibrated_logit = self.calibration.slope * logits + self.calibration.intercept
        return 1.0 / (1.0 + np.exp(-calibrated_logit))


def _labels(frame: pl.DataFrame) -> np.ndarray:
    return frame["success"].to_numpy().astype(np.int8, copy=False)


def _fit_calibrated(
    name: str,
    estimator: Estimator,
    splits: DatasetSplits,
) -> FittedModel:
    estimator.fit(feature_matrix(splits.train), _labels(splits.train))
    raw_probabilities = np.asarray(
        estimator.predict_proba(feature_matrix(splits.calibration)),
        dtype=np.float64,
    )
    raw = np.clip(
        raw_probabilities[:, 1],
        1e-6,
        1.0 - 1e-6,
    )
    logits = np.log(raw / (1.0 - raw)).reshape(-1, 1)
    calibrator = LogisticRegression(C=1000.0, random_state=0, solver="liblinear").fit(
        logits,
        _labels(splits.calibration),
    )
    return FittedModel(
        name=name,
        estimator=estimator,
        calibration=PlattCalibration(
            slope=float(np.asarray(calibrator.coef_, dtype=np.float64)[0, 0]),
            intercept=float(np.asarray(calibrator.intercept_, dtype=np.float64)[0]),
        ),
    )


def _evaluate(model: FittedModel, frame: pl.DataFrame) -> ProbabilityMetrics:
    return classification_metrics(_labels(frame), model.predict(feature_matrix(frame)))


def _cohort_metrics(
    model: FittedModel,
    frame: pl.DataFrame,
) -> dict[str, ProbabilityMetrics]:
    return {
        cohort: _evaluate(model, frame.filter(pl.col("cohort") == cohort))
        for cohort in sorted(frame["cohort"].unique().to_list())
    }


def _model_metrics(
    model: FittedModel,
    splits: DatasetSplits,
) -> dict[str, dict[str, float | int]]:
    result = {
        "temporal": _evaluate(model, splits.temporal_holdout).as_json(),
        "user_holdout": _evaluate(model, splits.user_holdout).as_json(),
    }
    for cohort, metrics in _cohort_metrics(model, splits.user_holdout).items():
        result[f"user_holdout:{cohort}"] = metrics.as_json()
    return result


def run_training(config: RunConfig) -> TrainingResult:
    frame = (
        load_snapshot(config.snapshot)
        if config.snapshot is not None
        else generate_synthetic_data(config.user_count, config.records_per_user, config.seed)
    )
    provenance = (
        "external_snapshot" if config.snapshot is not None else "deterministic_synthetic_fallback"
    )
    splits = split_history(frame)
    logistic = _fit_calibrated(
        "logistic_regression",
        Pipeline(
            [
                ("scale", StandardScaler()),
                (
                    "classifier",
                    LogisticRegression(
                        C=0.5,
                        max_iter=500,
                        random_state=config.seed,
                        solver="liblinear",
                    ),
                ),
            ]
        ),
        splits,
    )
    challenger = _fit_calibrated(
        "hist_gradient_boosting",
        HistGradientBoostingClassifier(
            learning_rate=0.06,
            max_iter=48,
            max_leaf_nodes=15,
            min_samples_leaf=30,
            random_state=config.seed,
        ),
        splits,
    )
    model_metrics = {
        logistic.name: _model_metrics(logistic, splits),
        challenger.name: _model_metrics(challenger, splits),
    }
    baseline_cohorts = _cohort_metrics(logistic, splits.user_holdout)
    challenger_cohorts = _cohort_metrics(challenger, splits.user_holdout)
    cohort_regressions = {
        cohort: relative_brier_regression(
            challenger_cohorts[cohort].brier,
            baseline_cohorts[cohort].brier,
        )
        for cohort in baseline_cohorts
    }
    cohort_gate = all(value <= REGRESSION_LIMIT for value in cohort_regressions.values())
    baseline_temporal = model_metrics[logistic.name]["temporal"]
    challenger_temporal = model_metrics[challenger.name]["temporal"]
    baseline_user_holdout = model_metrics[logistic.name]["user_holdout"]
    challenger_user_holdout = model_metrics[challenger.name]["user_holdout"]
    challenger_wins = bool(
        challenger_temporal["brier"] < baseline_temporal["brier"]
        and challenger_user_holdout["brier"] < baseline_user_holdout["brier"]
        and cohort_gate
    )
    selected_model = challenger.name if challenger_wins else logistic.name
    reason = (
        "challenger improved temporal Brier and passed every cohort gate"
        if challenger_wins
        else (
            "logistic retained: challenger did not improve Brier on both temporal and unseen-user "
            "holdouts within cohort gates"
        )
    )
    config.output_dir.mkdir(parents=True, exist_ok=True)
    mobile_artifact = config.output_dir / "progression_logistic_v1.json"
    if not isinstance(logistic.estimator, Pipeline):
        message = "mobile export only supports the logistic pipeline"
        raise TypeError(message)
    mobile_size = write_mobile_logistic(
        logistic.estimator,
        logistic.calibration,
        mobile_artifact,
    )
    challenger_artifact = config.output_dir / "hist_gradient_boosting.pkl"
    challenger_artifact.write_bytes(pickle.dumps(challenger, protocol=pickle.HIGHEST_PROTOCOL))
    serialized_sizes = {
        "progression_logistic_v1.json": mobile_size,
        "hist_gradient_boosting.pkl": challenger_artifact.stat().st_size,
    }
    metrics: MetricsJson = {
        "generated_at": "2026-08-23T00:00:00Z",
        "data_provenance": provenance,
        "rows": frame.height,
        "split_rows": {
            "train": splits.train.height,
            "calibration": splits.calibration.height,
            "temporal_holdout": splits.temporal_holdout.height,
            "user_holdout": splits.user_holdout.height,
        },
        "models": model_metrics,
        "cohort_brier_regression": cohort_regressions,
        "cohort_gate_passed": cohort_gate,
        "selected_model": selected_model,
        "selection_reason": reason,
        "serialized_bytes": serialized_sizes,
        "mobile_size_limit_bytes": SIZE_LIMIT_BYTES,
        "mobile_size_gate_passed": mobile_size < SIZE_LIMIT_BYTES,
    }
    metrics_artifact = config.output_dir / "metrics.json"
    write_metrics(metrics, metrics_artifact)
    return TrainingResult(
        mobile_artifact=mobile_artifact,
        metrics_artifact=metrics_artifact,
        mobile_size_bytes=mobile_size,
        feature_count=len(FEATURE_COLUMNS),
        selected_model=selected_model,
    )


def _parse_args() -> RunConfig:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--snapshot", type=Path)
    parser.add_argument("--out", type=Path, default=Path("artifacts"))
    parser.add_argument("--seed", type=int, default=20260823)
    arguments = parser.parse_args()
    return RunConfig(output_dir=arguments.out, snapshot=arguments.snapshot, seed=arguments.seed)


def main() -> None:
    result = run_training(_parse_args())
    print(
        f"selected={result.selected_model} mobile_bytes={result.mobile_size_bytes} "
        f"metrics={result.metrics_artifact}"
    )


if __name__ == "__main__":
    main()
