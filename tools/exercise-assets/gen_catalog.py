#!/usr/bin/env python3
"""Generate the RepForge exercise catalog (todo 24 -> todo 26 scale).

Produces tools/exercise-assets/exercise_catalog.yaml with 250+ entries spanning
all 12 movement families x applicable equipment types x variations. Every entry
satisfies build_assets.validate_catalog (REQUIRED_FIELDS, vocabularies in
rig_spec.py, exact PHASES list, bones_required subset of RIG_BONES, unique ids
and animation actions).

Run:
    uv run --with pyyaml python tools/exercise-assets/gen_catalog.py \
        --out tools/exercise-assets/exercise_catalog.yaml [--min 250]
"""
from __future__ import annotations

import argparse
import pathlib
import sys

import yaml

sys.path.insert(0, str(pathlib.Path(__file__).parent))
from rig_spec import (  # noqa: E402
    BONE_PRESETS,
    EQUIPMENT_TYPES,
    FAMILY_PRIMARY_MUSCLE,
    MOVEMENT_FAMILIES,
    PATTERNS,
    PHASES,
)

# family -> list of movement templates:
#   (base_name, pattern, secondary_muscles, allowed_equipment or None=all)
TEMPLATES: dict[str, list[tuple[str, str, tuple[str, ...], set[str] | None]]] = {
    "chest": [
        ("bench press", "push_horizontal", ("triceps", "front_delts"), None),
        ("incline press", "push_horizontal", ("triceps", "front_delts"), {"barbell", "dumbbell", "machine"}),
        ("fly", "isolation", ("front_delts",), {"dumbbell", "cable", "band"}),
        ("dip", "push_horizontal", ("triceps", "front_delts"), {"bodyweight"}),
        ("push-up", "push_horizontal", ("triceps", "core"), {"bodyweight", "band"}),
        ("crossover", "isolation", ("front_delts",), {"cable", "band"}),
    ],
    "back": [
        ("row", "pull_horizontal", ("biceps", "rear_delts"), {"barbell", "dumbbell", "cable", "machine", "band"}),
        ("lat pulldown", "pull_vertical", ("biceps", "rear_delts"), {"machine", "cable", "band"}),
        ("pull-up", "pull_vertical", ("biceps", "core"), {"bodyweight"}),
        ("deadlift", "hinge", ("hamstrings", "glutes", "traps"), {"barbell"}),
        ("pullover", "isolation", ("chest", "triceps"), {"dumbbell", "cable", "band"}),
        ("shrug", "isolation", ("traps",), {"barbell", "dumbbell", "machine"}),
    ],
    "shoulders": [
        ("overhead press", "push_vertical", ("triceps", "chest"), {"barbell", "dumbbell", "machine", "band"}),
        ("lateral raise", "isolation", ("traps",), {"dumbbell", "cable", "band", "machine"}),
        ("front raise", "isolation", ("front_delts",), {"dumbbell", "cable", "band"}),
        ("rear delt row", "pull_horizontal", ("rhomboids", "rear_delts"), {"dumbbell", "cable", "band", "machine"}),
        ("face pull", "pull_horizontal", ("rear_delts", "rhomboids"), {"cable", "band"}),
        ("arnold press", "push_vertical", ("triceps", "chest"), {"dumbbell"}),
    ],
    "biceps": [
        ("curl", "isolation", ("forearms",), {"barbell", "dumbbell", "cable", "band", "machine"}),
        ("hammer curl", "isolation", ("forearms",), {"dumbbell", "cable", "band"}),
        ("preacher curl", "isolation", ("forearms",), {"barbell", "dumbbell", "machine"}),
        ("concentration curl", "isolation", ("forearms",), {"dumbbell", "cable", "band"}),
    ],
    "triceps": [
        ("pushdown", "isolation", ("forearms",), {"cable", "band"}),
        ("overhead extension", "isolation", ("forearms",), {"dumbbell", "cable", "band"}),
        ("skullcrusher", "isolation", ("chest",), {"barbell", "dumbbell", "cable", "band"}),
        ("kickback", "isolation", (), {"dumbbell", "cable", "band"}),
        ("close-grip bench press", "push_horizontal", ("chest", "front_delts"), {"barbell", "dumbbell", "machine"}),
    ],
    "quads": [
        ("squat", "squat", ("glutes", "core"), {"barbell", "dumbbell", "bodyweight", "band", "machine"}),
        ("leg press", "squat", ("glutes",), {"machine"}),
        ("lunge", "squat", ("glutes", "hamstrings"), {"barbell", "dumbbell", "bodyweight", "kettlebell"}),
        ("leg extension", "isolation", (), {"machine", "band"}),
        ("step-up", "squat", ("glutes", "calves"), {"dumbbell", "bodyweight", "kettlebell", "barbell"}),
    ],
    "hamstrings": [
        ("romanian deadlift", "hinge", ("glutes", "lower_back"), {"barbell", "dumbbell", "band", "kettlebell"}),
        ("leg curl", "isolation", ("calves",), {"machine", "band"}),
        ("good morning", "hinge", ("glutes", "lower_back"), {"barbell", "band"}),
        ("nordic curl", "hinge", ("calves", "core"), {"bodyweight"}),
    ],
    "glutes": [
        ("hip thrust", "hinge", ("hamstrings",), {"barbell", "dumbbell", "band", "machine", "bodyweight"}),
        ("bridge", "hinge", ("hamstrings", "core"), {"bodyweight", "band"}),
        ("abduction", "isolation", ("obliques",), {"band", "cable", "machine"}),
        ("kickback", "isolation", ("hamstrings",), {"cable", "band", "machine"}),
    ],
    "calves": [
        ("calf raise", "isolation", (), {"barbell", "dumbbell", "machine", "bodyweight", "band"}),
        ("seated calf raise", "isolation", ("tibialis",), {"machine", "dumbbell", "band"}),
    ],
    "core": [
        ("plank", "core_stability", ("obliques",), {"bodyweight", "band"}),
        ("dead bug", "core_stability", ("obliques",), {"bodyweight", "band"}),
        ("hollow hold", "core_stability", ("hip_flexors",), {"bodyweight"}),
        ("leg raise", "core_dynamic", ("hip_flexors",), {"bodyweight", "cable"}),
        ("crunch", "core_dynamic", ("obliques",), {"bodyweight", "cable", "band", "machine"}),
        ("pallof press", "core_stability", ("obliques",), {"cable", "band"}),
        ("russian twist", "rotation", ("obliques",), {"dumbbell", "kettlebell", "band", "bodyweight"}),
        ("woodchopper", "rotation", ("obliques", "front_delts"), {"cable", "band", "dumbbell"}),
    ],
    "carry": [
        ("farmer carry", "carry", ("traps", "core"), {"dumbbell", "kettlebell", "barbell"}),
        ("suitcase carry", "carry", ("obliques", "core"), {"dumbbell", "kettlebell"}),
        ("overhead carry", "carry", ("side_delts", "core"), {"dumbbell", "kettlebell", "kettlebell"}),
    ],
    "mobility": [
        ("hip flexor stretch", "mobility", ("quads",), {"bodyweight", "band"}),
        ("cossack squat", "mobility", ("adductors", "glutes"), {"bodyweight", "kettlebell"}),
        ("jefferson curl", "mobility", ("hamstrings", "lower_back"), {"kettlebell", "dumbbell", "barbell", "bodyweight"}),
        ("tibialis raise", "mobility", ("calves",), {"bodyweight", "band"}),
        ("world's greatest stretch", "mobility", ("adductors", "hip_flexors"), {"bodyweight", "band"}),
    ],
}

EQUIPMENT_LABEL = {
    "barbell": "Barbell",
    "dumbbell": "Dumbbell",
    "cable": "Cable",
    "machine": "Machine",
    "bodyweight": "",
    "band": "Band",
    "kettlebell": "Kettlebell",
}

VARIATIONS = (
    ("", False),
    ("close grip", False),
    ("wide grip", False),
    ("single arm", True),
    ("single leg", True),
    ("tempo", False),
    ("paused", False),
)

BONES_BY_PATTERN = {
    "push_horizontal": BONE_PRESETS["upper_body"],
    "push_vertical": BONE_PRESETS["upper_body"],
    "pull_horizontal": BONE_PRESETS["upper_body"],
    "pull_vertical": BONE_PRESETS["upper_body"],
    "isolation": BONE_PRESETS["upper_body"],
    "squat": BONE_PRESETS["lower_body"],
    "hinge": BONE_PRESETS["lower_body"],
    "carry": BONE_PRESETS["full_body"],
    "core_stability": BONE_PRESETS["core_stack"],
    "core_dynamic": BONE_PRESETS["core_stack"],
    "rotation": BONE_PRESETS["core_stack"],
    "mobility": BONE_PRESETS["lower_body"],
    "plyometric": BONE_PRESETS["full_body"],
}


def slug(text: str) -> str:
    cleaned = ''.join(ch if ch.isalnum() else '_' for ch in text.strip().lower())
    return '_'.join(p for p in cleaned.split('_') if p)


def difficulty_for(index: int) -> str:
    return ("beginner", "intermediate", "advanced")[index % 3]


def tracking_for(pattern: str) -> str:
    if pattern in ("core_stability", "mobility"):
        return "hold"
    if pattern in ("core_dynamic", "carry"):
        return "time"
    return "reps"


def _family_entries(family: str) -> list[tuple[str, dict]]:
    primary = FAMILY_PRIMARY_MUSCLE[family]
    templates = TEMPLATES[family]
    out: list[tuple[str, dict]] = []
    seen: set[str] = set()

    # variation-outer ordering: every pattern surfaces in early pool cycles
    for var_index in range(len(VARIATIONS)):
        var_text, unilateral = VARIATIONS[var_index]
        for base_name, pattern, secondary, allowed_eq in templates:
            equipments = sorted(EQUIPMENT_TYPES) if allowed_eq is None else sorted(allowed_eq)
            for eq in equipments:
                label = EQUIPMENT_LABEL[eq]
                parts = [p for p in (var_text, base_name, label.lower()) if p]
                ex_id = slug(" ".join(parts))
                display = " ".join(p.capitalize() for p in parts)
                key = ex_id if ex_id not in seen else f"{family}_{ex_id}"
                if key in seen:
                    continue
                seen.add(key)
                out.append((key, {
                    "id": key,
                    "name": display,
                    "equipment": eq,
                    "pattern": pattern,
                    "primary_muscles": [primary],
                    "secondary_muscles": list(secondary),
                    "tracking_mode": tracking_for(pattern),
                    "difficulty": difficulty_for(var_index),
                    "unilateral": unilateral,
                    "animation_action": f"EX_{key}",
                    "phases": list(PHASES),
                    "bones_required": list(BONES_BY_PATTERN[pattern]),
                }))
    return out


def generate(min_entries: int) -> dict:
    # Round-robin over families: sequential iteration would drop later families
    # (core/carry/mobility) entirely once min_entries is reached.
    pools = {family: _family_entries(family) for family in MOVEMENT_FAMILIES}
    exercises: dict[str, dict] = {}
    while len(exercises) < min_entries and any(pools.values()):
        for family in MOVEMENT_FAMILIES:
            if not pools[family]:
                continue
            key, entry = pools[family].pop(0)
            if key in exercises:
                continue
            exercises[key] = entry
            if len(exercises) >= min_entries:
                break

    trimmed = dict(sorted(exercises.items())[: max(min_entries, 0)])
    return {
        "schema": 1,
        "character": {
            "base": "athlete_master.blend",
            "rig": "repforge_v1",
            "note": "procedurally generated by build_rig.py (see README)",
        },
        "exercises": trimmed,
    }


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--out", default="tools/exercise-assets/exercise_catalog.yaml")
    ap.add_argument("--min", type=int, default=250)
    args = ap.parse_args()

    catalog = generate(args.min)
    out = pathlib.Path(args.out)
    out.write_text(
        "# RepForge exercise catalog - GENERATED by gen_catalog.py; do not hand-edit.\n"
        f"# {len(catalog['exercises'])} entries across "
        f"{len({e['pattern'] for e in catalog['exercises'].values()})} movement patterns.\n"
        + yaml.safe_dump(catalog, sort_keys=False, width=100),
        encoding="utf-8",
    )
    print(f"wrote {out} with {len(catalog['exercises'])} exercises")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
