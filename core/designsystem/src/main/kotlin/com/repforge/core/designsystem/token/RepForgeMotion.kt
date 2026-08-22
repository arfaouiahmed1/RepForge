package com.repforge.core.designsystem.token

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Motion vocabulary — one expressive focal interaction per screen.
 *
 * Spring-first per M3 Expressive (May 2025): component animation uses spring physics,
 * not fixed easing curves. Springs have no fixed duration — stiffness/damping are tuned
 * so settle time approximates the MD3 duration scale:
 *  - [emphasized] ≈ 500ms — screen-level morphs, set→rest transition focal moment
 *  - [standard]   ≈ 400ms — component transitions (enter/begin on screen)
 *  - [quick]      ≈ 200ms — micro feedback (press, chip toggles)
 *
 * Easing/duration remains ONLY for what springs cannot express (MD3 spec carve-out):
 * repeating ambient motion (`infiniteRepeatable` requires a DurationBasedAnimationSpec)
 * and enter/exit transitions. Those use the md.sys emphasized easings below.
 *
 * Morphing vocabulary (set → rest): COMPLETE SET press → [MorphSpring] compress/expand
 * → checkmark via [quick] → completed row lifts with [BouncySpring] → rest timer
 * emerges with [emphasized]. One focal morph per screen; everything else is QUIET.
 */
object RepForgeMotion {

    // ------------------------------------------------------------------
    // Spring vocabulary (generic factories — usable for Float, IntSize, Color…)
    // ------------------------------------------------------------------

    /** ~500ms settle. Emphasized screen-level morphs (set→rest, hero reveals). */
    fun <T> emphasized(): SpringSpec<T> = spring(dampingRatio = 0.82f, stiffness = 170f)

    /** ~400ms settle. Standard component transitions. */
    fun <T> standard(): SpringSpec<T> = spring(dampingRatio = 0.86f, stiffness = 260f)

    /** ~200ms settle. Quick micro feedback. */
    fun <T> quick(): SpringSpec<T> = spring(dampingRatio = 0.90f, stiffness = 700f)

    /** Expressive overshoot for celebratory moments (GiantStart press). */
    fun <T> bouncy(): SpringSpec<T> = spring(dampingRatio = 0.55f, stiffness = 320f)

    /** Gentle non-bouncy drift for large surfaces. */
    fun <T> gentle(): SpringSpec<T> = spring(dampingRatio = 0.88f, stiffness = 180f)

    /** Playful wobble, used sparingly (achievement unlock). */
    fun <T> wobbly(): SpringSpec<T> = spring(dampingRatio = 0.52f, stiffness = 260f)

    // Float-typed canonical instances for animate*AsState call sites.
    val EmphasizedSpring: SpringSpec<Float> = emphasized()
    val StandardSpring: SpringSpec<Float> = standard()
    val QuickSpring: SpringSpec<Float> = quick()
    val BouncySpring: SpringSpec<Float> = bouncy()
    val GentleSpring: SpringSpec<Float> = gentle()
    val WobblySpring: SpringSpec<Float> = wobbly()

    /** The set-complete morph — emphasized with a touch more life than plain emphasized. */
    val MorphSpring: SpringSpec<Float> = spring(dampingRatio = 0.72f, stiffness = 220f)

    // ------------------------------------------------------------------
    // md.sys easing + duration scale — reserved for transitions & ambient repeats.
    // ------------------------------------------------------------------
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val ExpressiveDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val ExpressiveAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    /** Brand expressive easing for ambient/repeating motion only. */
    val ExpressiveEasing = CubicBezierEasing(0.34f, 0.80f, 0.34f, 1.0f)

    const val DurationEmphasized = 500 // begin-and-end-on-screen transitions
    const val DurationStandard = 400   // enter transitions
    const val DurationQuick = 200      // exit transitions

    /** Typed view of a Float spring for APIs demanding [FiniteAnimationSpec]. */
    fun <T> asFinite(spec: SpringSpec<T>): FiniteAnimationSpec<T> = spec
}
