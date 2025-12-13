package com.zandeveloper.tiktokly.utils.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError

class AdsApp(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun preload() {
        if (isLoading || interstitialAd != null) return

        isLoading = true
        val request = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            "ca-app-pub-8266726360742140/3215784649", // real id
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun showOrContinue(activity: Activity, onContinue: () -> Unit) {
        val ad = interstitialAd

        if (ad == null) {
            // gak ada iklan → GAS
            preload()
            onContinue()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preload()
                onContinue()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                interstitialAd = null
                preload()
                onContinue()
            }

            override fun onAdShowedFullScreenContent() {
                interstitialAd = null
            }
        }

        ad.show(activity)
    }
}