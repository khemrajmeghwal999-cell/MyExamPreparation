package com.example.myexampreparation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

enum class AdMode {
    ADS_OFF,
    TEST_ADS_ON,
    PRODUCTION_ADS_ON
}

object AdMobConfig {
    private const val TAG = "AdMobConfig"

    // CENTRALIZED CONTROL: Set active ad mode here
    val currentAdMode: AdMode = AdMode.ADS_OFF

    // Official Google Test Banner Ad Unit ID
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

    // Production Banner Ad Unit ID
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-4390329210582267/1100944152"

    // Official Google Test Interstitial Ad Unit ID
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Production Interstitial Ad Unit ID
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4390329210582267/8422040137"

    // 30-Minute Cooldown in milliseconds (30 * 60 * 1000 = 1,800,000 ms)
    const val INTERSTITIAL_COOLDOWN_MS = 30 * 60 * 1000L

    fun getActiveBannerAdUnitId(): String? {
        return when (currentAdMode) {
            AdMode.ADS_OFF -> null
            AdMode.TEST_ADS_ON -> TEST_BANNER_AD_UNIT_ID
            AdMode.PRODUCTION_ADS_ON -> PROD_BANNER_AD_UNIT_ID
        }
    }

    fun getActiveInterstitialAdUnitId(): String? {
        return when (currentAdMode) {
            AdMode.ADS_OFF -> null
            AdMode.TEST_ADS_ON -> TEST_INTERSTITIAL_AD_UNIT_ID
            AdMode.PRODUCTION_ADS_ON -> PROD_INTERSTITIAL_AD_UNIT_ID
        }
    }
}

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val adUnitId = AdMobConfig.getActiveBannerAdUnitId()
    if (adUnitId == null) {
        // ADS_OFF: Render nothing (no empty container or blank space)
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("AdMob", "Banner Ad loaded successfully ($adUnitId).")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e("AdMob", "Banner Ad failed to load: ${error.message} (Code: ${error.code})")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
