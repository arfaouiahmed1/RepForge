package com.repforge.core.data.auth

import com.repforge.core.datastore.PreferencesDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Guest-first: app works without account. Auth only for sync/backup.
 * Uses Firebase Auth (Google) later; for V1 keep local userId in DataStore.
 */
@Singleton
class AuthManager @Inject constructor(
    private val prefs: PreferencesDataSource,
) {
    suspend fun currentUserId(): String? = prefs.userId.first()

    suspend fun ensureGuestId(): String {
        val existing = currentUserId()
        if (existing != null) return existing
        val newId = "guest_" + java.util.UUID.randomUUID().toString().take(8)
        prefs.setUserId(newId)
        return newId
    }

    suspend fun signInWithGoogle(idToken: String): String {
        // TODO: FirebaseAuth.getInstance().signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
        val uid = "user_" + idToken.take(6)
        prefs.setUserId(uid)
        return uid
    }

    suspend fun signOut() {
        // FirebaseAuth.getInstance().signOut()
        prefs.setUserId("guest_" + java.util.UUID.randomUUID().toString().take(8))
    }
}
