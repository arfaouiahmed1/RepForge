package com.repforge.core.ml

import kotlinx.serialization.Serializable

@Serializable
data class ModelManifest(
    val version: String, // e.g. "progression-12"
    val schemaVersion: Int,
    val minAppVersion: Int,
    val sha256: String,
    val createdAt: String,
    val metrics: ModelMetrics,
)

@Serializable
data class ModelMetrics(
    val brier: Double,
    val logLoss: Double,
    val auc: Double? = null,
)

/**
 * Remote model store flow:
 * app starts -> check manifest -> new compatible model? -> download -> verify SHA-256 -> load challenger -> fallback to embedded on failure
 * Never leaves app unusable if CDN dies.
 */
interface ModelStore {
    suspend fun checkForUpdate(currentVersion: String): ModelManifest?
    suspend fun downloadAndVerify(manifest: ModelManifest): ByteArray?
}
