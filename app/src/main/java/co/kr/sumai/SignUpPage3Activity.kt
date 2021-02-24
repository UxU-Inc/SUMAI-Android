package co.kr.sumai

import kotlinx.android.synthetic.main.activity_sign_up_page_3.*
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity

class SignUpPage3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page_3)
        var infor: SignUpInfor = intent.getSerializableExtra("infor") as SignUpInfor
        val spannable = SpannableString("""
    ${infor.email}로 인증 메일을 보냈습니다. 이메일을 확인해 주세요.
    
    이메일 인증 후 회원가입이 완료됩니다.
    """.trimIndent())
        spannable.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),
                0, infor.email.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textViewContext.text = spannable
        buttonComplete.setOnClickListener { finish() }


        // 정보 전송
    }
}