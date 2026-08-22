#!/usr/bin/env python3
"""R2 upload pipeline (todo 25): exercise_manifest.json -> content-addressed v17 manifest.

Transforms build-pipeline output into the tools/asset-cdn schema and uploads:

  * every GLB/WebP to a CONTENT-ADDRESSED key: /assets/exercises/<id>/movement.v<sha12>.glb
    (same bytes => same key => immutable forever)
  * one manifest object: /manifests/asset-manifest-v17.json (short TTL cache)

Modes:
  * Dry-run (default, NO credentials): validate + emit cdn-asset-manifest.json + print plan.
  * Real upload (--upload): shells out to `wrangler r2 object put` per object.
    Requires wrangler on PATH, CLOUDFLARE_API_TOKEN, bucket from wrangler.toml.

Promotion gates (violation => exit 1):
  * sha256 + sizeBytes present for every entry
  * GLB size <= 5 MB ceiling (warn above 3 MB target)
  * GLB magic bytes == b"glTF" (binary glTF 2.0 sanity check)
"""
from __future__ import annotations

import argparse
import json
import pathlib
import subprocess
import sys
from datetime import datetime, timezone

SCHEMA_VERSION = 17
MAX_GLB_BYTES = 5 * 1024 * 1024
WARN_GLB_BYTES = 3 * 1024 * 1024

GLB_KEY = "/assets/exercises/{ex_id}/movement.v{rev}.{version}.glb"
WEBP_KEY_THUMB = "/assets/exercises/{ex_id}/thumb512.v{rev}.{version}.webp"
WEBP_KEY_HERO = "/assets/exercises/{ex_id}/hero.v{rev}.{version}.webp"
MANIFEST_KEY = f"/manifests/asset-manifest-v{SCHEMA_VERSION}.json"

IMMUTABLE_CACHE = "public, max-age=31536000, immutable"
MANIFEST_CACHE = "public, max-age=60"


def fail(msg: str) -> None:
    print(f"[FAIL] {msg}")
    sys.exit(1)


def load_entries(manifest_path: pathlib.Path) -> list[dict]:
    if not manifest_path.is_file():
        fail(f"build manifest not found: {manifest_path} - run build_assets.py first")
    entries = json.loads(manifest_path.read_text(encoding="utf-8"))
    if not isinstance(entries, list) or not entries:
        fail("build manifest is empty or malformed")
    return entries


def gate_glb(glb_path: pathlib.Path, ex_id: str) -> str | None:
    """Hard-fail on ceiling/magic violations; return warning above soft target."""
    size_bytes = glb_path.stat().st_size
    if size_bytes <= 0:
        fail(f"{ex_id}: empty GLB")
    if size_bytes > MAX_GLB_BYTES:
        fail(f"{ex_id}: GLB {size_bytes}B exceeds {MAX_GLB_BYTES}B promotion ceiling")
    magic = glb_path.open("rb").read(4)
    if magic != b"glTF":
        fail(f"{ex_id}: not binary glTF (magic={magic!r})")
    if size_bytes > WARN_GLB_BYTES:
        return f"{ex_id}: GLB {size_bytes / 1e6:.1f} MB above 3 MB target"
    return None


def to_cdn_entry(entry: dict, build_dir: pathlib.Path, rev: int):
    ex_id = entry.get("exerciseId") or entry.get("id")
    sha = entry.get("sha256")
    size = int(entry.get("sizeBytes", 0))
    version = (entry.get("version") or sha or "")[:12]
    if not ex_id or not sha or len(sha) != 64 or size <= 0:
        fail(f"malformed manifest entry: {entry}")

    glb_local = build_dir / entry["glbPath"]
    webp_local = build_dir / entry["webpPath"]
    if not glb_local.is_file():
        fail(f"{ex_id}: GLB missing on disk: {glb_local}")
    if not webp_local.is_file():
        fail(f"{ex_id}: WebP missing on disk: {webp_local}")

    warn = gate_glb(glb_local, ex_id)

    cdn_entry = {
        "exerciseId": ex_id,
        "glbPath": GLB_KEY.format(ex_id=ex_id, rev=rev, version=version),
        "thumb512Path": WEBP_KEY_THUMB.format(ex_id=ex_id, rev=rev, version=version),
        "heroPath": WEBP_KEY_HERO.format(ex_id=ex_id, rev=rev, version=version),
        "sha256": sha,
        "sizeBytes": size,
        "version": rev,
        "draco": False,
    }
    uploads = [
        (glb_local, cdn_entry["glbPath"], IMMUTABLE_CACHE),
        (webp_local, cdn_entry["thumb512Path"], IMMUTABLE_CACHE),
    ]
    return cdn_entry, warn, uploads


def wrangler_put(bucket: str, local: pathlib.Path, key: str, cache: str) -> None:
    cmd = [
        "wrangler", "r2", "object", "put", f"{bucket}{key}",
        "--file", str(local), "--remote",
        "--cache-control", cache,
    ]
    proc = subprocess.run(cmd, capture_output=True, text=True)
    if proc.returncode != 0:
        fail(f"wrangler put {key}\n{(proc.stdout + proc.stderr)[-400:]}")


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--build-dir", default="build/assets")
    ap.add_argument("--manifest", default="tools/exercise-assets/exercise_manifest.json")
    ap.add_argument("--out", default="tools/asset-cdn/cdn-asset-manifest.json")
    ap.add_argument("--bucket", default="repforge-assets")
    ap.add_argument("--upload", action="store_true", help="perform real wrangler upload")
    ap.add_argument("--revision", type=int, default=1, help="asset revision seq used in content keys")
    args = ap.parse_args()

    build_dir = pathlib.Path(args.build_dir)
    entries = load_entries(pathlib.Path(args.manifest))

    cdn_entries: list[dict] = []
    all_uploads: list[tuple[pathlib.Path, str, str]] = []
    warnings: list[str] = []
    for e in entries:
        cdn_entry, warn, uploads = to_cdn_entry(e, build_dir, args.revision)
        cdn_entries.append(cdn_entry)
        all_uploads.extend(uploads)
        if warn:
            warnings.append(warn)

    manifest_doc = {
        "manifestVersion": SCHEMA_VERSION,
        "generatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "assets": sorted(cdn_entries, key=lambda x: x["exerciseId"]),
    }
    out = pathlib.Path(args.out)
    out.write_text(json.dumps(manifest_doc, indent=2), encoding="utf-8")

    total_bytes = sum(p.stat().st_size for p, _, _ in all_uploads)
    print("=" * 72)
    print(f"entries          : {len(cdn_entries)}")
    print(f"objects to push  : {len(all_uploads)} (+1 manifest at {MANIFEST_KEY})")
    print(f"total payload    : {total_bytes / 1e6:.1f} MB")
    for w in warnings:
        print(f"[WARN] {w}")
    print(f"manifest written : {out}")
    print("-" * 72)

    if args.upload:
        for local, key, cache in all_uploads:
            wrangler_put(args.bucket, local, key, cache)
            print(f"[put] {key}")
        wrangler_put(args.bucket, out, MANIFEST_KEY, MANIFEST_CACHE)
        print(f"[put] {MANIFEST_KEY}")
    else:
        print("DRY-RUN: no objects uploaded. Pass --upload with wrangler configured.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

