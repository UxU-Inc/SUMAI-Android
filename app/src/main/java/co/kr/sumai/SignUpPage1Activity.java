package co.kr.sumai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpPage1Activity extends AppCompatActivity {
    private SignUpInfor infor;

    private EditText editTextEmail;
    private EditText editTextName;
    private EditText editTextPassword;
    private EditText editTextPasswordCheck;

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
            @Override
            public void onClick(View view) {
                infor.setEmail("1");
                infor.setName("2");
                infor.setPassword("3");
                Intent intent = new Intent(getApplicationContext(), SignUpPage2Activity.class);
                intent.putExtra("infor", infor);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.left_out);
                finish();
            }
        });
    }

    private void verify() {
        String email = editTextEmail.toString();
        String name = editTextName.toString();
        String password = editTextPassword.toString();
        String passwordVerification = editTextPasswordCheck.toString();



    }

    private void initLayout() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordCheck = findViewById(R.id.editTextPasswordCheck);

        checkBoxTerms = findViewById(R.id.checkboxTerms);
        checkBoxPrivacy = findViewById(R.id.checkboxPrivacy);

        buttonTerms = findViewById(R.id.buttonTerms);
        buttonPrivacy = findViewById(R.id.buttonTerms);

        buttonNext = findViewById(R.id.button);
    }
}