-- ============================================================================
-- RepForge — 0001_init.sql
-- Supabase Postgres schema for workout aggregates (offline-first sync).
--
-- Conventions (tombstone pattern) — EVERY synchronized entity carries:
--   id          UUID PRIMARY KEY DEFAULT gen_random_uuid()
--   created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
--   updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()   -- sync cursor field
--   revision    BIGINT      NOT NULL DEFAULT 1       -- optimistic concurrency
--   deleted_at  TIMESTAMPTZ NULL                     -- NULL = live; set = tombstone
--
-- Sync contract:
--   * Pull:    GET /v1/sync/pull?cursor=<updated_at>  → rows WHERE updated_at > cursor
--              Tombstones are returned too so clients can delete locally.
--   * Push:    POST /v1/sync/push with operations {operation_id, entity_type,
--              entity_id, base_revision, mutation} + idempotency_key.
--              Server rejects when row.revision != base_revision (409 conflict).
--   * Identity: Firebase Auth owns identity (user_profile.firebase_uid);
--              Postgres never authenticates users directly. The Ktor backend
--              uses the service role key; RLS below denies anon/authenticated
--              roles by default (no policies = no access), service bypasses.
-- ============================================================================

create extension if not exists pgcrypto; -- gen_random_uuid()

-- ----------------------------------------------------------------------------
-- Shared trigger: bump updated_at + revision on UPDATE unless the writer set
-- them explicitly (sync writers pass client values through unchanged).
-- ----------------------------------------------------------------------------
create or replace function rf_touch_row() returns trigger as $$
begin
  if new.updated_at is not distinct from old.updated_at then
    new.updated_at := now();
  end if;
  if new.revision is not distinct from old.revision then
    new.revision := old.revision + 1;
  end if;
  return new;
end;
$$ language plpgsql;

-- ----------------------------------------------------------------------------
-- user_profile — mirrors Firebase Auth identity, owned workout preferences
-- ----------------------------------------------------------------------------
create table user_profile (
  id               uuid primary key default gen_random_uuid(),
  firebase_uid     text not null unique,
  email            text,
  display_name     text,
  avatar_url       text,
  timezone         text not null default 'UTC',
  units            text not null default 'METRIC' check (units in ('METRIC','IMPERIAL')),
  sex              text check (sex in ('MALE','FEMALE','OTHER','UNDISCLOSED')),
  height_cm        numeric(5,2),
  body_weight_kg   numeric(6,3),
  birth_date       date,
  marketing_opt_in boolean not null default false,
  created_at       timestamptz not null default now(),
  updated_at       timestamptz not null default now(),
  revision         bigint not null default 1,
  deleted_at       timestamptz
);
create trigger trg_user_profile_touch before update on user_profile
  for each row execute function rf_touch_row();
create index idx_user_profile_updated on user_profile (updated_at);

-- ----------------------------------------------------------------------------
-- gym_profile — per-user training environment (bar weight, plates, rest)
-- ----------------------------------------------------------------------------
create table gym_profile (
  id                uuid primary key default gen_random_uuid(),
  user_id           uuid not null references user_profile(id) on delete cascade,
  name              text not null default 'Home',
  barbell_weight_kg numeric(5,2) not null default 20.0,
  plate_inventory   jsonb not null default '{}'::jsonb, -- {"1.25":4,"2.5":2,...}
  default_rest_s    int not null default 180,
  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now(),
  revision          bigint not null default 1,
  deleted_at        timestamptz
);
create trigger trg_gym_profile_touch before update on gym_profile
  for each row execute function rf_touch_row();
create index idx_gym_profile_user on gym_profile (user_id, updated_at);

-- ----------------------------------------------------------------------------
-- exercise — catalog metadata (global rows have owner_user_id IS NULL)
-- ----------------------------------------------------------------------------
create table exercise (
  id             uuid primary key default gen_random_uuid(),
  owner_user_id  uuid references user_profile(id) on delete cascade, -- null = global catalog
  name           text not null,
  category       text not null check (category in
                   ('BARBELL','DUMBBELL','MACHINE','CABLE','BODYWEIGHT','BAND','KETTLEBELL','OTHER')),
  muscle_groups  text[] not null default '{}',       -- e.g. {chest,triceps,front_delts}
  equipment      text,
  instructions   text,
  video_url      text,
  is_custom      boolean not null default false,
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now(),
  revision       bigint not null default 1,
  deleted_at     timestamptz
);
create trigger trg_exercise_touch before update on exercise
  for each row execute function rf_touch_row();
-- one global exercise per lowercase name; one custom exercise per user+name
create unique index uq_exercise_global_name on exercise (lower(name)) where owner_user_id is null;
create unique index uq_exercise_user_name on exercise (owner_user_id, lower(name)) where owner_user_id is not null;
create index idx_exercise_updated on exercise (updated_at);

-- ----------------------------------------------------------------------------
-- program / program_day / program_exercise — training plans
-- ----------------------------------------------------------------------------
create table program (
  id            uuid primary key default gen_random_uuid(),
  user_id       uuid not null references user_profile(id) on delete cascade,
  name          text not null,
  goal          text,                                -- e.g. STRENGTH, HYPERTROPHY
  days_per_week int check (days_per_week between 1 and 7),
  start_date    date,
  is_active     boolean not null default true,
  source        text not null default 'USER' check (source in ('USER','TEMPLATE')),
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),
  revision      bigint not null default 1,
  deleted_at    timestamptz
);
create trigger trg_program_touch before update on program
  for each row execute function rf_touch_row();
create index idx_program_user on program (user_id, updated_at);

create table program_day (
  id         uuid primary key default gen_random_uuid(),
  program_id uuid not null references program(id) on delete cascade,
  day_index  int not null check (day_index between 1 and 14),
  name       text not null,                          -- "PUSH DAY"
  focus      text[],
  notes      text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision   bigint not null default 1,
  deleted_at timestamptz,
  unique (program_id, day_index)
);
create trigger trg_program_day_touch before update on program_day
  for each row execute function rf_touch_row();
create index idx_program_day_program on program_day (program_id, updated_at);

create table program_exercise (
  id               uuid primary key default gen_random_uuid(),
  program_day_id   uuid not null references program_day(id) on delete cascade,
  exercise_id      uuid not null references exercise(id),
  order_index      int not null,
  target_sets      int not null default 3 check (target_sets between 1 and 20),
  target_reps_min  int,
  target_reps_max  int,
  target_rir       numeric(3,1),
  rest_seconds     int not null default 180,
  tempo            text,                             -- "3-0-1"
  notes            text,
  created_at       timestamptz not null default now(),
  updated_at       timestamptz not null default now(),
  revision         bigint not null default 1,
  deleted_at       timestamptz,
  unique (program_day_id, order_index)
);
create trigger trg_program_exercise_touch before update on program_exercise
  for each row execute function rf_touch_row();
create index idx_program_exercise_day on program_exercise (program_day_id, updated_at);

-- ----------------------------------------------------------------------------
-- planned_workout — a scheduled occurrence of a program day
-- ----------------------------------------------------------------------------
create table planned_workout (
  id              uuid primary key default gen_random_uuid(),
  user_id         uuid not null references user_profile(id) on delete cascade,
  program_day_id  uuid references program_day(id) on delete set null,
  scheduled_date  date not null,
  status          text not null default 'PLANNED'
                    check (status in ('PLANNED','IN_PROGRESS','COMPLETED','SKIPPED')),
  notes           text,
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now(),
  revision        bigint not null default 1,
  deleted_at      timestamptz
);
create trigger trg_planned_workout_touch before update on planned_workout
  for each row execute function rf_touch_row();
create index idx_planned_workout_user_date on planned_workout (user_id, scheduled_date);
create index idx_planned_workout_user_upd on planned_workout (user_id, updated_at);

-- ----------------------------------------------------------------------------
-- workout_session / session_exercise / set_log — what actually happened
-- ----------------------------------------------------------------------------
create table workout_session (
  id                  uuid primary key default gen_random_uuid(),
  user_id             uuid not null references user_profile(id) on delete cascade,
  planned_workout_id  uuid references planned_workout(id) on delete set null,
  device_id           uuid references device_installation(id) on delete set null,
  started_at          timestamptz not null,
  ended_at            timestamptz,
  status              text not null default 'ACTIVE'
                        check (status in ('ACTIVE','COMPLETED','ABANDONED')),
  duration_seconds    int,
  volume_kg           numeric(12,3),
  notes               text,
  created_at          timestamptz not null default now(),
  updated_at          timestamptz not null default now(),
  revision            bigint not null default 1,
  deleted_at          timestamptz
);
create trigger trg_workout_session_touch before update on workout_session
  for each row execute function rf_touch_row();
create index idx_workout_session_user_started on workout_session (user_id, started_at desc);
create index idx_workout_session_user_upd on workout_session (user_id, updated_at);

create table session_exercise (
  id                 uuid primary key default gen_random_uuid(),
  workout_session_id uuid not null references workout_session(id) on delete cascade,
  exercise_id        uuid not null references exercise(id),
  order_index        int not null,
  notes              text,
  created_at         timestamptz not null default now(),
  updated_at         timestamptz not null default now(),
  revision           bigint not null default 1,
  deleted_at         timestamptz,
  unique (workout_session_id, order_index)
);
create trigger trg_session_exercise_touch before update on session_exercise
  for each row execute function rf_touch_row();
create index idx_session_exercise_session on session_exercise (workout_session_id, updated_at);

-- raw evidence is append-only in spirit: recommendation fields may change,
-- load/reps/rir/raw_evidence never get overwritten by later mutations.
create table set_log (
  id                      uuid primary key default gen_random_uuid(),
  session_exercise_id     uuid not null references session_exercise(id) on delete cascade,
  set_index               int not null check (set_index >= 1),
  load_kg                 numeric(6,2),
  reps                    int not null check (reps between 0 and 1000),
  rir                     numeric(3,1),
  rpe                     numeric(3,1),
  completed_at            timestamptz not null,
  is_warmup               boolean not null default false,
  failure_type            text check (failure_type in ('NONE','CONCENTRIC','FORM','PAIN')),
  recommendation_id       uuid,
  recommendation_accepted boolean,
  raw_evidence            jsonb,                     -- immutable sensor/timing evidence
  created_at              timestamptz not null default now(),
  updated_at              timestamptz not null default now(),
  revision                bigint not null default 1,
  deleted_at              timestamptz,
  unique (session_exercise_id, set_index)
);
create trigger trg_set_log_touch before update on set_log
  for each row execute function rf_touch_row();
create index idx_set_log_se on set_log (session_exercise_id, updated_at);

-- ----------------------------------------------------------------------------
-- personal_record — est 1RM / max load / max reps per exercise
-- ----------------------------------------------------------------------------
create table personal_record (
  id          uuid primary key default gen_random_uuid(),
  user_id     uuid not null references user_profile(id) on delete cascade,
  exercise_id uuid not null references exercise(id),
  record_type text not null check (record_type in ('EST_1RM','MAX_LOAD','MAX_REPS','MAX_VOLUME')),
  value       numeric(12,3) not null,
  achieved_at timestamptz not null,
  set_log_id  uuid references set_log(id) on delete set null,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  revision    bigint not null default 1,
  deleted_at  timestamptz
);
create trigger trg_personal_record_touch before update on personal_record
  for each row execute function rf_touch_row();
create index idx_pr_user_exercise on personal_record (user_id, exercise_id, record_type);
create index idx_pr_user_upd on personal_record (user_id, updated_at);

-- ----------------------------------------------------------------------------
-- achievement_definition (catalog) / achievement_unlock (per user)
-- ----------------------------------------------------------------------------
create table achievement_definition (
  id          uuid primary key default gen_random_uuid(),
  code        text not null unique,                   -- "FIRST_SESSION", "PR_100KG_BENCH"
  title       text not null,
  description text,
  tier        text not null default 'BRONZE' check (tier in ('BRONZE','SILVER','GOLD','PLATINUM')),
  criteria    jsonb not null default '{}'::jsonb,     -- {"type":"sessions_completed","count":1}
  icon_key    text,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  revision    bigint not null default 1,
  deleted_at  timestamptz
);
create trigger trg_achievement_def_touch before update on achievement_definition
  for each row execute function rf_touch_row();

create table achievement_unlock (
  id           uuid primary key default gen_random_uuid(),
  user_id      uuid not null references user_profile(id) on delete cascade,
  definition_id uuid not null references achievement_definition(id),
  unlocked_at  timestamptz not null,
  seen_at      timestamptz,
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now(),
  revision     bigint not null default 1,
  deleted_at   timestamptz,
  unique (user_id, definition_id)
);
create trigger trg_achievement_unlock_touch before update on achievement_unlock
  for each row execute function rf_touch_row();
create index idx_achievement_unlock_user on achievement_unlock (user_id, updated_at);

-- ----------------------------------------------------------------------------
-- entitlement — verified Play Billing state (never trust client "isPro")
-- ----------------------------------------------------------------------------
create table entitlement (
  id                uuid primary key default gen_random_uuid(),
  user_id           uuid not null references user_profile(id) on delete cascade,
  product_id        text not null,                    -- e.g. repforge.pro.monthly
  purchase_token    text not null,
  state             text not null check (state in ('ACTIVE','CANCELED','GRACE_PERIOD','ON_HOLD','EXPIRED')),
  source            text not null default 'PLAY_BILLING',
  expires_at        timestamptz,
  verified_at       timestamptz not null default now(),
  auto_renewing     boolean not null default true,
  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now(),
  revision          bigint not null default 1,
  deleted_at        timestamptz,
  unique (purchase_token)
);
create trigger trg_entitlement_touch before update on entitlement
  for each row execute function rf_touch_row();
create index idx_entitlement_user on entitlement (user_id, updated_at);

-- ----------------------------------------------------------------------------
-- device_installation — FCM tokens / install provenance for sync fan-out
-- ----------------------------------------------------------------------------
create table device_installation (
  id             uuid primary key default gen_random_uuid(),
  user_id        uuid not null references user_profile(id) on delete cascade,
  installation_id uuid not null,                      -- client-generated stable UUID
  platform       text not null check (platform in ('ANDROID','WEAR_OS','IOS','WEB')),
  fcm_token      text,
  model          text,
  os_version     text,
  app_version    text,
  last_seen_at   timestamptz not null default now(),
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now(),
  revision       bigint not null default 1,
  deleted_at     timestamptz,
  unique (user_id, installation_id)
);
create trigger trg_device_installation_touch before update on device_installation
  for each row execute function rf_touch_row();
create index idx_device_installation_user on device_installation (user_id, updated_at);

-- ----------------------------------------------------------------------------
-- sync_operations — idempotency ledger for POST /v1/sync/push
--   operation_id    PK  — client op UUID (one row per pushed operation)
--   idempotency_key UNIQUE — retry of the same HTTP request maps to same row
--   base_revision   expected server revision; mismatch ⇒ CONFLICT (409)
--   mutation        JSONB patch applied to entity_id's table
-- ----------------------------------------------------------------------------
create table sync_operations (
  operation_id    uuid primary key,
  user_id         uuid not null references user_profile(id) on delete cascade,
  entity_type     text not null check (entity_type in (
                    'user_profile','gym_profile','exercise','program','program_day',
                    'program_exercise','planned_workout','workout_session',
                    'session_exercise','set_log','personal_record',
                    'achievement_unlock','entitlement','device_installation')),
  entity_id       uuid not null,
  base_revision   bigint,
  mutation        jsonb not null,
  idempotency_key text not null unique,
  status          text not null default 'PENDING'
                    check (status in ('PENDING','APPLIED','CONFLICT','REJECTED')),
  error_code      text,
  client_op_at    timestamptz,
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now(),
  revision        bigint not null default 1,
  deleted_at      timestamptz
);
create trigger trg_sync_operations_touch before update on sync_operations
  for each row execute function rf_touch_row();
create index idx_sync_ops_user_created on sync_operations (user_id, created_at desc);
create index idx_sync_ops_entity on sync_operations (entity_type, entity_id);

-- ----------------------------------------------------------------------------
-- Row Level Security: backend talks with the service role (bypasses RLS).
-- No policies for anon/authenticated ⇒ direct client access denied by default.
-- ----------------------------------------------------------------------------
alter table user_profile            enable row level security;
alter table gym_profile             enable row level security;
alter table exercise                enable row level security;
alter table program                 enable row level security;
alter table program_day             enable row level security;
alter table program_exercise        enable row level security;
alter table planned_workout         enable row level security;
alter table workout_session         enable row level security;
alter table session_exercise        enable row level security;
alter table set_log                 enable row level security;
alter table personal_record         enable row level security;
alter table achievement_definition  enable row level security;
alter table achievement_unlock      enable row level security;
alter table entitlement             enable row level security;
alter table device_installation     enable row level security;
alter table sync_operations         enable row level security;

-- Global exercise catalog must be readable by the backend service role only;
-- if a future Supabase-Auth path is added, add explicit owner policies here.
