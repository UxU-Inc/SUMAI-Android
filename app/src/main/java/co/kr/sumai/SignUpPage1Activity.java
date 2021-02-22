package co.kr.sumai;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import co.kr.sumai.net.CheckEmailRequest;
import co.kr.sumai.net.CheckEmailResponse;
import co.kr.sumai.net.LoginRequest;
import co.kr.sumai.net.LoginResponse;
import co.kr.sumai.net.NetRetrofitStore;
import co.kr.sumai.net.SumaiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpPage1Activity extends AppCompatActivity {
    private SignUpInfor infor;

    private EditText editTextEmailAddress;
    private EditText editTextName;
    private EditText editTextPassword;
    private EditText editTextPasswordCheck;

    private TextView textViewErrorEmail;
    private TextView textViewErrorName;
    private TextView textViewErrorPassword;
    private TextView textViewErrorPasswordCheck;

    private CheckBox checkBoxTerms;
    private CheckBox checkBoxPrivacy;

    private Button buttonTerms;
    private Button buttonPrivacy;

    private Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page_1);

        infor = (SignUpInfor)getIntent().getSerializableExtra("infor");

        initLayout();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                CompletableFuture.runAsync(() -> {
//                    if(!verify()) return;

                    infor.setEmail(editTextEmailAddress.getText().toString());
                    infor.setName(editTextName.getText().toString());
                    infor.setPassword(editTextPassword.getText().toString());
                    Intent intent = new Intent(getApplicationContext(), SignUpPage2Activity.class);
                    intent.putExtra("infor", infor);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in,R.anim.left_out);
                    finish();
                });
            }
        });
    }

    private boolean verify() {
        textViewErrorEmail.setVisibility(View.GONE);
        textViewErrorName .setVisibility(View.GONE);
        textViewErrorPassword.setVisibility(View.GONE);
        textViewErrorPasswordCheck.setVisibility(View.GONE);

        String email = editTextEmailAddress.getText().toString();
        String name = editTextName.getText().toString();
        String password = editTextPassword.getText().toString();
        String passwordVerification = editTextPasswordCheck.getText().toString();
        boolean terms = checkBoxTerms.isChecked();
        boolean privacy = checkBoxPrivacy.isChecked();;

        if(!email.matches("^[0-9A-z]([-_.]?[0-9A-z])*@[0-9A-z]([-_.]?[0-9A-z])*\\.[A-z]{2,}$")) {
            textViewErrorEmail.setVisibility(View.VISIBLE);
            textViewErrorEmail.invalidate();
        }
        else if(!name.matches("^[a-zA-Z가-힣0-9]{2,10}$")){
            textViewErrorName.setVisibility(View.VISIBLE);
        }
        else if(!password.matches("^.*(?=^.{8,15}$)(?=.*\\d)(?=.*[a-zA-Z])(?=.*[`~!@#$%^&+*()\\-_+=.,<>/?'\";:\\[\\]{}\\\\|]).*$")) {
            textViewErrorPassword.setVisibility(View.VISIBLE);
        }
        else if(!password.equals(passwordVerification)) {
            textViewErrorPasswordCheck.setVisibility(View.VISIBLE);
        }
        else if(!terms) {
            Toast.makeText(this, "이용약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
        }
        else if(!privacy) {
            Toast.makeText(this, "개인정보처리방침에 동의해주세요..", Toast.LENGTH_SHORT).show();
        }
        else {
            SumaiService service = NetRetrofitStore.getInstance().getService();

            final Call<CheckEmailResponse> getObject = service.checkEmail(new CheckEmailRequest(email));

            try {
                if(getObject.execute().body().isSuccess()) {
                 return true;
                }
                Toast.makeText(this, "해당 이메일로 가입한 계정이 존재합니다.", Toast.LENGTH_SHORT).show();
            } catch(IOException e) {
                return false;
            }
        }
        return false;
    }

    private void initLayout() {
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordCheck = findViewById(R.id.editTextPasswordCheck);

        editTextEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!editTextEmailAddress.getText().toString().matches("^[0-9A-z]([-_.]?[0-9A-z])*@[0-9A-z]([-_.]?[0-9A-z])*\\.[A-z]{2,}$")) {
                    textViewErrorEmail.setVisibility(View.VISIBLE);
                } else {
                    textViewErrorEmail.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력 끝났을 때
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전
            }
        });


        textViewErrorEmail = findViewById(R.id.textViewErrorEmail);
        textViewErrorName = findViewById(R.id.textViewErrorName);
        textViewErrorPassword = findViewById(R.id.textViewErrorPassword);
        textViewErrorPasswordCheck = findViewById(R.id.textViewErrorPasswordCheck);

        textViewErrorEmail.setVisibility(View.GONE);
        textViewErrorName .setVisibility(View.GONE);
        textViewErrorPassword.setVisibility(View.GONE);
        textViewErrorPasswordCheck.setVisibility(View.GONE);

        checkBoxTerms = findViewById(R.id.checkboxTerms);
        checkBoxPrivacy = findViewById(R.id.checkboxPrivacy);

        buttonTerms = findViewById(R.id.buttonTerms);
        buttonPrivacy = findViewById(R.id.buttonTerms);

        buttonNext = findViewById(R.id.button);

        editTextEmailAddress.setText(infor.getEmail());
        editTextName.setText(infor.getName());
        editTextPassword.setText(infor.getPassword());
        editTextPasswordCheck.setText(infor.getPassword());


    }
}