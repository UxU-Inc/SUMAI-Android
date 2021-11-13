package co.kr.sumai.func

import java.security.MessageDigest

class AvatarSettings {
    fun toMD5(string: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(string.toByteArray())
        return bytes.toHex()
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    fun reName(name: String): String {
        val pattern = Regex(pattern = "[a-zA-Z0-9]")

        val reName = if(pattern.matches(name.first().toString())) {
            name.first().toString()
        } else if(3 <= name.length) {
            if(pattern.matches(name.substring(name.length - 2, name.length))) {
                name.first().toString()
            } else {
                name.substring(name.length - 2, name.length)
            }
        } else {
            name
        }

        return reName
    }
}