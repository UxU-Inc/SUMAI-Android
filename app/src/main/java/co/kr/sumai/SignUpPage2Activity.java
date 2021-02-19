package co.kr.sumai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpPage2Activity extends AppCompatActivity {
    private SignUpInfor infor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page_2);
        Button buttonNext = findViewById(R.id.button);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignUpPage3Activity.class);
                intent.putExtra("infor", infor);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.left_out);
                finish();
            }
        });

        infor = (SignUpInfor)getIntent().getSerializableExtra("infor");

    }


    private void back() {
        Intent intent = new Intent(getApplicationContext(), SignUpPage1Activity.class);
        intent.putExtra("infor", infor);
        startActivity(intent);
        overridePendingTransition(R.anim.left_in,R.anim.right_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }
}