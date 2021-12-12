package co.kr.sumai.net.voi

data class RecordListLatestRequest(
    val idForMobile: String,
    val model_idx: String?,
    val limitNumber: Int
)