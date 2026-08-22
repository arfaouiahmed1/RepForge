package com.repforge.core.threed

import kotlinx.coroutines.flow.StateFlow

/**
 * Render lifecycle surface for 3D exercise assets.
 *
 * This is the ONLY type surface feature modules should see from the 3D stack:
 * every io.github.sceneview / com.google.android.filament import stays
 * inside core/threeD (see [SceneViewExerciseRenderer]).
 */
sealed interface ExerciseRenderState {
    data object Idle : ExerciseRenderState

    data class Loading(val assetId: String, val assetUrl: String) : ExerciseRenderState

    data class Ready(val assetId: String, val assetUrl: String) : ExerciseRenderState

    data class Failed(val assetId: String?, val assetUrl: String?, val message: String) : ExerciseRenderState
}

/**
 * Abstraction over the 3D engine (Filament + SceneView) so feature modules
 * never import io.github.sceneview directly.
 *
 * Contract:
 * - [preload] is idempotent per resolved [assetId]; calling it twice with the same
 *   URL returns the existing id without re-parsing.
 * - [release] frees GPU/native resources for one asset; releasing an unknown id is a no-op.
 * - [state] always reflects the most recent preload/release outcome.
 */
interface ExerciseRenderer {
    val state: StateFlow<ExerciseRenderState>

    /**
     * Loads a GLB from an app-asset path ("models/bench.glb"), a local file path,
     * or an https URL (download handling delegated to ModelCache where applicable).
     *
     * @return stable [assetId] derived from [assetUrl], or null on failure
     *         (details published on [state] as [ExerciseRenderState.Failed]).
     */
    suspend fun preload(assetUrl: String): String?

    fun release(assetId: String)

    fun releaseAll()
}
