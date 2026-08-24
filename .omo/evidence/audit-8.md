# MD3 Compliance Audit — core/designsystem (plan todo 8)

Target: `core/designsystem/src/main/kotlin/com/repforge/core/designsystem/`
Date: 2026-08-22
Auditor: github-health-finisher (design lane, task #6)
Method: material-3 skill audit procedure on source code (SKILL.md § MD3 Compliance Audit)
Overall Score: **79/100** — PASS (threshold ≥70; every category ≥7)

## Scores by Category

| Category       | Score | Status |
|----------------|-------|--------|
| Color tokens   | 9/10  | pass |
| Typography     | 8/10  | pass |
| Shape          | 9/10  | pass |
| Elevation      | 7/10  | pass |
| Components     | 7/10  | pass |
| Layout         | 7/10  | pass |
| Navigation     | 7/10  | pass |
| Motion         | 9/10  | pass |
| Accessibility  | 7/10  | pass |
| Theming        | 9/10  | pass |

## Passing

### Color tokens — 9/10
Single token source with full md.sys role coverage in both themes:
- `token/FitnessVisualTokens.kt:125-162` — `fitnessLightColorScheme()` maps all 29+ roles
  (primary/onPrimary, 3 accent container pairs, error quartet, surface + 5 surface-container
  tiers, surfaceDim/Bright, inverse trio, outline/outlineVariant, scrim).
- `token/FitnessVisualTokens.kt:164-201` — `fitnessDarkColorScheme()` full dark mapping.
- Tonal pairing enforced by construction: each `onX` comes from the same brand palette as
  its `X` (e.g. light primaryContainer=Ember90 / onPrimaryContainer=Ember10,
  FitnessVisualTokens.kt:129-130).
- Gate 2 proof: all 64 `Color(0x` occurrences live in this one file; zero elsewhere.
- Warning (−1): 17 `.copy(alpha = …)` overlays on `onSurfaceVariant`/content colors
  (e.g. `component/RestTimer.kt:63`, `component/MetricPill.kt:29`) weaken contrast
  guarantees under dynamic color; replace with dedicated low-emphasis roles later.

### Typography — 8/10
- Google Sans Flex variable axes (wght/wdth/opsz/GRAD/ROND) attached per-role via
  `Font(..., variationSettings)` — `token/RepForgeTypography.kt:25-48`.
- All 15 MaterialTheme type slots now mapped to brand roles (`RepForgeTypography.kt:112-128`),
  so features consuming defaults still get the brand scale.
- Correct role usage in components: Display hero (`component/HeroMetric.kt:67`),
  Metric numerals (`component/RestTimer.kt:75`), Label for buttons/chips
  (`component/MorphingSetButton.kt:59,61`), Body supporting (`component/WorkoutToolbar.kt:54-56`).
- Warning (−2): no separate emphasized type-scale tokens (MD3 expressive emphasized
  variants are approximated by weight-axis copies); acceptable while axes are hand-tuned.

### Shape — 9/10
- md.sys corner scale none→full defined once: `token/FitnessVisualTokens.kt:215-227`
  (`FitnessShapeScale`: 0/4/8/12/16/20/28/32/48dp/full).
- Fitness shape ROLES built on the scale: hero/media/compactAction/achievement/sheet/
  navPill/timer/card tiers — `FitnessVisualTokens.kt:241-293`.
- Baseline `Shapes` wired into MaterialTheme: `theme/RepForgeTheme.kt:44`
  (`shapes = FitnessVisualTokens.materialShapes`, built at `FitnessVisualTokens.kt:229-236`).
- Legacy API preserved by delegation with instance identity intact
  (`token/RepForgeShapes.kt`; asserted by `core/model/src/test/.../RepForgeShapesTest.kt:9-18`).
- Asymmetric morph targets stay literal-dp by design and are documented
  (`FitnessVisualTokens.kt:244-247`) — intentional deviation, not drift.

### Elevation — 7/10
- Depth is communicated tonally per MD3: containers use the surface-container ladder
  (`surfaceContainer` → `Highest`) instead of shadows — e.g. `component/RestTimer.kt:58`
  (surfaceContainerHigh), `component/MetricPill.kt:44` (surfaceContainer),
  `component/ConnectedNumberControl.kt:54` (surfaceContainerHigh vs primaryContainer emphasis).
- No shadow abuse anywhere in the module (grep: no shadow-elevation calls).
- Warning (−3): no explicit elevation-level tokens documented; floating elements
  (nav pill) will need level mapping when features adopt them.

### Components — 7/10
- Expressive components consume only theme roles + shape/type/motion tokens:
  MorphingSetButton (`component/MorphingSetButton.kt:48,59-61`), RestTimer hero
  (`component/RestTimer.kt:58,67,75`), ConnectedNumberControl button-group pattern
  (`component/ConnectedNumberControl.kt:34-40`), WorkoutToolbar/SetRow data table
  (`component/WorkoutToolbar.kt:25-56`).
- Correct tonal pairings throughout (primary+onPrimary at `MorphingSetButton.kt:48,61`;
  secondaryContainer+onSecondaryContainer at `:48,59`).
- Warning (−3): chips/buttons are hand-rolled `Box+clickable` rather than M3 composables;
  acceptable for the expressive morph showcase but M3 Button/Chip variants should be
  preferred where morphing isn't required.

### Layout — 7/10
- 8dp spacing system defined per I/O 2026 guidance: `FitnessVisualTokens.kt:298-306`
  (`FitnessSpacing` xxs4→xxl48).
- Window-size-class dependency already on the module
  (`core/designsystem/build.gradle.kts:18`, material3-windowSize) for adaptive work.
- Documented deviation: component-internal paddings keep bespoke optical values
  (e.g. `RestTimer.kt:62` 22×26dp, `HeroMetric.kt:33` 26×28dp) — deliberate expressive
  optical tuning, not grid drift; screen-level margins/gutters must adopt FitnessSpacing
  when features migrate (todos 11-14).
- Canonical layouts (feed/list-detail/supporting-pane) belong to feature screens — out of
  this module's scope by task constraint.

### Navigation — 7/10
- Nav affordances tokenized: floating pill/bar shapes (`FitnessVisualTokens.kt:278-280`),
  consumed by app NavHost (`app/RepForgeNavHost.kt:47` uses `RepForgeShapes.NavPill`).
- Animated nav icon set with spring selection morph: `icon/RepForgeIcons.kt:41-45`
  (BouncySpring scale), idle bob ambient loop `:36-40`.
- Warning (−3): adaptive navigation switching (bottom bar ↔ rail ↔ drawer by window class)
  lives in feature/app lanes; module provides primitives only.

### Motion — 9/10
- Spring-first vocabulary replacing tween piles: `token/RepForgeMotion.kt:33-42` —
  `emphasized()` ≈500ms, `standard()` ≈400ms, `quick()` ≈200ms settle-time springs,
  plus bouncy/gentle/wobbly expressive variants.
- All one-shot tweens eliminated inside the module: MorphingSetButton content-size morph
  now `standard()` (`component/MorphingSetButton.kt:54`); icon lift/check animations now
  springs (`icon/RepForgeIcons.kt:151-155,172-176`).
- MD3-sanctioned carve-outs documented inline: `infiniteRepeatable` ambient pulses keep
  duration-based specs because springs cannot reverse-repeat
  (`component/RestTimer.kt:41-43`, `icon/RepForgeIcons.kt:36-40`).
- Set→rest morphing vocabulary documented at the API surface (`RepForgeMotion.kt:20-23`):
  press → compress/expand → checkmark → row lift → timer emerges; one focal morph per screen.
- Known limitation (−1): M3 `MotionScheme.expressive()` is internal in material3 1.4.0 —
  adoption deferred, documented at `theme/RepForgeTheme.kt:17-20`.

### Accessibility — 7/10
- Touch targets: rest/control chips enforce 48dp minimums
  (`component/RestTimer.kt:100`, `component/ConnectedNumberControl.kt:53`);
  primary actions exceed 48dp by construction (`MorphingSetButton.kt:55` padding,
  `GiantStartShape` 196dp circle at `:81`).
- Haptic confirmation on focal interactions (`MorphingSetButton.kt:51`,
  `RestTimer.kt:49-52`).
- Text scales via sp throughout the type roles; no fixed-px text.
- Warnings (−3): alpha-reduced label text (17 sites, see Color) needs a contrast pass;
  Canvas-drawn nav icons carry no semantics/contentDescription — they must be paired with
  text labels or given `semantics {}` when features adopt them.

### Theming — 9/10
- One source of truth: `theme/RepForgeTheme.kt:36-38` builds schemes exclusively from
  `FitnessVisualTokens.fitnessDark/LightColorScheme()`; zero inline colors remain in theme.
- Dynamic color fallback path per theming-and-dynamic-color.md: wallpaper schemes on
  Android 12+ behind an explicit opt-in flag, default OFF for brand-first identity
  (`RepForgeTheme.kt:28-34`) — matches the skill's Compose pattern.
- Light/dark parity: both schemes cover identical role sets including inverse/dim/bright
  tiers (`FitnessVisualTokens.kt:125-201`).
- Experimental-API isolation: module compiles without leaking any opt-ins; the only
  experimental surface attempted (MotionScheme) proved internal and was removed.

## Critical Issues

None. No category scored below 7.

## Warnings

1. Alpha overlays on variant text (17 sites) — replace with real low-emphasis roles under
   dynamic color (todos 11-14 migration).
2. Hand-rolled chips instead of M3 components where morphing isn't needed.
3. Nav icon Canvases lack semantics; pair with labels or add contentDescription at adoption.
4. Component-internal optical paddings intentionally off-grid; document per-component when
   features consume them.

## Recommended Fixes (Priority Order)

1. Introduce dedicated low-emphasis text roles to replace `.copy(alpha=…)` overlays.
2. Adopt `MotionScheme.expressive()` when material3 exposes it publicly (tracked in
   RepForgeTheme doc comment).
3. Add `semantics { contentDescription }` to RepForgeNavIconView at feature adoption.
4. Map nav/floating surfaces to explicit MD3 elevation levels when features land.

## Deviations Required by Task Constraints

- **Paparazzi goldens deferred to todo 49** — no screenshot infrastructure added in this
  todo, per assignment instruction. Visual regression coverage therefore scores nowhere
  yet; revisit in the todo-49 audit.
- **feature/**, app/** untouched** — token consumption migrations are todos 11-14; this
  audit covers the designsystem module only.
