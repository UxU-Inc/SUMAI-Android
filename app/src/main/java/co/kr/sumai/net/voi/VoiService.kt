package co.kr.sumai.net.voi

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface VoiService {
    @POST("/api/voi/voice_model_full_list")
    fun getModelFullList(): Call<AllVoiceModelResponse>

    @POST("/api/voi/voice_request")
    fun requestTTS(@Body textToSpeechRequest: TTSRequest): Call<TTSResponse>

    @POST("/api/voi/voice_model_list")
    fun getModelList(): Call<VoiceModelResponse>
}