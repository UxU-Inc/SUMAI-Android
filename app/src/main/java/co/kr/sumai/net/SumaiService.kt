package co.kr.sumai.net

import co.kr.sumai.AccountInformation
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SumaiService {
    @POST("/api/account/loginMob")
    fun postLogin(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @get:GET("/api/account/getinfo")
    val info: Call<LoginInforResponse>

    @POST(value = "/api/account/checkSignupEmail")
    fun checkEmail(@Body checkEmailRequest: CheckEmailRequest): Call<CheckEmailResponse>

    @POST("/api/summary/request")
    fun getSummary(@Body summaryRequest: SummaryRequest): Call<SummaryResponse>

    @POST("/api/sumaiMobile/account")
    fun getLoginState(@Body SNSLoginRequest: Unit): Call<SNSLoginResponse>

    @POST("/api/email/sendEmailCertification")
    fun signUp(@Body signUpInforRequest: SignUpInforRequest): Call<Unit>

    @POST("/api/account/accountLoad/{id}")
    fun loadAccount(@Path("id") id: String?): Call<AccountInformation>
}