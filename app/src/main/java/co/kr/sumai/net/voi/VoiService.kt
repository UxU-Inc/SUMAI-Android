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
    fun getModelDelete(@Body modelDeleteRequest: ModelInfoRequest): Call<ModelDeleteResponse>

    @POST("/api/voi/voice_model_delete_cancel")
    fun getModelDeleteCancel(@Body modelDeleteRequest: ModelInfoRequest): Call<ModelDeleteResponse>

    @POST("/api/voi/voice_model_load")
    fun getModelInfo(@Body modelInfoRequest: ModelInfoRequest): Call<ModelInfoResponse>

    @POST("/api/voi/voice_model_settings_create")
    fun createModelInfo(@Body modelUpdateRequest: ModelUpdateRequest): Call<ModelUpdateResponse>

    @POST("/api/voi/voice_model_settings_update")
    fun updateModelInfo(@Body modelUpdateRequest: ModelUpdateRequest): Call<ModelUpdateResponse>

    @POST("/api/voi/voice_model_request_state")
    fun requestModelState(@Body modelStateRequest: ModelInfoRequest): Call<ModelStateResponse>

    @POST("/api/voi/voice_model_request")
    fun requestModelTraining(@Body modelTrainingRequest: ModelTrainingRequest): Call<ModelTrainingResponse>

    @POST("/api/voi/voice_model_request_cancel")
    fun requestModelTrainingCancel(@Body modelTrainingRequest: ModelTrainingRequest): Call<Unit>
}