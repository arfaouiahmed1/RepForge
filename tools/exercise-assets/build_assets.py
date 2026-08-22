#!/usr/bin/env python3
"""RepForge headless GLB asset pipeline (todo 24).

Pipeline: load catalog YAML -> validate -> per exercise invoke Blender headless
(build animations, export GLB, render WebP thumbnail) -> SHA256 over real GLB
bytes -> write exercise_manifest.json.

Modes
-----
* Real mode (needs Blender 4.x on PATH or via --blender / $REPFORGE_BLENDER):
    uv run --with pyyaml python tools/exercise-assets/build_assets.py \
        --catalog tools/exercise-assets/exercise_catalog.yaml --out build/assets
  Generates athlete_master.blend procedurally if missing (build_rig.py), then
  one `blender --background` subprocess per exercise (export_glb.py).

* Dry-run (NO Blender required) - full catalog validation + build plan:
    uv run --with pyyaml python tools/exercise-assets/build_assets.py \
        --catalog tools/exercise-assets/exercise_catalog.yaml \
        --out build/assets --dry-run

Manifest entry schema consumed downstream by todo 25 (R2 upload):
    {exerciseId, glbPath, webpPath, sha256, sizeBytes, version}
where glbPath/webpPath are relative to the output dir, sha256 is over the GLB
bytes, sizeBytes is the GLB size in bytes and version = sha256[:12].
"""
from __future__ import annotations

import argparse
import concurrent.futures
import datetime as _dt
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

try:
    import yaml
except ImportError:  # pragma: no cover - friendly hint instead of a traceback
    sys.stderr.write(
        "error: PyYAML is required. Run via uv:\n"
        "  uv run --with pyyaml python tools/exercise-assets/build_assets.py ...\n"
    )
    raise SystemExit(2)

sys.path.insert(0, str(Path(__file__).resolve().parent))

from rig_spec import (  # noqa: E402
    BUILD_RIG_SCRIPT,
    DIFFICULTIES,
    EQUIPMENT_PROPS,
    EQUIPMENT_TYPES,
    EXPORT_GLB_SCRIPT,
    MANIFEST_ENTRY_KEYS,
    MANIFEST_SCHEMA_VERSION,
    MUSCLES,
    PATTERNS,
    PHASES,
    RIG_BONES,
    RIG_FILE,
    TRACKING_MODES,
    VERSION_HEX_LEN,
)

TOOLS_DIR = Path(__file__).resolve().parent
DEFAULT_CATALOG = TOOLS_DIR / "exercise_catalog.yaml"
DEFAULT_OUT = Path("build/assets")

_ID_RE = re.compile(r"^[a-z][a-z0-9_]*$")
ACTION_RE = re.compile(r"^EX_[a-z0-9_]+$")

REQUIRED_FIELDS = (
    "id", "name", "equipment", "pattern", "primary_muscles", "secondary_muscles",
    "tracking_mode", "difficulty", "unilateral", "animation_action", "phases",
    "bones_required",
)


# ---------------------------------------------------------------------------
# Catalog loading + validation
# ---------------------------------------------------------------------------


class _DuplicateKeyLoader(yaml.SafeLoader):
    """SafeLoader that rejects duplicate mapping keys (e.g. duplicate ids)."""


def _no_dup_keys(loader, node, deep=False):
    seen = set()
    for key_node, _value_node in node.value:
        key = loader.construct_object(key_node, deep=deep)
        try:
            hash(key)
        except TypeError:
            hash(str(key))
        if key in seen:
            raise yaml.constructor.ConstructorError(
                None, None, f"duplicate key {key!r} in mapping", node.start_mark)
        seen.add(key)
    return yaml.SafeLoader.construct_mapping(loader, node, deep)


_DuplicateKeyLoader.add_constructor(
    yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG, _no_dup_keys)


def load_catalog(path: Path) -> dict:
    """Parse the catalog YAML; raises SystemExit with a clear message on error."""
    if not path.is_file():
        raise SystemExit(f"error: catalog not found: {path}")
    try:
        catalog = yaml.load(path.read_text(encoding="utf-8"), Loader=_DuplicateKeyLoader)
    except yaml.YAMLError as exc:
        raise SystemExit(f"error: invalid YAML in {path}:\n{exc}")
    if not isinstance(catalog, dict):
        raise SystemExit(f"error: catalog root must be a mapping, got {type(catalog).__name__}")
    return catalog


def validate_catalog(catalog: dict) -> list[str]:
    """Return a list of validation errors (empty list = valid)."""
    errors: list[str] = []

    exercises = catalog.get("exercises")
    if not isinstance(exercises, dict) or not exercises:
        errors.append("catalog: 'exercises' must be a non-empty mapping")
        return errors

    seen_actions: dict[str, str] = {}
    for ex_id, cfg in exercises.items():
        def p(msg, _ex=ex_id):  # noqa: E731
            errors.append(f"{_ex}: {msg}")

        if not _ID_RE.match(str(ex_id)):
            p(f"id {ex_id!r} must match {_ID_RE.pattern}")
        if not isinstance(cfg, dict):
            p("entry must be a mapping")
            continue

        missing = [f for f in REQUIRED_FIELDS if f not in cfg]
        if missing:
            p(f"missing required field(s): {missing}")
            continue  # cannot type-check an incomplete entry safely

        if cfg.get("id") != ex_id:
            p(f"'id' field ({cfg.get('id')!r}) does not match key ({ex_id!r})")

        name = cfg.get("name")
        if not isinstance(name, str) or not name.strip():
            p("'name' must be a non-empty string")

        if cfg.get("equipment") not in EQUIPMENT_TYPES:
            p(f"'equipment' must be one of {sorted(EQUIPMENT_TYPES)}, "
              f"got {cfg.get('equipment')!r}")

        if cfg.get("pattern") not in PATTERNS:
            p(f"'pattern' must be one of {sorted(PATTERNS)}, got {cfg.get('pattern')!r}")

        for mfield in ("primary_muscles", "secondary_muscles"):
            muscles = cfg.get(mfield)
            if not isinstance(muscles, list) or not all(isinstance(m, str) for m in muscles):
                p(f"'{mfield}' must be a list of strings")
                continue
            unknown = [m for m in muscles if m not in MUSCLES]
            if unknown:
                p(f"'{mfield}' has unknown muscles {unknown}; "
                  f"vocabulary: {sorted(MUSCLES)}")
        primaries = cfg.get("primary_muscles")
        if isinstance(primaries, list) and not primaries:
            p("'primary_muscles' must not be empty")

        if cfg.get("tracking_mode") not in TRACKING_MODES:
            p(f"'tracking_mode' must be one of {sorted(TRACKING_MODES)}, "
              f"got {cfg.get('tracking_mode')!r}")

        if cfg.get("difficulty") not in DIFFICULTIES:
            p(f"'difficulty' must be one of {sorted(DIFFICULTIES)}, "
              f"got {cfg.get('difficulty')!r}")

        if not isinstance(cfg.get("unilateral"), bool):
            p("'unilateral' must be a boolean")

        action = cfg.get("animation_action")
        expected_action = f"EX_{ex_id}"
        if action != expected_action:
            p(f"'animation_action' must be {expected_action!r}, got {action!r}")
        elif isinstance(action, str) and not ACTION_RE.match(action):
            p(f"'animation_action' {action!r} must match {ACTION_RE.pattern}")
        else:
            other = seen_actions.get(action)
            if other is not None:
                p(f"duplicate animation_action {action!r} (also used by {other})")
            seen_actions[action] = ex_id

        phases = cfg.get("phases")
        if phases != list(PHASES):
            p(f"'phases' must be exactly {list(PHASES)}, got {phases!r}")

        bones = cfg.get("bones_required")
        if not isinstance(bones, list) or not bones:
            p("'bones_required' must be a non-empty list")
        elif not all(isinstance(b, str) for b in bones):
            p("'bones_required' must contain only strings")
        else:
            unknown_bones = sorted(set(bones) - set(RIG_BONES))
            if unknown_bones:
                p(f"'bones_required' references bones outside RIG_BONES: {unknown_bones}")
            if len(bones) != len(set(bones)):
                p("'bones_required' contains duplicates")

    return errors


def iter_exercises(catalog: dict):
    yield from sorted(catalog.get("exercises", {}).items())


def planned_outputs(ex_id: str, out_dir: Path) -> tuple[Path, Path]:
    return out_dir / f"{ex_id}.glb", out_dir / f"{ex_id}.webp"


# ---------------------------------------------------------------------------
# Hashing + manifest assembly
# ---------------------------------------------------------------------------


def compute_sha256(path: Path) -> tuple[str, int]:
    """Stream-hash a file. Returns (hex digest, size in bytes)."""
    h = hashlib.sha256()
    size = 0
    with path.open("rb") as fh:
        while chunk := fh.read(1 << 20):
            h.update(chunk)
            size += len(chunk)
    return h.hexdigest(), size


def assemble_manifest_entry(ex_id: str, glb_path: Path, webp_path: Path,
                            sha256: str, size_bytes: int) -> dict:
    """One manifest entry in the todo-25 upload format."""
    entry = {
        "exerciseId": ex_id,
        "glbPath": glb_path.name,   # relative to the output dir (R2 object key)
        "webpPath": webp_path.name,
        "sha256": sha256,
        "sizeBytes": size_bytes,
        "version": sha256[:VERSION_HEX_LEN],
    }
    assert set(entry) == set(MANIFEST_ENTRY_KEYS)
    return entry


def write_manifest(entries: list[dict], path: Path, catalog_sha256: str) -> None:
    entries = sorted(entries, key=lambda e: e["exerciseId"])
    doc = {
        "schemaVersion": MANIFEST_SCHEMA_VERSION,
        "generatedAtUtc": _dt.datetime.now(_dt.timezone.utc).isoformat(timespec="seconds"),
        "catalogSha256": catalog_sha256,
        "entryFormat": list(MANIFEST_ENTRY_KEYS),
        "exercises": entries,
    }
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(doc, indent=2) + "\n", encoding="utf-8")


# ---------------------------------------------------------------------------
# Blender invocation
# ---------------------------------------------------------------------------


def find_blender(explicit: str | None) -> str | None:
    candidate = explicit or os.environ.get("REPFORGE_BLENDER") or shutil.which("blender")
    return candidate


def ensure_rig(blender: str, rig_path: Path, timeout: int = 300) -> Path:
    """Generate athlete_master.blend procedurally when absent."""
    if rig_path.is_file() and rig_path.stat().st_size > 0:
        return rig_path
    rig_path.parent.mkdir(parents=True, exist_ok=True)
    script = TOOLS_DIR / BUILD_RIG_SCRIPT
    cmd = [blender, "--background", "--python", str(script), "--", "--out", str(rig_path)]
    print(f"[rig] generating {rig_path} ...")
    proc = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout)
    marker = [ln for ln in proc.stdout.splitlines() if ln.startswith("RESULT_JSON:")]
    if proc.returncode != 0 or not rig_path.is_file():
        tail = "\n".join((proc.stdout + proc.stderr).splitlines()[-25:])
        raise RuntimeError(f"build_rig failed (rc={proc.returncode}):\n{tail}")
    print(f"[rig] ok {marker[-1][len('RESULT_JSON:'):] if marker else ''}")
    return rig_path


def export_one(blender: str, rig: Path, ex_id: str, pattern: str, equipment: str,
               out_dir: Path, timeout: int = 600) -> dict:
    """Run one headless Blender export; returns a manifest entry."""
    glb_path, webp_path = planned_outputs(ex_id, out_dir)
    props = ",".join(EQUIPMENT_PROPS.get(equipment, ()))
    cmd = [
        blender, "--background", str(rig), "--python", str(TOOLS_DIR / EXPORT_GLB_SCRIPT),
        "--",
        "--exercise-id", ex_id,
        "--pattern", pattern,
        "--glb", str(glb_path),
        "--thumb", str(webp_path),
        "--props", props,
    ]
    proc = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout)
    markers = [ln for ln in proc.stdout.splitlines() if ln.startswith("RESULT_JSON:")]
    result = json.loads(markers[-1][len("RESULT_JSON:"):]) if markers else {}
    if proc.returncode != 0 or not result.get("ok"):
        tail = "\n".join((proc.stdout + proc.stderr).splitlines()[-25:])
        raise RuntimeError(f"blender export failed for {ex_id} (rc={proc.returncode}):"
                           f"\n{tail}\nresult={result}")

    if not glb_path.is_file():
        raise RuntimeError(f"{ex_id}: blender reported success but {glb_path} is missing")
    sha256, size_bytes = compute_sha256(glb_path)
    if size_bytes == 0:
        raise RuntimeError(f"{ex_id}: exported GLB is empty")
    if not webp_path.is_file() or webp_path.stat().st_size == 0:
        raise RuntimeError(f"{ex_id}: thumbnail {webp_path} missing/empty")
    return assemble_manifest_entry(ex_id, glb_path, webp_path, sha256, size_bytes)


# ---------------------------------------------------------------------------
# Modes
# ---------------------------------------------------------------------------


def run_dry_run(catalog: dict, out_dir: Path) -> int:
    """Validate everything we can WITHOUT Blender. Returns exit code."""
    errors = validate_catalog(catalog)
    exercises = dict(iter_exercises(catalog))

    by_equipment: dict[str, int] = {}
    by_pattern: dict[str, int] = {}
    for _, cfg in exercises.items():
        by_equipment[cfg["equipment"]] = by_equipment.get(cfg["equipment"], 0) + 1
        by_pattern[cfg["pattern"]] = by_pattern.get(cfg["pattern"], 0) + 1

    print("=" * 72)
    print("RepForge asset pipeline - DRY RUN (no Blender invocation)")
    print("=" * 72)
    print(f"catalog exercises : {len(exercises)}")
    print(f"output dir        : {out_dir}")
    print("per equipment     : " + ", ".join(f"{k}={v}" for k, v in sorted(by_equipment.items())))
    print("per pattern       : " + ", ".join(f"{k}={v}" for k, v in sorted(by_pattern.items())))
    print("-" * 72)
    shown = 0
    for ex_id, cfg in exercises.items():
        if shown >= 5 and len(exercises) > 10:
            print(f"  ... ({len(exercises) - shown} more)")
            break
        glb, webp = planned_outputs(ex_id, out_dir)
        print(f"  [dry-run] EX_{ex_id:<38} pattern={cfg['pattern']:<16}"
              f" eq={cfg['equipment']:<10} -> {glb.name} + {webp.name}")
        shown += 1
    print("-" * 72)

    if errors:
        print(f"VALIDATION FAILED - {len(errors)} error(s):")
        for err in errors[:50]:
            print(f"  [FAIL] {err}")
        if len(errors) > 50:
            print(f"  ... and {len(errors) - 50} more")
        return 1
    print(f"VALIDATION OK - {len(exercises)} exercises, "
          f"{len(exercises) * len(PHASES)} phase actions would be generated.")
    print("No files were written (dry-run).")
    return 0


def run_build(args: argparse.Namespace) -> int:
    blender = find_blender(args.blender)
    if blender is None:
        sys.stderr.write(
            "error: Blender not found.\n"
            "  - install Blender 4.x and put it on PATH, or\n"
            "  - pass --blender /path/to/blender, or set REPFORGE_BLENDER, or\n"
            "  - use --dry-run to validate without Blender (see README.md).\n"
        )
        return 2

    catalog = load_catalog(Path(args.catalog))
    errors = validate_catalog(catalog)
    if errors:
        print(f"VALIDATION FAILED - {len(errors)} error(s):")
        for err in errors[:50]:
            print(f"  [FAIL] {err}")
        return 1

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    rig = ensure_rig(blender, Path(args.rig))

    exercises = list(iter_exercises(catalog))
    if args.limit is not None:
        exercises = exercises[: args.limit]
        print(f"[limit] building first {len(exercises)} exercise(s) only")

    print(f"[build] {len(exercises)} exercise(s) -> {out_dir} "
          f"(blender={blender}, jobs={args.jobs})")

    entries: list[dict] = []
    failures: list[str] = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=args.jobs) as pool:
        futures = {
            pool.submit(export_one, blender, rig, ex_id, cfg["pattern"],
                        cfg["equipment"], out_dir): ex_id
            for ex_id, cfg in exercises
        }
        for fut in concurrent.futures.as_completed(futures):
            ex_id = futures[fut]
            try:
                entry = fut.result()
            except Exception as exc:  # noqa: BLE001 - collect and continue
                failures.append(f"{ex_id}: {exc}")
                print(f"  [FAIL] {ex_id}: {exc.__class__.__name__}: {exc}")
            else:
                entries.append(entry)
                print(f"  [ok] {ex_id}: glb={entry['sizeBytes']}B "
                      f"sha256={entry['sha256'][:16]}...")

    if failures:
        print(f"\nBUILD FAILED - {len(failures)}/{len(exercises)} exercise(s) failed:")
        for f in failures:
            print(f"  [FAIL] {f}")
        return 1

    manifest_path = Path(args.manifest) if args.manifest else out_dir / "exercise_manifest.json"
    catalog_sha, _ = compute_sha256(Path(args.catalog))
    write_manifest(entries, manifest_path, catalog_sha)
    total_bytes = sum(e["sizeBytes"] for e in entries)
    print(f"\nwrote {manifest_path} with {len(entries)} entries "
          f"({total_bytes / 1024:.1f} KiB of GLB data)")
    return 0


# ---------------------------------------------------------------------------


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--catalog", default=str(DEFAULT_CATALOG),
                        help=f"path to exercise_catalog.yaml (default: {DEFAULT_CATALOG})")
    parser.add_argument("--out", default=str(DEFAULT_OUT),
                        help=f"output directory for GLB/WebP (default: {DEFAULT_OUT})")
    parser.add_argument("--manifest", default=None,
                        help="manifest path (default: <out>/exercise_manifest.json)")
    parser.add_argument("--dry-run", action="store_true",
                        help="validate catalog + print plan WITHOUT invoking Blender")
    parser.add_argument("--blender", default=None,
                        help="path to blender executable (else $REPFORGE_BLENDER / PATH)")
    parser.add_argument("--rig", default=str(TOOLS_DIR / RIG_FILE),
                        help="athlete_master.blend path; auto-generated if missing")
    parser.add_argument("--jobs", type=int, default=min(4, os.cpu_count() or 1),
                        help="parallel blender processes (default: 4)")
    parser.add_argument("--limit", type=int, default=None,
                        help="only build the first N exercises (CI smoke test)")
    args = parser.parse_args(argv)

    if args.dry_run:
        catalog = load_catalog(Path(args.catalog))
        return run_dry_run(catalog, Path(args.out))
    return run_build(args)


if __name__ == "__main__":
    raise SystemExit(main())
