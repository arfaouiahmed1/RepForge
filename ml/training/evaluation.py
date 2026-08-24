"""Probability metrics and cohort promotion gates."""

from dataclasses import asdict, dataclass

import numpy as np
from sklearn.metrics import (
    average_precision_score,
    brier_score_loss,
    log_loss,
    roc_auc_score,
)


@dataclass(frozen=True, slots=True)
class ProbabilityMetrics:
    """Calibration and ranking metrics for a binary probability model."""

    brier: float
    logloss: float
    ece: float
    pr_auc: float
    roc_auc: float
    samples: int

    def as_json(self) -> dict[str, float | int]:
        return asdict(self)


def expected_calibration_error(
    labels: np.ndarray,
    probabilities: np.ndarray,
    bins: int = 10,
) -> float:
    """Compute equal-width expected calibration error."""
    boundaries = np.linspace(0.0, 1.0, bins + 1)
    indices = np.minimum(np.digitize(probabilities, boundaries[1:-1]), bins - 1)
    error = 0.0
    for index in range(bins):
        selected = indices == index
        if np.any(selected):
            weight = float(np.mean(selected))
            observed = float(np.mean(labels[selected]))
            predicted = float(np.mean(probabilities[selected]))
            error += weight * abs(observed - predicted)
    return error


def classification_metrics(
    labels: np.ndarray,
    probabilities: np.ndarray,
) -> ProbabilityMetrics:
    """Evaluate calibrated probabilities without threshold-dependent metrics."""
    clipped = np.clip(probabilities, 1e-7, 1.0 - 1e-7)
    return ProbabilityMetrics(
        brier=float(brier_score_loss(labels, clipped)),
        logloss=float(log_loss(labels, clipped)),
        ece=expected_calibration_error(labels, clipped),
        pr_auc=float(average_precision_score(labels, clipped)),
        roc_auc=float(roc_auc_score(labels, clipped)),
        samples=int(labels.size),
    )


def relative_brier_regression(candidate: float, baseline: float) -> float:
    """Return positive fractional degradation relative to the logistic baseline."""
    return (candidate - baseline) / baseline
