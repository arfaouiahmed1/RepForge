# RepForge Design System

## 1. Atmosphere & Identity

RepForge feels like a warm, precise training workshop: forged charcoal provides weight,
paper surfaces keep the product human, and ember appears only where action or progress
deserves heat. The signature is the Forged R, an anvil-topped monogram with a forward
cut that suggests both a loaded bar and measurable upward progress. Brand moments are
restrained; one strong mark and one meaningful motion beat repeated decoration.

## 2. Color

`FitnessVisualTokens` in `core/designsystem` is the canonical Compose source. Android
resource colors mirror these tokens only where the platform requires XML resources for
launcher icons and the system splash screen.

| Role | Compose token | Light | Dark | Usage |
|---|---|---:|---:|---|
| Primary | `colorScheme.primary` | Ember 40 `#9C2A00` | Ember 80 `#FFB59E` | Primary action, active progress |
| Primary container | `colorScheme.primaryContainer` | Ember 90 `#FFDBCF` | Ember 30 `#7A2200` | Brand focal surfaces |
| Background / surface | `colorScheme.background` | Paper 98 `#FFF8F4` | Warm forge `#201A18` | App canvas |
| On surface | `colorScheme.onSurface` | Forge 10 `#1A1C1E` | Warm paper `#EDE0DB` | Primary text and icons |
| Surface container | `colorScheme.surfaceContainer` | `#F8EEE8` | `#271E1C` | Grouped content |
| Surface container high | `colorScheme.surfaceContainerHigh` | `#F2E6DF` | `#322823` | Elevated tonal sections |
| Secondary | `colorScheme.secondary` | Steel 40 `#005F9E` | Steel 80 `#8FCCFF` | Informational emphasis |
| Tertiary | `colorScheme.tertiary` | Brass 40 `#6B5E00` | Brass 80 `#DCC64E` | Rare achievement accent |
| Error | `colorScheme.error` | `#93000A` | `#FFB4AB` | Destructive and invalid states |

Rules:

- Pair each Material color role only with its matching `on*` role.
- Ember is interactive or progress-bearing, never ambient decoration.
- Tonal containers create most depth. Shadows are reserved for floating navigation.
- Raw color values live only in token definitions or platform XML token resources.

## 3. Typography

Google Sans Flex is bundled and configured in `RepForgeTypography.kt`. Its width,
optical-size, weight, grade, and roundness axes are part of each role.

| Role | Token | Size / line height | Usage |
|---|---|---:|---|
| Display XL | `RepForgeTypeRoles.DisplayHeroLarge` | 84sp / 72sp | Wide-screen brand hero only |
| Display | `RepForgeTypeRoles.DisplayHero` | 62sp / 56sp | Screen and compact brand hero |
| Headline | `RepForgeTypeRoles.HeadlineLoud` | 28sp / 30sp | Screen proposition and section anchor |
| Metric large | `RepForgeTypeRoles.MetricLarge` | 52sp / 52sp | Primary measured result |
| Metric medium | `RepForgeTypeRoles.MetricMedium` | 34sp / 34sp | Supporting measured result |
| Body | `RepForgeTypeRoles.BodySupport` | 14sp / 20sp | Explanatory copy |
| Label | `RepForgeTypeRoles.LabelExpressive` | 11sp / 16sp | Buttons, steps, compact labels |
| Label small | `RepForgeTypeRoles.LabelSmall` | 10sp / 14sp | Tertiary metadata only |

Sentence case is preferred for explanatory copy. Uppercase belongs to short training
commands, compact labels, and the split RepForge wordmark.

## 4. Spacing & Layout

`FitnessVisualTokens.FitnessSpacing` is the spacing source. The system is based on 8dp
with a 4dp half-step.

| Token | Value | Usage |
|---|---:|---|
| `xxs` | 4dp | Tight icon or label separation |
| `xs` | 8dp | Compact internal gap |
| `sm` | 12dp | Related control gap |
| `md` | 16dp | Standard component inset |
| `lg` | 24dp | Screen gutter and section gap |
| `xl` | 32dp | Major internal separation |
| `xxl` | 48dp | Brand hero separation |

Compact screens use one readable vertical flow with 24dp gutters. The onboarding flow
is centered and constrained to 720dp on larger windows; existing content grids remain
constrained to 1040dp. Content handles system bars and vertical scrolling, including
large font scales and short displays.

## 5. Components

### Forged R Mark

- **Structure**: Android vector foreground over a Forge tonal field.
- **Variants**: full Ember/Paper mark, monochrome themed-icon silhouette.
- **Sizing**: 72dp compact, 112dp loading, and 144dp hero.
- **States**: static by default; launch and loading contexts may animate the entire mark.
- **Accessibility**: meaningful instances announce "RepForge forged R"; decorative
  instances have no duplicate description.
- **Motion**: never animates individual vector paths. Transform and opacity only.

### Forge Loading State

- **Structure**: centered Forged R plus concise preparation label on the app surface.
- **States**: indeterminate loading, static reduced-motion fallback.
- **Accessibility**: exposes an indeterminate progress semantic and a clear state label.
- **Motion**: a low-amplitude scale and opacity pulse using brand transition tokens;
  reduced motion renders the final static mark.

### Guest Onboarding

- **Structure**: brand lockup, proposition, three evidence steps, primary guest CTA,
  and local-data reassurance in one scroll-safe column.
- **Variants**: compact stacked layout and expanded horizontal evidence rail.
- **States**: entering, ready, CTA pressed, CTA disabled while completion is persisted.
- **Accessibility**: semantic heading, logical reading order, at least 48dp controls,
  concise labels, AA contrast, and no account dark pattern.
- **Motion**: mark, evidence, and CTA settle in as three semantic stages. Reduced motion
  shows the ready state immediately.

### Giant Start Shape

- **Structure**: existing circular high-emphasis action from `core/designsystem`.
- **States**: default, press compression, disabled, completed where applicable.
- **Accessibility**: clear action label and large touch target.
- **Motion**: `RepForgeMotion.BouncySpring` with haptic feedback.

## 6. Motion & Interaction

Spatial motion uses the named spring vocabulary in `RepForgeMotion`:

| Token | Character | Usage |
|---|---|---|
| `emphasized()` | about 500ms, weighted settle | Brand mark and screen-level reveal |
| `standard()` | about 400ms, quiet settle | Supporting content reveal |
| `quick()` | about 200ms | Press and state feedback |
| `BouncySpring` | controlled overshoot | One focal CTA only |
| `DurationStandard` + `EmphasizedEasing` | 400ms transition | System splash exit |

The splash icon lifts 8dp and scales to 1.08 while the splash surface fades. Onboarding
stages are separated by half of `DurationQuick`, begin at 0.88 scale / 24dp lift, and
settle to their final positions. Loading pulses from 0.96 to 1.0 scale and 0.72 to 1.0
opacity. When Android reports animations disabled, the splash is removed immediately,
loading is static, and onboarding starts fully revealed.

## 7. Depth & Surface

The strategy is tonal shift. Paper, surface-container, and primary-container roles build
the hierarchy without card borders. Asymmetric `RepForgeShapes` roles create contrast:
`Hero` for brand propositions, `CardLarge` for grouped evidence, `PrimaryActionSquircle`
for the main guest action, and `Pill` only for compact labels. Launcher artwork uses a
flat Forge field so platform masks and themed-icon treatment remain legible.

## 8. Accessibility Constraints & Accepted Debt

Constraints:

- Target WCAG 2.2 AA contrast: 4.5:1 body text and 3:1 large text / UI boundaries.
- Preserve TalkBack reading order and expose progress state for launch loading.
- Keep interactive targets at least 48dp and support large font scales without clipping.
- Respect Android's animator-duration setting with a complete static path.
- Do not require an account to begin; explain local storage before the guest action.

Accepted debt: none for the branding and onboarding flow.
