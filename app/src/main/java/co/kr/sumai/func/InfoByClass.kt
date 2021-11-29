package co.kr.sumai.func

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import co.kr.sumai.MainActivity
import co.kr.sumai.R
import co.kr.sumai.caii.CaiiMainActivity
import co.kr.sumai.voi.VoiMainActivity

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

    fun getIntentByLogo(context: Context, logoText: String): Intent {
        return when(logoText) {
            "보이스" -> Intent(context, VoiMainActivity::class.java)
            "영어 콜봇" -> Intent(context, CaiiMainActivity::class.java)
            else -> Intent(context, MainActivity::class.java)
        }
    }

    fun getIntent(context: Context, className: String): Intent {
        return when(className) {
            "VoiMainActivity" -> Intent(context, VoiMainActivity::class.java)
            "CaiiMainActivity" -> Intent(context, CaiiMainActivity::class.java)
            else -> Intent(context, MainActivity::class.java)
        }
    }
}