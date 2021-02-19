package co.kr.sumai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import co.kr.sumai.net.LoginInforResponse;
import co.kr.sumai.net.LoginRequest;
import co.kr.sumai.net.LoginResponse;
import co.kr.sumai.net.NetRetrofitStore;
import co.kr.sumai.net.SumaiService;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


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

    private Button buttonTerms;
    private Button buttonPrivacy;

    private SumaiService service;

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

        // terms, privacy button 초기화
        buttonTerms.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), GuideActivity.class));
            }
        });
        buttonPrivacy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GuideActivity.class);
                Bundle b = new Bundle();
                b.putInt("page", 1);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initTag();
    }
}