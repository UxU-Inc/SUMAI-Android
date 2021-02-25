package co.kr.sumai

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class KakaoGlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 다른 초기화 코드들

        // Kakao SDK 초기화
        KakaoSdk.init(this, "afe8ceccaf28e3cdb8e388a500246c61")
    }
}