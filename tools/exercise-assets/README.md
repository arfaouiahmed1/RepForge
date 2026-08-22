# RepForge exercise asset pipeline

Headless Blender pipeline producing one GLB + WebP thumbnail per exercise from a
single procedural athlete rig (todo 24), feeding the R2 upload step (todo 25).

## Quick start (no Blender required)

    uv run --with pyyaml python tools/exercise-assets/gen_catalog.py --min 250
    uv run --with pyyaml python tools/exercise-assets/build_assets.py \
        --catalog tools/exercise-assets/exercise_catalog.yaml --out build/assets --dry-run

Dry-run validates the full catalog (vocabularies, phases, bone subsets, unique
ids/actions) and prints the build plan. Exit 0 = valid.

## Real build (needs Blender 4.x on PATH or $REPFORGE_BLENDER / --blender)

    uv run --with pyyaml python tools/exercise-assets/build_assets.py \
        --catalog tools/exercise-assets/exercise_catalog.yaml --out build/assets

Generates `athlete_master.blend` procedurally on first run (build_rig.py), then
runs one `blender --background` export per exercise (export_glb.py), hashing
real GLB bytes into exercise_manifest.json:

    {exerciseId, glbPath, webpPath, sha256, sizeBytes, version=sha256[:12]}

## Tests

    uv run --with pyyaml --with pytest python -m pytest tools/exercise-assets/tests -q

## CI

Ubuntu runners install Blender via https://github.com/marketplace/actions/blender-build-script
or apt; the same commands apply. Catalog regeneration is deterministic
(gen_catalog.py round-robins all 12 movement families).
