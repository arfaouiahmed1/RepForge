# Contributing to RepForge

Thanks for your interest in contributing. RepForge is a pre-alpha project, so
the fastest way to help right now is small, focused pull requests: bug fixes,
test coverage, docs corrections, and design-system polish.

## Build prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | 17 | Android Studio's bundled JDK works; `winget install Microsoft.OpenJDK.17` also works |
| Android Studio | Ladybug or newer | Open the repo root and let Gradle sync |
| Android SDK | API 36 (compile/target), minSdk 26 | Installed via Android Studio SDK Manager |
| uv | latest | Python toolchain for `ml/` training code. No pip, no conda |
| Blender | any recent 4.x | Optional. Only needed if you touch 3D assets rendered by Filament |

First sync needs internet to pull Gradle dependencies. You do not need
`app/google-services.json` to build the app offline-first paths, but Firebase
features will not initialize without it.

## Module overview

```
:app                     — navigation host, DI wiring, Live Update notifications
:core:designsystem       — M3 Expressive tokens, roles, springs, fonts (single source of truth)
:core:model              — pure Kotlin domain types
:core:database           — Room schema, DAOs, migrations
:core:datastore          — DataStore preferences
:core:data               — repositories bridging Room/DataStore/network
:core:health             — Health Connect client wrappers
:core:ml                 — LiteRT inference, feature building, calibration
:core:analytics          — event pipeline (no raw health data)
:core:notifications      — channels + Live Update notifier
:feature:today           — Today screen
:feature:workout         — active workout state machine
:feature:progress        — charts, est 1RM, trends
:feature:routine         — plan editor
:feature:settings        — settings, Health & data
:feature:onboarding      — first-run flow
:feature:lab             — Form Lab shell
:feature:formlab         — CameraX + ML Kit pose (BETA)
:feature:paywall         — Pro upsell
:wear                    — Wear OS companion
:baselineprofile         — Macrobenchmark startup profiles
ml/                      — Python training pipeline (uv + LightGBM + MLflow)
backend/                 — entitlement verification service
web/                     — landing page and hosted policy pages
```

Rules of thumb:

- Design tokens live only in `:core:designsystem`. Features never invent
  radius, font, or spring values.
- Data flows one way: `Compose → ViewModel → UseCase → Repository → Room`,
  then out through the sync queue.
- Experimental Material 3 Expressive APIs are isolated inside
  `:core:designsystem`.

## Commit convention

Use Conventional Commits style with a scope:

```
type(scope): summary
```

- Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `build`, `ci`
- Scope: module or area without the leading colon, e.g. `feature-workout`,
  `core-data`, `ml`, `gradle`
- Summary: lowercase, imperative, no trailing period, 72 characters or fewer

Examples:

```
feat(feature-workout): persist set-in-progress across process death
fix(core-ml): clamp success probability to [0,1] before display
docs(readme): correct minSdk badge
```

## Pull requests

Before opening a PR, run the same checks CI runs (see
`.github/workflows/pr.yml`):

```bash
./gradlew spotlessCheck detekt   # format + static analysis
./gradlew lintDebug              # Android Lint
./gradlew testDebugUnitTest      # unit + Room + ML golden vectors
./gradlew assembleDebug          # it compiles
```

PR checklist:

- [ ] Commits follow `type(scope): summary`
- [ ] `spotlessCheck` and `detekt` pass
- [ ] New behavior has tests (unit test, Paparazzi screenshot, or both)
- [ ] No new permissions added without discussion
- [ ] No health data flows into analytics or crash reports
- [ ] Docs updated if you changed build steps, modules, or public behavior

Keep PRs small. If a change touches more than three modules, consider
splitting it.

## Reporting issues

Bug reports should include device/API level, exact steps, expected vs actual
behavior, and logs with any personal data removed. Security issues go through
[SECURITY.md](SECURITY.md), never through public issues.

## Licensing

By contributing, you agree that your contributions are licensed under the
Apache License 2.0, the project's license. See [LICENSE](LICENSE) and
[NOTICE](NOTICE).
