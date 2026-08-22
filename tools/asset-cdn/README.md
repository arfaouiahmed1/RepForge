# RepForge Asset CDN (Cloudflare R2) - configuration package

This directory holds the **configuration and validation package** for the
RepForge asset CDN: manifest schemas, a manifest validator, fixtures, and the
wrangler template. It is everything needed to wire up Cloudflare R2 in ~15
minutes once a Cloudflare account exists.

> **STATUS (plan todo 7):** configuration only. **No R2 bucket, domain, or any
> cloud resource has been created yet.** Creating them requires a Cloudflare
> account, which is a manual step for the repo owner - see "Manual setup
> steps" below. Nothing in this todo requires an account or secret.

## Files

| File | Purpose |
| --- | --- |
| `asset-manifest.schema.json` | JSON Schema draft 2020-12 for `assetManifest` v17 (3D exercise GLBs + WebP thumbs/heroes). Content-addressed paths embed `sha256[:12]`. |
| `model-manifest.schema.json` | JSON Schema draft 2020-12 for the LiteRT `modelManifest` (blueprint p.769): name/version/schemaVersion/minAppVersion/sha256/url/rollout. |
| `validate_manifest.py` | Stdlib-only validator for both manifest types. Exit 0 = valid, 1 = invalid (reasons printed), 2 = usage error. Enforces cross-field rules JSON Schema cannot express (path hash == sha256 prefix, path version == version field, unique exerciseIds). |
| `fixtures/valid-manifest.json` | Two-asset valid example (`bench_press` v3, `squat` v1). |
| `fixtures/corrupt-hash-manifest.json` | Invalid example: malformed sha256 **and** glbPath hash not matching the sha256 prefix. |
| `wrangler.toml.example` | Wrangler template: bucket `repforge-assets`, binding `ASSETS`, plus the exact cache-rule definitions as comments. |

## Validate manifests locally (no account needed)

```bash
uv run python tools/asset-cdn/validate_manifest.py tools/asset-cdn/fixtures/valid-manifest.json
# OK ... EXIT 0

uv run python tools/asset-cdn/validate_manifest.py tools/asset-cdn/fixtures/corrupt-hash-manifest.json
# FAIL ... EXIT 1 with reasons
```

The upload pipeline (plan todo 25) must run this validator against every
generated manifest before uploading; the Android client (plan todo 27)
independently re-verifies each object's SHA-256 before caching.

## Manual setup steps (repo owner, one-time)

These steps need a human with a browser and a payment-free Cloudflare account.
They are NOT part of any automated todo.

### Step 0 - Install wrangler

```bash
npm install -g wrangler        # or: use `npx wrangler ...` without installing
wrangler --version             # sanity check
```

### Step 1 - Create the Cloudflare account and DNS zone

1. Sign up at <https://dash.cloudflare.com/sign-up> (free plan; no card).
2. Add the domain `repforge.app` to Cloudflare and update the registrar's
   nameservers to the ones Cloudflare shows. R2 custom domains require the
   zone to live on Cloudflare.
3. Enable R2: dashboard -> **R2 Object Storage** -> purchase the free plan
   ($0/month; requires confirming, no card for the free tier).

### Step 2 - Create the bucket

```bash
wrangler login                                       # opens browser OAuth
wrangler r2 bucket create repforge-assets            # default (auto) location
wrangler r2 bucket list                              # verify it exists
```

### Step 3 - Connect the custom domain

Dashboard -> **R2 Object Storage** -> `repforge-assets` -> **Settings** ->
**Custom Domains** -> **Connect Domain** -> enter `assets.repforge.app` ->
confirm. Cloudflare creates the CNAME and the certificate automatically
(allow a few minutes for DNS/cert propagation).

Verify once an object exists:

```bash
echo test | wrangler r2 object put repforge-assets/hello.txt --file -
curl -sI https://assets.repforge.app/hello.txt   # expect HTTP 200/404 pre-upload, headers from CF
```

### Step 4 - Create the cache rules

Dashboard -> zone `repforge.app` -> **Caching** -> **Cache Rules** -> create
exactly the two rules documented in `wrangler.toml.example`:

1. `manifests-short-ttl`: URI Path ends with `-manifest.json` ->
   Edge TTL 60s, Browser TTL 60s, response header
   `Cache-Control: public,max-age=60`.
2. `hashed-assets-immutable`: URI Path starts with `/assets/exercises/` ->
   Edge TTL 31536000s, Browser TTL 31536000s, response header
   `Cache-Control: public,max-age=31536000,immutable`.

### Step 5 - CI credentials (only when wiring uploads)

For the GitHub Actions upload workflow (plan todos 25/29), create an R2 API
token: dashboard -> **R2** -> **Manage R2 API Tokens** -> Create API Token
with **Object Read & Write** scoped to `repforge-assets`. Store as repo
secrets `CLOUDFLARE_ACCOUNT_ID` and `CLOUDFLARE_API_TOKEN`. Prefer OIDC or
short-lived tokens where possible; never commit tokens.

## CI upload pattern (implemented for real in plan todo 25)

```yaml
# .github/workflows/asset-release.yml (sketch - todo 25 owns the real one)
- name: Upload hashed assets to R2
  env:
    CLOUDFLARE_ACCOUNT_ID: ${{ secrets.CLOUDFLARE_ACCOUNT_ID }}
    CLOUDFLARE_API_TOKEN: ${{ secrets.CLOUDFLARE_API_TOKEN }}
  run: |
    uv run python tools/exercise-assets/build_assets.py --out build/assets
    # build_assets.py / todo 25 pipeline emits content-addressed files:
    #   assets/exercises/<id>/movement.v<N>.<sha256-12>.glb (+ thumb512/hero .webp)
    while IFS= read -r f; do
      rel="${f#build/assets/}"
      case "$rel" in
        *.glb|*.webp) CC="public,max-age=31536000,immutable" ;;
        *manifest*.json) CC="public,max-age=60" ;;
      esac
      npx wrangler r2 object put "repforge-assets/$rel" --file "$f" \
        --cache-control "$CC" --remote
    done < <(find build/assets -type f)
    npx wrangler r2 object put repforge-assets/assets-manifest.json \
      --file build/assets/assets-manifest.json \
      --cache-control "public,max-age=60" --remote
```

The manifest is uploaded **last**, after all hashed objects exist, so clients
never see a manifest referencing a missing object.

## Cost note (free-tier constraint)

R2 free tier (per month): **10 GB storage**, 1M Class A operations (writes),
10M Class B operations (reads), **zero egress fees**. Beyond that:
$0.015/GB-month storage, $4.50/million Class A, $0.36/million Class B.

Expected steady-state footprint: 250 GLBs at 1-3 MB (~0.25-0.75 GB) plus
~500 WebPs at 20-80 KB (~10-40 MB) plus manifests (<1 MB) - roughly **under
1 GB total**, comfortably inside the 10 GB free tier. Old versions of hashed
objects should be lifecycle-deleted after a grace window (todo 25 defines the
retention policy) so storage does not grow unbounded across releases.

## Downstream contracts

- **Android client (todo 27):** fetches `assets-manifest.json` /
  `models-manifest.json` (60s cache), then downloads objects and verifies
  SHA-256 before writing to the LRU disk cache; corrupt hash => reject +
  fall back (the corrupt fixture here models exactly that failure).
- **Upload pipeline (todo 25):** computes SHA-256 per GLB, names objects
  `movement.v<N>.<sha256-12>.glb`, emits manifests matching these schemas,
  runs `validate_manifest.py` as a release gate.
