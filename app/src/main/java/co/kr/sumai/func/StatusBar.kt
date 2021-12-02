package co.kr.sumai.func

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class StatusBar(activity: AppCompatActivity) {
    val activity = activity
    val window = this.activity.window

    fun remove() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    fun transparent() {
        if (Build.VERSION.SDK_INT in 21..29) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = Color.TRANSPARENT
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_LAYOUT_STABLE

        } else if (Build.VERSION.SDK_INT >= 30) {
            window.statusBarColor = Color.TRANSPARENT
            // Making status bar overlaps with the activity
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }
}