"""Portable model and metrics artifact contracts."""

import json
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import TypedDict

import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

from training.data import FEATURE_COLUMNS


class MetricsJson(TypedDict):
    generated_at: str
    data_provenance: str
    rows: int
    split_rows: dict[str, int]
    models: dict[str, dict[str, dict[str, float | int]]]
    cohort_brier_regression: dict[str, float]
    cohort_gate_passed: bool
    selected_model: str
    selection_reason: str
    serialized_bytes: dict[str, int]
    mobile_size_limit_bytes: int
    mobile_size_gate_passed: bool


@dataclass(frozen=True, slots=True)
class PlattCalibration:
    slope: float
    intercept: float


def write_mobile_logistic(
    pipeline: Pipeline,
    calibration: PlattCalibration,
    destination: Path,
) -> int:
    scaler = pipeline.named_steps["scale"]
    classifier = pipeline.named_steps["classifier"]
    if not isinstance(scaler, StandardScaler) or not isinstance(classifier, LogisticRegression):
        message = "logistic pipeline has an unsupported preprocessing contract"
        raise TypeError(message)
    payload = {
        "schema_version": 1,
        "model_type": "standardized_logistic_regression_with_platt_calibration",
        "target": "P(target set completed at prescribed load within desired RIR)",
        "feature_order": list(FEATURE_COLUMNS),
        "feature_dtype": "float32",
        "mean": np.asarray(scaler.mean_, dtype=np.float64).tolist(),
        "scale": np.asarray(scaler.scale_, dtype=np.float64).tolist(),
        "coefficients": np.asarray(classifier.coef_, dtype=np.float64)[0].tolist(),
        "intercept": float(np.asarray(classifier.intercept_, dtype=np.float64)[0]),
        "calibration": asdict(calibration),
    }
    destination.write_text(
        json.dumps(payload, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    return destination.stat().st_size


def write_metrics(metrics: MetricsJson, destination: Path) -> None:
    destination.write_text(
        json.dumps(metrics, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
