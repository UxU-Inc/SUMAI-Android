package co.kr.sumai.net

import android.content.Context
import co.kr.sumai.net.voi.VoiService
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
}

val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(interceptor) // Turn off if you don't want Log
        .build()


val retrofit = Retrofit.Builder()
        .baseUrl("https://www.sumai.co.kr")
        .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
        .client(okHttpClient)
        .build()

val service = retrofit.create(SumaiService::class.java)
val voiService = retrofit.create(VoiService::class.java)