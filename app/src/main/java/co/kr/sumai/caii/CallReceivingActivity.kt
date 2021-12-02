package co.kr.sumai.caii

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import co.kr.sumai.caii.CaiiCallingActivity
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityCallReceivingBinding
import co.kr.sumai.func.StatusBar

class CallReceivingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallReceivingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallReceivingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        StatusBar(this).transparent()
        backgroundSetting()
        soundVibratorSetting()
        callButtonEventSettings()
    }

    private fun backgroundSetting() {
        binding.videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.call_receiving_backgrond))
        binding.videoView.start()
    }

    private fun soundVibratorSetting() {
        val uriRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(this, uriRingtone)
        ringtone.play()

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect.createOneShot(3000, 100)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        vibrator.vibrate(vibrationEffect)
    }

    private fun callButtonEventSettings() {
        binding.callButton.setOnClickListener {
            val intent = Intent(this@CallReceivingActivity, CaiiCallingActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.endButton.setOnClickListener {
            finish()
        }
    }

}