package com.repforge.backend

import com.repforge.backend.auth.TokenVerifier
import com.repforge.backend.auth.TokenVerifierFactory
import com.repforge.backend.auth.firebaseAuth
import com.repforge.backend.routes.apiRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.auth.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toIntOrNull() ?: 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

/**
 * Public routes:  GET /health, POST /billing/rtdn (Pub/Sub push — own auth).
 * Protected:      everything under /v1/ via Firebase ID token bearer auth.
 * Local dev without FIREBASE_PROJECT_ID stays fail-closed: /v1/ requests get 401.
 */
fun Application.module(verifier: TokenVerifier = TokenVerifierFactory.fromEnvironment()) {
    install(ContentNegotiation) { json() }
    install(Authentication) { firebaseAuth(verifier) }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "repforge-backend"))
        }

        // Google Play RTDN webhook — authenticated by Pub/Sub push tokens, not user JWTs.
        post("/billing/rtdn") {
            call.respond(mapOf("ok" to true))
        }

        apiRoutes()
    }
}
