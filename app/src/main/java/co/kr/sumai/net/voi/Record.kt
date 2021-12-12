package co.kr.sumai.net.voi

data class Record(
    val item_idx: Int,
    val idx: Int,
    val sentence: String,
    val recognition_sentence: String,
    val record_file_name: String,
)