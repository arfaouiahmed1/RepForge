"""Procedurally build athlete_master.blend inside Blender (headless).

Run via:
    blender --background --python build_rig.py -- --out /path/to/athlete_master.blend

Creates:
  * an armature "AthleteRig" with exactly the bones in rig_spec.RIG_BONES,
  * simple capsule/box body meshes parented to those bones (bone-relative),
  * an "EQUIPMENT" collection with the kit primitives from rig_spec.EQUIPMENT_KIT
    (hidden from render by default; export_glb.py enables props per exercise),
  * a thumbnail camera + three-point lighting rig.

No external assets are required, so CI can generate the rig without binaries.
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

from rig_spec import EQUIPMENT_KIT, RIG_BONES  # noqa: E402

# ---------------------------------------------------------------------------
# Bone layout: name -> (head, tail) in metres. Figure stands at origin facing
# -Y, ~1.8 m tall, T-pose arms hanging slightly out. Z is up in Blender; the
# glTF exporter converts to Y-up on export.
# ---------------------------------------------------------------------------


def _v(x: float, y: float, z: float) -> tuple[float, float, float]:
    return (x, y, z)


BONE_LAYOUT: dict[str, tuple[tuple[float, float, float], tuple[float, float, float]]] = {
    "root": (_v(0, 0, 0.00), _v(0, 0, 0.90)),
    "pelvis": (_v(0, 0, 0.95), _v(0, 0, 1.05)),
    "spine": (_v(0, 0, 1.05), _v(0, 0, 1.25)),
    "chest": (_v(0, 0, 1.25), _v(0, 0, 1.45)),
    "neck": (_v(0, 0, 1.45), _v(0, 0, 1.55)),
    "head": (_v(0, 0, 1.55), _v(0, 0, 1.78)),
}

for side, sx in (("L", 1.0), ("R", -1.0)):
    BONE_LAYOUT[f"shoulder.{side}"] = (_v(sx * 0.04, 0, 1.42), _v(sx * 0.18, 0, 1.42))
    BONE_LAYOUT[f"upper_arm.{side}"] = (_v(sx * 0.19, 0, 1.40), _v(sx * 0.21, 0, 1.12))
    BONE_LAYOUT[f"forearm.{side}"] = (_v(sx * 0.21, 0, 1.12), _v(sx * 0.22, 0, 0.86))
    BONE_LAYOUT[f"hand.{side}"] = (_v(sx * 0.22, 0, 0.86), _v(sx * 0.23, 0, 0.73))
    BONE_LAYOUT[f"hip.{side}"] = (_v(sx * 0.09, 0, 0.94), _v(sx * 0.10, 0, 0.82))
    BONE_LAYOUT[f"thigh.{side}"] = (_v(sx * 0.10, 0, 0.82), _v(sx * 0.11, 0, 0.48))
    BONE_LAYOUT[f"shin.{side}"] = (_v(sx * 0.11, 0, 0.48), _v(sx * 0.11, 0, 0.09))
    BONE_LAYOUT[f"foot.{side}"] = (_v(sx * 0.11, 0, 0.07), _v(sx * 0.11, -0.16, 0.03))

#: bone -> parent bone (None = parented to nothing; root is the top).
BONE_PARENTS: dict[str, str | None] = {
    "root": None,
    "pelvis": "root",
    "spine": "pelvis",
    "chest": "spine",
    "neck": "chest",
    "head": "neck",
}
for side in ("L", "R"):
    BONE_PARENTS[f"shoulder.{side}"] = "chest"
    BONE_PARENTS[f"upper_arm.{side}"] = f"shoulder.{side}"
    BONE_PARENTS[f"forearm.{side}"] = f"upper_arm.{side}"
    BONE_PARENTS[f"hand.{side}"] = f"forearm.{side}"
    BONE_PARENTS[f"hip.{side}"] = "pelvis"
    BONE_PARENTS[f"thigh.{side}"] = f"hip.{side}"
    BONE_PARENTS[f"shin.{side}"] = f"thigh.{side}"
    BONE_PARENTS[f"foot.{side}"] = f"shin.{side}"

assert set(BONE_LAYOUT) == set(RIG_BONES), "BONE_LAYOUT must match RIG_BONES exactly"
assert set(BONE_PARENTS) == set(RIG_BONES), "BONE_PARENTS must match RIG_BONES exactly"


# ---------------------------------------------------------------------------
# Scene helpers
# ---------------------------------------------------------------------------


def clear_scene() -> None:
    bpy.ops.wm.read_factory_settings(use_empty=True)


def make_material(name: str, color: tuple[float, float, float, float]) -> bpy.types.Material:
    mat = bpy.data.materials.new(name)
    mat.use_nodes = True
    bsdf = mat.node_tree.nodes.get("Principled BSDF")
    if bsdf is not None:
        bsdf.inputs["Base Color"].default_value = color
        bsdf.inputs["Roughness"].default_value = 0.6
    return mat


def build_armature() -> bpy.types.Object:
    arm_data = bpy.data.armatures.new("AthleteRigData")
    arm_obj = bpy.data.objects.new("AthleteRig", arm_data)
    bpy.context.scene.collection.objects.link(arm_obj)
    bpy.context.view_layer.objects.active = arm_obj

    bpy.ops.object.mode_set(mode="EDIT")
    edit_bones = {}
    for name in RIG_BONES:  # hierarchy order guarantees parents exist first
        eb = arm_data.edit_bones.new(name)
        head, tail = BONE_LAYOUT[name]
        eb.head, eb.tail = head, tail
        eb.roll = 0.0
        edit_bones[name] = eb
    for name, parent in BONE_PARENTS.items():
        if parent is not None:
            edit_bones[name].parent = edit_bones[parent]
    # root must not deform the mesh; it is a motion control bone.
    arm_data.bones["root"].use_deform = False
    bpy.ops.object.mode_set(mode="OBJECT")
    return arm_obj


def _box(name: str, size: tuple[float, float, float], loc: tuple[float, float, float],
         mat: bpy.types.Material, parent: bpy.types.Object, bone: str | None) -> bpy.types.Object:
    bpy.ops.mesh.primitive_cube_add(size=1.0, location=loc)
    obj = bpy.context.active_object
    obj.name = name
    obj.scale = size
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    obj.data.materials.append(mat)
    if bone is not None:
        obj.parent = parent
        obj.parent_type = "BONE"
        obj.parent_bone = bone
    return obj


def build_body(arm_obj: bpy.types.Object) -> None:
    skin = make_material("AthleteMat", (0.62, 0.66, 0.72, 1.0))
    dark = make_material("AthleteDark", (0.16, 0.17, 0.20, 1.0))

    parts = [
        ("torso_lower", (0.30, 0.19, 0.22), (0, 0, 1.02), "pelvis"),
        ("torso_upper", (0.36, 0.21, 0.30), (0, 0, 1.32), "chest"),
        ("head_mesh", (0.20, 0.23, 0.24), (0, 0, 1.64), "head"),
        ("neck_mesh", (0.10, 0.10, 0.10), (0, 0, 1.50), "neck"),
    ]
    for side in ("L", "R"):
        sx = 1.0 if side == "L" else -1.0
        parts += [
            (f"shoulder_{side}", (0.14, 0.14, 0.14), (sx * 0.20, 0, 1.40), f"shoulder.{side}"),
            (f"upper_arm_{side}", (0.09, 0.09, 0.28), (sx * 0.20, 0, 1.26), f"upper_arm.{side}"),
            (f"forearm_{side}", (0.08, 0.08, 0.26), (sx * 0.215, 0, 0.99), f"forearm.{side}"),
            (f"hand_{side}", (0.06, 0.09, 0.13), (sx * 0.225, 0, 0.795), f"hand.{side}"),
            (f"thigh_{side}", (0.13, 0.13, 0.34), (sx * 0.105, 0, 0.65), f"thigh.{side}"),
            (f"shin_{side}", (0.10, 0.10, 0.38), (sx * 0.11, 0, 0.285), f"shin.{side}"),
            (f"foot_{side}", (0.10, 0.24, 0.08), (sx * 0.11, -0.07, 0.05), f"foot.{side}"),
        ]
    for name, size, loc, bone in parts:
        _box(name, size, loc, dark if name.startswith("torso") else skin, arm_obj, bone)


# ---------------------------------------------------------------------------
# Equipment kit primitives (collection "EQUIPMENT", hidden from render)
# ---------------------------------------------------------------------------


def _cyl(name: str, radius: float, depth: float, loc, rot=(0.0, 0.0, 0.0),
         mat=None, coll=None) -> bpy.types.Object:
    bpy.ops.mesh.primitive_cylinder_add(radius=radius, depth=depth, location=loc)
    obj = bpy.context.active_object
    obj.name = name
    obj.rotation_euler = rot
    if mat:
        obj.data.materials.append(mat)
    if coll is not None:
        for c in list(obj.users_collection):
            c.objects.unlink(obj)
        coll.objects.link(obj)
    return obj


def build_equipment_kit() -> None:
    coll = bpy.data.collections.new("EQUIPMENT")
    bpy.context.scene.collection.children.link(coll)
    steel = make_material("KitSteel", (0.75, 0.76, 0.78, 1.0))
    rubber = make_material("KitRubber", (0.12, 0.12, 0.14, 1.0))
    wood = make_material("KitWood", (0.45, 0.30, 0.16, 1.0))

    # olympic bar across X at shoulder height
    _cyl("olympic_bar", 0.014, 2.2, (0, 0, 1.42), rot=(0, math.pi / 2, 0), mat=steel, coll=coll)

    # plates: pairs of increasing radius along the bar sleeves
    radii = {"plate_2_5kg": 0.08, "plate_5kg": 0.11, "plate_10kg": 0.155,
             "plate_15kg": 0.19, "plate_20kg": 0.225}
    x = 0.95
    for pname, r in radii.items():
        for side, sx in (("L", 1.0), ("R", -1.0)):
            _cyl(f"{pname}_{side}", r, 0.03, (sx * x, 0, 1.42),
                 rot=(0, math.pi / 2, 0), mat=rubber, coll=coll)
        x -= 0.05

    # dumbbell pair on the floor
    for side, sx in (("L", 1.0), ("R", -1.0)):
        cx = sx * 0.55
        _cyl(f"dumbbell_handle_{side}", 0.017, 0.34, (cx, 0, 0.12),
             rot=(0, math.pi / 2, 0), mat=steel, coll=coll)
        for end, ex in (("a", -0.14), ("b", 0.14)):
            bpy.ops.mesh.primitive_uv_sphere_add(radius=0.075, location=(cx + ex, 0, 0.12))
            s = bpy.context.active_object
            s.name = f"dumbbell_head_{side}_{end}"
            s.data.materials.append(rubber)
            for c in list(s.users_collection):
                c.objects.unlink(s)
            coll.objects.link(s)

    # flat bench
    _box("bench_flat", (0.33, 1.25, 0.09), (0, -0.15, 0.46), wood, None, None)
    bench = bpy.data.objects["bench_flat"]
    for c in list(bench.users_collection):
        c.objects.unlink(bench)
    coll.objects.link(bench)
    for leg_i, lx in ((0, -0.45), (1, 0.45)):
        _cyl(f"bench_leg_{leg_i}", 0.025, 0.42, (lx, -0.15, 0.21), mat=steel, coll=coll)

    # squat rack: two uprights + crossbar + J-hooks
    for side, sx in (("L", 1.0), ("R", -1.0)):
        _cyl(f"rack_upright_{side}", 0.04, 2.3, (sx * 0.65, -0.35, 1.15), mat=steel, coll=coll)
        _cyl(f"rack_hook_{side}", 0.02, 0.18, (sx * 0.58, -0.35, 1.42),
             rot=(math.pi / 2, 0, 0), mat=rubber, coll=coll)
    _cyl("rack_crossbar", 0.025, 1.3, (0, -0.35, 2.28), rot=(0, math.pi / 2, 0), mat=steel, coll=coll)

    # cable handles pair
    for side, sx in (("L", 1.0), ("R", -1.0)):
        _cyl(f"cable_handle_{side}", 0.016, 0.16, (sx * 0.35, 0, 1.0),
             rot=(math.pi / 2, 0, 0), mat=rubber, coll=coll)

    # lat pulldown frame (simple)
    for side, sx in (("L", 1.0), ("R", -1.0)):
        _cyl(f"pulldown_frame_{side}", 0.04, 2.4, (sx * 0.55, 0.45, 1.2), mat=steel, coll=coll)
    _cyl("pulldown_bar", 0.014, 1.1, (0, 0.45, 2.05), rot=(0, math.pi / 2, 0), mat=steel, coll=coll)
    _box("pulldown_seat", (0.30, 0.30, 0.08), (0, 0.45, 0.45), rubber, None, None)
    seat = bpy.data.objects["pulldown_seat"]
    for c in list(seat.users_collection):
        c.objects.unlink(seat)
    coll.objects.link(seat)

    # leg press sled (angled platform + seat)
    sled = _box("leg_press_sled", (0.9, 0.12, 0.9), (0, -0.8, 0.7), steel, None, None)
    sled.rotation_euler = (math.radians(-35), 0, 0)
    for c in list(sled.users_collection):
        c.objects.unlink(sled)
    coll.objects.link(sled)
    _box("leg_press_seat", (0.4, 0.4, 0.1), (0, -0.15, 0.35), rubber, None, None)
    lp_seat = bpy.data.objects["leg_press_seat"]
    for c in list(lp_seat.users_collection):
        c.objects.unlink(lp_seat)
    coll.objects.link(lp_seat)

    # kettlebell 16 kg
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.115, location=(0.85, 0.35, 0.115))
    kb = bpy.context.active_object
    kb.name = "kettlebell_16"
    kb.data.materials.append(rubber)
    for c in list(kb.users_collection):
        c.objects.unlink(kb)
    coll.objects.link(kb)
    bpy.ops.mesh.primitive_torus_add(major_radius=0.055, minor_radius=0.014,
                                     location=(0.85, 0.35, 0.245))
    handle = bpy.context.active_object
    handle.name = "kettlebell_handle"
    handle.rotation_euler = (math.radians(90), 0, 0)
    handle.data.materials.append(steel)
    for c in list(handle.users_collection):
        c.objects.unlink(handle)
    coll.objects.link(handle)

    # resistance band loop
    bpy.ops.mesh.primitive_torus_add(major_radius=0.45, minor_radius=0.008,
                                     location=(-0.85, 0.35, 0.01))
    band = bpy.context.active_object
    band.name = "bands"
    band.scale = (1.0, 1.0, 0.35)
    band.data.materials.append(make_material("KitBand", (0.85, 0.25, 0.20, 1.0)))
    for c in list(band.users_collection):
        c.objects.unlink(band)
    coll.objects.link(band)

    # hide the whole kit from render/export until a build enables specific props
    for obj in coll.objects:
        obj.hide_render = True
        obj.hide_viewport = False


# ---------------------------------------------------------------------------
# Camera + lights for thumbnails
# ---------------------------------------------------------------------------


def build_stage() -> None:
    cam_data = bpy.data.cameras.new("ThumbCam")
    cam_data.lens = 50.0
    cam = bpy.data.objects.new("ThumbCam", cam_data)
    cam.location = (2.6, -3.2, 1.6)
    bpy.context.scene.collection.objects.link(cam)

    target = bpy.data.objects.new("ThumbTarget", None)  # empty
    target.empty_display_size = 0.1
    target.location = (0, 0, 0.95)
    bpy.context.scene.collection.objects.link(target)

    con = cam.constraints.new("TRACK_TO")
    con.target = target

    key = bpy.data.lights.new("KeyLight", type="AREA")
    key.energy = 800.0
    key.size = 2.0
    key_obj = bpy.data.objects.new("KeyLight", key)
    key_obj.location = (2.5, -2.0, 2.5)
    bpy.context.scene.collection.objects.link(key_obj)

    rim = bpy.data.lights.new("RimLight", type="AREA")
    rim.energy = 500.0
    rim.size = 1.5
    rim_obj = bpy.data.objects.new("RimLight", rim)
    rim_obj.location = (-2.5, -1.5, 2.0)
    bpy.context.scene.collection.objects.link(rim_obj)

    fill = bpy.data.lights.new("FillLight", type="AREA")
    fill.energy = 300.0
    fill.size = 1.5
    fill_obj = bpy.data.objects.new("FillLight", fill)
    fill_obj.location = (-1.5, -3.0, 1.0)
    bpy.context.scene.collection.objects.link(fill_obj)

    scene = bpy.context.scene
    scene.camera = cam
    scene.render.resolution_x = 1024
    scene.render.resolution_y = 1024
    scene.render.film_transparent = True


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------


def main() -> int:
    argv = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", required=True, help="path to write athlete_master.blend")
    args = parser.parse_args(argv)

    clear_scene()
    arm = build_armature()
    build_body(arm)
    build_equipment_kit()
    build_stage()

    created = sorted(b.name for b in bpy.data.armatures[0].bones)
    missing = [b for b in RIG_BONES if b not in created]
    extra = [b for b in created if b not in RIG_BONES]
    if missing or extra:
        print(f"RESULT_JSON: {json.dumps({'ok': False, 'missing': missing, 'extra': extra})}")
        return 1

    out = os.path.abspath(args.out)
    os.makedirs(os.path.dirname(out), exist_ok=True)
    bpy.ops.wm.save_as_mainfile(filepath=out, compress=True)

    print("RESULT_JSON: " + json.dumps({
        "ok": True,
        "rig": out,
        "bones": created,
        "equipment": sorted(o.name for o in bpy.data.collections["EQUIPMENT"].objects),
    }))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
