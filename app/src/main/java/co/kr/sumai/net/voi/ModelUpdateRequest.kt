package co.kr.sumai.net.voi

import okhttp3.MultipartBody

data class ModelUpdateRequest(
    val idForMobile: String,
    val modelIdx: String?,
    val modelName: String,
    val img: MultipartBody?
)