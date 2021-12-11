package co.kr.sumai.voi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityModelSettingsBinding
import co.kr.sumai.databinding.DialogVoiDeleteBinding
import co.kr.sumai.func.AdmobSettings
import co.kr.sumai.func.AvatarSettings
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.voi.*
import co.kr.sumai.net.voiService
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ModelSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModelSettingsBinding

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var admob: AdmobSettings

    private val avatar = AvatarSettings()

    private var userID = ""
    private var userName = ""

    private var modelIdx: String? = null
    private var curModelName: String? = ""
    private var requestState: String? = null

    private var curPhotoPath: String? = null

    private var isRequestLoading = false

    private val galleryStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            saveImageFile(intent?.data!!)
        }
    }

    private val pickImage = Intent.createChooser(Intent(Intent.ACTION_PICK).apply {
        type = "image/*"
        action = Intent.ACTION_GET_CONTENT
    }, "")

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
        requestModelState()

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
        binding.modelImage.setOnClickListener {
            createImageFile()
            galleryStartForResult.launch(pickImage)
        }
        binding.modelName.editText?.addTextChangedListener {
            if(binding.modelName.isErrorEnabled) binding.modelName.isErrorEnabled = false
        }
        binding.btnCreateNSave.setOnClickListener {
            if (checkModelName()) {
                hideKeyboard()
                requestModelUpdate(binding.modelName.editText?.text.toString())
            } else {
                binding.modelName.isErrorEnabled = true
                binding.modelName.error = "사용할 수 없는 음성 모델 이름 입니다."
                binding.modelName.editText?.setSelection(binding.modelName.editText?.length()!!)
            }
        }
        binding.btnModelCreate.setOnClickListener {
            hideKeyboard()
            if (curModelName == null) {
                Toast.makeText(applicationContext, "음성 모델 생성을 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            trainingDialog(curModelName!!, !requestState.isNullOrEmpty())
        }
        binding.btnRecord.setOnClickListener {
            hideKeyboard()
            if (curModelName == null) {
                Toast.makeText(applicationContext, "음성 모델 생성을 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(applicationContext, SpeechRecordActivity::class.java)
            startActivity(intent)
        }
        binding.recyclerView.adapter = VoiceRecordRecyclerViewAdapter(this, mutableListOf("", "", ""))
    }

    private fun createImageFile() {
        val fileList = filesDir.listFiles { _, name -> name.endsWith(".jpg") }
        fileList?.forEach { it.delete() }
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
        val storageDir: File? = filesDir
        File.createTempFile("JPEG_${timestamp}_",".jpg", storageDir)
            .apply { curPhotoPath = absolutePath }
    }

    private fun saveImageFile(uri: Uri) {
        binding.modelImage.setImageURI(uri)

        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        val out = FileOutputStream(curPhotoPath)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        bitmap.recycle()
    }

    private fun checkModelName(): Boolean {
        val name = binding.modelName.editText?.text.toString()
        val regex = "^[가-힣a-zA-Z0-9 ]{2,10}$".toRegex()
        binding.modelName.editText?.setText(name.replace(" ", ""))
        return name.matches(regex)
    }

    private fun trainingDialog(modelName: String, cancel: Boolean) {
        val dialogBinding = DialogVoiDeleteBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.title.text = if (cancel) "해당 모델의 음성 학습 요청을 취소하시겠습니까?" else "해당 모델의 학습을 요청하시겠습니까?"
        dialogBinding.message.text = modelName
        dialogBinding.negativeBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.positiveBtn.setOnClickListener {
            if (isRequestLoading) return@setOnClickListener

            if (cancel) requestModelTrainingCancel {
                requestModelState()
                dialog.dismiss()
            }
            else requestModelTraining {
                requestModelState()
                dialog.dismiss()
            }
        }

        dialog.show()
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
        curModelName = modelInfo.model_name
        binding.modelName.editText?.setText(modelInfo.model_name)

        if(!modelInfo.model_name.isNullOrEmpty()) {
            binding.btnCreateNSave.text = "저장"
        }
    }

    private fun requestModelState() {
        val res: Call<ModelStateResponse> = voiService.requestModelState(ModelInfoRequest(userID, modelIdx))
        res.enqueue(object : Callback<ModelStateResponse> {
            override fun onResponse(call: Call<ModelStateResponse>, response: Response<ModelStateResponse>) {
                if (response.isSuccessful) {
                    requestState = response.body()?.requestState
                    binding.btnModelCreate.text = when(requestState) {
                        "waiting" -> "모델 생성 대기중"
                        "training" -> "모델 생성 학습중"
                        else -> "음성합성 모델 생성"
                    }
                } else {
                    Toast.makeText(applicationContext, "모델 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ModelStateResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "모델 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun requestModelUpdate(modelName: String) {
        binding.layoutLoading.visibility = View.VISIBLE
        // Need to modify nodejs server.
//        var partImage: MultipartBody? = null
//        if (curPhotoPath != null) {
//            val imageFile = File(curPhotoPath!!)
//            val reqBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile)
//            partImage = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("img", imageFile.name, reqBody)
//                .build()
//        }

        val request = ModelUpdateRequest(userID, modelIdx, modelName, null)

        val res: Call<ModelUpdateResponse> =
            if (modelIdx != null) voiService.updateModelInfo(request)
            else voiService.createModelInfo(request)
        res.enqueue(object : Callback<ModelUpdateResponse> {
            override fun onResponse(call: Call<ModelUpdateResponse>, response: Response<ModelUpdateResponse>) {
                when {
                    response.isSuccessful -> {
                        if (response.body()?.code == 200) {
                            Toast.makeText(applicationContext, "모델이 저장되었습니다.", Toast.LENGTH_SHORT)
                                .show()
                        } else if (response.body()?.code == 201) {
                            Toast.makeText(applicationContext, "모델이 생성되었습니다.", Toast.LENGTH_SHORT)
                                .show()
                            binding.btnCreateNSave.text = "저장"
                        }
                        curModelName = modelName
                    }
                    response.code() == 412 -> {
                        Toast.makeText(applicationContext, "중복된 모델 이름입니다.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(applicationContext, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                binding.layoutLoading.visibility = View.INVISIBLE
            }

            override fun onFailure(call: Call<ModelUpdateResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                binding.layoutLoading.visibility = View.INVISIBLE
            }
        })
    }

    private fun requestModelTraining(execute: () -> Unit) {
        isRequestLoading = true
        val res: Call<ModelTrainingResponse> = voiService.requestModelTraining(ModelTrainingRequest(userID, modelIdx))
        res.enqueue(object : Callback<ModelTrainingResponse> {
            override fun onResponse(call: Call<ModelTrainingResponse>, response: Response<ModelTrainingResponse>) {
                if (response.isSuccessful) {
                    if (response.body()?.min_record_number != null)
                        Toast.makeText(applicationContext, "녹음 개수가 부족합니다. ${response.body()?.min_record_number}개 이상 녹음해주세요.", Toast.LENGTH_SHORT).show()
                    if (response.body()?.code == 20100)
                        Toast.makeText(applicationContext, "음성합성 모델 생성 신청되었습니다. 모델 학습 완료까지 일주일 이상 소요될 수 있습니다.", Toast.LENGTH_SHORT).show()
                    execute()
                } else {
                    Toast.makeText(applicationContext, "학습 요청 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
                isRequestLoading = false
            }

            override fun onFailure(call: Call<ModelTrainingResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "학습 요청 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                isRequestLoading = false
            }
        })
    }

    private fun requestModelTrainingCancel(execute: () -> Unit) {
        isRequestLoading = true
        val res: Call<Unit> = voiService.requestModelTrainingCancel(ModelTrainingRequest(userID, modelIdx))
        res.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    execute()
                } else {
                    Toast.makeText(applicationContext, "취소 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
                isRequestLoading = false
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(applicationContext, "취소 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                isRequestLoading = false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}