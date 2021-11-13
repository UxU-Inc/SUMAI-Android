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
import co.kr.sumai.voi.VoiMainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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

    private lateinit var mOAuthLoginModule: OAuthLogin
    private lateinit var mContext: Context

    private lateinit var caller: String

    override fun onStart() {
        super.onStart()
        // 사용자가 이미 로그인 한 경우 기존 Google 로그인 계정을 확인합니다.
        // GoogleSignInAccount는 null이 아닙니다.
        val currentUser = firebaseAuth.currentUser
        // updateUI(currentUser); TODO:
    }

    private fun initTag() {

        buttonLogin.setOnClickListener(View.OnClickListener {
            val email = editTextEmail.getText().toString()
            val password = editTextPassword.getText().toString()

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
                    savePreferences(applicationContext, "loginData", "id", response.body()?.id)
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

        mContext = this
        mOAuthLoginModule = OAuthLogin.getInstance()
        mOAuthLoginModule.init(this, getString(R.string.naver_client_id), getString(R.string.naver_client_secret), getString(R.string.app_name))
        buttonLoginNaver!!.setOnClickListener { mOAuthLoginModule.startOauthLoginActivity(this, mOAuthLoginHandler); }
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
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                // Google 로그인에 실패, UI를 적절하게 업데이트
                Toast.makeText(applicationContext, "구글 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        Log.e("account", ""+account)
        Log.e("account.idToken", ""+account.idToken)
        Log.e("credential", ""+credential)
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Log.e("task", ""+task)
                    Log.e("task.isSuccessful", ""+task.isSuccessful)
                    Log.e("task.exception", ""+task.exception)
                    if (task.isSuccessful) {
                        // 로그인 성공, 로그인한 사용자 정보로 UI 업데이트
                        val user = firebaseAuth.currentUser
                        val SNSType = "GOOGLE"
                        val accessToken = account.idToken.toString()
                        val email = user!!.email
                        val name = user.displayName
                        val id = account.id
                        val gender: String? = null
                        val birth: String? = null
                        val ageRange: String? = null
                        val imageURL = user.photoUrl.toString()
                        SNSLoginRequestFun(SNSType, accessToken, email, name, id, gender, birth, ageRange, imageURL)
                    } else {
                        // 로그인에 실패하면 사용자에게 메시지를 표시
                        Toast.makeText(applicationContext, "구글 로그인 인증에 실패했습니다.", Toast.LENGTH_SHORT).show()
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

                    SNSLoginRequestFun(SNSType, accessToken, email, name, id, gender, birth, ageRange, imageURL)
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

    private fun SNSLoginRequestFun(SNSType: String, accessToken: String, email: String?, name: String?, id: String?, gender: String?, birth: String?, ageRange: String?, imageURL: String?) {

        var SNSName = ""
        if (SNSType == "GOOGLE") SNSName = "구글"
        else if (SNSType == "NAVER") SNSName = "네이버"
        val finalSNSName = SNSName

        val res = service.getLoginState(SNSLoginRequest(SNSType, accessToken, email, name, id, gender, birth, ageRange, imageURL))
        res.enqueue(object : Callback<SNSLoginResponse> {
            override fun onResponse(call: Call<SNSLoginResponse>, response: Response<SNSLoginResponse>) {
                if (response.body() != null) {
                    Log.e("test", response.body()!!.complete.toString())
                    if (response.body()!!.complete == 1) {
                        savePreferences(applicationContext, "loginData", "id", id)
                        Toast.makeText(applicationContext, "$finalSNSName 로그인되었습니다.", Toast.LENGTH_SHORT).show()
                        refreshActivity()
                    } else if (response.body()!!.complete == 2) {
                        savePreferences(applicationContext, "loginData", "id", id)
                        Toast.makeText(applicationContext, "$finalSNSName 회원 가입되었습니다.", Toast.LENGTH_SHORT).show()
                        refreshActivity()
                    } else if (response.body()!!.complete == -1) {
                        Toast.makeText(applicationContext, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    } else if (response.body()!!.complete == -4) {
                        Toast.makeText(applicationContext, "로그인 에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "$finalSNSName 로그인 중 서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    if (SNSType == "GOOGLE") {
                        Firebase.auth.signOut()
                    } else if (SNSType == "NAVER") {
                        mOAuthLoginModule.logout(mContext);
                    }
                    refreshActivity()
                }
            }

            override fun onFailure(call: Call<SNSLoginResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "$finalSNSName 로그인 중 서버 접속 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun refreshActivity() {
        when(caller) {
            "VoiMainActivity" -> {
                val intent = Intent(applicationContext, VoiMainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
            "CaiiMainActivity" -> {

            }
            else -> {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        caller = intent.getStringExtra("caller")!!

        initTag()
    }

    companion object {
        private val session: String? = null
        const val RC_SIGN_IN = 1
    }
}