# Dataset research evidence

Research was performed through an unauthenticated Kaggle browser session with Playwright on
2026-08-23. No login was bypassed and no restricted dataset was downloaded.

## 721 Weight Training Workouts

- Source: <https://www.kaggle.com/datasets/joep89/weightlifting>
- Kaggle license: **Unknown**. This excludes it from training or redistribution.
- Observed scope: 721 workouts exported from Strong, covering nearly three years for one person.
- Observed fields: date, workout name, exercise name, set order, weight, reps, distance, seconds,
  notes, and workout notes.
- Useful for: understanding workout-log shape and longitudinal feature engineering.
- Limitations: one athlete; pounds; exercise names are inconsistent; warmups are mostly omitted;
  extreme values may be typos; dumbbell/bodyweight conventions require special handling; no target
  reps, RIR/RPE, recommendation, acceptance, success label, sleep, or heart-rate readiness context.

## Gym Members Exercise Dataset

- Source: <https://www.kaggle.com/datasets/valakhorasani/gym-members-exercise-dataset>
- Kaggle license: Apache-2.0.
- Observed scope: 973 samples and 15 fields. The data card says the data was generated to reflect
  realistic exercise-tracking scenarios, so it is synthetic rather than observed outcomes.
- Observed fields: age, gender, weight, height, max/average/resting BPM, session duration, calories,
  workout type, body-fat percentage, water intake, workout frequency, experience level, and BMI.
- Useful for: schema prototyping for broad session and physiological covariates.
- Limitations: cross-sectional; no stable user history or timestamp; no exercise/set load, reps,
  RIR/RPE, prescribed target, or success outcome. The page also contains conflicting prose saying
  not to use it for research, so even Apache licensing does not make it fitness evidence.

## Sleep Health and Lifestyle Dataset

- Source: <https://www.kaggle.com/datasets/uom190346a/sleep-health-and-lifestyle-dataset>
- Kaggle license: CC0 Public Domain.
- Observed scope: 400 rows and 13 columns. The creator explicitly states the data is synthetic and
  illustrative.
- Observed fields: person ID, gender, age, occupation, sleep duration, sleep quality, physical
  activity, stress, BMI category, blood pressure, resting heart rate, daily steps, sleep disorder.
- Useful for: prototyping nullable readiness covariates and documenting feature semantics.
- Limitations: synthetic, small, cross-sectional, and not linked to workouts or strength outcomes;
  it cannot validate readiness effects or RepForge's target probability.

## Decision

None of the reviewed Kaggle datasets legitimately supplies the full RepForge label and covariate
contract. The pipeline therefore uses deterministic synthetic data only as an openly labeled
fallback. Production evaluation requires consented RepForge recommendation/outcome logs with raw
set evidence preserved, user-level governance, and temporal semantics.
