package co.kr.sumai

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import co.kr.sumai.databinding.ActivityServiceListBinding
import co.kr.sumai.func.AdmobSettings
import co.kr.sumai.voi.VoiMainActivity

class ServiceListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServiceListBinding
    private lateinit var admob: AdmobSettings
    lateinit var caller: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        caller = intent.getStringExtra("caller")!!
        admob = AdmobSettings(this)
        admob.loadBanner(binding.adViewContainer)

        initBtn()
    }

    private fun initBtn() {
        binding.sumaiBtn.setOnClickListener {
            if(caller == "MainActivity") finish()
            else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }
        binding.sumaiNewsBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.news_url)))
            startActivity(intent)
            finish()
        }
        binding.voiBtn.setOnClickListener {
            if(caller == "VoiMainActivity") finish()
            else {
                val intent = Intent(this, VoiMainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }
        binding.caiiBtn.setOnClickListener {

        }
    }
}