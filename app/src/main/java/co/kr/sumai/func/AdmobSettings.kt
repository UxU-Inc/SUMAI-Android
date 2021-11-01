package co.kr.sumai.func

import android.app.Activity
import android.util.DisplayMetrics
import android.view.Display
import android.widget.FrameLayout
import co.kr.sumai.BuildConfig
import co.kr.sumai.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdmobSettings(private val activity: Activity) {
    var mInterstitialAd // 애드몹 전면 광고
            : InterstitialAd? = null
    private var adMobBannerID: String
    private var adMobInterstitialID: String

    init {
        if (BuildConfig.DEBUG) {
            adMobBannerID = activity.getString(R.string.adaptive_banner_ad_unit_id_test)
            adMobInterstitialID = activity.getString(R.string.adaptive_interstitial_ad_unit_id_test)
        } else {
            adMobBannerID = activity.getString(R.string.adaptive_banner_ad_unit_id)
            adMobInterstitialID = activity.getString(R.string.adaptive_interstitial_ad_unit_id)
        }
        MobileAds.initialize(activity) { }
    }

    //┌────────────────────────────── 배너 광고 ──────────────────────────────┐
    fun loadBanner(ad_view_container: FrameLayout) {
        val adView = AdView(activity)
        adView.adUnitId = adMobBannerID
        ad_view_container.addView(adView)
        val adRequest = AdRequest.Builder().build()
        val adSize = adSize
        adView.adSize = adSize
        adView.loadAd(adRequest)
    }
    private val adSize: AdSize
        get() {
            val display: Display = activity.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val widthPixels: Float = outMetrics.widthPixels.toFloat()
            val density: Float = outMetrics.density
            val adWidth = (widthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
        }

    //┌────────────────────────────── 전면 광고 ──────────────────────────────┐
    fun loadInterstitial(setCount: () -> Unit) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(activity, adMobInterstitialID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                // 광고 로딩
                mInterstitialAd = interstitialAd
                mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // 전면 광고가 닫힐 때 호출
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        // 전면 광고가 나오지 않았올 때 호출
                    }

                    override fun onAdShowedFullScreenContent() {
                        // 전면 광고가 나왔올 때 호출
                        mInterstitialAd = null
                        setCount()
                    }
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // 오류 처리
                mInterstitialAd = null
            }
        })
    }
}