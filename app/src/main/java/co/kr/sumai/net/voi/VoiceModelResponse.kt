package co.kr.sumai.net.voi

data class VoiceModelResponse (
    val model_list: MutableList<VoiceModel>?,
    val code: Int?
)