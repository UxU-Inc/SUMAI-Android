package co.kr.sumai.func

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

    fun loadPreferences(context: Context, name: String, key: String): String {
        val pref = context.getSharedPreferences(name, AppCompatActivity.MODE_PRIVATE)
        return pref.getString(key, "")!!
    }

    // 값 저장하기
    fun savePreferences(context: Context, name: String, key: String, defValue: String?) {
        val pref = context.getSharedPreferences(name, AppCompatActivity.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(key, defValue)
        editor.commit()
    }

    // 값 삭제하기
    fun deletePreferences(context: Context, name: String, key: String) {
        val pref = context.getSharedPreferences(name, AppCompatActivity.MODE_PRIVATE)
        val editor = pref.edit()
        editor.remove(key)
        editor.commit()
    }