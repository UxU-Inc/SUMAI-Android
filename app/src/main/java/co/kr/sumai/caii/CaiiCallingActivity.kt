package co.kr.sumai.caii

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityCaiiCallingBinding
import co.kr.sumai.func.StatusBar
import co.kr.sumai.net.service
import co.kr.sumai.net.caii.CaiiService
import co.kr.sumai.net.caii.ConversationRequest
import co.kr.sumai.net.voi.TTSResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer
import android.util.Base64
import android.widget.Toast
import co.kr.sumai.ServiceListActivity
import co.kr.sumai.func.InfoByClass
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.caiiService

class CaiiCallingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCaiiCallingBinding

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var amanager: AudioManager
    private var mediaPlayer: MediaPlayer? = null

    private var userID = ""
    private var session_name = ""

    override fun onBackPressed() {  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaiiCallingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userID = loadPreferences(applicationContext, "loginData", "id")
        session_name = getRandomString()
        amanager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        StatusBar(this).transparent()
        backgroundSetting()
        callTimeStart()
        buttonEvent()
        STT()
    }

    private fun backgroundSetting() {
        binding.videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.call_receiving_backgrond))
        binding.videoView.start()
    }

    private fun getRandomString(): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomString = (1..20)
          .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
          .map(charPool::get)
          .joinToString("");

        return randomString
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

    private fun buttonEvent() {
        binding.endButton.setOnClickListener {
            val intent = Intent(this, ServiceListActivity::class.java)
            intent.putExtra("caller", "CaiiMainActivity")
            intent.putExtra("theme", InfoByClass().getTheme("CaiiMainActivity"))
            startActivity(intent)
            finish()
        }
    }

    private fun STT() {
        mute()

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(recognitionListener())
            startListening(speechRecognizerIntent)
        }
    }

    private fun mute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            amanager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
        } else {
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        }
    }

    private fun unMute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            amanager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
        } else {
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        }
    }

    private fun recognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.e("onReadyForSpeech", "onReadyForSpeech")
        }
        override fun onBeginningOfSpeech() {
            Log.e("onBeginningOfSpeech", "onBeginningOfSpeech")
        }
        override fun onRmsChanged(rmsdB: Float) {  }
        override fun onBufferReceived(buffer: ByteArray?) { Log.e("onBufferReceived", "onBufferReceived") }
        override fun onEndOfSpeech() { Log.e("onEndOfSpeech", "onEndOfSpeech") }
        override fun onError(error: Int) {
            var message: String
            when (error) {
                SpeechRecognizer.ERROR_AUDIO -> message = "ERROR_AUDIO"
                SpeechRecognizer.ERROR_CLIENT -> message = "ERROR_CLIENT"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "ERROR_INSUFFICIENT_PERMISSIONS"
                SpeechRecognizer.ERROR_NETWORK -> message = "ERROR_NETWORK"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> message = "ERROR_NETWORK_TIMEOUT"
                SpeechRecognizer.ERROR_NO_MATCH -> {
                    message = "ERROR_NO_MATCH"
                    STT()
                }
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "ERROR_RECOGNIZER_BUSY"
                SpeechRecognizer.ERROR_SERVER -> message = "ERROR_SERVER"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "ERROR_SPEECH_TIMEOUT" else -> message = "ERROR"
            }
            Log.e("error", message + error.toString())
        }
        override fun onResults(results: Bundle?) {
            val matches: ArrayList<String> = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>

            for (i in 0 until matches.size) {
                Log.e("result", matches[i])

                reqConv(matches[i]) { url ->
                    if (mediaPlayer != null) {
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }

                    mediaPlayer = MediaPlayer().apply {
                        unMute()
                        setDataSource(url)
                        prepare()
                        start()
                    }

                    mediaPlayer!!.setOnCompletionListener {
                        STT()
                    }

                }
            }

        }
        override fun onPartialResults(partialResults: Bundle?) { Log.e("onPartialResults", "onPartialResults") }
        override fun onEvent(eventType: Int, params: Bundle?) { Log.e("onEvent", "onEvent") }
    }

    private fun reqConv(conversation: String, execute: (String) -> Unit) {
        val res: Call<TTSResponse> = caiiService.requestConversation(ConversationRequest(userID, session_name, conversation))
        res.enqueue(object : Callback<TTSResponse> {
            override fun onResponse(call: Call<TTSResponse>, response: Response<TTSResponse>) {
                if (response.isSuccessful) {
                    val base64 = Base64.encodeToString(response.body()?.buffer?.data?.toByteArray(), Base64.DEFAULT)
                    val url = "data:audio/wav;base64,$base64"
                    execute(url)
                } else {
                    Toast.makeText(applicationContext, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TTSResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        backgroundSetting()
        STT()
    }

    override fun onPause() {
        super.onPause()
        unMute()
    }

    override fun onStop() {
        super.onStop()
        unMute()
    }

    override fun onDestroy() {
        super.onDestroy()
        unMute()
    }
}