package com.repforge.backend.auth

import io.ktor.server.auth.*

/** Ktor principal carrying the userId derived from the verified Firebase ID token. */
data class FirebasePrincipal(val userId: String, val email: String?) : Principal

/**
 * Installs the "firebase" authentication provider.
 *
 * Middleware contract:
 *  - Reads `Authorization: Bearer <firebase-id-token>`.
 *  - Verifies the token via [TokenVerifier] (signature, expiry, revocation, audience).
 *  - On success the route handler receives [FirebasePrincipal]; userId comes ONLY
 *    from the verified token — never from query params or request bodies.
 *  - Missing/invalid token or unconfigured verifier → 401 (fail-closed).
 */
fun AuthenticationConfig.firebaseAuth(verifier: TokenVerifier) {
    if (!verifier.configured) {
        // Loud startup signal in local dev; behavior stays fail-closed (401).
        org.slf4j.LoggerFactory.getLogger("FirebaseAuth")
            .warn("FIREBASE_PROJECT_ID not set — protected routes will reject all requests with 401")
    }
    bearer("firebase") {
        realm = "RepForge"
        authenticate { credential ->
            verifier.verify(credential.token)
                ?.let { FirebasePrincipal(userId = it.userId, email = it.email) }
        }
    }
}
