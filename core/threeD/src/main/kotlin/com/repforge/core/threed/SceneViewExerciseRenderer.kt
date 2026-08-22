package com.repforge.core.threed

import android.content.Context
import com.google.android.filament.Engine
import com.google.android.filament.utils.Utils
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.Model
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

/**
 * SceneView/Filament-backed [ExerciseRenderer].
 *
 * ISOLATION BOUNDARY (todo 23): this is the only file in the repo allowed to hold
 * io.github.sceneview node types ([ModelNode]) together with filament engine types.
 * Feature modules depend on [ExerciseRenderer] + [ExerciseRenderState] exclusively.
 *
 * Threading: [preload] is suspend; ModelLoader.loadModel hops to Main internally for
 * asset parsing, so callers may invoke from any dispatcher. release/releaseAll/destroy
 * are safe no-ops after [destroy].
 */
class SceneViewExerciseRenderer(context: Context) : ExerciseRenderer {

    private class LoadedAsset(val model: Model, val node: ModelNode)

    private val appContext = context.applicationContext

    // Lazily created: native libs load on first use so merely constructing the renderer
    // in a preview/test environment never touches JNI.
    private var engine: Engine? = null
    private var modelLoader: ModelLoader? = null

    private val loaded = LinkedHashMap<String, LoadedAsset>()

    private val _state = MutableStateFlow<ExerciseRenderState>(ExerciseRenderState.Idle)
    override val state: StateFlow<ExerciseRenderState> = _state.asStateFlow()

    override suspend fun preload(assetUrl: String): String? {
        val assetId = assetIdFor(assetUrl)
        if (loaded.containsKey(assetId)) {
            _state.value = ExerciseRenderState.Ready(assetId, assetUrl)
            return assetId
        }
        _state.value = ExerciseRenderState.Loading(assetId, assetUrl)
        return try {
            val loader = loader()
            val model = loader.loadModel(assetUrl)
                ?: error("SceneView could not parse GLB: $assetUrl")
            val instance = loader.createInstance(model)
                ?: error("SceneView could not instantiate GLB: $assetUrl")
            loaded[assetId] = LoadedAsset(model, ModelNode(modelInstance = instance))
            _state.value = ExerciseRenderState.Ready(assetId, assetUrl)
            assetId
        } catch (t: Throwable) {
            _state.value = ExerciseRenderState.Failed(
                assetId = assetId,
                assetUrl = assetUrl,
                message = t.message ?: t.javaClass.simpleName
            )
            null
        }
    }

    override fun release(assetId: String) {
        val entry = loaded.remove(assetId) ?: return
        runCatching { modelLoader?.destroyModel(entry.model) }
        when (val current = _state.value) {
            is ExerciseRenderState.Ready -> if (current.assetId == assetId) _state.value = ExerciseRenderState.Idle
            is ExerciseRenderState.Loading -> if (current.assetId == assetId) _state.value = ExerciseRenderState.Idle
            else -> Unit
        }
    }

    override fun releaseAll() {
        for (id in loaded.keys.toList()) release(id)
        loaded.clear()
        if (_state.value !is ExerciseRenderState.Failed) _state.value = ExerciseRenderState.Idle
    }

    /** Tears down the SceneView loader and Filament engine. Idempotent. */
    fun destroy() {
        releaseAll()
        runCatching { modelLoader?.destroy() }
        modelLoader = null
        runCatching { engine?.destroy() }
        engine = null
    }

    private fun loader(): ModelLoader {
        modelLoader?.let { return it }
        Utils.init() // loads filament-jni + gltfio natives exactly once per process
        val newEngine = Engine.create()
        val newLoader = ModelLoader(newEngine, appContext)
        engine = newEngine
        modelLoader = newLoader
        return newLoader
    }

    private fun assetIdFor(assetUrl: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(assetUrl.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
            .take(16)
}
