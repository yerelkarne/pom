package com.pomodorofocus.app.data

import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.appopen.AppOpenAd

object AdConstants {
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
}

class AdManager(private val application: Application) {
    private var appOpenAd: AppOpenAd? = null
    private var loading = false

    fun loadAppOpenAd() {
        if (loading || appOpenAd != null) return
        loading = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            AdConstants.APP_OPEN_AD_UNIT_ID,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loading = false
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    Log.w("AdManager", "App open ad failed: ${error.message}")
                    loading = false
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity) {
        if (appOpenAd == null) {
            loadAppOpenAd()
            return
        }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                loadAppOpenAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                loadAppOpenAd()
            }
        }
        appOpenAd?.show(activity)
    }
}
