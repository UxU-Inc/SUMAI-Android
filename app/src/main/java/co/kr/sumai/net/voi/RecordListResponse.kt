package co.kr.sumai.net.voi

data class RecordListResponse(
    val record_list: MutableList<Record>,
    val model_delete_state: Boolean
)