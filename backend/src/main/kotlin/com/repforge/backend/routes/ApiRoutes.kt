package com.repforge.backend.routes

import com.repforge.backend.auth.FirebasePrincipal
import com.repforge.backend.sync.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant

/**
 * Contract endpoints (hybrid architecture decision Q1-A):
 *   POST   /v1/sync/push          — batched mutations, idempotent, revision-checked
 *   GET    /v1/sync/pull?cursor=  — changes since cursor (tombstones included)
 *   POST   /v1/billing/verify     — Play purchase token → verified entitlement
 *   POST   /v1/device/register    — FCM token + installation provenance
 *   DELETE /v1/account            — GDPR delete (tombstone user_profile)
 *   POST   /v1/export             — GDPR data export
 *   GET    /v1/me/entitlements    — current verified entitlements
 *
 * Every handler derives userId from the VERIFIED Firebase token
 * (call.principal<FirebasePrincipal>()) — never from the request payload.
 * Persistence wiring lands with the Supabase client task; these are the
 * stable request/response skeletons.
 */
fun Route.apiRoutes() {
    authenticate("firebase") {
        route("/v1") {

            post("/sync/push") {
                val userId = call.userId()
                val request = call.receive<SyncPushRequest>()
                // TODO(task-32): apply mutations in a transaction against sync_operations;
                //  base_revision mismatch → CONFLICT; duplicate idempotency_key → DUPLICATE.
                val results = request.operations.map { op ->
                    SyncPushResult(operationId = op.operationId, status = "APPLIED")
                }
                call.respond(SyncPushResponse(results = results, serverTime = Instant.now().toString()))
            }

            get("/sync/pull") {
                val userId = call.userId()
                val cursor = call.request.queryParameters["cursor"]
                    ?: Instant.EPOCH.toString() // full initial pull
                // TODO(task-32): SELECT ... WHERE user_id = :userId AND updated_at > :cursor
                call.respond(
                    SyncPullResponse(changes = emptyList(), nextCursor = Instant.now().toString())
                )
            }

            post("/billing/verify") {
                val userId = call.userId()
                // TODO(task-34): Play Developer API purchases.products.get / subscriptionsv2.get,
                //  upsert entitlement row keyed by purchase_token.
                call.respond(mapOf("verified" to true, "userId" to userId))
            }

            post("/device/register") {
                val userId = call.userId()
                // TODO(task-37): upsert device_installation by (user_id, installation_id).
                call.respond(mapOf("registered" to true, "userId" to userId))
            }

            delete("/account") {
                val userId = call.userId()
                // TODO(task-39): tombstone cascade (deleted_at = now()) + Firebase user delete.
                call.respond(mapOf("deleted" to true, "userId" to userId))
            }

            post("/export") {
                val userId = call.userId()
                // TODO(task-39): stream all rows for user as JSON/zip artifact.
                call.respond(mapOf("queued" to true, "userId" to userId))
            }

            get("/me/entitlements") {
                val userId = call.userId()
                // TODO(task-34): read live entitlement rows for user.
                call.respond(EntitlementsResponse(userId = userId, entitlements = emptyList()))
            }
        }
    }
}

/** Extracts the server-derived userId or fails the call — routes only run inside authenticate{}. */
private fun ApplicationCall.userId(): String =
    principal<FirebasePrincipal>()?.userId
        ?: error("FirebasePrincipal missing outside authenticated route")

@kotlinx.serialization.Serializable
data class EntitlementsResponse(val userId: String, val entitlements: List<String>)
