package com.repforge.core.database.seed

import com.repforge.core.database.entity.ExerciseEntity
import com.repforge.core.database.entity.RoutineEntity
import com.repforge.core.database.entity.RoutineExerciseEntity

object ExerciseSeed {
    // 60 exercises — bench, shoulder press, etc. Each maps to 3D model + muscle heat for 3D view
    val exercises = listOf(
        // CHEST 10
        ex("bench_bb", "Barbell Bench Press", "CHEST", "SHOULDERS,TRICEPS", "BARBELL", "bench_press.glb", "pecs 0.9, triceps 0.6, front_delts 0.5"),
        ex("bench_db", "Dumbbell Bench Press", "CHEST", "SHOULDERS,TRICEPS", "DUMBBELL", "db_bench.glb"),
        ex("incline_bb", "Incline Barbell Press", "CHEST", "SHOULDERS,TRICEPS", "BARBELL", "incline_bench.glb"),
        ex("incline_db", "Incline Dumbbell Press", "CHEST", "SHOULDERS", "DUMBBELL", "incline_db.glb"),
        ex("decline_bb", "Decline Bench Press", "CHEST", "TRICEPS", "BARBELL", "decline.glb"),
        ex("chest_fly_db", "Dumbbell Fly", "CHEST", "", "DUMBBELL", "fly.glb"),
        ex("cable_fly", "Cable Fly", "CHEST", "", "CABLE", "cable_fly.glb"),
        ex("pushup", "Push-Up", "CHEST", "CORE,TRICEPS", "BODYWEIGHT", "pushup.glb"),
        ex("machine_chest", "Machine Chest Press", "CHEST", "TRICEPS", "MACHINE", "chest_machine.glb"),
        ex("pec_deck", "Pec Deck", "CHEST", "", "MACHINE", "pec_deck.glb"),

        // BACK 10
        ex("deadlift", "Deadlift", "BACK", "LEGS,GLUTES", "BARBELL", "deadlift.glb", "erectors 0.9, lats 0.6, glutes 0.8"),
        ex("row_bb", "Barbell Row", "BACK", "BICEPS", "BARBELL", "row_bb.glb"),
        ex("row_db", "One-Arm Dumbbell Row", "BACK", "BICEPS", "DUMBBELL", "row_db.glb"),
        ex("lat_pulldown", "Lat Pulldown", "LATS", "BICEPS", "CABLE", "lat_pulldown.glb"),
        ex("pullup", "Pull-Up", "LATS", "BICEPS", "BODYWEIGHT", "pullup.glb"),
        ex("chinup", "Chin-Up", "LATS", "BICEPS", "BODYWEIGHT", "chinup.glb"),
        ex("cable_row", "Seated Cable Row", "BACK", "BICEPS", "CABLE", "cable_row.glb"),
        ex("tbar_row", "T-Bar Row", "BACK", "", "MACHINE", "tbar.glb"),
        ex("straight_pulldown", "Straight Arm Pulldown", "LATS", "", "CABLE", "pulldown.glb"),
        ex("back_ext", "Back Extension", "BACK", "GLUTES", "BODYWEIGHT", "back_ext.glb"),

        // LEGS 12
        ex("squat_bb", "Barbell Back Squat", "LEGS", "GLUTES,CORE", "BARBELL", "squat.glb", "quads 0.9, glutes 0.8, hamstrings 0.5"),
        ex("front_squat", "Front Squat", "QUADS", "CORE,GLUTES", "BARBELL", "front_squat.glb"),
        ex("bulgarian", "Bulgarian Split Squat", "QUADS", "GLUTES", "DUMBBELL", "bulgarian.glb"),
        ex("leg_press", "Leg Press", "LEGS", "GLUTES", "MACHINE", "leg_press.glb"),
        ex("rdl", "Romanian Deadlift", "HAMSTRINGS", "GLUTES,BACK", "BARBELL", "rdl.glb"),
        ex("leg_curl", "Lying Leg Curl", "HAMSTRINGS", "", "MACHINE", "leg_curl.glb"),
        ex("leg_ext", "Leg Extension", "QUADS", "", "MACHINE", "leg_ext.glb"),
        ex("calf_raise", "Standing Calf Raise", "CALVES", "", "MACHINE", "calf.glb"),
        ex("hip_thrust", "Hip Thrust", "GLUTES", "HAMSTRINGS", "BARBELL", "hip_thrust.glb"),
        ex("lunge", "Walking Lunge", "LEGS", "GLUTES", "DUMBBELL", "lunge.glb"),
        ex("hack_squat", "Hack Squat", "QUADS", "GLUTES", "MACHINE", "hack.glb"),
        ex("goblet", "Goblet Squat", "QUADS", "GLUTES,CORE", "KETTLEBELL", "goblet.glb"),

        // SHOULDERS 8
        ex("ohp_bb", "Overhead Press", "SHOULDERS", "TRICEPS", "BARBELL", "ohp.glb", "front_delts 0.9, triceps 0.6"),
        ex("ohp_db", "Dumbbell Shoulder Press", "SHOULDERS", "TRICEPS", "DUMBBELL", "ohp_db.glb"),
        ex("lateral", "Lateral Raise", "SHOULDERS", "", "DUMBBELL", "lateral.glb"),
        ex("rear_fly", "Rear Delt Fly", "SHOULDERS", "", "DUMBBELL", "rear_fly.glb"),
        ex("face_pull", "Face Pull", "SHOULDERS", "TRAPS", "CABLE", "face_pull.glb"),
        ex("upright_row", "Upright Row", "TRAPS", "SHOULDERS", "BARBELL", "upright.glb"),
        ex("arnold", "Arnold Press", "SHOULDERS", "", "DUMBBELL", "arnold.glb"),
        ex("cable_lateral", "Cable Lateral Raise", "SHOULDERS", "", "CABLE", "cable_lateral.glb"),

        // ARMS 10
        ex("curl_bb", "Barbell Curl", "BICEPS", "FOREARMS", "BARBELL", "curl_bb.glb", "biceps 0.9"),
        ex("curl_db", "Dumbbell Curl", "BICEPS", "", "DUMBBELL", "curl_db.glb"),
        ex("hammer", "Hammer Curl", "BICEPS", "FOREARMS", "DUMBBELL", "hammer.glb"),
        ex("preacher", "Preacher Curl", "BICEPS", "", "MACHINE", "preacher.glb"),
        ex("incline_curl", "Incline Dumbbell Curl", "BICEPS", "", "DUMBBELL", "incline_curl.glb"),
        ex("pushdown", "Triceps Pushdown", "TRICEPS", "", "CABLE", "pushdown.glb", "triceps 0.9"),
        ex("skull", "Skullcrusher", "TRICEPS", "", "BARBELL", "skull.glb"),
        ex("dips", "Dips", "TRICEPS", "CHEST,SHOULDERS", "BODYWEIGHT", "dips.glb"),
        ex("close_bench", "Close-Grip Bench Press", "TRICEPS", "CHEST", "BARBELL", "close_bench.glb"),
        ex("cable_curl", "Cable Curl", "BICEPS", "", "CABLE", "cable_curl.glb"),

        // CORE + OTHER 10
        ex("plank", "Plank", "CORE", "", "BODYWEIGHT", "plank.glb"),
        ex("leg_raise", "Hanging Leg Raise", "CORE", "", "BODYWEIGHT", "leg_raise.glb"),
        ex("russian", "Russian Twist", "CORE", "", "BODYWEIGHT", "russian.glb"),
        ex("woodchopper", "Cable Woodchopper", "CORE", "", "CABLE", "woodchopper.glb"),
        ex("ab_wheel", "Ab Wheel", "CORE", "", "BODYWEIGHT", "ab_wheel.glb"),
        ex("crunch", "Crunch", "CORE", "", "BODYWEIGHT", "crunch.glb"),
        ex("reverse_fly", "Reverse Fly", "SHOULDERS", "BACK", "MACHINE", "reverse_fly.glb"),
        ex("shrug", "Barbell Shrug", "TRAPS", "", "BARBELL", "shrug.glb"),
        ex("farmers", "Farmer Carry", "FULL_BODY", "FOREARMS,CORE", "DUMBBELL", "farmers.glb"),
        ex("burpee", "Burpee", "FULL_BODY", "", "BODYWEIGHT", "burpee.glb"),
        ex("front_squat_bb", "Front Squat", "QUADS", "CORE", "BARBELL", "front_squat.glb"),
        ex("split_squat_db", "Dumbbell Split Squat", "QUADS", "GLUTES", "DUMBBELL", "split_squat.glb"),
        ex("plate_loaded_hack_squat", "Plate-Loaded Hack Squat", "QUADS", "GLUTES", "MACHINE", "hack_squat.glb"),
        ex("smith_row", "Smith Machine Row", "BACK", "BICEPS", "SMITH", "smith_row.glb"),
        ex("ez_curl", "EZ-Bar Curl", "BICEPS", "FOREARMS", "EZ_BAR", "ez_curl.glb"),
        ex("ez_preacher_curl", "EZ Preacher Curl", "BICEPS", "", "EZ_BAR", "ez_preacher.glb"),
        ex("kb_swing", "Kettlebell Swing", "HAMSTRINGS", "GLUTES,CORE", "KETTLEBELL", "kb_swing.glb"),
        ex("goblet_squat_kb", "Goblet Squat", "QUADS", "GLUTES,CORE", "KETTLEBELL", "goblet_squat.glb"),
        ex("band_row", "Band Row", "BACK", "BICEPS", "BAND", "band_row.glb"),
        ex("band_ohp", "Band Overhead Press", "SHOULDERS", "TRICEPS", "BAND", "band_ohp.glb"),
        ex("band_curl", "Band Curl", "BICEPS", "", "BAND", "band_curl.glb"),
        ex("band_pushdown", "Band Pushdown", "TRICEPS", "", "BAND", "band_pushdown.glb"),
        ex("cable_fly_low", "Low-to-High Cable Fly", "CHEST", "FRONT_DELTS", "CABLE", "cable_fly_low.glb"),
        ex("cable_kickback_glute", "Cable Glute Kickback", "GLUTES", "HAMSTRINGS", "CABLE", "cable_kickback.glb"),
        ex("machine_chest_press", "Machine Chest Press", "CHEST", "TRICEPS", "MACHINE", "machine_chest_press.glb"),
        ex("smith_bench", "Smith Machine Bench Press", "CHEST", "TRICEPS", "SMITH", "smith_bench.glb"),
        ex("behind_shrug", "Behind-the-Back Shrug", "TRAPS", "", "BARBELL", "behind_shrug.glb"),
        ex("db_fly", "Dumbbell Fly", "CHEST", "FRONT_DELTS", "DUMBBELL", "db_fly.glb"),
        ex("arnold_db", "Arnold Press", "SHOULDERS", "TRICEPS", "DUMBBELL", "arnold.glb"),
        ex("upright_row_bb", "Upright Row", "TRAPS", "SIDE_DELTS", "BARBELL", "upright_row.glb"),
        ex("neutral_chinup", "Neutral-Grip Chin-Up", "BICEPS", "LATS", "BODYWEIGHT", "chinup.glb"),
        ex("pike_pushup", "Pike Push-Up", "SHOULDERS", "TRICEPS", "BODYWEIGHT", "pike_pushup.glb"),
        ex("hyperextension", "Back Extension", "LOWER_BACK", "GLUTES", "BODYWEIGHT", "hyperextension.glb"),
        ex("walking_lunge_db", "Walking Lunge", "QUADS", "GLUTES", "DUMBBELL", "walking_lunge.glb"),
    )

    // ── 60 MORE — fill gaps: forearms, neck, mobility, carries, unilateral, band ──
    val extended: List<ExerciseEntity> = listOf(
        // CHEST 6 more
        rich("diamond_pushup", "Diamond Push-Up", "CHEST", "TRICEPS", "BODYWEIGHT", "HOME", "ISOLATION", "REPS_ONLY", "BEGINNER", false, "Close-hand pushup biasing triceps", listOf("Hands form diamond under chest", "Elbows tuck 30°", "Lockout without flaring"), "diamond.glb"),
        rich("wide_pushup", "Wide Push-Up", "CHEST", "", "BODYWEIGHT", "HOME", "ISOLATION", "REPS_ONLY", "BEGINNER", false, "Chest stretch emphasis", listOf("Hands 1.5x shoulder width", "Chest to 2cm off floor", "Scapula protract at top"), "wide_pushup.glb"),
        rich("svend_press", "Svend Press", "CHEST", "SHOULDERS", "DUMBBELL", "GYM", "ISOLATION", "WEIGHT_REPS", "INTERMEDIATE", false, "Isometric chest squeeze", listOf("Press plates together", "Extend without losing squeeze", "Slow eccentric 3s"), "svend.glb"),
        rich("cable_crossover", "Cable Crossover", "CHEST", "", "CABLE", "GYM", "ISOLATION", "WEIGHT_REPS", "INTERMEDIATE", false, "High-to-low fly", listOf("Slight forward lean", "Arms slight bend fixed", "Squeeze 1s at midline"), "crossover.glb"),
        rich("incline_machine", "Incline Machine Press", "CHEST", "SHOULDERS", "MACHINE", "GYM", "HORIZONTAL_PUSH", "WEIGHT_REPS", "BEGINNER", false, "Guided incline press", listOf("Seat: handles at upper chest", "Back tight", "Do not lock elbows hard"), "incline_machine.glb"),
        rich("chest_dips", "Chest Dips", "CHEST", "TRICEPS,SHOULDERS", "BODYWEIGHT", "GYM", "HORIZONTAL_PUSH", "REPS_ONLY", "INTERMEDIATE", false, "Lean forward chest bias dip", listOf("Lean 20° forward", "Elbows 45°", "Descend to 90°"), "chest_dips.glb"),
        // BACK 6 more
        rich("rack_pull", "Rack Pull", "BACK", "TRAPS,GLUTES", "BARBELL", "GYM", "HINGE", "WEIGHT_REPS", "ADVANCED", false, "Partial deadlift from pins", listOf("Bar at knee height", "Hinge then drive hips", "Do not shrug"), "rack_pull.glb"),
        rich("chest_supported_row", "Chest-Supported Row", "BACK", "BICEPS", "MACHINE", "GYM", "HORIZONTAL_PULL", "WEIGHT_REPS", "BEGINNER", false, "No momentum row", listOf("Chest on pad", "Row to lower ribs", "Squeeze 1s"), "cs_row.glb"),
        rich("band_pullapart", "Band Pull-Apart", "BACK", "SHOULDERS", "BAND", "HOME", "HORIZONTAL_PULL", "REPS_ONLY", "BEGINNER", false, "Rear delt / mid-traps health", listOf("Band at eye height", "Pull apart with straight arms", "Pinch shoulder blades"), "band_pull.glb"),
        rich("single_arm_cable_row", "Single-Arm Cable Row", "BACK", "BICEPS", "CABLE", "GYM", "HORIZONTAL_PULL", "WEIGHT_REPS", "INTERMEDIATE", true, "Unilateral row", listOf("Non-working hand on knee", "Pull to hip", "Full stretch"), "single_cable_row.glb"),
        rich("pendlay_row", "Pendlay Row", "BACK", "BICEPS", "BARBELL", "GYM", "HORIZONTAL_PULL", "WEIGHT_REPS", "ADVANCED", false, "Dead-stop barbell row", listOf("Torso parallel", "Explode from floor each rep", "No bounce"), "pendlay.glb"),
        rich("inverted_row", "Inverted Row", "BACK", "BICEPS", "BODYWEIGHT", "HOME", "HORIZONTAL_PULL", "REPS_ONLY", "BEGINNER", false, "Tabletop row", listOf("Bar at waist height", "Body rigid plank", "Pull chest to bar"), "inverted.glb"),
        // LEGS 10 more
        rich("step_up", "Dumbbell Step-Up", "LEGS", "GLUTES", "DUMBBELL", "BOTH", "LUNGE", "WEIGHT_REPS", "INTERMEDIATE", true, "Unilateral quad/glute", listOf("Box at knee height", "Drive via front heel", "No push off back foot"), "step_up.glb"),
        rich("pistol_squat", "Pistol Squat", "QUADS", "GLUTES,CORE", "BODYWEIGHT", "HOME", "SQUAT", "REPS_ONLY", "ADVANCED", true, "Single-leg squat", listOf("Arms forward counterbalance", "Heel flat", "Depth controlled"), "pistol.glb"),
        rich("cossack_squat", "Cossack Squat", "LEGS", "GLUTES", "BODYWEIGHT", "HOME", "SQUAT", "REPS_ONLY", "INTERMEDIATE", true, "Lateral squat mobility", listOf("Wide stance", "Shift weight", "Keep chest tall"), "cossack.glb"),
        rich("nordic_curl", "Nordic Ham Curl", "HAMSTRINGS", "CORE", "BODYWEIGHT", "HOME", "HINGE", "REPS_ONLY", "ADVANCED", false, "Eccentric ham overload", listOf("Knees on pad", "Slow 5s lower", "Push off hands if needed"), "nordic.glb"),
        rich("glute_bridge", "Glute Bridge", "GLUTES", "HAMSTRINGS", "BODYWEIGHT", "HOME", "HINGE", "REPS_ONLY", "BEGINNER", false, "Glute activation", listOf("Feet hip width", "Squeeze 2s top", "No back arch"), "glute_bridge.glb"),
        rich("calf_raise_db", "Dumbbell Calf Raise", "CALVES", "", "DUMBBELL", "BOTH", "ISOLATION", "WEIGHT_REPS", "BEGINNER", false, "Single-leg optional calf", listOf("Ball of foot on edge", "Full stretch", "Pause top"), "calf_db.glb"),
        rich("sled_push", "Sled Push", "LEGS", "FULL_BODY", "MACHINE", "GYM", "LOCOMOTION", "WEIGHT_DISTANCE", "INTERMEDIATE", false, "Power conditioning", listOf("45° lean", "Drive knees", "Short choppy steps"), "sled.glb"),
        rich("belt_squat", "Belt Squat", "QUADS", "GLUTES", "MACHINE", "GYM", "SQUAT", "WEIGHT_REPS", "INTERMEDIATE", false, "Spine-friendly squat", listOf("Upright torso", "Knees track toes", "Handles light"), "belt_squat.glb"),
        rich("good_morning", "Good Morning", "HAMSTRINGS", "BACK,GLUTES", "BARBELL", "GYM", "HINGE", "WEIGHT_REPS", "ADVANCED", false, "Hip hinge with load on back", listOf("Soft knee bend", "Hinge until torso ~parallel", "Ham stretch then return"), "good_morning.glb"),
        rich("sumo_deadlift", "Sumo Deadlift", "BACK", "LEGS,GLUTES", "BARBELL", "GYM", "HINGE", "WEIGHT_REPS", "ADVANCED", false, "Wide stance deadlift", listOf("Feet wide toes out 30°", "Knees out", "Wedge hips in"), "sumo.glb"),
        // SHOULDERS 6 more + TRAPS
        rich("cable_face_pull2", "Cable Face Pull (High)", "SHOULDERS", "TRAPS", "CABLE", "GYM", "HORIZONTAL_PULL", "WEIGHT_REPS", "BEGINNER", false, "Rear delt external rotation", listOf("Rope at nose height", "Pull to forehead", "Externally rotate"), "face_high.glb"),
        rich("y_raise", "Y-Raise", "SHOULDERS", "TRAPS", "DUMBBELL", "BOTH", "ISOLATION", "WEIGHT_REPS", "BEGINNER", false, "Lower trap raise", listOf("Incline 30°", "Thumbs up", "Raise to Y"), "y_raise.glb"),
        rich("bus_driver", "Plate Bus Driver", "SHOULDERS", "", "DUMBBELL", "GYM", "ISOLATION", "WEIGHT_REPS", "BEGINNER", false, "Front steer", listOf("Plate at arm length", "Rotate 90° each side", "No shrug"), "bus.glb"),
        rich("shrug_db", "Dumbbell Shrug", "TRAPS", "", "DUMBBELL", "BOTH", "ISOLATION", "WEIGHT_REPS", "BEGINNER", false, "Trap elevation", listOf("Shoulders to ears", "Hold 1s", "No roll"), "shrug_db.glb"),
        rich("neck_harness", "Neck Harness Extension", "TRAPS", "", "MACHINE", "GYM", "ISOLATION", "WEIGHT_REPS", "ADVANCED", false, "Neck strength", listOf("Light load only", "Controlled flexion/extension", "No momentum"), "neck.glb"),
        rich("landmine_press", "Landmine Press", "SHOULDERS", "TRICEPS", "BARBELL", "GYM", "VERTICAL_PUSH", "WEIGHT_REPS", "INTERMEDIATE", true, "Single-arm angled press", listOf("Kneeling or standing", "Press at 45°", "Core tight"), "landmine.glb"),
        // ARMS 8 more (forearms emphasis)
        rich("wrist_curl", "Barbell Wrist Curl", "FOREARMS", "", "BARBELL", "GYM", "ISOLATION", "WEIGHT_REPS", "BEGINNER", false, "Flexor curl", listOf("Forearms on thighs", "Only hands move", "Full ROM"), "wrist_curl.glb"),
        rich("reverse_wrist", "Reverse Wrist Curl", "FOREARMS", "", "BARBELL", "GYM", "ISOLATION", "WEIGHT_REPS", "BEGINNER", false, "Extensor curl", listOf("Pronated grip", "Lift only hands", "Do not swing"), "reverse_wrist.glb"),
        rich("zottman_curl", "Zottman Curl", "BICEPS", "FOREARMS", "DUMBBELL", "BOTH", "ISOLATION", "WEIGHT_REPS", "INTERMEDIATE", false, "Curl then pronated lower", listOf("Curl supinated", "Flip to pronated at top", "Lower pronated"), "zottman.glb"),
        rich("spider_curl", "Spider Curl", "BICEPS", "", "DUMBBELL", "GYM", "ISOLATION", "WEIGHT_REPS", "INTERMEDIATE", false, "Chest-supported curl", listOf("Incline 75° prone", "No shoulder swing", "Squeeze"), "spider.glb"),
        rich("jm_press", "JM Press", "TRICEPS", "CHEST", "BARBELL", "GYM", "HORIZONTAL_PUSH", "WEIGHT_REPS", "ADVANCED", false, "Hybrid bench/skull", listOf("Bar to chin", "Elbows tuck", "Press back"), "jm.glb"),
        rich("cable_overhead", "Cable Overhead Extension", "TRICEPS", "", "CABLE", "GYM", "ISOLATION", "WEIGHT_REPS", "INTERMEDIATE", false, "Long head stretch", listOf("Back to cable", "Elbows overhead", "Full stretch"), "cable_over.glb"),
        rich("drag_curl", "Drag Curl", "BICEPS", "", "BARBELL", "GYM", "ISOLATION", "WEIGHT_REPS", "INTERMEDIATE", false, "Bar drags torso", listOf("Bar close to body", "Elbows back", "Do not lean"), "drag.glb"),
        rich("tate_press", "Tate Press", "TRICEPS", "", "DUMBBELL", "BOTH", "ISOLATION", "WEIGHT_REPS", "INTERMEDIATE", false, "Flared DB extension", listOf("DBs flare out", "Elbows wide", "Extend to lockout"), "tate.glb"),
        // CORE+CARRY 8 more
        rich("hanging_knee", "Hanging Knee Raise", "CORE", "", "BODYWEIGHT", "GYM", "ISOLATION", "REPS_ONLY", "BEGINNER", false, "Lower ab regression", listOf("Hang dead", "Knees to chest", "No swing"), "knee_raise.glb"),
        rich("dead_bug", "Dead Bug", "CORE", "", "BODYWEIGHT", "HOME", "ISOLATION", "REPS_ONLY", "BEGINNER", false, "Anti-extension", listOf("Low back flat", "Opposite arm/leg extend", "Exhale hard"), "deadbug.glb"),
        rich("pallof_press", "Pallof Press", "CORE", "", "CABLE", "GYM", "ROTATION", "WEIGHT_REPS", "INTERMEDIATE", false, "Anti-rotation", listOf("Cable at chest height", "Press straight out", "Resist rotation 2s"), "pallof.glb"),
        rich("suitcase_carry", "Suitcase Carry", "CORE", "FOREARMS", "KETTLEBELL", "BOTH", "CARRY", "WEIGHT_DISTANCE", "BEGINNER", true, "Unilateral carry", listOf("KB one side", "Stand tall no lean", "Walk 20m"), "suitcase.glb"),
        rich("overhead_carry", "Overhead Carry", "SHOULDERS", "CORE", "DUMBBELL", "BOTH", "CARRY", "WEIGHT_DISTANCE", "INTERMEDIATE", true, "Lockout walk", listOf("DB overhead locked", "Ribs down", "Walk slow"), "oh_carry.glb"),
        rich(" Copenhagen", "Copenhagen Plank", "CORE", "LEGS", "BODYWEIGHT", "HOME", "ISOLATION", "TIME", "ADVANCED", true, "Adductor + core", listOf("Top leg on bench", "Body side plank", "Hold 30s"), "copenhagen.glb"),
        rich("hollow_hold", "Hollow Body Hold", "CORE", "", "BODYWEIGHT", "HOME", "ISOLATION", "TIME", "INTERMEDIATE", false, "Gymnast hollow", listOf("Low back glued to floor", "Shoulders/legs off floor", "Breathe shallow"), "hollow.glb"),
        rich("windshield", "Windshield Wiper", "CORE", "", "BODYWEIGHT", "GYM", "ROTATION", "REPS_ONLY", "ADVANCED", false, "Hanging oblique", listOf("Hang from bar", "Legs straight", "Rotate side to side"), "wiper.glb"),
        // MOBILITY/BAND/MISC 6
        rich("band_apart2", "Band Chest Fly", "CHEST", "", "BAND", "HOME", "ISOLATION", "REPS_ONLY", "BEGINNER", false, "Band fly", listOf("Anchor behind", "Arms slight bend", "Cross midline slightly"), "band_fly.glb"),
        rich("band_goodmorning", "Band Good Morning", "HAMSTRINGS", "BACK", "BAND", "HOME", "HINGE", "REPS_ONLY", "BEGINNER", false, "Band hinge", listOf("Band under feet over neck", "Hinge", "Squeeze glutes"), "band_gm.glb"),
        rich("tibialis_raise", "Tibialis Raise", "CALVES", "", "BODYWEIGHT", "HOME", "ISOLATION", "REPS_ONLY", "BEGINNER", false, "Shin raise", listOf("Heels on edge", "Toes lift high", "Slow lower"), "tibialis.glb"),
        rich("jefferson_curl", "Jefferson Curl", "BACK", "HAMSTRINGS", "KETTLEBELL", "BOTH", "HINGE", "WEIGHT_REPS", "ADVANCED", false, "Segmental flexion (light!)", listOf("Very light", "Round spine segment by segment", "Strict control"), "jefferson.glb"),
        rich("face_pull_band", "Band Face Pull", "SHOULDERS", "BACK", "BAND", "HOME", "HORIZONTAL_PULL", "REPS_ONLY", "BEGINNER", false, "Home face pull", listOf("Band at face height", "Pull apart", "Hold 1s"), "band_face.glb"),
        rich("sissy_squat", "Sissy Squat", "QUADS", "", "BODYWEIGHT", "HOME", "SQUAT", "REPS_ONLY", "ADVANCED", false, "Quad isolation squat", listOf("Hold support", "Knees forward", "Lean back"), "sissy.glb"),
    )

    val all = exercises + extended

    private fun locationFor(eq: String) = when (eq) {
        "BODYWEIGHT", "BAND" -> "HOME"
        "DUMBBELL", "KETTLEBELL", "EZ_BAR" -> "BOTH"
        else -> "GYM" // BARBELL, MACHINE, CABLE, SMITH
    }
    private fun ex(id: String, name: String, group: String, secondary: String, eq: String, glb: String, heat: String = "") = ExerciseEntity(
        id = id, name = name, muscleGroup = group, secondaryMuscles = secondary, equipment = eq,
        location = locationFor(eq),
        difficulty = "INTERMEDIATE", description = "$name — See detail for cues.", glbAsset = "models/$glb", isCustom = false
    )
    private fun rich(id: String, name: String, group: String, secondary: String, eq: String, loc: String, pattern: String, tracking: String, diff: String, uni: Boolean, desc: String, cues: List<String>, glb: String) = ExerciseEntity(
        id = id.trim(), name = name, muscleGroup = group, secondaryMuscles = secondary, equipment = eq,
        location = loc, movementPattern = pattern, trackingType = tracking, difficulty = diff, unilateral = uni,
        description = desc, instructions = cues.joinToString(" | "), setupCues = cues.take(1).joinToString(), executionCues = cues.drop(1).joinToString(" | "),
        glbAsset = "models/$glb", defaultRestSeconds = if (pattern in listOf("SQUAT","HINGE","HORIZONTAL_PUSH") ) 120 else 75, isCustom = false
    )
}

object RoutineSeed {
    val routines = listOf(
        RoutineEntity("ppl_push", "PPL — Push", "Chest·Shoulders·Triceps • heavy", 1, 62, "INTERMEDIATE", now(), now()),
        RoutineEntity("ppl_pull", "PPL — Pull", "Back·Biceps • vertical/horizontal", 2, 58, "INTERMEDIATE", now(), now()),
        RoutineEntity("ppl_legs", "PPL — Legs", "Quads·Hams·Glutes·Calves", 4, 65, "INTERMEDIATE", now(), now()),
        RoutineEntity("upper", "Upper", "Chest/Back/Shoulders/Arms", 1, 55, "BEGINNER", now(), now()),
        RoutineEntity("lower", "Lower", "Squat/Hinge/Accessories", 3, 60, "BEGINNER", now(), now()),
        RoutineEntity("full_a", "Full Body A", "Squat + Push + Pull", 1, 50, "BEGINNER", now(), now()),
        RoutineEntity("full_b", "Full Body B", "Hinge + Push + Pull", 3, 50, "BEGINNER", now(), now()),
        RoutineEntity("five_x5", "5×5 Strength", "Compound 5×5 progression", 1, 45, "INTERMEDIATE", now(), now()),
    )
    val routineExercises = listOf(
        // PPL Push
        re("ppl_push", "bench_bb", 0, 4, 8, 2, 90), re("ppl_push", "ohp_bb", 1, 3, 8, 2, 90), re("ppl_push", "incline_db", 2, 3, 10, 1, 75), re("ppl_push", "lateral", 3, 4, 12, 1, 60), re("ppl_push", "pushdown", 4, 3, 12, 1, 60),
        // PPL Pull
        re("ppl_pull", "pullup", 0, 4, 6, 1, 90), re("ppl_pull", "row_bb", 1, 4, 8, 2, 90), re("ppl_pull", "lat_pulldown", 2, 3, 10, 1, 75), re("ppl_pull", "curl_bb", 3, 3, 12, 1, 60), re("ppl_pull", "hammer", 4, 3, 12, 1, 60),
        // PPL Legs
        re("ppl_legs", "squat_bb", 0, 4, 6, 2, 120), re("ppl_legs", "rdl", 1, 3, 8, 2, 90), re("ppl_legs", "leg_press", 2, 3, 10, 1, 75), re("ppl_legs", "leg_curl", 3, 3, 12, 1, 60), re("ppl_legs", "calf_raise", 4, 4, 15, 1, 45),
        // Upper/Lower + Full + 5x5
        re("upper", "bench_db", 0, 3, 10, 2, 90), re("upper", "row_db", 1, 3, 10, 2, 90), re("upper", "ohp_db", 2, 3, 10, 2, 75), re("upper", "lat_pulldown", 3, 3, 10, 1, 75), re("upper", "lateral", 4, 3, 12, 1, 60), re("upper", "hammer", 5, 3, 12, 1, 60),
        re("lower", "squat_bb", 0, 4, 6, 2, 120), re("lower", "hip_thrust", 1, 3, 10, 1, 75), re("lower", "leg_curl", 2, 3, 12, 1, 60), re("lower", "leg_ext", 3, 3, 12, 1, 60), re("lower", "calf_raise_db", 4, 4, 15, 1, 45),
        re("full_a", "squat_bb", 0, 3, 8, 2, 120), re("full_a", "bench_bb", 1, 3, 8, 2, 90), re("full_a", "row_bb", 2, 3, 8, 2, 90), re("full_a", "ohp_db", 3, 3, 10, 1, 75), re("full_a", "plank", 4, 3, 1, 0, 60),
        re("full_b", "rdl", 0, 3, 8, 2, 90), re("full_b", "incline_db", 1, 3, 10, 2, 90), re("full_b", "lat_pulldown", 2, 3, 10, 1, 75), re("full_b", "bulgarian", 3, 3, 10, 1, 75), re("full_b", "pallof_press", 4, 3, 12, 1, 60),
        re("five_x5", "squat_bb", 0, 5, 5, 1, 180), re("five_x5", "bench_bb", 1, 5, 5, 1, 180), re("five_x5", "row_bb", 2, 5, 5, 1, 180),
    )
    private fun re(routine: String, ex: String, pos: Int, sets: Int, reps: Int, rir: Int, rest: Int) = RoutineExerciseEntity(routine, ex, pos, sets, reps, rir, rest)
    private fun now() = System.currentTimeMillis()
}
