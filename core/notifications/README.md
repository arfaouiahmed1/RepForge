# Live Updates — RepForge

Android 16+ promoted ongoing workout. See `liveupdate/LiveWorkoutNotifier.kt`.

## When to promote
- User tapped START (user-initiated)
- Ongoing session ACTIVE (not NOT_STARTED/COMPLETED)
- Time-sensitive (rest 90s countdown, next set advice) — updates every 1s for rest, on set complete for lift.

Do NOT use for: onboarding, PR alerts, sync, social.

## What it shows
| Phase | Title | Content | Chip | Progress | Actions |
|---|---|---|---|---|---|
| LIFT | PUSH DAY — Bench 3/4 | 82.5 kg ×8 • 78% success | 82KG | 32% (9/28 sets) + 7 segments | NEXT, END |
| REST | REST — 00:42 | Next: Incline DB 32×10 | countdown | same | +15s, SKIP |

Segments: per-exercise (ember=done, steel=current, grey=remaining). Points: per-set squares (green=done).

## Permissions
`POST_NOTIFICATIONS` (33+) runtime via `rememberNotificationPermissionState()` on Today. `POST_PROMOTED_NOTIFICATIONS` (16+) is normal; promotion requested via `setRequestPromotedOngoing(true)`.

## Restrictions
- Style = ProgressStyle (not custom RemoteViews)
- `setOngoing(true)` + `setRequestPromotedOngoing(true)` + `setOnlyAlertOnce(true)`
- Channel IMPORTANCE_DEFAULT (not MIN)
- Title required, not group summary, not colorized

## Dismiss
`setDeleteIntent` ? `LiveWorkoutActions.ACTION_DISMISS` ? suppress 5 min (`dismissedUntil`), do not re-post.

## Testing
Real device only (emulator chip not shown). Test with `adb shell dumpsys notification | grep repforge`.

## Compose
No Compose wrapper yet — use NotificationCompat builder under the hood, same as Views doc:
https://developer.android.com/develop/ui/views/notifications/live-update
Compose live-update page uses identical ProgressStyle + chip APIs.

