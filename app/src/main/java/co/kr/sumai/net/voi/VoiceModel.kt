package co.kr.sumai.net.voi

data class VoiceModel (
    val idx: Int,
    val model_idx: String,
    val model_name: String,
    val model_image: String?,
    val user_image: String?,
    val model_delete_time: String?,
    val model_delete_date: String?,
    val server_current_time: String,
)