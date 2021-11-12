package co.kr.sumai

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import co.kr.sumai.func.AdmobSettings
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.SummaryRequest
import co.kr.sumai.net.SummaryResponse
import co.kr.sumai.net.service
import com.google.android.gms.ads.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.android.synthetic.main.toolbar_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var drawerToggle: ActionBarDrawerToggle? = null
    private lateinit var toolbar: CustomToolbar
    lateinit var admob: AdmobSettings
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    var ID: String = ""
    private val record = 1
    private var summaryRequestCount = 0
    private var summaryRequestFirst = true

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

        ID = loadPreferences(applicationContext, "loginData", "id")
        toolbar.init(ID)

        // Firebase
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // AdMob
        admob = AdmobSettings(this)
        admob.loadBanner(ad_view_container)
    }

    private fun initLayout() {
        // toolbar, drawer, navigation Component
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
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

    private fun summaryRequest(data: String) {
        layoutLoading.setVisibility(View.VISIBLE)
        layoutLoading.setClickable(true)
        if (admob.mInterstitialAd != null && (summaryRequestFirst || 4 <= summaryRequestCount)) {
            admob.mInterstitialAd!!.show(this)
        }
        admob.loadInterstitial { summaryRequestCount = 0 }
        val res: Call<SummaryResponse> = service.getSummary(SummaryRequest(data, ID, record))
        res.enqueue(object : Callback<SummaryResponse> {
            override fun onResponse(call: Call<SummaryResponse>, response: Response<SummaryResponse>) {
                textViewSummaryResult.setText(response.body()?.summarize)
                textViewSummaryResult.setVisibility(View.VISIBLE)
                layoutLoading.setVisibility(View.INVISIBLE)
                layoutLoading.setClickable(false)
                summaryRequestFirst = false
                summaryRequestCount++
            }

            override fun onFailure(call: Call<SummaryResponse>, t: Throwable) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                layoutLoading.setVisibility(View.INVISIBLE)
                layoutLoading.setClickable(false)
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