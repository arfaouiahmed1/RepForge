# Security Policy

RepForge is a pre-alpha strength-training app. There are no published releases
yet, so the only supported target is the `main` branch. Please report anything
you find before it becomes a shipped problem.

## Supported versions

| Version | Branch / ref | Security fixes |
|---|---|---|
| main | default branch | Yes |
| latest tagged release | none published yet; will be supported once releases start (planned with the release pipeline) | Yes, from its tag forward |
| older tags and branches | historical | No |

## Reporting a vulnerability

Do not open a public issue for security problems.

1. Email <security@repforge.app> from any address.
2. Include: affected commit or branch, device/API level if relevant,
   reproduction steps or a proof of concept, and your assessment of impact.
3. You will get an acknowledgment within 3 business days and a status update
   at least every 7 days until the issue is resolved.

## Disclosure policy

We follow coordinated disclosure with a 90-day window:

- Day 0: report received, triage starts.
- Within 90 days: fix released to `main` (and to the latest release if one
  exists), or a documented reason why no fix is needed.
- After the fix ships (or at day 90, whichever comes first): public
  disclosure, credit to the reporter unless anonymity is requested.

If a report is in active exploitation or leaks user health data, we will
shorten the timeline and disclose as soon as users can act on the fix.

## Scope

In scope: the Android app (`app/`, `core/`, `feature/`, `wear/`), the
entitlement backend (`backend/`), the training pipeline (`ml/`), GitHub
Actions workflows, and the hosted web pages under `web/`.

Out of scope: vulnerabilities in third-party services themselves (Firebase,
Google Play) — report those to the vendor — and reports from automated
scanners without a demonstrated impact.

## Data-handling commitments worth testing

These are product guarantees, and violations of them count as security
reports:

- Health Connect data stays on device unless the user opts into sync.
- Raw health data never goes to analytics or crash reporting.
- Account deletion works in-app and at https://repforge.app/delete.

## Thanks

Security researchers who report responsibly will be credited in release notes
unless they prefer otherwise.
