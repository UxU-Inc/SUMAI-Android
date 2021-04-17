package co.kr.sumai

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import co.kr.sumai.func.deletePreferences
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.SummaryRequest
import co.kr.sumai.net.SummaryResponse
import co.kr.sumai.net.service
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.user.UserApiClient
import com.nhn.android.naverlogin.OAuthLogin
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.android.synthetic.main.toolbar_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, PopupMenu.OnMenuItemClickListener {
    var drawerToggle: ActionBarDrawerToggle? = null
    var toolbar: Toolbar? = null
    private var mInterstitialAd // 애드몹 전면 광고
            : InterstitialAd? = null
    private var adMobBannerID: String? = null
    private var adMobInterstitialID: String? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    var ID: String = ""
    private val record = 1
    private var summaryRequestCount = 0
    private var accountInformation: AccountInformation? = null

    //뒤로 버튼 두번 연속 클릭 시 종료
    private var time: Long = 0
    override fun onBackPressed() {
        if (dl_main_drawer_root.isDrawerOpen(GravityCompat.START)) {
            dl_main_drawer_root.closeDrawer(GravityCompat.START)
        } else {
            if (System.currentTimeMillis() - time >= 2000) {
                time = System.currentTimeMillis()
                Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
            } else if (System.currentTimeMillis() - time < 2000) {
                this@MainActivity.finish()
            }
        }
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initLayout()
        clickEvent()

        ID = loadPreferences(applicationContext, "loginData", "id")
        if (ID.isNotEmpty()) {
            service.loadAccount(ID).enqueue(object : Callback<AccountInformation> {
                override fun onResponse(call: Call<AccountInformation>, response: Response<AccountInformation>) {
                    if (response.isSuccessful) {
                        accountInformation = response.body()

                        setAvatar()
                    }
                }

                override fun onFailure(call: Call<AccountInformation>, t: Throwable) {
                    Toast.makeText(applicationContext, "로그인 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    layoutLogin.visibility = View.VISIBLE
                }
            })
        } else {
            layoutLogin.visibility = View.VISIBLE
        }

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
        imageButtonSummary.setOnClickListener(View.OnClickListener {
            if (0 < editTextSummary.getText().toString().length) {
                summaryRequest(editTextSummary.getText().toString())
            }
            else {
                Toast.makeText(applicationContext, "요약할 텍스트를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        })
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

    lateinit var layoutLogin: LinearLayout
    lateinit var layoutAccount: FrameLayout

    private fun clickEvent() {
        // toolbar
        val buttonNews: ImageButton = findViewById<ImageButton>(R.id.buttonNews)
        buttonNews.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://news.sumai.co.kr"))
            startActivity(intent)
        })

        layoutLogin = findViewById<LinearLayout>(R.id.layoutLogin)
        layoutLogin.visibility = View.GONE
        layoutLogin.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        })

        layoutAccount = findViewById<FrameLayout>(R.id.layoutAccount)
        layoutAccount.visibility = View.GONE
        layoutAccount.setOnClickListener {
            val popup = PopupMenu(this, layoutAccount)
            popup.setOnMenuItemClickListener(this@MainActivity)
            popup.menuInflater.inflate(R.menu.account_menu, popup.menu)
            popup.show()
        }
    }

    private fun setAvatar() {
        layoutAccount.visibility = View.VISIBLE

        if (accountInformation!!.image.isNotEmpty()) {  // 프로필 이미지 있으면
            Glide.with(this)
                    .load(accountInformation!!.image)
                    .circleCrop()
                    .into(imageViewAccount)
        } else {  // 프로필 이미지 없으면
            val drawable = ContextCompat.getDrawable(this, R.drawable.circle) as GradientDrawable?
            drawable!!.setColor(Color.parseColor("#" + ID.toMD5().substring(1, 7)))
            imageViewAccount.setImageDrawable(drawable)

            Glide.with(this)
                    .load(drawable)
                    .circleCrop()
                    .into(imageViewAccount)

            textViewName.text = reName(accountInformation!!.name)
        }
    }

    fun String.toMD5(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return bytes.toHex()
    }
    fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    fun reName(name: String): String {
        var reName: String = ""

        val pattern = Regex(pattern = "[a-zA-Z0-9]")

        if(pattern.matches(name.first().toString())) {
            reName = name.first().toString()
        } else if(3 <= name.length) {
            if(pattern.matches(name.substring(name.length - 2, name.length))) {
                reName = name.first().toString()
            } else {
                reName = name.substring(name.length - 2, name.length)
            }
        } else {
            reName = name
        }

        return reName
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

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.accountManage -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sumai.co.kr/login?url=https://sumai.co.kr/accounts")))
                true
            }
            R.id.logout -> {
                val SNSType = accountInformation?.type
                val mOAuthLoginModule = OAuthLogin.getInstance()
                mOAuthLoginModule.init(this, getString(R.string.naver_client_id), getString(R.string.naver_client_secret), getString(R.string.app_name))
                when (SNSType) {
                    "GOOGLE" -> Firebase.auth.signOut()
                    "KAKAO" -> UserApiClient.instance.logout {}
                    "NAVER" -> mOAuthLoginModule.logout(this);
                    "FACEBOOK" -> LoginManager.getInstance().logOut()
                }
                accountInformation = null
                deletePreferences(applicationContext, "loginData", "id")
                finish()
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
                true
            }
            else -> false
        }
    }

}