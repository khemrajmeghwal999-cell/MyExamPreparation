package com.example.myexampreparation

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"
    private const val PREF_NAME = "admob_interstitial_prefs"
    private const val KEY_LAST_SHOW_TIME = "last_interstitial_show_time"

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun getLastShowTimestamp(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_SHOW_TIME, 0L)
    }

    fun recordShowTimestamp(context: Context) {
        val now = System.currentTimeMillis()
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_SHOW_TIME, now)
            .apply()
        Log.i(TAG, "Recorded Interstitial show timestamp: $now (30-min cooldown started)")
    }

    fun isCooldownActive(context: Context): Boolean {
        val lastShow = getLastShowTimestamp(context)
        if (lastShow <= 0L) return false
        val elapsed = System.currentTimeMillis() - lastShow
        val remaining = AdMobConfig.INTERSTITIAL_COOLDOWN_MS - elapsed
        val isCooldown = elapsed < AdMobConfig.INTERSTITIAL_COOLDOWN_MS
        if (isCooldown) {
            val remainingMins = remaining / (60 * 1000L)
            Log.d(TAG, "Interstitial on cooldown. $remainingMins mins remaining before next allowed show.")
        }
        return isCooldown
    }

    fun preloadAd(
        context: Context
    ) {
        val adUnitId = AdMobConfig.getActiveInterstitialAdUnitId()
        if (adUnitId == null) {
            Log.d(TAG, "Skipping Interstitial preload: ADS_OFF mode active.")
            return
        }

        if (isCooldownActive(context)) {
            Log.d(TAG, "Skipping Interstitial preload because 30-min cooldown is active.")
            return
        }

        if (interstitialAd != null || isLoading) {
            Log.d(TAG, "Interstitial already loaded or currently loading.")
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                    Log.i(TAG, "Interstitial Ad preloaded successfully ($adUnitId).")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    interstitialAd = null
                    Log.e(TAG, "Interstitial Ad failed to preload: ${error.message} (Code: ${error.code})")
                }
            }
        )
    }

    fun showAdIfReady(
        activity: Activity,
        onAdDismissedOrSkipped: () -> Unit
    ) {
        val activeAdUnitId = AdMobConfig.getActiveInterstitialAdUnitId()
        if (activeAdUnitId == null) {
            Log.d(TAG, "Skipping Interstitial show: ADS_OFF mode active. Proceeding directly.")
            onAdDismissedOrSkipped()
            return
        }

        // 1. Check Cooldown
        if (isCooldownActive(activity)) {
            Log.i(TAG, "Skipping Interstitial show: Cooldown active (<30 mins since last show).")
            onAdDismissedOrSkipped()
            return
        }

        // 2. Check Ad Availability
        val ad = interstitialAd
        if (ad == null) {
            Log.w(TAG, "Interstitial Ad not ready/loaded. Proceeding directly without blocking.")
            onAdDismissedOrSkipped()
            preloadAd(activity)
            return
        }

        // 3. Set FullScreenContentCallback
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "Interstitial Ad displayed on screen. Starting 30-min cooldown.")
                recordShowTimestamp(activity)
                interstitialAd = null
            }

            override fun onAdDismissedFullScreenContent() {
                Log.i(TAG, "Interstitial Ad dismissed by user. Opening Quiz Result screen.")
                onAdDismissedOrSkipped()
                preloadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Failed to show Interstitial Ad: ${adError.message}. Proceeding directly.")
                interstitialAd = null
                onAdDismissedOrSkipped()
                preloadAd(activity)
            }
        }

        // 4. Safely Show Ad
        try {
            if (!activity.isFinishing && !activity.isDestroyed) {
                ad.show(activity)
            } else {
                Log.w(TAG, "Activity finishing or destroyed. Skipping Interstitial show.")
                onAdDismissedOrSkipped()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception showing Interstitial Ad: ${e.localizedMessage}", e)
            interstitialAd = null
            onAdDismissedOrSkipped()
        }
    }
}
