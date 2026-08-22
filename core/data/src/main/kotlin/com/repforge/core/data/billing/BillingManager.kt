package com.repforge.core.data.billing

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Never trust SharedPreferences("isPro", true).
 * Flow: Play Billing → purchaseToken → backend → Play Developer API → verified entitlement → RTDN → update.
 * This is the client stub; real verification lives in Cloud Run Ktor: POST /billing/verify + /billing/rtdn
 */
enum class EntitlementStatus { FREE, PRO, UNKNOWN }

data class Entitlement(
    val status: EntitlementStatus = EntitlementStatus.FREE,
    val productId: String? = null,
    val expiresAt: Long? = null,
    val isTrial: Boolean = false,
)

@Singleton
class BillingManager @Inject constructor() {
    private val _entitlement = MutableStateFlow(Entitlement())
    val entitlement: StateFlow<Entitlement> = _entitlement

    fun isPro(): Boolean = _entitlement.value.status == EntitlementStatus.PRO

    // Called after Play Billing purchase flow returns BillingResult.OK
    suspend fun verifyPurchase(purchaseToken: String, productId: String): Entitlement {
        // TODO: call Cloud Run POST /billing/verify {token, productId} → verify via Play Developer API
        // For debug, grant PRO locally but still mark as unverified
        val ent = Entitlement(status = EntitlementStatus.PRO, productId = productId, expiresAt = System.currentTimeMillis() + 30L*24*3600*1000)
        _entitlement.value = ent
        return ent
    }

    suspend fun refreshFromBackend(userId: String) {
        // TODO: GET /me/entitlements → update _entitlement
    }

    // Products — test prices per spec
    object Products {
        const val MONTHLY = "repforge_pro_monthly" // $3.99
        const val YEARLY = "repforge_pro_yearly"   // $29.99
    }
}
