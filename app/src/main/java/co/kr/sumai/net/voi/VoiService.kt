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
    fun getModelList(@Body voiceModelRequest: VoiceModelRequest): Call<VoiceModelResponse>

    @POST("/api/voi/voice_model_delete")
    fun getModelDelete(@Body modelDeleteRequest: ModelDeleteRequest): Call<ModelDeleteResponse>

    @POST("/api/voi/voice_model_delete_cancel")
    fun getModelDeleteCancel(@Body modelDeleteRequest: ModelDeleteRequest): Call<ModelDeleteResponse>
}