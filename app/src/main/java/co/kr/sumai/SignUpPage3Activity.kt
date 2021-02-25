package co.kr.sumai

import kotlinx.android.synthetic.main.activity_sign_up_page_3.*
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.kr.sumai.net.SignUpInforRequest
import co.kr.sumai.net.service
import kotlinx.android.synthetic.main.activity_sign_up_page_1.*
import retrofit2.Call
import retrofit2.Response

class SignUpPage3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page_3)
        var inforRequest: SignUpInforRequest = intent.getSerializableExtra("infor") as SignUpInforRequest
        val spannable = SpannableString("""
    ${inforRequest.email}로 인증 메일을 보냈습니다. 이메일을 확인해 주세요.
    
    이메일 인증 후 회원가입이 완료됩니다.
    """.trimIndent())
        spannable.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),
                0, inforRequest.email.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textViewContext.text = spannable
        buttonComplete.setOnClickListener { finish() }


        // 정보 전송
        val signUp = service.signUp(inforRequest).enqueue(object: retrofit2.Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@SignUpPage3Activity, "다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@SignUpPage3Activity, "서버 연결을 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}