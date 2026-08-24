# Model card: RepForge progression logistic v1

## Summary

- Status: **not eligible for product promotion**; pipeline-validation artifact only.
- Target: probability that a prescribed load and rep target is completed within desired RIR.
- Artifact: `artifacts/progression_logistic_v1.json`.
- Training data: 5,760 deterministic synthetic rows across 120 synthetic users.
- Size: 1,596 bytes, below the 5 MB mobile gate.

## Intended use

The eventual model may rank conservative next-set or next-session load options after the user logs
real set outcomes. It must remain advisory, explainable, offline-capable, and subordinate to the
rule-based fallback. It is not for injury prediction, diagnosis, treatment, or generalized athlete
readiness scoring.

## Inputs and output

Inputs are ordered float32 values: exercise code, load/estimated-1RM ratio, target reps, recent RIR,
recent RPE, sets today, rest seconds, days since exposure, strength trend, sleep delta, resting-HR
delta, and body-weight trend. Output is a Platt-calibrated probability in `[0, 1]`.

Missing Health Connect inputs must be resolved by the future app-side schema contract before
inference. This training artifact does not define sentinel values.

## Evaluation design

Complete users were reserved as an unseen-user holdout (1,152 rows). For all other users, ordered
history was split into fit (2,784), calibration (960), and future temporal holdout (864). No user
history was randomly split. The compact HistGradientBoosting challenger used 48 iterations and at
most 15 leaves. Promotion required lower Brier on both holdouts and no user cohort above 10% Brier
regression relative to logistic regression.

| Model / split | Brier | Logloss | ECE | PR-AUC | ROC-AUC |
|---|---:|---:|---:|---:|---:|
| Logistic / temporal | 0.07662 | 0.28526 | 0.02051 | 0.94502 | 0.60281 |
| Challenger / temporal | 0.07537 | 0.28180 | 0.01579 | 0.94188 | 0.59549 |
| Logistic / unseen user | 0.10085 | 0.34431 | 0.02224 | 0.94030 | 0.70242 |
| Challenger / unseen user | 0.10145 | 0.34731 | 0.02310 | 0.93820 | 0.69247 |

The challenger improved temporal Brier but regressed unseen-user Brier, so logistic regression was
retained. Challenger Brier regression was 2.53% for cold-start and 0.27% for warm users, both within
the 10% guardrail. These figures describe only the synthetic generator and must not be used in app,
portfolio, research, or marketing performance claims.

## Limitations and risks

- Synthetic labels encode assumptions and cannot establish real calibration, causality, or value.
- Exercise is represented by a numeric code in this fixture; production encoding must avoid false
  ordinal meaning and define unknown/cold-start behavior.
- Small cold-start cohorts can hide failures for rare exercises, demographics, and accessibility
  needs. Production gates need minimum support and confidence intervals.
- Sleep and heart-rate values are wellness context, not medical signals. Opt-in, minimization,
  deletion, and no-diagnosis constraints apply.
- The logistic JSON is portable but not yet wired to Android. The pickle challenger is offline-only.

## Deployment requirements

Before promotion, retrain on governed observed outcomes; freeze a feature schema; add Android golden
vectors; measure on-device p50/p95 latency, memory, and battery; verify SHA-256 delivery; test missing
health inputs; and demonstrate calibration/cohort gates on a time-forward release candidate. Until
then, the embedded rule engine remains champion.
