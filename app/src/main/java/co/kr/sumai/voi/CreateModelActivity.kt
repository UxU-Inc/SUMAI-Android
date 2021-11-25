package co.kr.sumai.voi

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityCreateModelBinding
import co.kr.sumai.func.AdmobSettings
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.voi.VoiceModel
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

//        requestModelList()
        modelList.add(VoiceModel(1, "a", "test1", null, null, null, null, ""))
        addPaddingModel()

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
        binding.recyclerView.adapter = CreateModelRecyclerViewAdapter(applicationContext, modelList)

        binding.btnCreate.setOnClickListener {

        }
    }

    private fun requestModelList() {
        val res: Call<VoiceModelResponse> = voiService.getModelList()
        res.enqueue(object : Callback<VoiceModelResponse> {
            override fun onResponse(call: Call<VoiceModelResponse>, response: Response<VoiceModelResponse>) {
//                modelList.addAll(response.body()?.model_list!!)
                Log.e("asdf", response.toString())
            }

            override fun onFailure(call: Call<VoiceModelResponse>, t: Throwable) {
                Log.e("asdf", t.message.toString())
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