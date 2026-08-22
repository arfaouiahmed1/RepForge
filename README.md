# RepForge — Material 3 Expressive Strength Training

> **Log your workout incredibly fast. See whether you are actually getting stronger. Let the app learn what load you can handle next.**

Offline-first Android strength training with on-device progression, Health Connect, and a genuine Material 3 Expressive system — not rounded cards.

## Why this exists (portfolio story)

| Project | Story |
|---|---|
| HuntFlow | agents, LLM workflows, RAG |
| SignalRank | retrieval, ranking |
| PitWall ML | streaming, MLOps |
| **RepForge** | **native Android, edge ML, Health APIs, offline sync, subscriptions, expressive UI** |

## Product loop

```
Training Plan → Today's Workout → Log Set (load/reps/RIR) → Performance Model → Next Set & Next Session → more data
```

Five questions: What am I training today? What load/reps? What did I do last time? Am I progressing? How should next workout change?

## Design system — the point

Not `RoundedCornerShape(24.dp)` everywhere. Roles, not radius:

- **Hero** organic large, **PrimaryAction** near-circle, **Metric** soft square, **Media** asymmetrical, **Sheet** XL top, **Nav** floating pill, **Timer** organic
- **Google Sans Flex** variable axes (weight/width/opsz/GRAD/ROND) via `FontVariation.Settings` — bundled TTF, not downloadable
- **Motion** vocabulary: one expressive focal interaction per screen (set complete morph, rest pulse → haptic → NEXT). No cocaine dashboard.
- Hierarchy: `VERY LOUD (Your Mix / PUSH DAY) → LOUD (giant START) → MEDIUM (artwork) → QUIET (nav)`

All tokens live in `core/designsystem` — no feature invents radius/font/spring. Experimental M3 Expressive APIs are isolated there.

## Screens (design spike is done first)

- **Today** — MONDAY / PUSH DAY hero + giant START (180dp circle) + 82% READY secondary + Last Time / Next Goal tertiary
- **Active Workout** — `BARBELL BENCH PRESS / 82.5 KG × 8 / Previous 80×8 @ RIR2 / [COMPLETE SET] / SET LOAD REPS RIR table` + connected controls (`-5 -2.5 +2.5 +5`, long-press keyboard)
- **Rest** — timer is hero (not card), `01:42 remaining / -15 SKIP +15 / NEXT 82.5×8 78% success`
- **Progress** — `94.3 KG est 1RM ↑7.2%` hero + sparkline (not Power BI) + Strength/Volume/Reps/Load toggles
- **Plan / Lab / Profile** — weekly rhythm, Form Lab (CameraX+ML Kit BETA), Health & data

## Stack

```
Kotlin + Compose + Material3 1.4.0 + Navigation + ViewModel/StateFlow + Coroutines/Flow + Hilt + Room + DataStore + WorkManager
Health Connect + CameraX + ML Kit Pose + LiteRT + Firebase (Auth/Firestore/Crashlytics/Remote Config) + Play Billing
Wear Compose + Health Services (later)
Python (uv) + LightGBM + MLflow → LiteRT export
```

Modules: `:app` | `:core:designsystem :core:model :core:database :core:datastore :core:data :core:health :core:ml :core:analytics :core:notifications` | `:feature:today :feature:workout :feature:progress :feature:routine :feature:settings :feature:onboarding :feature:lab :feature:formlab :feature:paywall` | `:wear` | `:baselineprofile`

Offline-first: `Compose → ViewModel → UseCase → Repository → Room` (authoritative during training) → `Sync Queue → Firestore`. App works with no network, no Health Connect, no account.

## Data model

`UserProfile / Exercise / Routine / RoutineExercise / TrainingSession / SessionExercise / SetLog (with recommendationId + acceptance) / PersonalRecord / BodyMetric / Recommendation / RecommendationOutcome / HealthSnapshot / SyncOperation / Entitlement`

`SetLog` preserves raw evidence — never overwritten when recommendation changes.

## Adaptive intelligence — narrow & calibrated

**Problem:** `P(target set completed at prescribed load within desired RIR)` — not "AI creates perfect program."

Features: `exercise, load/est1RM, target_reps, recent RIR/RPE, setsToday, restDuration, daysSince, volume, strengthTrend, sleepDelta, hrDelta, bodyWeightTrend`

Output: `recommended load + success probability (0–1) + explanation` — e.g. `82.5×8 79% — Last:80×8@RIR2, trend improving, fatigue rising`

- **V0 champion:** rule-based (`+2.5 if comfortable, maintain if barely, reduce if failed`)
- **V1:** logistic regression → LightGBM, calibrated. Metrics: Brier, logloss, ROC/PR-AUC, calibration error, cohort breakdown. **Key is calibration** — 80% should succeed 80 times out of 100.
- **Never random-split history** — temporal per user. Evaluate cold-start vs warm, per exercise.
- **Later:** global model + personal rolling stats → on-device fine-tuning via LiteRT `CompiledModel` (V3 research).

Every recommendation becomes training data via `RecommendationOutcome` (accepted/chosen load/actual). Track acceptance rate, calibration, performance after acceptance.

Model delivery: bundled `assets/models/progression_v1.tflite` + remote manifest with SHA-256, schema version, metrics. Fallback to embedded if CDN dies.

## Health & Wear & Live Updates

Health Connect (opt-in, additive): weight, sleep, HR, exercise sessions. Write workouts. No medical claims. Wear: `BENCH 82.5×8 [COMPLETE] / REST 1:34` via `ExerciseClient`.

**Live Update (Android 16+ promoted ongoing)** — for active workouts only (not chats/promos). Triggered by user tapping START (user-initiated, ongoing, time-sensitive, updates 1-2s). Uses `POST_NOTIFICATIONS` + `POST_PROMOTED_NOTIFICATIONS`, `ProgressStyle` (segments = per-exercise, points = per-set, tracker = dumbbell), `setOngoing(true)` + `setRequestPromotedOngoing(true)` + `setOnlyAlertOnce(true)`. 
- **LIFT:** `PUSH DAY — Bench Press 3/4` • `82.5 kg × 8 • 78% success` • chip `82KG` • actions `NEXT`/`END` • thick line = workout % + 7 colored segments.
- **REST:** `REST — 01:42` • `Next: Incline DB Press` • chip countdown via `setWhen(future)+usesChronometer+chronometerCountDown` • actions `+15s`/`SKIP` → morphs to `NEXT SET`. Respects `deleteIntent` — if user dismisses, suppress 5 min, don't re-post. Channel `IMPORTANCE_DEFAULT` (not MIN), title required, no custom RemoteViews. Falls back to standard ongoing on <16. See `core/notifications/liveupdate/LiveWorkoutNotifier.kt:1`.

## Build

**Requires:** JDK 17, Android Studio Ladybug+, AGP 8.9.x (target 9.3 when stable), SDK 36, **uv** for Python.

```bash
./gradlew assembleDebug          # debug APK
./gradlew testDebugUnitTest      # unit + Room + golden ML vectors (output 0–1, latency, checksum)
./gradlew lintDebug              # Android Lint
./gradlew connectedDebugAndroidTest # Compose UI + screenshot

# ML (uv — no pip)
cd ml && uv sync --group dev
uv run python training/train.py --help
```

UI regression: Paparazzi/Showkase for Today/Workout/Rest/Plan/Progress/Paywall × light/dark × font scale.

Baseline Profiles: `baselineprofile` covers `launch → Today → Start → log set → rest → Progress`, measured with Macrobenchmark.

## CI/CD

`pr.yml` (lint, unit, lint, debug APK), `nightly.yml` (API 28/34/36 phone/tablet + Wear + Health Connect + Macrobenchmark), `model-training.yml` (snapshot → temporal split → baseline vs candidate → calibration → champion gate → LiteRT export), `release.yml` (AAB → Play).

App and model pipelines are separate. Promotion requires: `candidate Brier <= champion - threshold AND no cohort regression AND calibration OK AND p95 < target AND size < target AND schema compat`.

## Subscriptions

Free: unlimited logging, history, basic charts, PRs, Health Connect. Pro: adaptive engine, advanced analytics, Form Lab, readiness, sync, Wear insights, export. **Never trust `SharedPreferences("isPro")`** — Play Billing → backend → Play Developer API + RTDN → verified entitlement table.

No ads in V1 — and never during active workout/rest/form camera.

## Roadmap

1. Design spike (done) 2. Training MVP (offline state machine `NOT_STARTED → ACTIVE{SET_IN_PROGRESS,RESTING,PAUSED} → COMPLETED`, persisting across process death) 3. Analytics (est 1RM, volume, adherence) 4. Rule-based progression + instrumentation 5. Health Connect 6. Form Lab BETA 7. Accounts/sync/billing 8. Wear + hardening (accessibility, offline/sync/billing tests, closed testing).

## What to do next

1. Install JDK 17 (`winget install Microsoft.OpenJDK.17` or Android Studio bundled JDK)
2. Install Android Studio, open `RepForge/`, let Gradle sync (needs internet first time)
3. Add `app/google-services.json` (Firebase) and `res/font/google_sans_flex.ttf` (variable TTF from https://github.com/google/fonts)
4. Run on emulator (API 34+): Today → START → log set → see Rest morph → Progress
5. `git init && git add . && git commit -m "feat: RepForge scaffold — M3 Expressive + offline-first + progression engine"` → push → enable GitHub Pages for `web/` landing

## Measured claims (fill with real numbers, not invented)

```
Recommendation Brier  ...
Acceptance rate       ...
Model p95             ... ms
Cold startup          ... ms
Crash-free            ... %
Screenshot coverage   ... screens
```

## License

MIT — do not use Health Connect data for medical diagnosis.
