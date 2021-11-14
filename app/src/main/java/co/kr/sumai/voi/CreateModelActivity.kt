package co.kr.sumai.voi

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityCreateModelBinding
import co.kr.sumai.func.AdmobSettings
import co.kr.sumai.func.loadPreferences
import com.google.firebase.analytics.FirebaseAnalytics

class CreateModelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateModelBinding

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var admob: AdmobSettings

    private var userID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateModelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userID = loadPreferences(applicationContext, "loginData", "id")

        initHeader()

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}