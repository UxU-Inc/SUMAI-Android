package co.kr.sumai

import kotlinx.android.synthetic.main.activity_login.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*

class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var facebookCallbackManager: CallbackManager? = null
    override fun onStart() {
        super.onStart()
        // 사용자가 이미 로그인 한 경우 기존 Google 로그인 계정을 확인합니다.
        // GoogleSignInAccount는 null이 아닙니다.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        // updateUI(account); TODO:
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


            // editTextEmail, editTextPassword를 https//sumai.co.kr/api/login 으로 전송
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
                    Log.e("test", response.body().toString())
                    finish()
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }
            })
        })
        buttonFindPassword.setOnClickListener(View.OnClickListener { // FindPasswordActivity로 이동
            Log.i("FP", "click")
        })
        buttonSignup.setOnClickListener(View.OnClickListener { // SignupActivity로 이동
            val intent = Intent(applicationContext, SignUpPage1Activity::class.java)
            intent.putExtra("infor", SignUpInfor())
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
        mAuth = FirebaseAuth.getInstance()
        buttonLoginGoogle!!.setOnClickListener { googleLogin() }
        buttonLoginKakao!!.setOnClickListener { kakaoLogin() }
        buttonLoginNaver!!.setOnClickListener { naverLogin() }
        facebookCallbackManager = CallbackManager.Factory.create()
        buttonLoginFacebook!!.setOnClickListener { btn_facebook_login_dumy!!.performClick() }
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
                    SNSLoginRequest(SNSType, email, name, id, gender, birth, ageRange, imageURL)
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

    private fun getFacebookUserInfo(response: GraphResponse, attribute: String): String? {
        var attr: String? = null
        try {
            attr = response.jsonObject.getString(attribute)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return attr
    }

    private fun googleLogin() {
        // Google 로그인 구성
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // gso에서 지정한 옵션으로 GoogleSignInClient 빌드
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = mGoogleSignInClient.getSignInIntent()
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
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공, 로그인한 사용자 정보로 UI 업데이트
                        val user = mAuth!!.currentUser
                        val SNSType = "google"
                        val email = user!!.email
                        val name = user.displayName
                        val id = account.id
                        val gender: String? = null
                        val birth: String? = null
                        val ageRange: String? = null
                        val imageURL = user.photoUrl.toString()
                        SNSLoginRequest(SNSType, email, name, id, gender, birth, ageRange, imageURL)
                    } else {
                        // 로그인에 실패하면 사용자에게 메시지를 표시
                        Toast.makeText(applicationContext, "구글 로그인 인증에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun kakaoLogin() {}
    private fun naverLogin() {}
    private fun facebookLogin() {}
    private fun SNSLoginRequest(SNSType: String, email: String?, name: String?, id: String?, gender: String?, birth: String?, ageRange: String?, imageURL: String?) {
        var SNSName = ""
        if (SNSType == "google") SNSName = "구글" else if (SNSType == "kakao") SNSName = "카카오" else if (SNSType == "naver") SNSName = "네이버" else if (SNSType == "facebook") SNSName = "페이스북"
        val finalSNSName = SNSName
        val res = service.getLoginState(SNSLoginRequest(SNSType, email, name, id, gender, birth, ageRange, imageURL))
        res!!.enqueue(object : Callback<SNSLoginResponse> {
            override fun onResponse(call: Call<SNSLoginResponse>, response: Response<SNSLoginResponse>) {
                if (response.body() != null && response.body()!!.complete) {
                    savePreferences("loginData", "SNSType", SNSType)
                    savePreferences("loginData", "email", email)
                    savePreferences("loginData", "name", name)
                    savePreferences("loginData", "id", id)
                    savePreferences("loginData", "gender", gender)
                    savePreferences("loginData", "birth", birth)
                    savePreferences("loginData", "ageRange", ageRange)
                    savePreferences("loginData", "imageURL", imageURL)
                    Toast.makeText(applicationContext, "$finalSNSName 로그인되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "$finalSNSName 로그인 중 서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    if (SNSType == "google") {
                        FirebaseAuth.getInstance().signOut()
                    } else if (SNSType == "kakao") {
                    } else if (SNSType == "naver") {
                    } else if (SNSType == "facebook") {
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

    // 값 불러오기
    private fun getPreferences(name: String, key: String): String? {
        val pref = getSharedPreferences(name, MODE_PRIVATE)
        return pref.getString(key, "")
    }

    // 값 저장하기
    private fun savePreferences(name: String, key: String, defValue: String?) {
        val pref = getSharedPreferences(name, MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(key, defValue)
        editor.commit()
    }

    // 값 삭제하기
    private fun deletePreferences(name: String, key: String) {
        val pref = getSharedPreferences(name, MODE_PRIVATE)
        val editor = pref.edit()
        editor.remove(key)
        editor.commit()
    }

    companion object {
        private val session: String? = null
        const val RC_SIGN_IN = 1
    }
}