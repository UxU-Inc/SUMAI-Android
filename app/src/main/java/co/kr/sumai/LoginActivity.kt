package co.kr.sumai

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.kr.sumai.func.savePreferences
import co.kr.sumai.net.*
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mOAuthLoginModule : OAuthLogin
    private lateinit var mContext: Context

    private var facebookCallbackManager: CallbackManager? = null

    override fun onStart() {
        super.onStart()
        // 사용자가 이미 로그인 한 경우 기존 Google 로그인 계정을 확인합니다.
        // GoogleSignInAccount는 null이 아닙니다.
        val currentUser = firebaseAuth.currentUser
        // updateUI(currentUser); TODO:
    }

    private fun initTag() {
        // get login infor
        val loginInfor = service.info
        loginInfor.enqueue(object : Callback<LoginInforResponse> {
            override fun onResponse(call: Call<LoginInforResponse>, response: Response<LoginInforResponse>) {
                if (!response.isSuccessful) {
                    try {
                        val jObjectError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(this@LoginActivity, jObjectError.getString("error"), Toast.LENGTH_SHORT).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    return
                }
                response.body().toString()
            }

            override fun onFailure(call: Call<LoginInforResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
            }
        })
        buttonLogin.setOnClickListener(View.OnClickListener {
            val email = editTextEmail.getText().toString()
            val password = editTextPassword.getText().toString()

            // 제한 사항 추가


            // editTextEmail, editTextPassword를 https//sumai.co.kr/api/loginMob 으로 전송
            val getObject = service.postLogin(LoginRequest(email, password))
            getObject!!.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    // 로그인 실패
                    if (!response.isSuccessful) {
                        try {
                            val jObjectError = JSONObject(response.errorBody()!!.string())
                            Toast.makeText(this@LoginActivity, jObjectError.getString("error"), Toast.LENGTH_SHORT).show()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        return
                    }
                    // 로그인 성공
                    savePreferences(applicationContext,"loginData", "id", response.body()?.id)
                    finish()
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }
            })
        })
        buttonFindPassword.setOnClickListener(View.OnClickListener { // FindPasswordActivity로 이동
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sumai.co.kr/login/password/reset")))
        })
        buttonSignup.setOnClickListener(View.OnClickListener { // SignupActivity로 이동
            val intent = Intent(applicationContext, SignUpPage1Activity::class.java)
            intent.putExtra("infor", SignUpInforRequest())
            startActivity(intent)
        })

        // sns login button 초기화
        SNSLoginInit()

        // terms, privacy button 초기화
        buttonTerms.setOnClickListener(View.OnClickListener { startActivity(Intent(applicationContext, GuideActivity::class.java)) })
        buttonPrivacy.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, GuideActivity::class.java)
            intent.putExtra("page", 1)
            startActivity(intent)
        })
    }

    private fun SNSLoginInit() {

        firebaseAuth = Firebase.auth
        buttonLoginGoogle!!.setOnClickListener { googleLogin() }


        buttonLoginKakao!!.setOnClickListener { kakaoLogin() }


        mContext = this
        mOAuthLoginModule = OAuthLogin.getInstance()
        mOAuthLoginModule.init(this, getString(R.string.naver_client_id), getString(R.string.naver_client_secret), getString(R.string.app_name))
        buttonLoginNaver!!.setOnClickListener { mOAuthLoginModule.startOauthLoginActivity(this, mOAuthLoginHandler); }


        buttonLoginFacebook!!.setOnClickListener { btn_facebook_login_dumy!!.performClick() }
        facebookCallbackManager = CallbackManager.Factory.create()
        btn_facebook_login_dumy!!.setReadPermissions(Arrays.asList("public_profile", "email"))
        btn_facebook_login_dumy!!.registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val graphRequest = GraphRequest.newMeRequest(
                        loginResult.accessToken
                ) { `object`, response ->
                    val SNSType = "facebook"
                    val email = getFacebookUserInfo(response, "email")
                    val name = getFacebookUserInfo(response, "name")
                    val id = getFacebookUserInfo(response, "id")
                    val gender = getFacebookUserInfo(response, "gender")
                    val birth = getFacebookUserInfo(response, "birth")
                    val ageRange = getFacebookUserInfo(response, "ageRange")
                    var imageURL: String? = null
                    try {
                        imageURL = response.jsonObject.getJSONObject("picture").getJSONObject("data").getString("url")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    SNSLoginRequestFun(SNSType, email, name, id, gender, birth, ageRange, imageURL)
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email,gender,birthday,age_range,picture")
                graphRequest.parameters = parameters
                graphRequest.executeAsync()
            }

            override fun onCancel() {}
            override fun onError(error: FacebookException) {
                Log.e("LoginErr", error.toString())
            }
        })
    }

    private fun googleLogin() {
        // Google 로그인 구성
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // gso에서 지정한 옵션으로 GoogleSignInClient 빌드
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // GoogleSignInApi.getSignInIntent(...)에서 인텐트를 시작하여 반환된 결과
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google 로그인 성공, Firebase로 인증
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google 로그인에 실패, UI를 적절하게 업데이트
                Toast.makeText(applicationContext, "구글 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // facebook
        facebookCallbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공, 로그인한 사용자 정보로 UI 업데이트
                        val user = firebaseAuth.currentUser
                        val SNSType = "google"
                        val email = user!!.email
                        val name = user.displayName
                        val id = account.id
                        val gender: String = ""
                        val birth: String = ""
                        val ageRange: String = ""
                        val imageURL = user.photoUrl.toString()
                        SNSLoginRequestFun(SNSType, email, name, id, gender, birth, ageRange, imageURL)
                    } else {
                        // 로그인에 실패하면 사용자에게 메시지를 표시
                        Toast.makeText(applicationContext, "구글 로그인 인증에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun kakaoLogin() {
        // 로그인 공통 callback 구성
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
//            if (error != null) {
//                Log.e("카카오", "로그인 실패", error)
//            }
//            else if (token != null) {
//                Log.e("카카오", "로그인 성공 ${token.accessToken}")
//            }
        }
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (LoginClient.instance.isKakaoTalkLoginAvailable(this)) {
            LoginClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            LoginClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("카카오", "사용자 정보 요청 실패", error)
            }
            else if (user != null) {
                val SNSType = "kakao"
                val email = user.kakaoAccount?.email
                val name = user.kakaoAccount?.profile?.nickname
                val id = user.id.toString()
                val gender: String = ""
                val birth: String = ""
                val ageRange: String = ""
                val imageURL = user.kakaoAccount?.profile?.thumbnailImageUrl

                SNSLoginRequestFun(SNSType, email, name, id, gender, birth, ageRange, imageURL)

                Log.e("카카오", "사용자 정보 요청 성공" +
                        "\n회원번호: ${id}" +
                        "\n이메일: ${email}" +
                        "\n닉네임: ${name}" +
                        "\n프로필사진: ${imageURL}")
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private val mOAuthLoginHandler: OAuthLoginHandler = object : OAuthLoginHandler() {
        override fun run(success: Boolean) {
            if (success) {
                val accessToken: String = mOAuthLoginModule.getAccessToken(baseContext)
                val refreshToken: String = mOAuthLoginModule.getRefreshToken(baseContext)
                val expiresAt: Long = mOAuthLoginModule.getExpiresAt(baseContext)
                val tokenType: String = mOAuthLoginModule.getTokenType(baseContext)

                val header = tokenType + " " + accessToken // tokenType 다음에 공백 추가
                val apiURL = "https://openapi.naver.com/v1/nid/me"
                val requestHeaders: MutableMap<String, String> = HashMap()
                requestHeaders["Authorization"] = header
                CoroutineScope(IO).launch {
                    val responseBody: String? = get(apiURL, requestHeaders)
                    val userInfo = JSONObject(responseBody).getJSONObject("response")

                    val SNSType = "NAVER"
                    val email = getNaverUserInfo(userInfo, "email")
                    val name = getNaverUserInfo(userInfo, "nickname")
                    val id = getNaverUserInfo(userInfo, "id")
                    val gender = getNaverUserInfo(userInfo, "gender")
                    val birth = getNaverUserInfo(userInfo, "birthday")
                    val ageRange = getNaverUserInfo(userInfo, "age")
                    val imageURL = getNaverUserInfo(userInfo, "profile_image")

                    SNSLoginRequestFun(SNSType, email, name, id, gender, birth, ageRange, imageURL)
                }

            } else {
                // val errorCode: String = mOAuthLoginModule .getLastErrorCode(mContext).code
                // val errorDesc = mOAuthLoginModule .getLastErrorDesc(mContext)

                Toast.makeText(this@LoginActivity, "네이버 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getNaverUserInfo(userInfo: JSONObject, attribute: String): String? {
        var attr: String? = null
        try {
            attr = userInfo.getString(attribute)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return attr
    }

    private operator fun get(apiUrl: String, requestHeaders: Map<String, String>): String? {
        val con: HttpURLConnection = connect(apiUrl)
        return try {
            con.setRequestMethod("GET")
            for ((key, value) in requestHeaders) {
                con.setRequestProperty(key, value)
            }
            val responseCode: Int = con.getResponseCode()
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                readBody(con.getInputStream())
            } else { // 에러 발생
                readBody(con.getErrorStream())
            }
        } catch (e: IOException) {
            throw RuntimeException("API 요청과 응답 실패", e)
        } finally {
            con.disconnect()
        }
    }

    private fun connect(apiUrl: String): HttpURLConnection {
        return try {
            val url = URL(apiUrl)
            url.openConnection() as HttpURLConnection
        } catch (e: MalformedURLException) {
            throw RuntimeException("API URL이 잘못되었습니다. : $apiUrl", e)
        } catch (e: IOException) {
            throw RuntimeException("연결이 실패했습니다. : $apiUrl", e)
        }
    }

    private fun readBody(body: InputStream): String? {
        val streamReader = InputStreamReader(body)
        try {
            BufferedReader(streamReader).use { lineReader ->
                val responseBody = StringBuilder()
                var line: String?
                while (lineReader.readLine().also { line = it } != null) {
                    responseBody.append(line)
                }
                return responseBody.toString()
            }
        } catch (e: IOException) {
            throw RuntimeException("API 응답을 읽는데 실패했습니다.", e)
        }
    }

    private fun getFacebookUserInfo(response: GraphResponse, attribute: String): String? {
        var attr: String? = null
        try {
            attr = response.jsonObject.getString(attribute)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return attr
    }

    private fun SNSLoginRequestFun(SNSType: String, email: String?, name: String?, id: String?, gender: String?, birth: String?, ageRange: String?, imageURL: String?) {

        var SNSName = ""
        if (SNSType == "GOOGLE") SNSName = "구글"
        else if (SNSType == "KAKAO") SNSName = "카카오"
        else if (SNSType == "NAVER") SNSName = "네이버"
        else if (SNSType == "FACEBOOK") SNSName = "페이스북"
        val finalSNSName = SNSName

        Log.e("Requset", "\n"+SNSType+"\n"+email+"\n"+id+"\n"+gender+"\n"+birth+"\n"+ageRange+"\n"+imageURL)

        val res = service.getLoginState(SNSLoginRequest(SNSType, email, name, id, gender, birth, ageRange, imageURL))
        res.enqueue(object : Callback<SNSLoginResponse> {
            override fun onResponse(call: Call<SNSLoginResponse>, response: Response<SNSLoginResponse>) {
                if (response.body() != null) {
                    if(response.body()!!.complete == 1) {
                        savePreferences(applicationContext, "loginData", "id", id)
                        Toast.makeText(applicationContext, "$finalSNSName 로그인되었습니다.", Toast.LENGTH_SHORT).show()
                    } else if(response.body()!!.complete == 2) {
                        Toast.makeText(applicationContext, "$finalSNSName 회원 가입되었습니다.", Toast.LENGTH_SHORT).show()
                    } else if(response.body()!!.complete == -1) {
                        Toast.makeText(applicationContext, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(applicationContext, "$finalSNSName 로그인 중 서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    if (SNSType == "GOOGLE") {
                        Firebase.auth.signOut()
                    } else if (SNSType == "KAKAO") {
                        UserApiClient.instance.logout{}
                    } else if (SNSType == "NAVER") {
                        mOAuthLoginModule.logout(mContext);
                    } else if (SNSType == "FACEBOOK") {
                        LoginManager.getInstance().logOut()
                    }
                }
                finish()
            }

            override fun onFailure(call: Call<SNSLoginResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "$finalSNSName 로그인 중 서버 접속 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initTag()
    }

    companion object {
        private val session: String? = null
        const val RC_SIGN_IN = 1
    }
}