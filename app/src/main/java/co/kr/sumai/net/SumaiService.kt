package co.kr.sumai.net

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SumaiService {
    @POST("/api/account/login")
    fun postLogin(@Body loginRequest: LoginRequest?): Call<LoginResponse?>?

    @get:GET("/api/account/getinfo")
    val info: Call<LoginInforResponse?>?

    @POST("/api/summary/request")
    fun getSummary(@Body summaryRequest: SummaryRequest?): Call<SummaryResponse?>?

    @POST("/api/sumaiMobile/account")
    fun getLoginState(@Body SNSLoginRequest: SNSLoginRequest?): Call<SNSLoginResponse?>?
}