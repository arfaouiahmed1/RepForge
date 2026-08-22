"""Pipeline unit tests: manifest assembly, hashing, output naming, validation."""
from __future__ import annotations

import hashlib
import pathlib

import pytest
import yaml

import build_assets as ba
from rig_spec import EQUIPMENT_TYPES, MANIFEST_ENTRY_KEYS, PATTERNS, PHASES


@pytest.fixture(scope="module")
def catalog() -> dict:
    return yaml.safe_load(
        (pathlib.Path(__file__).parent.parent / "exercise_catalog.yaml").read_text(encoding="utf-8")
    )


def test_sha256_known_vector(tmp_path: pathlib.Path) -> None:
    p = tmp_path / "blob.bin"
    p.write_bytes(b"repforge")
    digest, size = ba.compute_sha256(p)
    assert digest == hashlib.sha256(b"repforge").hexdigest()
    assert size == len(b"repforge")


def test_manifest_entry_matches_todo25_contract(tmp_path: pathlib.Path) -> None:
    glb = tmp_path / "x.glb"
    webp = tmp_path / "x.webp"
    glb.write_bytes(b"\x00glb-bytes")
    webp.write_bytes(b"\x00webp")
    sha, size = ba.compute_sha256(glb)
    entry = ba.assemble_manifest_entry("bench_press", glb, webp, sha, size)
    assert tuple(entry.keys()) == MANIFEST_ENTRY_KEYS
    assert entry["exerciseId"] == "bench_press"
    assert entry["version"] == sha[: ba.VERSION_HEX_LEN]
    assert entry["sizeBytes"] == len(b"\x00glb-bytes")


def test_planned_outputs_naming() -> None:
    glb, webp = ba.planned_outputs("cable_row", pathlib.Path("out"))
    assert glb.name == "cable_row.glb"
    assert webp.name == "cable_row.webp"


def test_validate_rejects_missing_required_field(catalog: dict) -> None:
    broken = {"exercises": {k: dict(v) for k, v in list(catalog["exercises"].items())[:1]}}
    (_, first), = broken["exercises"].items()
    del first["equipment"]
    errors = ba.validate_catalog(broken)
    assert any("missing required field" in e for e in errors)


def test_validate_rejects_unknown_muscle(catalog: dict) -> None:
    broken = {"exercises": {k: dict(v) for k, v in list(catalog["exercises"].items())[:1]}}
    (_, first), = broken["exercises"].items()
    first["primary_muscles"] = ["shoulder"]  # not in vocabulary
    errors = ba.validate_catalog(broken)
    assert any("unknown muscles" in e for e in errors)


def test_validate_rejects_wrong_phases(catalog: dict) -> None:
    broken = {"exercises": {k: dict(v) for k, v in list(catalog["exercises"].items())[:1]}}
    (_, first), = broken["exercises"].items()
    first["phases"] = list(reversed(PHASES))
    errors = ba.validate_catalog(broken)
    assert any("'phases' must be exactly" in e for e in errors)


def test_dry_run_green_on_generated_catalog(catalog: dict) -> None:
    assert ba.validate_catalog(catalog) == []


def test_vocabularies_consistent_with_rig_spec() -> None:
    assert len(PHASES) == 5
    assert "barbell" in EQUIPMENT_TYPES and "squat" in PATTERNS
