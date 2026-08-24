from pathlib import Path

import numpy as np
import polars as pl
from training.data import FEATURE_COLUMNS, generate_synthetic_data, split_history
from training.evaluation import classification_metrics
from training.train import RunConfig, run_training


def test_split_history_keeps_users_and_time_isolated() -> None:
    # Given
    frame = generate_synthetic_data(user_count=20, records_per_user=24, seed=7)

    # When
    splits = split_history(frame, user_holdout_fraction=0.2)

    # Then
    train_users = set(splits.train["user_id"].to_list())
    holdout_users = set(splits.user_holdout["user_id"].to_list())
    assert train_users.isdisjoint(holdout_users)
    latest_train = splits.train.group_by("user_id").agg(pl.col("timestamp").max())
    earliest_temporal = splits.temporal_holdout.group_by("user_id").agg(pl.col("timestamp").min())
    joined = latest_train.join(earliest_temporal, on="user_id", suffix="_test")
    assert (joined["timestamp"] < joined["timestamp_test"]).all()


def test_classification_metrics_are_finite_and_bounded() -> None:
    # Given
    labels = np.array([0, 0, 1, 1], dtype=np.int8)
    probabilities = np.array([0.1, 0.3, 0.7, 0.9], dtype=np.float64)

    # When
    metrics = classification_metrics(labels, probabilities)

    # Then
    assert 0.0 <= metrics.brier <= 1.0
    assert 0.0 <= metrics.ece <= 1.0
    assert metrics.roc_auc == 1.0
    assert metrics.pr_auc == 1.0


def test_training_writes_mobile_artifact_and_metrics(tmp_path: Path) -> None:
    # Given
    config = RunConfig(
        output_dir=tmp_path,
        snapshot=None,
        seed=11,
        user_count=30,
        records_per_user=30,
    )

    # When
    result = run_training(config)

    # Then
    assert result.mobile_artifact.exists()
    assert result.metrics_artifact.exists()
    assert result.mobile_size_bytes < 5 * 1024 * 1024
    assert result.feature_count == len(FEATURE_COLUMNS)
