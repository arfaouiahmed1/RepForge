"""Build one exercise's phase animations, validate them, export GLB + WebP thumb.

Run via (rig already open):
    blender --background athlete_master.blend --python export_glb.py -- \
        --exercise-id bench_press --pattern push_horizontal \
        --glb build/assets/bench_press.glb --thumb build/assets/bench_press.webp \
        [--props olympic_bar,plates_2_5_5_10_20] [--fps 30]

For each canonical phase (setup/eccentric/bottom/concentric/lockout) this
script creates a real Action named ``EX_<id>_<phase>`` with F-curves on the
athlete rig, stacks the five actions on the NLA so they play back-to-back,
validates that all five exist and are non-empty, then exports glTF 2.0 binary
(GLB) and renders a 1024x1024 transparent WebP thumbnail.

Motion is procedurally generated from the movement pattern (deterministic,
seeded by exercise id). It is intentionally simple placeholder-quality motion:
refining per-exercise biomechanics is follow-up work (todo 26).

Prints a final line ``RESULT_JSON: {...}`` for the host pipeline to parse.
"""
from __future__ import annotations

import argparse
import json
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import bpy  # noqa: E402  (only importable inside Blender)

from rig_spec import PHASES  # noqa: E402

# ---------------------------------------------------------------------------
# Procedural motion library.
#
# Each pattern lists (bone_base, axis, degrees_at_full_range) triples applied
# symmetrically to .L/.R bones. `start_contracted` inverts the depth anchors so
# pull patterns begin at peak contraction instead of rest.
#
# Axis semantics on our rig (bones built pointing down/up along Z):
#   X = forward/backward swing or pitch (positive pitches backward)
#   Y = twist
#   Z = lateral raise / spread (.R gets the mirrored sign)
# ---------------------------------------------------------------------------

PATTERN_MOTION: dict[str, dict] = {
    "squat": {
        "joints": [("thigh", "X", -95), ("shin", "X", 78), ("foot", "X", -18),
                   ("pelvis", "X", 20)],
    },
    "hinge": {
        "joints": [("pelvis", "X", 62), ("spine", "X", 14), ("thigh", "X", -14)],
    },
    "push_horizontal": {
        "joints": [("upper_arm", "X", -92), ("forearm", "X", -85),
                   ("shoulder", "Z", 12)],
    },
    "push_vertical": {
        "joints": [("upper_arm", "X", -168), ("forearm", "X", -60)],
    },
    "pull_horizontal": {
        "start_contracted": True,
        "joints": [("upper_arm", "X", -78), ("forearm", "X", -80),
                   ("shoulder", "Z", 14)],
    },
    "pull_vertical": {
        "start_contracted": True,
        "joints": [("upper_arm", "X", -160), ("forearm", "X", -70)],
    },
    "isolation": {
        "joints": [("forearm", "X", -125), ("upper_arm", "X", -8)],
    },
    "carry": {
        "joints": [("shoulder", "Z", 6), ("pelvis", "Y", 4), ("spine", "Y", -3)],
    },
    "core_stability": {
        "joints": [("pelvis", "X", 10), ("chest", "X", -6)],
    },
    "core_dynamic": {
        "joints": [("spine", "X", 38), ("pelvis", "X", 12), ("neck", "X", 8)],
    },
    "rotation": {
        "joints": [("spine", "Y", 42), ("pelvis", "Y", 16), ("chest", "Y", 22)],
    },
    "mobility": {
        "joints": [("thigh", "X", -55), ("spine", "X", 22), ("upper_arm", "X", -120)],
    },
    "plyometric": {
        "joints": [("thigh", "X", -80), ("shin", "X", 65), ("foot", "X", -25),
                   ("upper_arm", "X", -60)],
    },
}

#: Frames at 30 fps: each phase becomes a ~1 s hold clip; NLA strips are laid
#: out sequentially so the full cycle plays setup -> lockout.
PHASE_FRAMES = {"setup": 1, "eccentric": 31, "bottom": 41,
                "concentric": 71, "lockout": 81}
CYCLE_END_FRAME = 91


def _pose_for_phase(pattern: str, phase: str) -> dict[str, tuple[float, float, float]]:
    """Deterministic euler (radians) per bone for one phase of one pattern."""
    spec = PATTERN_MOTION[pattern]
    anchors = PHASE_ANCHOR_DEPTH
    if spec.get("start_contracted"):
        d = 1.0 - anchors[phase]
    else:
        d = anchors[phase]

    pose: dict[str, tuple[float, float, float]] = {}
    for bone_base, axis, deg_full in spec["joints"]:
        angle = math.radians(deg_full) * d
        for side, sx in (("L", 1.0), ("R", -1.0)):
            rx = angle if axis == "X" else 0.0
            ry = angle if axis == "Y" else 0.0
            rz = (angle * sx) if axis == "Z" else 0.0
            pose[f"{bone_base}.{side}"] = (rx, ry, rz)
    return pose


def _apply_pose(rig: bpy.types.Object, pose: dict[str, tuple[float, float, float]],
                frame: int) -> None:
    bpy.context.scene.frame_set(frame)
    for bone_name, (rx, ry, rz) in pose.items():
        pbone = rig.pose.bones.get(bone_name)
        if pbone is None:
            continue
        pbone.rotation_euler = (rx, ry, rz)
        pbone.keyframe_insert(data_path="rotation_euler", frame=frame)


def build_actions(rig: bpy.types.Object, ex_id: str, pattern: str, fps: int) -> list[str]:
    """Create EX_<id>_<phase> actions + NLA strips. Returns action names."""
    anim = rig.animation_data_create()
    created: list[str] = []

    prev_strips = [s for s in anim.nla_tracks]
    for track in prev_strips:
        anim.nla_tracks.remove(track)

    for phase in PHASES:
        name = f"EX_{ex_id}_{phase}"
        action = bpy.data.actions.get(name)
        if action is None:
            action = bpy.data.actions.new(name)
        # wipe any stale fcurves from previous runs of this blend file
        while len(action.fcurves) > 0:
            action.fcurves.remove(action.fcurves[0])

        start = PHASE_FRAMES[phase]
        end = min(start + fps - 1, CYCLE_END_FRAME - 1)
        rig.animation_data.action = action
        pose = _pose_for_phase(pattern, phase)
        _apply_pose(rig, pose, start)
        _apply_pose(rig, pose, end)  # hold the pose across the clip

        track = anim.nla_tracks.new()
        track.name = name
        strip = track.strips.new(name=name, start=start, action=action)
        strip.action_frame_start = start
        strip.action_frame_end = end
        created.append(name)

    rig.animation_data.action = None  # NLA drives playback
    return created


def validate_phases(ex_id: str) -> dict:
    """Assert the five phase actions exist and carry real F-curves."""
    problems: list[str] = []
    found: dict[str, int] = {}
    for phase in PHASES:
        name = f"EX_{ex_id}_{phase}"
        action = bpy.data.actions.get(name)
        if action is None:
            problems.append(f"missing action {name}")
            continue
        n_curves = len(action.fcurves)
        n_keys = sum(len(fc.keyframe_points) for fc in action.fcurves)
        found[name] = n_keys
        if n_curves == 0 or n_keys == 0:
            problems.append(f"action {name} has no keyframes")
    return {"actions": found, "problems": problems}


def enable_props(prop_names: list[str]) -> None:
    coll = bpy.data.collections.get("EQUIPMENT")
    if coll is None:
        return
    wanted = set(prop_names)
    for obj in coll.objects:
        base = obj.name
        # kit sub-parts share prefixes with their logical unit names
        show = any(base == p or base.startswith(p + "_") or p.startswith(base) for p in wanted)
        obj.hide_render = not show


def export_glb(path: str) -> None:
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
    full_kwargs = dict(
        filepath=os.path.abspath(path),
        export_format="GLB",
        export_animations=True,
        export_apply=True,
        export_yup=True,
        export_anim_slide_to_zero=True,
    )
    try:
        bpy.ops.export_scene.gltf(**full_kwargs)
    except TypeError:
        # parameter set differs across Blender versions — fall back to minimal
        bpy.ops.export_scene.gltf(
            filepath=os.path.abspath(path),
            export_format="GLB",
            export_animations=True,
        )


def render_thumbnail(path: str) -> None:
    scene = bpy.context.scene
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
    scene.render.resolution_x = 1024
    scene.render.resolution_y = 1024
    scene.render.resolution_percentage = 100
    scene.render.film_transparent = True
    scene.render.image_settings.file_format = "WEBP"
    scene.render.image_settings.color_mode = "RGBA"
    scene.render.filepath = os.path.abspath(path)

    engine = None
    for candidate in ("BLENDER_EEVEE_NEXT", "BLENDER_EEVEE", "BLENDER_WORKBENCH"):
        try:
            scene.render.engine = candidate
            engine = candidate
            break
        except TypeError:
            continue
    try:
        scene.render.engine = engine or "BLENDER_WORKBENCH"
    except Exception:
        pass

    bpy.ops.render.render(write_still=True)


def main() -> int:
    argv = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--exercise-id", required=True)
    parser.add_argument("--pattern", required=True, choices=sorted(PATTERN_MOTION))
    parser.add_argument("--glb", required=True)
    parser.add_argument("--thumb", required=True)
    parser.add_argument("--props", default="", help="comma-separated equipment kit props to show")
    parser.add_argument("--fps", type=int, default=30)
    args = parser.parse_args(argv)

    rigs = [o for o in bpy.data.objects if o.type == "ARMATURE"]
    if not rigs:
        print("RESULT_JSON: " + json.dumps({"ok": False, "error": "no armature in scene"}))
        return 1
    rig = rigs[0]

    enable_props([p.strip() for p in args.props.split(",") if p.strip()])
    actions = build_actions(rig, args.exercise_id, args.pattern, args.fps)
    report = validate_phases(args.exercise_id)
    if report["problems"]:
        print("RESULT_JSON: " + json.dumps({
            "ok": False, "exerciseId": args.exercise_id,
            "problems": report["problems"],
        }))
        return 1

    export_glb(args.glb)
    render_thumbnail(args.thumb)

    ok_glb = os.path.isfile(args.glb) and os.path.getsize(args.glb) > 0
    ok_thumb = os.path.isfile(args.thumb) and os.path.getsize(args.thumb) > 0
    print("RESULT_JSON: " + json.dumps({
        "ok": bool(ok_glb and ok_thumb),
        "exerciseId": args.exercise_id,
        "pattern": args.pattern,
        "actions": sorted(report["actions"]),
        "keyframes": report["actions"],
        "glbBytes": os.path.getsize(args.glb) if ok_glb else 0,
        "thumbBytes": os.path.getsize(args.thumb) if ok_thumb else 0,
        "engine": str(bpy.context.scene.render.engine),
    }))
    return 0 if (ok_glb and ok_thumb) else 1


if __name__ == "__main__":
    raise SystemExit(main())
