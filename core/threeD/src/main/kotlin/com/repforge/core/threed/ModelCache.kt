package com.repforge.core.threed

import android.content.Context
import java.io.File

/**
 * On-demand GLB cache — do not ship 300×3MB = 900MB APK.
 * Ships top 40 in assets/models, rest downloaded to filesDir/models/ with LRU 300MB.
 * Manifest: { version: "progression-12", url: "https://cdn.repforge.app/models/bench_press.glb", sha256: "..." }
 */
object ModelCache {
    private const val MAX_BYTES = 300L * 1024 * 1024
    private const val DIR = "models"

    fun getCachedFile(context: Context, glbAsset: String): File? {
        val name = glbAsset.substringAfterLast("/")
        val f = File(File(context.filesDir, DIR), name)
        return if (f.exists()) f else null
    }

    suspend fun ensureLocal(context: Context, glbAsset: String): File? {
        val assetExists = try { context.assets.open(glbAsset).close(); true } catch (_: Exception) { false }
        if (assetExists) return null // bundled
        val cached = getCachedFile(context, glbAsset)
        if (cached?.exists() == true) return cached
        // TODO: fetch from ModelStore manifest: download, verify SHA256, save to filesDir/models/, evict LRU if >300MB
        // val bytes = httpClient.get(manifest.url); if (sha256(bytes)==manifest.sha256) save
        evictIfNeeded(context)
        return cached
    }

    private fun evictIfNeeded(context: Context) {
        val dir = File(context.filesDir, DIR).also { it.mkdirs() }
        val files = dir.listFiles()?.sortedBy { it.lastModified() } ?: return
        var total = files.sumOf { it.length() }
        for (f in files) {
            if (total <= MAX_BYTES) break
            total -= f.length()
            f.delete()
        }
    }

    fun shouldPreloadAllOnWifi(): Boolean = false // user setting: Download all on Wi-Fi (Pro offline)
}
