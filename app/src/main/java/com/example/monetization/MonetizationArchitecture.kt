package com.example.monetization

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interface contract for Google Play Billing in Billora.
 * Prepared for plug-and-play integration of com.android.billingclient:billing.
 */
interface BillingManager {
    val isPremium: StateFlow<Boolean>
    fun launchBillingFlow(activity: Activity, skuId: String = "billora_premium_lifetime")
    fun restorePurchases()
}

/**
 * Default offline implementation providing full access to all features.
 */
class LocalBillingManager : BillingManager {
    private val _isPremium = MutableStateFlow(true) // Free version with full core capability unlocked
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    override fun launchBillingFlow(activity: Activity, skuId: String) {
        // Ready for Google Play Billing Client integration
    }

    override fun restorePurchases() {
        // Ready for Google Play Billing Client restore queries
    }
}

/**
 * Interface contract for Google AdMob in Billora.
 * Prepared for plug-and-play integration of play-services-ads.
 */
interface AdManager {
    val adsEnabled: Boolean
    fun showInterstitial(activity: Activity, onClosed: () -> Unit = {})
}

class LocalAdManager : AdManager {
    override val adsEnabled: Boolean = false // Live ads not activated as requested

    override fun showInterstitial(activity: Activity, onClosed: () -> Unit) {
        onClosed()
    }
}
