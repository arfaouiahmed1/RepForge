package com.repforge.backend.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Wire contract for POST /v1/sync/push — mirrors sync_operations table:
 * (operation_id, entity_type, entity_id, base_revision, mutation, idempotency_key).
 */
@Serializable
data class SyncOperationDto(
    val operationId: String,
    val entityType: String,
    val entityId: String,
    /** Expected server revision; mismatch ⇒ CONFLICT (409 semantics in results). */
    val baseRevision: Long? = null,
    /** JSON patch for the entity's table row. */
    val mutation: JsonObject,
    /** Per-operation client key; retried pushes with the same key are no-ops. */
    val idempotencyKey: String,
    val clientOpAt: String? = null,
)

@Serializable
data class SyncPushRequest(val operations: List<SyncOperationDto>)

@Serializable
data class SyncPushResult(
    val operationId: String,
    /** APPLIED | DUPLICATE | CONFLICT | REJECTED */
    val status: String,
    val revision: Long? = null,
    val errorCode: String? = null,
)

@Serializable
data class SyncPushResponse(val results: List<SyncPushResult>, val serverTime: String)

/** GET /v1/sync/pull?cursor=<iso8601> response — changes since cursor incl. tombstones. */
@Serializable
data class SyncPullResponse(
    val changes: List<JsonObject> = emptyList(),
    val nextCursor: String,
    val hasMore: Boolean = false,
)
