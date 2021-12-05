package co.kr.sumai.caii

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import co.kr.sumai.caii.CaiiCallingActivity
import co.kr.sumai.R
import co.kr.sumai.ServiceListActivity
import co.kr.sumai.databinding.ActivityCallReceivingBinding
import co.kr.sumai.func.InfoByClass
import co.kr.sumai.func.StatusBar

class CallReceivingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallReceivingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallReceivingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        StatusBar(this).transparent()
        backgroundSetting()
        soundSetting()
        vibratorSetting()
        callButtonEventSettings()
    }

    private fun backgroundSetting() {
        binding.videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.call_receiving_backgrond))
        binding.videoView.start()
    }

    private lateinit var uriRingtone: Uri
    private lateinit var ringtone: Ringtone
    private fun soundSetting() {
        uriRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(this, uriRingtone)
        ringtone.play()
    }

    private lateinit var vibrator: Vibrator
    private fun vibratorSetting() {
        val arrayTimings = longArrayOf(2000, 1500)
        vibrator = if (Build.VERSION.SDK_INT >= 31) getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as Vibrator
                   else getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(arrayTimings, 0)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(arrayTimings, 0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun callButtonEventSettings() {
        var startX = 0
        var startY = 0

        var endX = 0
        var endY = 0
        binding.callButton.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX =  motionEvent.x.toInt()
                    startY =  motionEvent.y.toInt()
                }

                MotionEvent.ACTION_MOVE -> {
                    endX = motionEvent.x.toInt()
                    endY = motionEvent.y.toInt()
                }

                // 이동 끝내고 조건 맞으면 잠금헤제
                else -> {
                    if( ((endX- startX)*(endX - startX)) + ((endY - startY)*(endY- startY)) >= 80000 ) {
                        val intent = Intent(this@CallReceivingActivity, CaiiCallingActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
            true
        }

        binding.endButton.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX =  motionEvent.x.toInt()
                    startY =  motionEvent.y.toInt()
                }

                MotionEvent.ACTION_MOVE -> {
                    endX = motionEvent.x.toInt()
                    endY = motionEvent.y.toInt()
                }

                // 이동 끝내고 조건 맞으면 잠금헤제
                else -> {
                    if( ((endX- startX)*(endX - startX)) + ((endY - startY)*(endY- startY)) >= 80000 ) {
                        val intent = Intent(this, ServiceListActivity::class.java)
                        intent.putExtra("caller", "CaiiMainActivity")
                        intent.putExtra("theme", InfoByClass().getTheme("CaiiMainActivity"))
                        startActivity(intent)
                        finish()
                    }
                }
            }
            true
        }

        binding.callButton.setOnClickListener {
            val intent = Intent(this@CallReceivingActivity, CaiiCallingActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.endButton.setOnClickListener {
            val intent = Intent(this, ServiceListActivity::class.java)
            intent.putExtra("caller", "CaiiMainActivity")
            intent.putExtra("theme", InfoByClass().getTheme("CaiiMainActivity"))
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        backgroundSetting()
        ringtone.play()
        vibratorSetting()
    }

    override fun onPause() {
        super.onPause()

        ringtone.stop()
        vibrator.cancel()
    }

    override fun onStop() {
        super.onStop()

        ringtone.stop()
        vibrator.cancel()
    }

}