package co.kr.sumai.voi

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityVoiMainBinding
import co.kr.sumai.func.AdmobSettings
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.voi.VoiceModel
import co.kr.sumai.net.voi.VoiceModelList
import co.kr.sumai.net.voiService
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main_content.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VoiMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var admob: AdmobSettings

    private var userID = ""
    private var modelIdx = ""
    private var modelList = mutableListOf<VoiceModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userID = loadPreferences(applicationContext, "loginData", "id")

        initHeader()
        initLayout()

        requestModelList()

        // Firebase
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // AdMob
        admob = AdmobSettings(this)
        admob.loadBanner(ad_view_container)
    }

    private fun initHeader() {
        // toolbar, drawer, navigation Component
        setSupportActionBar(binding.content.toolbar)
        binding.content.toolbar.init(userID)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)

        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.dlMainDrawerRoot,
            binding.content.toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        binding.dlMainDrawerRoot.addDrawerListener(drawerToggle)

        binding.nvMainNavigationRoot.init(binding.dlMainDrawerRoot)

    }

    private fun initLayout() {
        with(binding.content) {
            btnClear.visibility = View.INVISIBLE
            btnClear.setOnClickListener {
                editTextVoice.setText("")
                btnClear.visibility = View.INVISIBLE
                if (textViewLimitGuide.isVisible)
                    textViewLimitGuide.visibility = View.INVISIBLE
            }

            btnPlay.setOnClickListener {  }
            btnDown.setOnClickListener {  }
            btnCreate.setOnClickListener {  }

            editTextVoice.addTextChangedListener(object : TextWatcher {
                // 입력 변화 시
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (editTextVoice.text.isNotEmpty()) {
                        if (!btnClear.isVisible)
                            btnClear.visibility = View.VISIBLE

                        if (100 <= editTextVoice.text.length)
                            textViewLimitGuide.visibility = View.VISIBLE
                        else if (textViewLimitGuide.isVisible)
                            textViewLimitGuide.visibility = View.INVISIBLE
                    } else {
                        if (btnClear.isVisible)
                            btnClear.visibility = View.INVISIBLE
                    }
                }

                // 입력 끝났을 때
                override fun afterTextChanged(arg0: Editable) { }

                // 입력하기 전
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            })

            textViewLimitGuide.visibility = View.INVISIBLE
            layoutLoading.visibility = View.INVISIBLE

            recyclerView.adapter = ModelRecyclerViewAdapter(applicationContext, modelList) {
                modelIdx = it
            }
        }
    }

    private fun requestModelList() {
        val res: Call<VoiceModelList> = voiService.getModelList()
        res.enqueue(object : Callback<VoiceModelList> {
            override fun onResponse(call: Call<VoiceModelList>, response: Response<VoiceModelList>) {
                modelList.addAll(response.body()?.model_list!!)
                binding.content.recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<VoiceModelList>, t: Throwable) {
            }
        })
    }

    //뒤로 버튼 두번 연속 클릭 시 종료
    private var time: Long = 0
    override fun onBackPressed() {
        if (binding.dlMainDrawerRoot.isDrawerOpen(GravityCompat.START)) {
            binding.dlMainDrawerRoot.closeDrawer(GravityCompat.START)
        } else {
            if (System.currentTimeMillis() - time >= 2000) {
                time = System.currentTimeMillis()
                Toast.makeText(applicationContext, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
            } else if (System.currentTimeMillis() - time < 2000) {
                this.finish()
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
        drawerToggle.drawerArrowDrawable.color = Color.WHITE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
}