# RepForge ML

This lane estimates `P(target set completed at prescribed load within desired RIR)`.
It does not generate programs or make medical/readiness claims.

## Reproduce

```powershell
cd ml
uv sync --group dev
uv run python -m training.train --out artifacts
uv run ruff check .
uv run basedpyright
uv run pytest -q
```

Without `--snapshot`, training uses a deterministic synthetic fallback. This is useful for
pipeline verification only and is never presented as product performance. A governed CSV or
Parquet snapshot can be supplied with:

```powershell
uv run python -m training.train --snapshot data/snapshot.parquet --out artifacts
```

The snapshot contract is `user_id`, `timestamp`, `success`, `cohort`, plus the feature order in
`artifacts/progression_logistic_v1.json`. Data preparation holds out complete users, then divides
each remaining user's ordered history into 60% fit, 20% calibration, and 20% temporal evaluation.

## Outputs

- `artifacts/metrics.json`: Brier, logloss, ECE, PR-AUC, ROC-AUC, cohort gates, and sizes.
- `artifacts/progression_logistic_v1.json`: portable standardized logistic model with Platt
  calibration. The explicit float32 feature contract is suitable for a small native/LiteRT
  implementation after Android golden-vector integration.
- `artifacts/hist_gradient_boosting.pkl`: offline challenger for reproducible evaluation only. A
  pickle is not a mobile or trust-boundary format and must never ship in the app.
- `MODEL_CARD.md`: intended use, evaluation, limitations, and promotion status.
- `evidence/DATASET_RESEARCH.md`: Playwright-observed source/license/field review.
- `evidence/CRISP_DM_FINDINGS.md`: business-to-deployment findings.

Promotion requires better Brier on temporal and unseen-user holdouts, no cohort with more than 10%
Brier regression, acceptable calibration, a model below 5 MB, schema compatibility, and Android
golden-vector/latency validation. Synthetic-only runs cannot be promoted regardless of metrics.
