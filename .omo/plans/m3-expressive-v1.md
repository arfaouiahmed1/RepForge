# RepForge — M3 Expressive V1 Redesign Plan

## Goal
Ship a publishable Play Store V1 that is **genuinely Material 3 Expressive** (not rounded-card generic), offline-first, with a complete exercise DB, working 3D viewers, expressive motion + haptics, animated icons, variable fonts, and green tests. Address the user's complaint "rn it looks like shit" with a cohesive, auditable overhaul.

## Audit — What exists vs what is "shit"

### Designsystem — GOOD BONES, UNDUSED
- `RepForgeShapes` already has role-based shapes: Hero (56/12/16/44), PrimaryActionCircle, Metric, Media asymmetrical, Sheet XL, NavPill 32dp, Timer 40dp. **Problem**: features don't use them consistently; many screens fallback to `CardMedium` or `RoundedCornerShape(24)`.
- `RepForgeColors` is expressive: Paper98 warm #FFF8F4, Ember burned orange-red, Steel vibrant, Brass surprise tertiary. **Problem**: not wired to tonal expressive surfaces (rest immersive dark, workout hero peach).
- `RepForgeTypography` uses `FontVariation.Settings` with 5 axes (weight/width/opsz/GRAD/ROND) but `GoogleSansFlexFallback = FontFamily.Default` — **no bundled TTF**, so axes have no effect. Needs `res/font/google_sans_flex.ttf` + real FontFamily.
- `RepForgeMotion` has expressive springs (Morph 0.72/280, Bouncy 0.55/320, Wobbly) and easings (0.34,0.80,0.34,1.0). **Problem**: only `MorphingSetButton` and `GiantStartShape` use them; RestTimer pulse is infinite but not haptic-choreographed; no shared-element or container transform.
- `RepForgeTheme` isolates experimental MotionScheme correctly, but leaves `MotionScheme.expressive()` commented — should gate behind SDK check when stable.

### Navigation — GENERIC
- `RepForgeNavHost` uses floating pill NavBar (correctly `RepForgeShapes.NavPill`) but `NavigationBarItem.icon = Text(firstLetter)` — **no icons at all**, let alone animated SVG. Labels are there but destinations use `MaterialTheme` plain. Missing: expressive nav morph, selected indicator shape per spec (pill vs squircle), haptics.
- 5 tabs: Today, Plan, Progress, Lab, Profile. Icons currently letters T/P/P/L/P — must become expressive dumbbell/chart/lab icons with animated selection.

### Screens — MIXED

**Today** (`feature/today/TodayScreen.kt`): Actually the best screen. `ExpressiveSplitHero` + `GiantStartShape` (196dp circle) + readiness Metric + Last Time / Next Goal tertiary cards. **Gaps**: readiness is static 82% box, should be animated radial ring with grade-animated typography; Live Update permission banner is an errorContainer block — should be expressive sheet; isRestDay branch is text-only; hero typography not animating on load.

**Active Workout** (`feature/workout/WorkoutScreen.kt:1`): Functional state machine (NOT_STARTED→ACTIVE→RESTING) with real Room flows, `WorkoutLiveUpdateController`, plate calculator, warmup builder. **Gaps**: layout is LazyColumn with plain `WorkoutToolbar` + centered `ConnectedNumberControl` + `MorphingSetButton` + table header Row + `SetRow` items — all linear, no hierarchy (timer should be hero when resting). `GhostWorkout.kt` exists but `ghost` is always null (`lastSet = null // TODO`). `WorkoutFocusMode.kt` exists but unused. Load control uses +/-5/2.5 generic chips. Plates string is opaque formatted text. No expressive chart for volume. Rest is handled via `_rest` but no choreographed morph (spec: control compresses → expands → check → row moves → rest emerges).

**Progress** (`feature/progress/ProgressScreen.kt`): `ProgressHero` + Canvas sparkline (4dp stroke, 6dp dots) + segmented chips + 4 metric cards. **Gaps**: Canvas is primitive (no gradient, no expressive smoothing), metric cards reuse `Metric` shape without differentiation, empty state is one line, no exercise picker that reuses 3D, no calibration badge.

**Routine/Plan** (`feature/routine/RoutineScreen.kt`): Stub. `RoutineViewModel` is empty class, `RoutineScreen()` uses hardcoded `RoutineUiState` (Aug 17-23) with 5 exercise strings. No Room observe, no drag reorder, no additive editing, no expressive week strip.

**Exercise Catalog** (`feature/exercise/ExerciseCatalogScreen.kt`): Best data screen. Real filtering (location/muscle/equipment/query) via Room + fallback seed. **Gaps**: filter chips are static `Box`+clickable, not animated `FilterChip` group with morph; rows are plain CardMedium; muscle map not visualized; stats pills are Boxes; no illustrated empty state; Detail navigation works but list has no hero images.

**Exercise Detail** (`feature/exercise/ExerciseDetailScreen.kt`): Has 3-mode `SegmentedButtonRow` (MOTION|MUSCLES|SETUP) but ViewModel is empty, exercise is hardcoded `Exercise(id=exerciseId, name="Barbell Bench Press"...)` ignoring DB, `ExerciseModelViewer` and `MuscleModelViewer` are stubs (Box placeholder). Needs real 3D.

**Settings/Profile** (`feature/settings/SettingsScreen.kt`): Actually correct (profile editor with BMI via `profileRepo.logBodyMetric`, notifications scheduler). **Gaps**: typography not expressive, cards are CardMedium without role differentiation, no validation feedback choreography.

**Onboarding** (`feature/onboarding/OnboardingScreen.kt`): 21 lines, `REP FORGE` DisplayHeroLarge + blurb + GiantStartShape. **Gaps**: no page controller, no illustration, no motion.

**Paywall** (`feature/paywall/PaywallScreen.kt`): Text bullet list + Pro CardLarge with price pill + GiantStartShape trial. **Gaps**: no pricing toggle animation, no feature comparison expressive table.

**Lab/FormLab/Achievements**: Lab has placeholder `ModelInsightsPlaceholder` text. FormLab likely similar stub. Achievements not read yet but likely list-only.

### 3D — STUBBED
- `core/threeD/ExerciseModelViewer.kt` (15 lines) returns empty `Box` when not null — "disabled for Kotlin 2.1 demo".
- `core/threeD/MuscleModelViewer.kt` similar.
- `core/threeD/ModelCache.kt` exists but small. No SceneView / Filament dependency in `core/threeD/build.gradle.kts` likely missing. GLB assets referenced in `SeedData` (models/*.glb) don't exist on disk — need procedural fallback + real viewer.
- Heat map `muscleHeat: Map<String,Float>` is in model but only 4 exercises have heat strings.

### Exercise DB — THIN
- `ExerciseSeed.exercises` = 60, but `ex()` helper uses `difficulty="INTERMEDIATE"` for all, `description = "$name — compound strength movement"` for all, empty `instructions/setupCues/commonMistakes/alternatives`. Only 4 have heat. No thumbnails, no instructions, no movementPattern, trackingType via default. Covers chest/back/legs/shoulders/arms/core but missing: forearms, neck, cardio, mobility, carries. `RoutineSeed` has 8 routines but `routineExercises` only fully populates PPL (15 entries) + partial upper/lower/5x5 — leaves Full Body A/B incomplete.

### Icons + SVG Animated — MISSING
- No `core/designsystem/icon` module. No `RepForgeIcons` object. No AnimatedVectorDrawable. Nav uses Text letters. Throughout screens, icons are either absent or material default not imported.

### Typography — NOT EXPRESSIVE IN PRACTICE
- `GoogleSansFlexFallback = FontFamily.Default` — variable axes do nothing. No `res/font/google_sans_flex.ttf`. No `providesFontVariation` on SDK 26+. No animation of axes on hero entry.
- Type roles exist (DisplayHero 62sp W800 width 75% opsz 72, MetricLarge 52sp W700, LabelExpressive 11sp) but used as static `Text(style=...)` without orchestrated hierarchy (VERY LOUD → QUIET) timing.

### Tests — ZERO
- Glob shows 84 .kt files, none are `*Test.kt` except `core/testing/ScreenshotTest.kt` which is a stub. No `testDebugUnitTest` passes beyond compilation, no Room in-memory tests, no compose UI tests, no Paparazzi. `detekt.yml` exists but no config verification. CI `pr.yml` likely just assembles.

### Build / Publish readiness
- Requires JDK 17, AGP 8.9.x, SDK 36, `uv` for ML. Not verified if `settings.gradle.kts` includes new modules (`core:threeD`, `feature:exercise`, `feature:achievements` are there). Missing: signing config, baselineprofile integration, bundle config, `google-services.json` present but unknown validity, `res/font` missing.

## Scope Decomposition — 8 Parallel Workstreams

### 1. Icons & Animated SVG [core/designsystem]
- Create `core/designsystem/src/main/kotlin/.../icon/RepForgeIcons.kt` + `AnimatedRepForgeIcon` composable using `rememberAnimatedVectorPainter` / Lottie alternative via `AnimatedVectorDrawable` composition. 12 icons: Today (calendar/sun), Workout (dumbbell animated lift), Progress (sparkline morph), Plan (layers), Lab (beaker), Profile (person), plus toolbar icons (play, rest, check, plates, ghost).
- Migrate `RepForgeNavHost.NavigationBarItem.icon` from `Text(firstLetter)` to expressive icons with selection morph (scale + ROND axis pulse).
- Add icon gallery to `DesignSystemCatalog`.

### 2. Navigation + Today + Workout Choreography [feature/*]
- Rework `RepForgeNavHost`: floating pill with `tonalElevation 6dp`, expressive selected indicator (Pill 50% → Squircle 36dp on select), spring 0.55/320, haptics. Add nav-state preservation + motion.
- Today: animate readiness radial (0→82% with `animateFloatAsState` + `RepForgeMotion.BouncySpring`), animate hero `DisplayHero` weight 800→900 on entry, add rest-day expressive variant (organic shape, not text).
- Workout: implement full morph choreography — `onCompleteSet` triggers: button compress 0.96 → checkmark expansion → `SetRow` slide-up with `animateContentSize` → RestTimer organic emerge with `RepForgeShapes.Timer` → haptic pulse → LiveUpdate `completeSet`. Wire ghost vs last session (query `setLogDao.observeForExercise`), surface `FocusMode` hide chrome, plate visualization via `PlateCalculator`.

### 3. Exercise DB Expansion [core/database + core/model]
- Expand `SeedData.kt` 60 → 120 entries: add forearms/neck, mobility (band pull-apart, face-pull already, add band work), carries (already farmers), rotational (already woodchopper, add cable rotations), cardio/mobility crossovers, and missing variations (close-grip pushup, diamond pushup, step-up, etc.).
- For each, fill: `instructions` (3 steps), `setupCues` (2), `executionCues` (2), `commonMistakes` (2), `alternatives`/`regressions`/`progressions` linking ex ids, `movementPattern`, `trackingType`, `defaultRestSeconds` tuned per compound vs isolation, `muscleHeat` per entry, `difficulty` varied, `license/attribution` if sourced. Source from wger/open catalog (CC-BY) but hand-curate for V1.
- Fix `RoutineSeed`: complete Full A/B, add mobility circuit.
- Migration: bump DB version, fallback via `DatabaseSeeder`.

### 4. 3D Viewers [core/threeD]
- Add `com.google.android.filament:filament-android` + `io.github.sceneview:sceneview` dependency (check `core/threeD/build.gradle.kts`). Implement `ExerciseModelViewer` with `ModelViewer` + `Lifecycle` + `SurfaceView` fallback to procedural muscle illustration when `glbAsset` missing. Ship 4 canonical GLBs (bench/squat/deadlift/ohp) as placeholders + procedural fallback for rest (colored primitive + heat tint). Wire `highlightMuscles` to material tint via `ModelViewer.setMaterialColor`.
- `MuscleModelViewer`: render anterior anatomy with heat overlay (pass `exercise.muscleHeat` to shader/tint). Add `ModelCache` disk+mem LRU.
- Detail screen: swipe rotate, pinch zoom, timeline drag (already spec) via `SceneView` gestures.

### 5. Typography & Fonts [core/designsystem]
- Bundle `res/font/google_sans_flex.ttf` (variable TTF). Create `FontFamily(Font(R.font.google_sans_flex, variationSettings=...))`. Wire `RepForgeTypography` to use real family not `Default`. Add `flexVariation` usage per role + entry animations (hero `GRAD 0→80` + `ROND 0→12` choreography via `animateIntAsState`). Verify on API 26+ (FontVariation requires O+). Add `ProvideExpressiveFonts` composition local.

### 6. Tests [all modules]
- Unit: `PlateCalculatorTest`, `WarmupBuilderTest`, `estimated1RMTest`, `RepForgeShapesTokenTest` (role distinctness), `SeedDataIntegrityTest` (counts, unique ids, valid heat), `SubstitutionGraphTest`.
- Room: `ExerciseDaoTest`, `SetLogDaoTest`, `RoutineDaoTest` with in-memory `Robolectric`.
- Compose UI: `TodayScreenTest`, `WorkoutScreenTest`, `ExerciseCatalogScreenTest` with `composeTestRule` — verify filters, empty states, morph button.
- Screenshot: Paparazzi/Showkase for Today/Workout/Rest/Progress light/dark/fontScale (stub ready via `core/testing/ScreenshotTest.kt` — expand).
- Target: 40+ tests, `./gradlew testDebugUnitTest` green, lint clean.

### 7. Remaining Screens Polish [feature/*]
- Progress: replace primitive Canvas with expressive chart — gradient filled path + bezier smoothing + tooltip morph, reuse `Metric` shapes for toggles with spring.
- Routine: make editable — drag reorder via `LazyColumn` `animateItem`, observe `RoutineDao`, allow add/remove exercises, week strip expressive dot morph.
- Onboarding: 3 pages + PageController + illustration hero + motion between.
- Paywall: animated price toggle (monthly/yearly) with squircle morph, comparison table with expressive check icons.
- Achievements/FormLab: surface streak/PR cards with metric shapes + animated icons.
- Settings: validation choreography (BMI field shake on invalid).

### 8. Build / Publish [app, baselineprofile, CI]
- Verify `settings.gradle.kts` modules, wire `core:threeD` + `feature:exercise` navigation graph fully. Add signing via `local.properties` template, bump versionCode/name for V1. Ensure `baselineprofile` covers launch→Today→Start→log set→rest→Progress. Wire `pr.yml` to run lint+unit+debug APK; ensure `lintDebug` passes. Generate release AAB with `assembleRelease`. Validate `google-services.json` presence.

## Constraints & Non-Goals
- Do NOT invent 60 perfect GLB models — ship 4 real + procedural fallback, document asset pipeline.
- Do NOT use `as any` / `@ts-ignore` / suppress Compose lint — match disciplined codebase (configs present, Hilt singletons).
- Offline-first remains authoritative: Room is source during training; sync queue via Firestore is additive. No network during workout.
- Do not trust SharedPreferences isPro — keep Play Billing → verified entitlement table (already correctly not trusted).
- Greenfield typography: variable axes only on O+, graceful degrade pre-O.

## Dependencies
- Requires network for first Gradle sync; thereafter offline.
- Font TTF from https://github.com/google/fonts (Google Sans Flex) — large ~1MB, verify license.
- Filament/SceneView adds ~3MB native libs — confirm Wear min SDK compatibility (likely exclude from wear module).

## Exit Criteria (publishable V1)
- All 5 nav destinations render expressive (not text letters), with animated icons.
- Today/Workout/Rest have choreographed motion + haptics, ghost vs last, plates.
- 120 exercises browsable, filterable, each with detail (motion/muscles/setup), 4 with real 3D, rest procedural.
- Variable font axes visible (fallback not Default) when TTF bundled.
- 40+ tests green, `./gradlew testDebugUnitTest` + `lintDebug` pass.
- Baseline profile module builds, AAB generated, no crash on emulator API 34 flow: Today→START→log set→rest→Progress.
- `DesignSystemCatalog` shows all roles.

## Execution
Spawn 6 deep subagents in parallel (icons/type, nav/today/workout, DB, 3D, tests, polish) + 1 oracle reviewer. Sisyphus orchestrates, verifies via `lsp_diagnostics` + `./gradlew testDebugUnitTest` on root, never does `background_cancel(all=true)`.
