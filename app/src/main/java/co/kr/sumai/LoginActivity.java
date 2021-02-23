package co.kr.sumai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import co.kr.sumai.net.LoginInforResponse;
import co.kr.sumai.net.LoginRequest;
import co.kr.sumai.net.LoginResponse;
import co.kr.sumai.net.NetRetrofitStore;
import co.kr.sumai.net.SNSLoginRequest;
import co.kr.sumai.net.SNSLoginResponse;
import co.kr.sumai.net.SumaiService;
import co.kr.sumai.net.SummaryRequest;
import co.kr.sumai.net.SummaryResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    static private String session;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;

    private Button buttonFindPassword;
    private Button buttonSignup;

    private FrameLayout buttonLoginGoogle;
    private FrameLayout buttonLoginKakao;
    private FrameLayout buttonLoginNaver;
    private FrameLayout buttonLoginFacebook;

    public static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    private CallbackManager facebookCallbackManager;
    private LoginButton btn_facebook_login_dumy;

    private Button buttonTerms;
    private Button buttonPrivacy;

    private SumaiService service;

    @Override
    protected void onStart() {
        super.onStart();
        // 사용자가 이미 로그인 한 경우 기존 Google 로그인 계정을 확인합니다.
        // GoogleSignInAccount는 null이 아닙니다.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        // updateUI(account); TODO:
    }

    private void initTag() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonFindPassword = findViewById(R.id.buttonFindPassword);
        buttonSignup = findViewById(R.id.buttonSignup);

        buttonLoginGoogle = findViewById(R.id.buttonLoginGoogle);
        buttonLoginKakao = findViewById(R.id.buttonLoginKakao);
        buttonLoginNaver = findViewById(R.id.buttonLoginNaver);
        buttonLoginFacebook = findViewById(R.id.buttonLoginFacebook);

        buttonTerms = findViewById(R.id.buttonTerms);
        buttonPrivacy = findViewById(R.id.buttonPrivacy);

        NetRetrofitStore.createNetRetrofit(this);

        service = NetRetrofitStore.getInstance().getService();

        // get login infor
        Call<LoginInforResponse> loginInfor = service.getInfo();
        loginInfor.enqueue(new Callback<LoginInforResponse>() {
            @Override
            public void onResponse(Call<LoginInforResponse> call, Response<LoginInforResponse> response) {
                if(!response.isSuccessful()) {
                    try {
                        JSONObject jObjectError = new JSONObject(response.errorBody().string());
                        Toast.makeText(LoginActivity.this, jObjectError.getString("error"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                response.body().toString();
            }

            @Override
            public void onFailure(Call<LoginInforResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                // 제한 사항 추가


                // editTextEmail, editTextPassword를 https//sumai.co.kr/api/login 으로 전송
                final Call<LoginResponse> getObject = service.postLogin(new LoginRequest(email, password));

                getObject.enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        // 로그인 실패
                        if(!response.isSuccessful()) {
                            try {
                                JSONObject jObjectError = new JSONObject(response.errorBody().string());
                                Toast.makeText(LoginActivity.this, jObjectError.getString("error"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        // 로그인 성공
                        Log.e("test",response.body().toString());
                        finish();
                    }
                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        buttonFindPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // FindPasswordActivity로 이동
                Log.i("FP", "click");
            }
        });
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // SignupActivity로 이동
                Intent intent = new Intent(getApplicationContext(), SignUpPage1Activity.class);
                intent.putExtra("infor", new SignUpInfor());
                startActivity(intent);
            }
        });

        // sns login button 초기화
        SNSLoginInit();

        // terms, privacy button 초기화
        buttonTerms.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), GuideActivity.class));
            }
        });
        buttonPrivacy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GuideActivity.class);
                intent.putExtra("page", 1);
                startActivity(intent);
            }
        });
    }

    private void SNSLoginInit() {
        mAuth = FirebaseAuth.getInstance();
        buttonLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleLogin();
            }
        });

        buttonLoginKakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kakaoLogin();
            }
        });

        buttonLoginNaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                naverLogin();
            }
        });

        facebookCallbackManager = CallbackManager.Factory.create();
        btn_facebook_login_dumy = (LoginButton) findViewById(R.id.btn_facebook_login_dumy);
        buttonLoginFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_facebook_login_dumy.performClick();
            }
        });
        btn_facebook_login_dumy.setReadPermissions(Arrays.asList("public_profile", "email"));
        btn_facebook_login_dumy.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest graphRequest
                        = GraphRequest.newMeRequest(
                        loginResult.getAccessToken()
                        , new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                String SNSType = "facebook";
                                String email = getFacebookUserInfo(response, "email");
                                String name = getFacebookUserInfo(response, "name");
                                String id = getFacebookUserInfo(response, "id");
                                String gender = getFacebookUserInfo(response, "gender");
                                String birth = getFacebookUserInfo(response, "birth");
                                String ageRange = getFacebookUserInfo(response, "ageRange");
                                String imageURL = null;
                                try {
                                    imageURL = response.getJSONObject().getJSONObject("picture").getJSONObject("data").getString("url");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                SNSLoginRequest(SNSType, email, name, id, gender, birth, ageRange, imageURL);
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,age_range,picture");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("LoginErr",error.toString());
            }
        });

    }

    private String getFacebookUserInfo(GraphResponse response, String attribute) {
        String attr = null;
        try {
            attr = response.getJSONObject().getString(attribute);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return attr;
    }

    private void googleLogin() {
        // Google 로그인 구성
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // gso에서 지정한 옵션으로 GoogleSignInClient 빌드
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // GoogleSignInApi.getSignInIntent(...)에서 인텐트를 시작하여 반환된 결과
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google 로그인 성공, Firebase로 인증
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google 로그인에 실패, UI를 적절하게 업데이트
                Toast.makeText(getApplicationContext(), "구글 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        // facebook
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공, 로그인한 사용자 정보로 UI 업데이트
                            FirebaseUser user = mAuth.getCurrentUser();

                            String SNSType = "google";
                            String email = user.getEmail();
                            String name = user.getDisplayName();
                            String id = account.getId();
                            String gender = null;
                            String birth = null;
                            String ageRange = null;
                            String imageURL = user.getPhotoUrl().toString();

                            SNSLoginRequest(SNSType, email, name, id, gender, birth, ageRange, imageURL);

                        } else {
                            // 로그인에 실패하면 사용자에게 메시지를 표시
                            Toast.makeText(getApplicationContext(), "구글 로그인 인증에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void kakaoLogin() {

    }

    private void naverLogin() {

    }

    private void facebookLogin() {
    }

    private void SNSLoginRequest(String SNSType, String email, String name, String id, String gender, String birth, String ageRange, String imageURL) {

        String SNSName = "";
        if(SNSType.equals("google")) SNSName = "구글";
        else if(SNSType.equals("kakao")) SNSName = "카카오";
        else if(SNSType.equals("naver")) SNSName = "네이버";
        else if(SNSType.equals("facebook")) SNSName = "페이스북";
        String finalSNSName = SNSName;

        Call<SNSLoginResponse> res = NetRetrofitStore.getInstance().getService().getLoginState(new SNSLoginRequest(SNSType, email, name, id, gender, birth, ageRange, imageURL));
        res.enqueue(new Callback<SNSLoginResponse>() {
            @Override
            public void onResponse(Call<SNSLoginResponse> call, Response<SNSLoginResponse> response) {
                if (response.body() != null && response.body().getComplete()) {

                    savePreferences("loginData", "SNSType", SNSType);
                    savePreferences("loginData", "email", email);
                    savePreferences("loginData", "name", name);
                    savePreferences("loginData", "id", id);
                    savePreferences("loginData", "gender", gender);
                    savePreferences("loginData", "birth", birth);
                    savePreferences("loginData", "ageRange", ageRange);
                    savePreferences("loginData", "imageURL", imageURL);

                    Toast.makeText(getApplicationContext(), finalSNSName + " 로그인되었습니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), finalSNSName + " 로그인 중 서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();

                    if(SNSType.equals("google")) {
                        FirebaseAuth.getInstance().signOut();
                    }
                    else if(SNSType.equals("kakao")) {

                    }
                    else if(SNSType.equals("naver")) {

                    }
                    else if(SNSType.equals("facebook")) {
                        LoginManager.getInstance().logOut();
                    }
                }
                finish();
            }

            @Override
            public void onFailure(Call<SNSLoginResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), finalSNSName + " 로그인 중 서버 접속 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initTag();
    }

    // 값 불러오기
    private String getPreferences(String name, String key){
        SharedPreferences pref = getSharedPreferences(name, MODE_PRIVATE);
        return pref.getString(key, "");
    }

    // 값 저장하기
    private void savePreferences(String name, String key, String defValue){
        SharedPreferences pref = getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, defValue);
        editor.commit();
    }

    // 값 삭제하기
    private void deletePreferences(String name, String key){
        SharedPreferences pref = getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.commit();
    }
}