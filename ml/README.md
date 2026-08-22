# RepForge ML — progression model

Uses **uv** for Python. No `pip install -r requirements.txt`.

## Setup

```bash
# install uv: https://docs.astral.sh/uv/getting-started/installation/
# Windows: powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"

cd ml
uv sync              # creates .venv and installs deps from pyproject.toml + uv.lock
uv sync --group dev  # + ruff/pytest
```

## Daily use

```bash
cd ml
uv run python training/validate.py
uv run python training/train.py --snapshot data/snapshot.parquet --out artifacts/
uv run python training/evaluate.py --candidate artifacts/candidate.pkl --champion artifacts/champion.json
uv run python training/benchmark.py --model artifacts/candidate.tflite
uv run python training/export.py

# lint / test
uv run ruff check .
uv run pytest
```

## Why uv

- 10–100× faster than pip, deterministic `uv.lock`
- `uv run` auto-uses `.venv` — no manual `source .venv/bin/activate`
- CI uses `astral-sh/setup-uv@v5` with cache

## Lockfile

`uv.lock` is committed. To add/upgrade a dep:

```bash
cd ml
uv add "lightgbm==4.6.0"
uv sync
```

Do not hand-edit `pyproject.toml` versions without `uv lock --upgrade`.

## Model contract

Problem: `P(target set completed at prescribed load within desired RIR)` — not "AI program generator."

- Never random-split a user's history — temporal split only (`train = history < cutoff`)
- Must beat rule-based champion on **Brier** + calibration + cohort breakdown + mobile p95 latency + size
- Every `Recommendation` → `RecommendationOutcome` becomes future training data (see `core/ml/ProgressionEngine.kt`)
