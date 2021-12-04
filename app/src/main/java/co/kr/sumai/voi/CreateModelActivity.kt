package co.kr.sumai.voi

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityCreateModelBinding
import co.kr.sumai.func.AdmobSettings
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.voi.VoiceModel
import co.kr.sumai.net.voi.VoiceModelRequest
import co.kr.sumai.net.voi.VoiceModelResponse
import co.kr.sumai.net.voiService
import com.google.firebase.analytics.FirebaseAnalytics
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateModelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateModelBinding

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var admob: AdmobSettings

    private var userID = ""
    private var modelList = mutableListOf<VoiceModel?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateModelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userID = loadPreferences(applicationContext, "loginData", "id")

        initHeader()
        initLayout()

        requestModelList()

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
        binding.recyclerView.layoutManager = object : GridLayoutManager(applicationContext, 2) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        binding.recyclerView.adapter = CreateModelRecyclerViewAdapter(this, modelList) {
            modelList.clear()
            binding.layoutLoading.visibility = View.VISIBLE
            requestModelList()
        }

        binding.btnCreate.setOnClickListener {
            val intent = Intent(applicationContext, ModelSettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestModelList() {
        val res: Call<VoiceModelResponse> = voiService.getModelList(VoiceModelRequest(userID))
        res.enqueue(object : Callback<VoiceModelResponse> {
            override fun onResponse(call: Call<VoiceModelResponse>, response: Response<VoiceModelResponse>) {
                if (response.isSuccessful) {
                    if (response.body()?.code == null) {
                        modelList.addAll(response.body()?.model_list!!)
                    }
                    addPaddingModel()
                } else {
                    Toast.makeText(applicationContext, "모델 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
                binding.layoutLoading.visibility = View.INVISIBLE
            }

            override fun onFailure(call: Call<VoiceModelResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "모델 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                binding.layoutLoading.visibility = View.INVISIBLE
            }
        })
    }

    private fun addPaddingModel() {
        while (modelList.size < 4) {
            modelList.add(null)
        }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}