package co.kr.sumai.net.caii

import co.kr.sumai.net.voi.TTSResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CaiiService {
    @POST("/api/caii_en/request/conversation/freeTalking")
    fun requestConversation(@Body conversationRequest: ConversationRequest): Call<TTSResponse>
}