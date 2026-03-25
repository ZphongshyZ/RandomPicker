package com.phongcha.pickora.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.phongcha.pickora.BuildConfig

/**
 * Manages interstitial ads — preloads and shows every [PICKS_BETWEEN_ADS] picks.
 */
class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var pickCount = 0

    companion object {
        private const val PICKS_BETWEEN_ADS = 4
        private const val TAG = "InterstitialAd"
    }

    init {
        loadAd()
    }

    /** Call when a pick result is shown. */
    fun recordPick() {
        pickCount++
    }

    /** Show interstitial if counter reached and ad is loaded. Returns true if shown. */
    fun showIfReady(activity: Activity): Boolean {
        if (pickCount < PICKS_BETWEEN_ADS) return false
        val ad = interstitialAd ?: return false
        pickCount = 0
        ad.show(activity)
        interstitialAd = null
        loadAd()
        return true
    }

    private fun loadAd() {
        val adUnitId = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712" // Google test interstitial
        } else {
            "ca-app-pub-4019714899445933/7939532594"
        }
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.d(TAG, "Interstitial failed to load: ${error.message}")
                }
            }
        )
    }
}
