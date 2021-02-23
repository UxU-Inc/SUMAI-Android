package co.kr.sumai.net

class LoginInforResponse {
    private val error: String? = null
    private val code = 0
    private val info: String? = null
    override fun toString(): String {
        return info!!
    }
}