package co.kr.sumai.net

class LoginRequest(private val email: String, private val password: String) {
    override fun toString(): String {
        return email + password
    }
}