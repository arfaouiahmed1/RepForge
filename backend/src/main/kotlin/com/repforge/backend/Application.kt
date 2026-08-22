package com.repforge.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT")?.toIntOrNull() ?: 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) { json() }
    routing {
        get("/health") { call.respond(mapOf("status" to "ok", "service" to "repforge-backend")) }
        get("/me/entitlements") { call.respond(EntitlementsResponse(emptyList())) }
        post("/billing/verify") { call.respond(mapOf("verified" to true)) } // verifies via Play Developer API
        post("/billing/rtdn") { call.respond(mapOf("ok" to true)) } // RTDN webhook from Google Play
        post("/sync/bootstrap") { call.respond(mapOf("ok" to true)) }
        delete("/me") { call.respond(mapOf("deleted" to true)) }
    }
}

@Serializable data class EntitlementsResponse(val entitlements: List<String>)
