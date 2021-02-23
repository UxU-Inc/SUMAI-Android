package co.kr.sumai.net

class LoginResponse {
    private val error: String? = null
    private val code: String? = null
    private val success: String? = null
    private val id: String? = null
    private val email: String? = null
    private val name: String? = null
    override fun toString(): String {
        return email!!
    }
}