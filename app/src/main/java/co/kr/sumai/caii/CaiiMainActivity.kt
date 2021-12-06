package co.kr.sumai.caii

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import co.kr.sumai.BuildConfig
import co.kr.sumai.LoginActivity
import co.kr.sumai.R
import co.kr.sumai.ServiceListActivity
import co.kr.sumai.databinding.ActivityCaiiMainBinding
import co.kr.sumai.func.AdmobSettings
import co.kr.sumai.func.InfoByClass
import co.kr.sumai.func.loadPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.FileOutputStream
import java.util.*

class CaiiMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaiiMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var admob: AdmobSettings

    private var userID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaiiMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userID = loadPreferences(applicationContext, "loginData", "id")

        initHeader()

        requestPermission()

        // Firebase
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // AdMob
//        admob = AdmobSettings(this)
//        admob.loadBanner(binding.content.adViewContainer)
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            } else {
                callReceivingStart()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callReceivingStart()
                } else {
                    Toast.makeText(this, "마이크 권한을 허용해주세요.", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, ServiceListActivity::class.java)
                    intent.putExtra("caller", "CaiiMainActivity")
                    intent.putExtra("theme", InfoByClass().getTheme("CaiiMainActivity"))
                    startActivity(intent)
                    finish()

                    val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    settingsIntent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                    startActivity(settingsIntent)
                }
            }
        }
    }

    private fun callReceivingStart() {
        GlobalScope.launch {
//            delay(3000L)
            val intent = Intent(this@CaiiMainActivity, CallReceivingActivity::class.java)
            startActivity(intent)
            finish()
        }
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