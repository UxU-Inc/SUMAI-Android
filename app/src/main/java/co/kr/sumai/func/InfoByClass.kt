package co.kr.sumai.func

import android.content.Context
import co.kr.sumai.R

class InfoByClass {
    fun getTheme(className: String): Int {
        return when(className) {
            "VoiMainActivity" -> R.style.AppVoiTheme
            "CaiiMainActivity" -> R.style.AppCaiiTheme
            else -> R.style.AppTheme
        }
    }

    fun getUrl(context: Context, className: String): String {
        return when(className) {
            "VoiMainActivity" -> context.getString(R.string.voi_url)
            "CaiiMainActivity" -> context.getString(R.string.caii_url)
            else -> context.getString(R.string.sumai_url)
        }
    }
}