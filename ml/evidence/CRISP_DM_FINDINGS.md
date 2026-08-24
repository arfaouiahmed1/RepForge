# CRISP-DM findings

## 1. Business understanding

RepForge needs a narrow probability that supports conservative load progression, not an autonomous
program generator or medical readiness score. The business loss is asymmetric: an overconfident
load increase is more harmful than a conservative recommendation. Calibration, cold-start safety,
offline inference, explainability, and fallback behavior therefore outrank raw discrimination.

Success gates are lower Brier on both future-history and unseen-user holdouts, reported logloss/ECE/
PR-AUC/ROC-AUC, no cohort above 10% Brier regression, serialized size under 5 MB, schema compatibility,
and device validation. Synthetic runs are pipeline checks and cannot be promoted.

## 2. Data understanding

Kaggle browsing found useful schema references but no legitimate dataset combining longitudinal
users, prescribed set targets, load, reps, RIR/RPE, recommendation exposure, and observed success.
The only longitudinal strength log reviewed has an unknown license and one athlete. The broader
fitness and sleep datasets are explicitly synthetic and lack the label. Details are in
`evidence/DATASET_RESEARCH.md`.

The minimum production grain is one recommendation-linked set outcome. Raw `SetLog` evidence must
remain immutable. Required governance includes consent/purpose limits for health features, stable
pseudonymous user IDs for grouped evaluation, timestamps, exercise identity, recommendation version,
acceptance/chosen load, and a label definition frozen before extraction.

## 3. Data preparation

The executable fallback creates deterministic product-shaped rows with the same feature families,
then reserves complete users before temporal splitting. Development histories are ordered per user:
60% fit, 20% Platt calibration, 20% future evaluation. Polars performs data generation, schema
projection, filtering, and sorting; NumPy provides numerical arrays. Pandas is absent.

External CSV/Parquet snapshots are accepted only when every contract column is present. Preparation
does not impute missing Health Connect values because a sentinel/mask policy is a product schema
decision that must be tested on observed missingness rather than invented here.

## 4. Modeling

The baseline is standardized logistic regression (`C=0.5`) with Platt calibration. The compact
challenger is histogram gradient boosting with 48 iterations, 15 leaves maximum, and minimum 30
samples per leaf, also Platt calibrated. Logistic exports as an explicit JSON equation; the
challenger pickle exists only to reproduce offline evaluation and is never a mobile artifact.

## 5. Evaluation

The full run used 5,760 synthetic rows: 2,784 fit, 960 calibration, 864 temporal holdout, and 1,152
unseen-user holdout. Logistic achieved temporal/unseen-user Brier of 0.07662/0.10085 and ECE of
0.02051/0.02224. The challenger achieved 0.07537/0.10145 Brier and 0.01579/0.02310 ECE. It failed the
requirement to improve both holdouts, so logistic remains selected. Cold-start and warm challenger
Brier regressions were 2.53% and 0.27%, below the 10% guardrail.

These are generator-recovery results, not evidence of fitness performance. ROC-AUC is modest despite
high PR-AUC because success prevalence is high, reinforcing why probability calibration and Brier
are primary and why prevalence must be monitored on observed data.

## 6. Deployment

The logistic artifact is 1,596 bytes and the offline challenger is 102,002 bytes, both below 5 MB.
Only the logistic JSON has a transparent mobile feature/order/normalization/calibration contract.
Deployment is blocked until observed-data promotion, Android golden-vector parity, missing-feature
semantics, SHA-256 manifest integration, and on-device latency/memory/battery measurements exist.
The rule-based engine must remain the production champion and fallback.
