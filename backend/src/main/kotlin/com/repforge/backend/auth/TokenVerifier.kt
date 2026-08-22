package com.repforge.backend.auth

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.File

/** Server-derived identity from a verified Firebase ID token. Never trust client-sent userId. */
data class FirebaseUser(val userId: String, val email: String?)

/**
 * Verifies Firebase ID tokens and derives the userId server-side.
 *
 * Fail-closed contract: when Firebase is not configured (local dev without
 * credentials) every verification returns null, so protected routes respond
 * 401 instead of trusting unverified callers. /health stays public.
 */
interface TokenVerifier {
    /** True when a real FirebaseApp is wired up. */
    val configured: Boolean

    /** @return verified user or null when the token is missing/expired/revoked/wrong audience. */
    fun verify(idToken: String): FirebaseUser?
}

/** Real verifier backed by firebase-admin. Requires FIREBASE_PROJECT_ID (+ optional SA JSON). */
class FirebaseAuthVerifier(private val app: FirebaseApp) : TokenVerifier {
    override val configured: Boolean = true
    private val auth = FirebaseAuth.getInstance(app)

    override fun verify(idToken: String): FirebaseUser? = try {
        val decoded = auth.verifyIdToken(idToken, /* checkRevoked = */ true)
        FirebaseUser(userId = decoded.uid, email = decoded.email)
    } catch (_: Exception) {
        // ExpiredIdTokenException, RevokedIdTokenException, malformed JWT,
        // wrong audience/issuer → all collapse to "unauthenticated" (401).
        null
    }
}

/** Fail-closed stub used when Firebase env vars are absent (local scaffolding). */
object UnconfiguredVerifier : TokenVerifier {
    override val configured: Boolean = false
    override fun verify(idToken: String): FirebaseUser? = null
}

object TokenVerifierFactory {
    /**
     * Env contract (see backend/.env.example):
     *   FIREBASE_PROJECT_ID                  — enables real verification when set
     *   GOOGLE_APPLICATION_CREDENTIALS       — ADC path to service-account JSON (optional)
     *   FIREBASE_SERVICE_ACCOUNT_JSON_PATH   — explicit path, wins over ADC (optional)
     */
    fun fromEnvironment(env: (String) -> String? = System::getenv): TokenVerifier {
        val projectId = env("FIREBASE_PROJECT_ID")
            ?: return UnconfiguredVerifier // fail-closed: every protected route → 401

        val builder = FirebaseOptions.Builder().setProjectId(projectId)
        val saPath = env("FIREBASE_SERVICE_ACCOUNT_JSON_PATH")
            ?: env("GOOGLE_APPLICATION_CREDENTIALS")
        saPath?.let { path ->
            File(path).takeIf(File::exists)?.inputStream()?.use { stream ->
                builder.setCredentials(GoogleCredentials.fromStream(stream))
            }
        }

        val app = FirebaseApp.initializeApp(builder.build(), "repforge-backend")
        return FirebaseAuthVerifier(app)
    }
}
