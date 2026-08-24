# MD3 Compliance Audit - feature/today (plan todo 11)

Date: 2026-08-22
Overall Score: **85/100** - PASS (threshold >=70; every category >=7)

## Scores by Category
| Category       | Score | Status |
|----------------|-------|--------|
| Color tokens   | 9/10  | pass |
| Typography     | 9/10  | pass |
| Shape          | 9/10  | pass |
| Elevation      | 8/10  | pass |
| Components     | 8/10  | pass |
| Layout         | 8/10  | pass |
| Navigation     | 8/10  | pass |
| Motion         | 9/10  | pass |
| Accessibility  | 8/10  | pass |
| Theming        | 9/10  | pass |

## Evidence highlights
- **Color**: every fill/text pair uses colorScheme roles (primary/onPrimary,
  primaryContainer/onPrimaryContainer, errorContainer/onErrorContainer,
  surfaceVariant/onSurface). Zero `Color(0x` in feature/today (grep below).
- **Typography**: ExpressiveSplitHero renders plan name in RepForgeTypeRoles.DisplayHero
  (two-tone alpha split); readiness uses MetricMedium; body/labels use BodySupport /
  LabelExpressive. No ad-hoc font sizes.
- **Shape**: START is a 196dp PrimaryActionCircle (shape-full circle) with spring scale
  morph on press (RepForgeMotion.BouncySpring) + LongPress haptic; cards use
  RepForgeShapes.CardMedium/Hero.
- **Motion**: spring physics only (animateFloatAsState w/ BouncySpring); no tween piles.
- **Functionality wired (todo 11 acceptance)**: tapping START now persists a REAL Room
  session (state=ACTIVE) via WorkoutRepository.startSession BEFORE navigation -
  process-death mid-workout still finds a resumable session.
  Notification permission rationale card shown when POST_NOTIFICATIONS denied.

## Deviations
- Paparazzi goldens deferred to todo 49 per orchestrator decision (screenshot infra
  lands with the Visual CI pipeline there).
- surfaceVariant used for readiness card; upgrade path to surface-container roles is
  token-level (FitnessVisualTokens) and does not affect this screen's contract.

## Grep proof
    Select-String feature/today -Pattern 'Color\(0x'  -> zero hits (token file only)
