package co.kr.sumai.net

import java.io.Serializable

data class SignUpInforRequest(
    var email: String = "",
    var name: String = "",
    var password: String = "",
    var birthday: String? = null,
    var gender: String = ""
) : Serializable