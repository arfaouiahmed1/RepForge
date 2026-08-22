#!/usr/bin/env python3
"""Validate RepForge CDN manifests (assetManifest v17 and modelManifest).

Usage:
    uv run python tools/asset-cdn/validate_manifest.py <manifest.json> [more.json ...]

Exit codes:
    0  every manifest is valid
    1  at least one manifest is invalid (reasons printed to stdout)
    2  usage error (no file argument, unreadable file, or not JSON)

Stdlib only - no third-party dependencies, so it runs anywhere `uv run python`
(or any Python >= 3.9) is available. It mirrors the two JSON Schema files in
this directory and additionally enforces the cross-field rules JSON Schema
cannot express:

  asset manifest (v17):
    * glbPath directory segment must equal exerciseId
    * glbPath embedded hash segment must equal the first 12 chars of sha256
      (content-addressing: a path is immutable for a given digest)
    * version segments embedded in glbPath/thumb512Path/heroPath must equal
      the top-level version field
    * exerciseId must be unique across assets

  model manifest:
    * url must be absolute https on the CDN host family
"""

from __future__ import annotations

import json
import re
import sys

SHA256_RE = re.compile(r"^[0-9a-f]{64}$")
EXERCISE_ID_RE = re.compile(r"^[a-z][a-z0-9_]*$")
GENERATED_AT_RE = re.compile(
    r"^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\.[0-9]+)?(Z|[+-][0-9]{2}:[0-9]{2})$"
)
GLB_PATH_RE = re.compile(
    r"^/assets/exercises/(?P<id>[a-z][a-z0-9_]*)"
    r"/movement\.v(?P<ver>[1-9][0-9]*)\.(?P<hash>[0-9a-f]{12})\.glb$"
)
THUMB_PATH_RE = re.compile(
    r"^/assets/exercises/(?P<id>[a-z][a-z0-9_]*)"
    r"/thumb512\.v(?P<ver>[1-9][0-9]*)\.[0-9a-f]{12}\.webp$"
)
HERO_PATH_RE = re.compile(
    r"^/assets/exercises/(?P<id>[a-z][a-z0-9_]*)"
    r"/hero\.v(?P<ver>[1-9][0-9]*)\.[0-9a-f]{12}\.webp$"
)
SEMVER_RE = re.compile(r"^[0-9]+\.[0-9]+\.[0-9]+$")
MODEL_NAME_RE = re.compile(r"^[a-z][a-z0-9_-]*$")
CDN_URL_RE = re.compile(r"^https://assets\.repforge\.app/")

ASSET_REQUIRED_FIELDS = (
    "exerciseId",
    "glbPath",
    "thumb512Path",
    "heroPath",
    "sha256",
    "sizeBytes",
    "version",
    "draco",
)
MODEL_REQUIRED_FIELDS = (
    "name",
    "version",
    "schemaVersion",
    "minAppVersion",
    "sha256",
    "url",
    "rollout",
)


def _is_int(value: object) -> bool:
    # bool is a subclass of int in Python; reject it explicitly.
    return isinstance(value, int) and not isinstance(value, bool)


def validate_asset_manifest(doc: object) -> list[str]:
    """Return a list of failure reasons; empty list means valid."""
    errors: list[str] = []
    if not isinstance(doc, dict):
        return ["document root must be a JSON object"]

    manifest_version = doc.get("manifestVersion")
    if manifest_version != 17:
        errors.append(
            f"manifestVersion: expected constant 17, got {manifest_version!r}"
        )

    generated_at = doc.get("generatedAt")
    if not isinstance(generated_at, str) or not GENERATED_AT_RE.match(generated_at):
        errors.append(
            "generatedAt: missing or not an RFC 3339 timestamp "
            f"(got {generated_at!r})"
        )

    assets = doc.get("assets")
    if not isinstance(assets, list) or len(assets) == 0:
        errors.append("assets: required non-empty array")
        return errors

    seen_ids: set[str] = set()
    for index, asset in enumerate(assets):
        prefix = f"assets[{index}]"
        if not isinstance(asset, dict):
            errors.append(f"{prefix}: must be a JSON object")
            continue

        for field in ASSET_REQUIRED_FIELDS:
            if field not in asset:
                errors.append(f"{prefix}.{field}: required field is missing")

        exercise_id = asset.get("exerciseId")
        if not isinstance(exercise_id, str) or not EXERCISE_ID_RE.match(exercise_id):
            errors.append(
                f"{prefix}.exerciseId: must match ^[a-z][a-z0-9_]*$ "
                f"(got {exercise_id!r})"
            )
        elif exercise_id in seen_ids:
            errors.append(f"{prefix}.exerciseId: duplicate exercise id {exercise_id!r}")
        else:
            seen_ids.add(exercise_id)

        sha256 = asset.get("sha256")
        sha_ok = isinstance(sha256, str) and SHA256_RE.match(sha256) is not None
        if not sha_ok:
            errors.append(
                f"{prefix}.sha256: must be 64 lowercase hex chars (got {sha256!r})"
            )

        size_bytes = asset.get("sizeBytes")
        if not _is_int(size_bytes) or size_bytes < 1:
            errors.append(
                f"{prefix}.sizeBytes: must be an integer >= 1 (got {size_bytes!r})"
            )

        version = asset.get("version")
        if not _is_int(version) or version < 1:
            errors.append(
                f"{prefix}.version: must be an integer >= 1 (got {version!r})"
            )

        draco = asset.get("draco")
        if not isinstance(draco, bool):
            errors.append(f"{prefix}.draco: must be a boolean (got {draco!r})")

        # --- cross-field checks -------------------------------------------
        glb_path = asset.get("glbPath")
        if isinstance(glb_path, str):
            match = GLB_PATH_RE.match(glb_path)
            if match is None:
                errors.append(
                    f"{prefix}.glbPath: must match "
                    "'/assets/exercises/<id>/movement.v<N>.<sha256-12>.glb' "
                    f"(got {glb_path!r})"
                )
            else:
                if isinstance(exercise_id, str) and match.group("id") != exercise_id:
                    errors.append(
                        f"{prefix}.glbPath: directory segment {match.group('id')!r} "
                        f"does not equal exerciseId {exercise_id!r}"
                    )
                if _is_int(version) and int(match.group("ver")) != version:
                    errors.append(
                        f"{prefix}.glbPath: embedded version v{match.group('ver')} "
                        f"does not equal version field {version}"
                    )
                # Content-addressing: path hash must be the sha256 prefix.
                # Compare even when sha256 itself is malformed so a corrupt
                # fixture reports both problems.
                expected_prefix = (
                    sha256[:12] if isinstance(sha256, str) and len(sha256) >= 12 else sha256
                )
                if match.group("hash") != expected_prefix:
                    errors.append(
                        f"{prefix}.glbPath: embedded hash '{match.group('hash')}' "
                        f"does not match first 12 chars of sha256 ({expected_prefix!r})"
                    )
        elif "glbPath" in asset:
            errors.append(f"{prefix}.glbPath: must be a string (got {glb_path!r})")

        for field, regex in (("thumb512Path", THUMB_PATH_RE), ("heroPath", HERO_PATH_RE)):
            path_value = asset.get(field)
            if not isinstance(path_value, str):
                if field in asset:
                    errors.append(f"{prefix}.{field}: must be a string (got {path_value!r})")
                continue
            match = regex.match(path_value)
            if match is None:
                kind = "thumb512" if field == "thumb512Path" else "hero"
                errors.append(
                    f"{prefix}.{field}: must match "
                    f"'/assets/exercises/<id>/{kind}.v<N>.<hash12>.webp' "
                    f"(got {path_value!r})"
                )
                continue
            if isinstance(exercise_id, str) and match.group("id") != exercise_id:
                errors.append(
                    f"{prefix}.{field}: directory segment {match.group('id')!r} "
                    f"does not equal exerciseId {exercise_id!r}"
                )
            if _is_int(version) and int(match.group("ver")) != version:
                errors.append(
                    f"{prefix}.{field}: embedded version v{match.group('ver')} "
                    f"does not equal version field {version}"
                )

    return errors


def validate_model_manifest(doc: object) -> list[str]:
    """Return a list of failure reasons; empty list means valid."""
    errors: list[str] = []
    if not isinstance(doc, dict):
        return ["document root must be a JSON object"]

    manifest_version = doc.get("manifestVersion")
    if not _is_int(manifest_version) or manifest_version < 1:
        errors.append(
            f"manifestVersion: must be an integer >= 1 (got {manifest_version!r})"
        )

    models = doc.get("models")
    if not isinstance(models, list) or len(models) == 0:
        errors.append("models: required non-empty array")
        return errors

    for index, model in enumerate(models):
        prefix = f"models[{index}]"
        if not isinstance(model, dict):
            errors.append(f"{prefix}: must be a JSON object")
            continue

        for field in MODEL_REQUIRED_FIELDS:
            if field not in model:
                errors.append(f"{prefix}.{field}: required field is missing")

        name = model.get("name")
        if not isinstance(name, str) or not MODEL_NAME_RE.match(name):
            errors.append(
                f"{prefix}.name: must match ^[a-z][a-z0-9_-]*$ (got {name!r})"
            )

        version = model.get("version")
        if not _is_int(version) or version < 1:
            errors.append(
                f"{prefix}.version: must be an integer >= 1 (got {version!r})"
            )

        schema_version = model.get("schemaVersion")
        if not _is_int(schema_version) or schema_version < 1:
            errors.append(
                f"{prefix}.schemaVersion: must be an integer >= 1 (got {schema_version!r})"
            )

        min_app_version = model.get("minAppVersion")
        if not isinstance(min_app_version, str) or not SEMVER_RE.match(min_app_version):
            errors.append(
                f"{prefix}.minAppVersion: must be semver X.Y.Z (got {min_app_version!r})"
            )

        sha256 = model.get("sha256")
        if not isinstance(sha256, str) or SHA256_RE.match(sha256) is None:
            errors.append(
                f"{prefix}.sha256: must be 64 lowercase hex chars (got {sha256!r})"
            )

        url = model.get("url")
        if not isinstance(url, str) or not CDN_URL_RE.match(url):
            errors.append(
                f"{prefix}.url: must be an absolute https URL on "
                f"assets.repforge.app (got {url!r})"
            )

        rollout = model.get("rollout")
        if not _is_int(rollout) or not 0 <= rollout <= 100:
            errors.append(
                f"{prefix}.rollout: must be an integer between 0 and 100 (got {rollout!r})"
            )

    return errors


def classify_and_validate(doc: object) -> tuple[str, list[str]]:
    if isinstance(doc, dict) and "assets" in doc:
        return "assetManifest", validate_asset_manifest(doc)
    if isinstance(doc, dict) and "models" in doc:
        return "modelManifest", validate_model_manifest(doc)
    return "unknown", [
        "unknown manifest type: expected top-level 'assets' (assetManifest v17) "
        "or 'models' (modelManifest)"
    ]


def main(argv: list[str]) -> int:
    if len(argv) < 2:
        print(__doc__.strip(), file=sys.stderr)
        return 2

    any_failed = False
    for raw_path in argv[1:]:
        try:
            with open(raw_path, "r", encoding="utf-8") as handle:
                doc = json.load(handle)
        except OSError as exc:
            print(f"FAIL {raw_path}: cannot read file ({exc})")
            any_failed = True
            continue
        except json.JSONDecodeError as exc:
            print(f"FAIL {raw_path}: invalid JSON ({exc})")
            any_failed = True
            continue

        kind, errors = classify_and_validate(doc)
        if errors:
            any_failed = True
            print(f"FAIL {raw_path} ({kind}): {len(errors)} problem(s)")
            for reason in errors:
                print(f"  - {reason}")
        else:
            count_key = "assets" if kind == "assetManifest" else "models"
            print(f"OK {raw_path} ({kind}): {len(doc[count_key])} entries validated")

    return 1 if any_failed else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
