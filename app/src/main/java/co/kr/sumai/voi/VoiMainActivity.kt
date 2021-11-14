package co.kr.sumai.voi

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import co.kr.sumai.LoginActivity
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityVoiMainBinding
import co.kr.sumai.func.AdmobSettings
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.voi.TTSRequest
import co.kr.sumai.net.voi.TTSResponse
import co.kr.sumai.net.voi.VoiceModel
import co.kr.sumai.net.voi.VoiceModelResponse
import co.kr.sumai.net.voiService
import com.google.firebase.analytics.FirebaseAnalytics
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.FileOutputStream
import java.util.*

class VoiMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var admob: AdmobSettings

    private var userID = ""
    private var modelIdx = ""
    private var modelList = mutableListOf<VoiceModel>()

    private var mediaPlayer: MediaPlayer? = null
    private var byteArray: ByteArray? = null
    private var isSamePrevious = false
    private var isPlaying = false

    private var voiceRequestCount = 0

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
        admob.loadBanner(binding.content.adViewContainer)
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
            textViewLimitGuide.visibility = View.INVISIBLE
            layoutLoading.visibility = View.INVISIBLE

            btnClear.setOnClickListener {
                isSamePrevious = false
                editTextVoice.setText("")
                btnClear.visibility = View.INVISIBLE
                if (textViewLimitGuide.isVisible)
                    textViewLimitGuide.visibility = View.INVISIBLE
            }

            editTextVoice.addTextChangedListener(textWatcher)

            btnPlay.setOnClickListener { playVoice() }
            btnDown.setOnClickListener { playVoice(down = true) }
            btnCreate.setOnClickListener {
                if (userID.isBlank()) {
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    intent.putExtra("caller", this@VoiMainActivity.javaClass.simpleName)
                    startActivity(intent)
                } else {
                    val intent = Intent(applicationContext, CreateModelActivity::class.java)
                    startActivity(intent)
                }
            }

            recyclerView.adapter = ModelRecyclerViewAdapter(applicationContext, modelList) {
                modelIdx = it
                isSamePrevious = false
            }
        }
    }

    private val textWatcher = object : TextWatcher {
        // 입력 변화 시
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            with(binding.content) {
                if(isSamePrevious) isSamePrevious = false

                if (editTextVoice.text.isNotBlank()) {
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
        }

        // 입력 끝났을 때
        override fun afterTextChanged(arg0: Editable) { }

        // 입력하기 전
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
    }

    private fun playVoice(down: Boolean = false) {
        val script = binding.content.editTextVoice.text.toString()

        if (script.isBlank()) return
        if (isSamePrevious) {
            when {
                isPlaying -> {
                    switchPlaying(false)
                    mediaPlayer?.pause()
                }
                down -> createFile()
                else -> {
                    switchPlaying(true)
                    mediaPlayer?.start()
                }
            }
            return
        }

        requestVoice(script) { url ->
            if (mediaPlayer != null) {
                mediaPlayer?.release()
                mediaPlayer = null
            }

            mediaPlayer = MediaPlayer().apply {
                isSamePrevious = true
                setDataSource(url)
                prepare()
                if (!down) {
                    switchPlaying(true)
                    start()
                }
                setOnCompletionListener {
                    switchPlaying(false)
                }
            }

            if (down) createFile()
        }
    }

    private val getDownloadResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val uri = result.data?.data!!
                contentResolver.openFileDescriptor(uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { file ->
                        file.write(byteArray)
                    }
                }
            } catch (e: Exception){
                Toast.makeText(applicationContext, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/wav"
            putExtra(Intent.EXTRA_TITLE, "VOI-${Date().time}.wav")
        }
        getDownloadResult.launch(intent)
    }

    private fun requestModelList() {
        val res: Call<VoiceModelResponse> = voiService.getModelList()
        res.enqueue(object : Callback<VoiceModelResponse> {
            override fun onResponse(call: Call<VoiceModelResponse>, response: Response<VoiceModelResponse>) {
                modelList.addAll(response.body()?.model_list!!)
                binding.content.recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<VoiceModelResponse>, t: Throwable) {
            }
        })
    }

    private fun requestVoice(script: String, execute: (String) -> Unit) {
        switchLoading(true)

        if (admob.mInterstitialAd != null && 4 <= voiceRequestCount) {
            admob.mInterstitialAd!!.show(this)
        }
        admob.loadInterstitial { voiceRequestCount = 0 }
        val res: Call<TTSResponse> = voiService.requestTTS(TTSRequest(modelIdx, script))
        res.enqueue(object : Callback<TTSResponse> {
            override fun onResponse(call: Call<TTSResponse>, response: Response<TTSResponse>) {
                if (response.isSuccessful) {
                    byteArray = response.body()?.buffer?.data?.toByteArray()
                    val base64 = Base64.encodeToString(response.body()?.buffer?.data?.toByteArray(), Base64.DEFAULT)
                    val url = "data:audio/wav;base64,$base64"

                    execute(url)

                    voiceRequestCount++
                    switchLoading(false)
                } else {
                    Toast.makeText(applicationContext, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    switchLoading(false)
                }
            }

            override fun onFailure(call: Call<TTSResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                switchLoading(false)
            }
        })
    }

    private fun switchPlaying(toggle: Boolean) {
        if(toggle) {
            binding.content.playImage.setImageResource(R.drawable.ic_baseline_stop_24)
            binding.content.playText.text = "정지하기"
            isPlaying = true
        } else {
            binding.content.playImage.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            binding.content.playText.text = "들어보기"
            isPlaying = false
        }
    }

    private fun switchLoading(toggle: Boolean) {
        binding.content.layoutLoading.visibility = if (toggle) View.VISIBLE else View.INVISIBLE
        binding.content.layoutLoading.isClickable = toggle
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