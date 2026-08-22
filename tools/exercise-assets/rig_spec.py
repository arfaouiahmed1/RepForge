"""Shared spec constants for the RepForge exercise asset pipeline.

This module is the single source of truth for:
  * the athlete skeleton bone list (RIG_BONES) and convenience presets,
  * animation phase names,
  * catalog vocabularies (equipment, patterns, muscles, tracking, difficulty).

It is imported by host-side tooling (build_assets.py, tests) AND by the
in-Blender scripts (build_rig.py, export_glb.py), which add this directory to
sys.path before importing. Keep it stdlib-only and Blender-free.
"""
from __future__ import annotations

# ---------------------------------------------------------------------------
# Skeleton
# ---------------------------------------------------------------------------

#: Canonical bone names of the single athlete skeleton in athlete_master.blend.
#: Order is hierarchy order (parents before children). build_rig.py creates
#: exactly these bones; catalog entries may only require a subset of them.
RIG_BONES: tuple[str, ...] = (
    "root",
    "pelvis",
    "spine",
    "chest",
    "neck",
    "head",
    # arms
    "shoulder.L", "upper_arm.L", "forearm.L", "hand.L",
    "shoulder.R", "upper_arm.R", "forearm.R", "hand.R",
    # legs
    "hip.L", "thigh.L", "shin.L", "foot.L",
    "hip.R", "thigh.R", "shin.R", "foot.R",
)

_R = set(RIG_BONES)


def _preset(*names: str) -> tuple[str, ...]:
    """Build an ordered preset from short names, expanding .L/.R pairs."""
    out: list[str] = []
    for n in names:
        if n.endswith(".L"):
            base = n[:-2]
            out.extend((f"{base}.L", f"{base}.R"))
        else:
            out.append(n)
    missing = [b for b in out if b not in _R]
    assert not missing, f"preset references unknown bones: {missing}"
    return tuple(out)


#: Bone-requirement presets used by the catalog generator. Catalog files store
#: explicit per-entry lists; these are only authoring helpers.
BONE_PRESETS: dict[str, tuple[str, ...]] = {
    "full_body": RIG_BONES,
    "upper_body": _preset(
        "root", "pelvis", "spine", "chest", "neck", "head",
        "shoulder.L", "upper_arm.L", "forearm.L", "hand.L",
    ),
    "lower_body": _preset(
        "root", "pelvis", "spine", "chest",
        "hip.L", "thigh.L", "shin.L", "foot.L",
    ),
    "core_stack": _preset("root", "pelvis", "spine", "chest", "neck", "head"),
}

# ---------------------------------------------------------------------------
# Animation phases
# ---------------------------------------------------------------------------

#: The five canonical animation phases every exercise must expose.
#: In Blender each phase becomes one Action named ``EX_<id>_<phase>``.
PHASES: tuple[str, ...] = ("setup", "eccentric", "bottom", "concentric", "lockout")

PHASE_ANCHOR_DEPTH = {  # fraction of full range-of-motion at each phase
    "setup": 0.0,
    "eccentric": 0.6,
    "bottom": 1.0,
    "concentric": 0.3,
    "lockout": 0.05,
}

# ---------------------------------------------------------------------------
# Catalog vocabularies
# ---------------------------------------------------------------------------

EQUIPMENT_TYPES: frozenset[str] = frozenset({
    "barbell", "dumbbell", "cable", "machine", "bodyweight", "band", "kettlebell",
})

PATTERNS: frozenset[str] = frozenset({
    "squat", "hinge",
    "push_horizontal", "push_vertical",
    "pull_horizontal", "pull_vertical",
    "isolation", "carry", "core_stability", "core_dynamic",
    "rotation", "mobility", "plyometric",
})

TRACKING_MODES: frozenset[str] = frozenset({"reps", "time", "hold"})

DIFFICULTIES: frozenset[str] = frozenset({"beginner", "intermediate", "advanced"})

MUSCLES: frozenset[str] = frozenset({
    # upper body
    "chest", "lats", "traps", "rhomboids", "lower_back",
    "front_delts", "side_delts", "rear_delts", "rotator_cuff",
    "biceps", "triceps", "forearms", "serratus", "neck",
    # lower body
    "quads", "hamstrings", "glutes", "adductors", "abductors",
    "calves", "tibialis", "hip_flexors",
    # trunk
    "abs", "obliques", "core",
})

MOVEMENT_FAMILIES: tuple[str, ...] = (
    "chest", "back", "shoulders", "biceps", "triceps", "quads",
    "hamstrings", "glutes", "calves", "core", "carry", "mobility",
)

#: Primary muscle that identifies each movement family (used by tests and by
#: the generator's family bookkeeping).
FAMILY_PRIMARY_MUSCLE: dict[str, str] = {
    "chest": "chest",
    "back": "lats",
    "shoulders": "side_delts",
    "biceps": "biceps",
    "triceps": "triceps",
    "quads": "quads",
    "hamstrings": "hamstrings",
    "glutes": "glutes",
    "calves": "calves",
    "core": "abs",
    "carry": "forearms",
    "mobility": "hip_flexors",
}

# ---------------------------------------------------------------------------
# Rig / equipment kit file layout
# ---------------------------------------------------------------------------

RIG_FILE = "athlete_master.blend"
BUILD_RIG_SCRIPT = "build_rig.py"
EXPORT_GLB_SCRIPT = "export_glb.py"

#: Equipment kit primitives created inside athlete_master.blend (collection
#: "EQUIPMENT"). Hidden from render/export unless explicitly enabled.
EQUIPMENT_KIT: tuple[str, ...] = (
    "olympic_bar", "plates_2_5_5_10_20", "dumbbells_pair", "bench_flat",
    "rack", "cable_handles", "lat_pulldown", "leg_press", "kettlebell_16",
    "bands",
)

#: Which kit props a catalog `equipment` value implies (cosmetic scene dressing).
EQUIPMENT_PROPS: dict[str, tuple[str, ...]] = {
    "barbell": ("olympic_bar", "plates_2_5_5_10_20"),
    "dumbbell": ("dumbbells_pair",),
    "kettlebell": ("kettlebell_16",),
    "cable": ("cable_handles",),
    "machine": ("rack", "bench_flat"),
    "bodyweight": (),
    "band": ("bands",),
}

# ---------------------------------------------------------------------------
# Manifest schema (consumed downstream by todo 25 R2 upload)
# ---------------------------------------------------------------------------

MANIFEST_SCHEMA_VERSION = 1

#: Keys of one manifest entry, exactly as consumed by the upload step:
#: {exerciseId, glbPath, webpPath, sha256, sizeBytes, version}
MANIFEST_ENTRY_KEYS = (
    "exerciseId", "glbPath", "webpPath", "sha256", "sizeBytes", "version",
)

VERSION_HEX_LEN = 12  # version := sha256[:12]
