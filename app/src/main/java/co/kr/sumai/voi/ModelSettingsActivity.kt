package co.kr.sumai.voi

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityModelSettingsBinding
import co.kr.sumai.func.AdmobSettings
import co.kr.sumai.func.AvatarSettings
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.voi.ModelInfoRequest
import co.kr.sumai.net.voi.ModelInfoResponse
import co.kr.sumai.net.voiService
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModelSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModelSettingsBinding

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var admob: AdmobSettings

    private val avatar = AvatarSettings()

    private var userID = ""
    private var userName = ""

    private var modelIdx: String? = null
    private var isModelExist: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModelSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userID = loadPreferences(applicationContext, "loginData", "id")
        userName = loadPreferences(applicationContext, "loginData", "name")

        modelIdx = intent.getStringExtra("modelIdx")

        initHeader()
        initLayout()

        requestModelInfo()

        // Firebase
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // AdMob
        admob = AdmobSettings(this)
        admob.loadBanner(binding.adViewContainer)
    }

    private fun initHeader() {
        // toolbar Component
        setSupportActionBar(binding.toolbar)
        binding.toolbar.init(userID)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
    }

    private fun initLayout() {
        binding.settingsLayout.setOnClickListener { hideKeyboard() }
        binding.modelName.editText?.addTextChangedListener {
            if(binding.modelName.isErrorEnabled) binding.modelName.isErrorEnabled = false
        }
        binding.btnCreateNSave.setOnClickListener {
            if (checkModelName()) {
                hideKeyboard()
            } else {
                binding.modelName.isErrorEnabled = true
                binding.modelName.error = "사용할 수 없는 음성 모델 이름 입니다."
                binding.modelName.editText?.setSelection(binding.modelName.editText?.length()!!)
            }
        }
        binding.recyclerView.adapter = VoiceRecordRecyclerViewAdapter(this, mutableListOf("", "", ""))
    }

    private fun checkModelName(): Boolean {
        val name = binding.modelName.editText?.text.toString()
        val regex = "^[가-힣a-zA-Z0-9 ]{2,10}$".toRegex()
        binding.modelName.editText?.setText(name.replace(" ", ""))
        return name.matches(regex)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        binding.modelName.editText?.clearFocus()
    }

    private fun requestModelInfo() {
        val res: Call<ModelInfoResponse> = voiService.getModelInfo(ModelInfoRequest(userID, modelIdx))
        res.enqueue(object : Callback<ModelInfoResponse> {
            override fun onResponse(call: Call<ModelInfoResponse>, response: Response<ModelInfoResponse>) {
                if (response.isSuccessful) {
                    if (response.body()?.model_delete_state == true) finish()
                    setModelImageAndName(response.body()!!)
                } else {
                    Toast.makeText(applicationContext, "모델 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
                binding.layoutLoading.visibility = View.INVISIBLE
            }

            override fun onFailure(call: Call<ModelInfoResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "모델 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                binding.layoutLoading.visibility = View.INVISIBLE
            }
        })
    }

    private fun setModelImageAndName(modelInfo: ModelInfoResponse) {
        if (modelInfo.model_image_url.isNullOrEmpty() && modelInfo.user_image_url.isNullOrEmpty()) {  // 모델, 프로필 이미지 없으면
            val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.circle)?.mutate() as GradientDrawable
            drawable.setColor(Color.parseColor("#" + avatar.toMD5(userID).substring(1, 7)))

            Glide.with(applicationContext)
                .load(drawable)
                .circleCrop()
                .into(binding.modelImage)

            binding.modelOwner.text = avatar.reName(userName)
        } else {  // 모델, 프로필 이미지 있으면
            Glide.with(applicationContext)
                .load(modelInfo.model_image_url ?: modelInfo.user_image_url)
                .circleCrop()
                .into(binding.modelImage)
        }
        binding.modelName.editText?.setText(modelInfo.model_name)

        if(!modelInfo.model_name.isNullOrEmpty()) binding.btnCreateNSave.text = "저장"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}