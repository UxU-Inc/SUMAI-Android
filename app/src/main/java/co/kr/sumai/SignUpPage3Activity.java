package co.kr.sumai;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpPage3Activity extends AppCompatActivity {
    private SignUpInfor infor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page_3);

        infor = (SignUpInfor)getIntent().getSerializableExtra("infor");

        TextView textViewContent = findViewById(R.id.textViewContext);

        SpannableString spannable = new SpannableString(infor.getEmail()+"로 인증 메일을 보냈습니다. 이메일을 확인해 주세요.\n\n이메일 인증 후 회원가입이 완료됩니다.");
        spannable.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)),
                0, infor.getEmail().length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        textViewContent.setText(spannable);

        Button buttonNext = findViewById(R.id.button);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 정보 전송



    }
}