"""Catalog generator tests: scale, uniqueness, vocabulary, pattern coverage."""
from __future__ import annotations

import gen_catalog
import yaml
from rig_spec import FAMILY_PRIMARY_MUSCLE, MOVEMENT_FAMILIES, MUSCLES, RIG_BONES


def load_generated() -> dict:
    return gen_catalog.generate(250)


def test_generates_at_least_250_entries() -> None:
    cat = load_generated()
    assert len(cat["exercises"]) >= 250


def test_all_ids_and_actions_unique_and_valid() -> None:
    import re

    ids = [e["id"] for e in load_generated()["exercises"].values()]
    assert len(ids) == len(set(ids))
    actions = [e["animation_action"] for e in load_generated()["exercises"].values()]
    assert len(actions) == len(set(actions))
    id_re = re.compile(r"^[a-z][a-z0-9_]*$")
    assert all(id_re.match(i) for i in ids)


def test_every_movement_family_represented() -> None:
    exercises = load_generated()["exercises"].values()
    primaries = {m for e in exercises for m in e["primary_muscles"]}
    expected = set(FAMILY_PRIMARY_MUSCLE.values())
    missing = expected - primaries
    assert not missing, f"families missing from catalog: {missing}"


def test_every_entry_valid_against_pipeline_validator() -> None:
    import build_assets as ba

    cat = load_generated()
    assert ba.validate_catalog(cat) == []


def test_bones_subset_of_rig() -> None:
    for e in load_generated()["exercises"].values():
        assert set(e["bones_required"]) <= set(RIG_BONES)


def test_muscles_within_vocabulary() -> None:
    for e in load_generated()["exercises"].values():
        assert set(e["primary_muscles"]) <= MUSCLES
        assert set(e["secondary_muscles"]) <= MUSCLES


def test_yaml_round_trip_matches_generator(tmp_path) -> None:
    import pathlib

    cat = load_generated()
    out = tmp_path / "cat.yaml"
    out.write_text(yaml.safe_dump(cat, sort_keys=False), encoding="utf-8")
    reloaded = yaml.safe_load(out.read_text(encoding="utf-8"))
    assert reloaded == cat
