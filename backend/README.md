# RepForge Backend — Ktor + Supabase Postgres (hybrid)

Standalone JVM service (Ktor 3.1.1, Kotlin 2.4.x, Java 17). **Not part of the Android
Gradle build** — excluded from `settings.gradle.kts`; build it directly:

```bash
./gradlew -p backend build          # compile + test
./gradlew -p backend run            # serve on :8080
```

## Architecture (decision Q1-A)

| Concern | Owner |
|---|---|
| Identity | Firebase Auth (ID tokens verified here; userId derived server-side) |
| Workout data | Supabase Postgres (`supabase/migrations/`) |
| Device services | Firebase (FCM, Crashlytics) |
| Assets | Cloudflare R2 |
| Billing truth | Play Developer API → `entitlement` table |

## Contract endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/health` | public | liveness probe |
| POST | `/v1/sync/push` | Bearer ID token | batched mutations; idempotency_key dedupe; base_revision conflict check |
| GET | `/v1/sync/pull?cursor=` | Bearer ID token | changes since cursor incl. tombstones |
| POST | `/v1/billing/verify` | Bearer ID token | purchase token → verified entitlement |
| POST | `/v1/device/register` | Bearer ID token | FCM token / installation provenance |
| DELETE | `/v1/account` | Bearer ID token | GDPR delete (tombstone cascade) |
| POST | `/v1/export` | Bearer ID token | GDPR export |
| GET | `/v1/me/entitlements` | Bearer ID token | current entitlements |
| POST | `/billing/rtdn` | Pub/Sub push | Google Play RTDN webhook |

Auth: `Authorization: Bearer <firebase-id-token>` → firebase-admin `verifyIdToken(checkRevoked=true)`
→ routes receive `FirebasePrincipal(userId)` from the **verified token only**.
Fail-closed: without `FIREBASE_PROJECT_ID` every `/v1/*` route returns 401.

## Data model

All synchronized entities carry the tombstone pattern:
`id UUID PK · created_at · updated_at · revision BIGINT · deleted_at TIMESTAMPTZ NULL`.

Tables: `user_profile`, `gym_profile`, `exercise`, `program`, `program_day`,
`program_exercise`, `planned_workout`, `workout_session`, `session_exercise`,
`set_log`, `personal_record`, `achievement_definition`, `achievement_unlock`,
`entitlement`, `device_installation`, `sync_operations`.

Sync ledger: `sync_operations(operation_id PK, entity_type, entity_id,
base_revision, mutation JSONB, idempotency_key UNIQUE)` — retried pushes with a
seen idempotency key are no-ops; `base_revision != row.revision` ⇒ CONFLICT.

RLS is enabled on every table with **no anon/authenticated policies** — direct
client access is denied by default; the backend uses the service role.

## Manual setup (no cloud credentials in this repo — by design)

1. **Create Supabase project** (free tier): https://supabase.com/dashboard → New project.
   Save the DB password; note project ref.
2. **Run migrations** against it:
   ```bash
   supabase link --project-ref YOUR-PROJECT-REF
   supabase db push            # applies supabase/migrations/*.sql in order
   # or psql: psql "$SUPABASE_DB_URL" -f supabase/migrations/0001_init.sql
   ```
3. **Create Firebase project** (or reuse the app's): enable Email/Google sign-in.
   Project settings → Service accounts → generate JSON → store locally, e.g.
   `~/.secrets/repforge-firebase-sa.json`. **Never commit it.**
4. **Configure env**: `cp backend/.env.example backend/.env`, fill values.
5. **Verify locally**:
   ```bash
   ./gradlew -p backend run &
   curl -s localhost:8080/health                      # 200 {"status":"ok",...}
   curl -si -X POST localhost:8080/v1/sync/push \
        -H 'Content-Type: application/json' -d '{"operations":[]}'   # HTTP/1.1 401
   ```
6. Deploy later via `backend/Dockerfile` (Cloud Run) — set env vars there too.

## Secrets policy

- Real keys/service-account JSON live ONLY in `.env` / secret managers — never in git, never in the APK.
- `firebase-admin` is a backend-only dependency; Android modules keep the lightweight Firebase SDKs.
