package co.kr.sumai.caii

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityCaiiCallingBinding
import co.kr.sumai.func.StatusBar
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

class CaiiCallingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCaiiCallingBinding

    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaiiCallingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        StatusBar(this).transparent()
        backgroundSetting()
        callTimeStart()
        STT()
    }

    fun backgroundSetting() {
        binding.videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.call_receiving_backgrond))
        binding.videoView.start()
    }

    private var startTime = System.currentTimeMillis()
    private var timerTask: Timer? = null
    fun callTimeStart() {
        var elapsedTime: Long = 0
        var hour: Long; var min: Long; var sec: Long;
        val df = DecimalFormat("00")

        timerTask = timer(period = 1000) {
            elapsedTime = (System.currentTimeMillis() - startTime) / 1000

            hour = elapsedTime / 3600
            min = (elapsedTime % 3600) / 60
            sec = elapsedTime % 60

            runOnUiThread {
                if (0 < hour) binding.callTime.text = "${df.format(hour)}:${df.format(min)}:${df.format(sec)}"
                else binding.callTime.text = "${df.format(min)}:${df.format(sec)}"
            }
        }
    }

    private fun STT() {
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(recognitionListener())
            startListening(speechRecognizerIntent)
        }
    }

    private fun recognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.e("onReadyForSpeech", "onReadyForSpeech")
        }
        override fun onBeginningOfSpeech() { }
        override fun onRmsChanged(rmsdB: Float) { }
        override fun onBufferReceived(buffer: ByteArray?) { }
        override fun onEndOfSpeech() { }
        override fun onError(error: Int) {
            var message: String
            when (error) {
                SpeechRecognizer.ERROR_AUDIO -> message = "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> message = "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> message = "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> message = "네트워크 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> message = "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "RECOGNIZER가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> message = "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "말하는 시간초과" else -> message = "알 수 없는 오류"
            }
            Log.e("error", message + error.toString())
        }
        override fun onResults(results: Bundle?) {
            var matches: ArrayList<String> = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>

            for (i in 0 until matches.size) {
                Log.e("result", matches[i])
            }
        }
        override fun onPartialResults(partialResults: Bundle?) { }
        override fun onEvent(eventType: Int, params: Bundle?) { }
    }
}