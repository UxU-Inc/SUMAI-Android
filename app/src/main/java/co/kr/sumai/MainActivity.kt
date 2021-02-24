package co.kr.sumai

import android.content.Intent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*

import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Display
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import co.kr.sumai.net.SummaryRequest
import co.kr.sumai.net.SummaryResponse
import co.kr.sumai.net.service
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var drawerToggle: ActionBarDrawerToggle? = null
    var toolbar: Toolbar? = null
    private var mInterstitialAd // 애드몹 전면 광고
            : InterstitialAd? = null
    private var adMobBannerID: String? = null
    private var adMobInterstitialID: String? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    var ID = ""
    private val record = 1
    private var summaryRequestCount = 0

    //뒤로 버튼 두번 연속 클릭 시 종료
    private var time: Long = 0
    override fun onBackPressed() {
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis()
            Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        } else if (System.currentTimeMillis() - time < 2000) {
            this@MainActivity.finish()
        }
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initLayout()
        clickEvent()

        // Firebase
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // AdMob
        AdmobInit()
    }

    private fun initLayout() {
        // toolbar, drawer, navigation Component
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerToggle = ActionBarDrawerToggle(
                this,
                dl_main_drawer_root,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        )
        dl_main_drawer_root.addDrawerListener(drawerToggle!!)
        nv_main_navigation_root.setNavigationItemSelectedListener(this)


        // main Component
        imageButtonClear.visibility = View.INVISIBLE
        imageButtonClear.setOnClickListener(View.OnClickListener {
            editTextSummary.setText("")
            imageButtonClear.visibility = View.INVISIBLE
        })
        imageButtonSummary.setOnClickListener(View.OnClickListener { summaryRequest(editTextSummary.getText().toString()) })
        editTextSummary.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 입력 변화 시
                if (0 < editTextSummary.getText().toString().length) {
                    if (imageButtonClear.getVisibility() == View.INVISIBLE) imageButtonClear.setVisibility(View.VISIBLE)
                    if (5000 <= editTextSummary.getText().toString().length) textViewLimitGuide.setVisibility(View.VISIBLE)
                } else {
                    textViewSummaryResult.setVisibility(View.INVISIBLE)
                    if (imageButtonClear.getVisibility() == View.VISIBLE) imageButtonClear.setVisibility(View.INVISIBLE)
                }
            }

            override fun afterTextChanged(arg0: Editable) {
                // 입력 끝났을 때
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // 입력하기 전
            }
        })
        textViewSummaryResult.visibility = View.INVISIBLE
        textViewLimitGuide.visibility = View.INVISIBLE
        layoutLoading.visibility = View.INVISIBLE
    }

    protected override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle!!.syncState()
        drawerToggle!!.drawerArrowDrawable.color = Color.WHITE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.home -> {
            }
            R.id.terms -> {
                intent = Intent(getApplicationContext(), GuideActivity::class.java)
                intent.putExtra("page", 0)
                startActivity(intent)
            }
            R.id.privacy -> {
                intent = Intent(getApplicationContext(), GuideActivity::class.java)
                intent.putExtra("page", 1)
                startActivity(intent)
            }
            R.id.notice -> {
                intent = Intent(getApplicationContext(), GuideActivity::class.java)
                intent.putExtra("page", 2)
                startActivity(intent)
            }
            R.id.sendFeedback -> sendMail()
        }
        dl_main_drawer_root.closeDrawer(GravityCompat.START)
        return false
    }

    private fun clickEvent() {
        // toolbar
        val buttonNews: ImageButton = findViewById<ImageButton>(R.id.buttonNews)
        buttonNews.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://news.sumai.co.kr"))
            startActivity(intent)
        })
        val layoutLogin: LinearLayout = findViewById<LinearLayout>(R.id.layoutLogin)
        layoutLogin.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        })
    }

    private fun summaryRequest(data: String) {
        layoutLoading.setVisibility(View.VISIBLE)
        layoutLoading.setClickable(true)
        if (mInterstitialAd != null && 4 <= summaryRequestCount) {
            mInterstitialAd!!.show(this)
        }
        loadInterstitial()
        val res: Call<SummaryResponse> = service.getSummary(SummaryRequest(data, ID, record))
        res.enqueue(object : Callback<SummaryResponse> {
            override fun onResponse(call: Call<SummaryResponse>, response: Response<SummaryResponse>) {
                textViewSummaryResult.setText(response.body()?.summarize)
                textViewSummaryResult.setVisibility(View.VISIBLE)
                layoutLoading.setVisibility(View.INVISIBLE)
                layoutLoading.setClickable(false)
                summaryRequestCount++
            }

            override fun onFailure(call: Call<SummaryResponse>, t: Throwable) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                layoutLoading.setVisibility(View.INVISIBLE)
                layoutLoading.setClickable(false)
            }
        })
    }

    private fun AdmobInit() {
        if (BuildConfig.DEBUG) {
            adMobBannerID = getString(R.string.adaptive_banner_ad_unit_id_test)
            adMobInterstitialID = getString(R.string.adaptive_interstitial_ad_unit_id_test)
        } else {
            adMobBannerID = getString(R.string.adaptive_banner_ad_unit_id)
            adMobInterstitialID = getString(R.string.adaptive_interstitial_ad_unit_id)
        }
        MobileAds.initialize(this, object : OnInitializationCompleteListener {
            override fun onInitializationComplete(initializationStatus: InitializationStatus) {}
        })


        //┌────────────────────────────── 배너 광고 ──────────────────────────────┐
        val adView = AdView(this)
        adView.setAdUnitId(adMobBannerID)
        ad_view_container.addView(adView)
        val adRequest = AdRequest.Builder().build()
        val adSize = adSize
        adView.setAdSize(adSize)
        adView.loadAd(adRequest)

        //┌────────────────────────────── 전면 광고 ──────────────────────────────┐
        loadInterstitial()
    }

    private val adSize: AdSize
        private get() {
            val display: Display = getWindowManager().getDefaultDisplay()
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val widthPixels: Float = outMetrics.widthPixels.toFloat()
            val density: Float = outMetrics.density
            val adWidth = (widthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, adMobInterstitialID!!, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                // 광고 로딩
                mInterstitialAd = interstitialAd
                mInterstitialAd!!.setFullScreenContentCallback(object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // 전면 광고가 닫힐 때 호출
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        // 전면 광고가 나오지 않았올 때 호출
                    }

                    override fun onAdShowedFullScreenContent() {
                        // 전면 광고가 나왔올 때 호출
                        mInterstitialAd = null
                        summaryRequestCount = 0
                    }
                })
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // 오류 처리
                mInterstitialAd = null
            }
        })
    }

    private fun sendMail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        try {
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("help@sumai.co.kr"))
            emailIntent.setType("text/html")
            emailIntent.setPackage("com.google.android.gm")
            if (emailIntent.resolveActivity(getPackageManager()) != null) startActivity(emailIntent)
            startActivity(emailIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            emailIntent.setType("text/html")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("help@sumai.co.kr"))
            startActivity(Intent.createChooser(emailIntent, "의견 보내기"))
        }
    }
}