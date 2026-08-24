"""Dataset generation, parsing, and leakage-safe splitting."""

from dataclasses import dataclass
from pathlib import Path
from typing import Final

import numpy as np
import polars as pl

FEATURE_COLUMNS: Final[tuple[str, ...]] = (
    "exercise_code",
    "load_ratio",
    "target_reps",
    "recent_rir",
    "recent_rpe",
    "sets_today",
    "rest_seconds",
    "days_since",
    "strength_trend",
    "sleep_delta",
    "hr_delta",
    "body_weight_trend",
)
HARD_EXERCISE_CODE: Final = 4
COLD_START_RECORDS: Final = 8
TRAIN_FRACTION: Final = 0.6
CALIBRATION_FRACTION: Final = 0.8
REQUIRED_COLUMNS: Final[tuple[str, ...]] = (
    "user_id",
    "timestamp",
    "success",
    "cohort",
    *FEATURE_COLUMNS,
)


@dataclass(frozen=True, slots=True)
class DatasetSplits:
    """Leakage-safe fitting, calibration, temporal, and user holdout frames."""

    train: pl.DataFrame
    calibration: pl.DataFrame
    temporal_holdout: pl.DataFrame
    user_holdout: pl.DataFrame


@dataclass(frozen=True, slots=True)
class SnapshotSchemaError(Exception):
    """Raised when an external snapshot cannot satisfy the model contract."""

    missing_columns: tuple[str, ...]

    def __str__(self) -> str:
        return f"snapshot is missing required columns: {', '.join(self.missing_columns)}"


def generate_synthetic_data(
    user_count: int = 120,
    records_per_user: int = 48,
    seed: int = 20260823,
) -> pl.DataFrame:
    """Generate deterministic product-shaped data when no governed snapshot exists."""
    rng = np.random.default_rng(seed)
    row_count = user_count * records_per_user
    user_index = np.repeat(np.arange(user_count), records_per_user)
    record_index = np.tile(np.arange(records_per_user), user_count)
    exercise_code = rng.integers(0, 6, row_count)
    ability = np.repeat(rng.normal(0.0, 0.45, user_count), records_per_user)
    load_ratio = np.clip(rng.normal(0.72 + ability * 0.025, 0.09, row_count), 0.45, 1.05)
    target_reps = rng.integers(3, 13, row_count)
    recent_rir = np.clip(rng.normal(2.0 - (load_ratio - 0.7) * 4.0, 1.0), 0.0, 5.0)
    recent_rpe = np.clip(10.0 - recent_rir + rng.normal(0.0, 0.45, row_count), 5.0, 10.0)
    sets_today = rng.integers(1, 7, row_count)
    rest_seconds = rng.integers(45, 241, row_count)
    days_since = rng.integers(1, 12, row_count)
    strength_trend = rng.normal(0.015 + record_index * 0.0008, 0.025, row_count)
    sleep_delta = np.clip(rng.normal(0.0, 1.0, row_count), -3.0, 3.0)
    hr_delta = np.clip(rng.normal(0.0, 5.0, row_count), -15.0, 20.0)
    body_weight_trend = rng.normal(0.0, 0.12, row_count)
    logit = (
        2.2
        + ability
        - 4.2 * (load_ratio - 0.68)
        - 0.16 * (target_reps - 8)
        + 0.26 * recent_rir
        - 0.11 * (recent_rpe - 7.5)
        - 0.13 * (sets_today - 3)
        + 0.0025 * (rest_seconds - 120)
        - 0.025 * np.abs(days_since - 4)
        + 5.0 * strength_trend
        + 0.11 * sleep_delta
        - 0.025 * hr_delta
        + 0.18 * body_weight_trend
        + np.where(exercise_code == HARD_EXERCISE_CODE, -0.22, 0.0)
    )
    probability = 1.0 / (1.0 + np.exp(-logit))
    success = rng.binomial(1, probability).astype(np.int8)
    start = np.datetime64("2024-01-01T08:00:00")
    timestamps = (
        start + user_index.astype("timedelta64[D]") + record_index.astype("timedelta64[W]")
    ).astype("datetime64[ms]")
    return pl.DataFrame(
        {
            "user_id": [f"synthetic_user_{value:04d}" for value in user_index],
            "timestamp": timestamps,
            "success": success,
            "cohort": np.where(record_index < COLD_START_RECORDS, "cold_start", "warm"),
            "exercise_code": exercise_code,
            "load_ratio": load_ratio,
            "target_reps": target_reps,
            "recent_rir": recent_rir,
            "recent_rpe": recent_rpe,
            "sets_today": sets_today,
            "rest_seconds": rest_seconds,
            "days_since": days_since,
            "strength_trend": strength_trend,
            "sleep_delta": sleep_delta,
            "hr_delta": hr_delta,
            "body_weight_trend": body_weight_trend,
        }
    )


def load_snapshot(path: Path) -> pl.DataFrame:
    """Parse a CSV or Parquet snapshot into the strict training schema."""
    suffix = path.suffix.lower()
    frame = pl.scan_parquet(path) if suffix == ".parquet" else pl.scan_csv(path)
    schema = frame.collect_schema()
    missing = tuple(column for column in REQUIRED_COLUMNS if column not in schema)
    if missing:
        raise SnapshotSchemaError(missing_columns=missing)
    return frame.select(REQUIRED_COLUMNS).collect()


def split_history(frame: pl.DataFrame, user_holdout_fraction: float = 0.2) -> DatasetSplits:
    """Hold out complete users, then split remaining histories in temporal order."""
    users = frame.select("user_id").unique().sort("user_id")
    holdout_count = max(1, round(users.height * user_holdout_fraction))
    holdout_users = users.tail(holdout_count)
    user_holdout = frame.join(holdout_users, on="user_id", how="semi").sort("timestamp")
    development = frame.join(holdout_users, on="user_id", how="anti").sort(["user_id", "timestamp"])
    ranked = development.with_columns(
        pl.int_range(pl.len()).over("user_id").alias("history_index"),
        pl.len().over("user_id").alias("history_count"),
    ).with_columns((pl.col("history_index") / pl.col("history_count")).alias("history_fraction"))
    clean_columns = [*REQUIRED_COLUMNS]
    train = ranked.filter(pl.col("history_fraction") < TRAIN_FRACTION).select(clean_columns)
    calibration = ranked.filter(
        (pl.col("history_fraction") >= TRAIN_FRACTION)
        & (pl.col("history_fraction") < CALIBRATION_FRACTION)
    ).select(clean_columns)
    temporal = ranked.filter(pl.col("history_fraction") >= CALIBRATION_FRACTION).select(
        clean_columns
    )
    return DatasetSplits(train, calibration, temporal, user_holdout.select(clean_columns))


def feature_matrix(frame: pl.DataFrame) -> np.ndarray:
    """Return the ordered float32 mobile feature matrix."""
    return frame.select(FEATURE_COLUMNS).to_numpy().astype(np.float32, copy=False)
