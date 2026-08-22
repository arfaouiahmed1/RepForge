# Privacy at RepForge

This is a plain-language summary of how RepForge handles your data. The
canonical, legally binding policy is the hosted privacy policy published with
the app's web presence (see "Where the full policy lives" below). If this file
and that policy ever disagree, the hosted policy wins.

## The short version

- **Your training log lives on your phone first.** Workouts, routines,
  personal records, and body metrics are stored locally in an on-device
  database. The app works fully offline: no network, no Health Connect, no
  account required.
- **Health Connect is opt-in and granular.** You choose exactly which data
  types to share (weight, sleep, heart rate, exercise sessions). Nothing is
  read or written until you grant it in Health Connect's own permission
  screen, and you can revoke any permission there at any time.
- **No health data goes to ads or analytics.** Ever. Raw health records are
  never sent to analytics or crash-reporting tools.
- **Sync is optional and tied to your account.** If you create an account,
  your training data syncs through Firebase so it follows you across devices.
  No account, no upload.
- **Deletion is real and self-serve.** Delete your account inside the app, or
  from any browser at https://repforge.app/delete. Deleting removes your
  synced server data; local data is removed by uninstalling the app or
  clearing its storage.

## What we collect

| Data | Where it lives | Leaves your device? |
|---|---|---|
| Workouts, sets, routines, PRs | On-device database | Only if you enable sync |
| Body metrics (weight etc.) | On-device database | Only if you enable sync |
| Health Connect reads/writes | On device via Health Connect | No (Health Connect itself governs its own storage) |
| Crash reports | Firebase Crashlytics | Yes, scrubbed of health data |
| Anonymous usage events | Analytics pipeline | Yes, event names and counters only, never raw health values |
| Subscription state | Entitlement backend + Google Play | Yes, needed to honor purchases |

## Recommendations are not profiling

The progression model predicts whether you can complete a prescribed set. It
runs on your device from bundled models plus your own training history. Your
data is not sold, not shared with advertisers, and not used to train models
for other users without explicit opt-in.

## Where the full policy lives

The canonical privacy policy is hosted alongside the app's web pages and is
published as part of the store-distribution work (Play listing preparation).
Until that hosting lands, this summary is the accurate description of current
behavior: local-first storage, opt-in Health Connect, no health data to ads
or analytics, deletion in-app and at https://repforge.app/delete.

Questions about privacy can go to <privacy@repforge.app>.
