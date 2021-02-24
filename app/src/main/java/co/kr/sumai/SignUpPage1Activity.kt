package co.kr.sumai

import kotlinx.android.synthetic.main.activity_sign_up_page_1.*
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import co.kr.sumai.net.CheckEmailRequest
import co.kr.sumai.net.CheckEmailResponse
import co.kr.sumai.net.service
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpPage1Activity : AppCompatActivity() {
    private lateinit var infor: SignUpInfor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page_1)
        infor = intent.getSerializableExtra("infor") as SignUpInfor
        initLayout()
        buttonNext.setOnClickListener { verify() }
    }

    private fun verify(): Boolean {
        textViewErrorEmail.visibility = View.GONE
        textViewErrorName.visibility = View.GONE
        textViewErrorPassword.visibility = View.GONE
        textViewErrorPasswordCheck.visibility = View.GONE
        editTextEmailAddress.isSelected = false
        editTextName.isSelected = false
        editTextPassword.isSelected = false
        editTextPasswordCheck.isSelected = false
        val email = editTextEmailAddress.text.toString()
        val name = editTextName.text.toString()
        val password = editTextPassword.text.toString()
        val passwordVerification = editTextPasswordCheck.text.toString()
        val terms = checkBoxTerms.isChecked
        val privacy = checkBoxPrivacy.isChecked

        val emailPattern = Regex(pattern = "^[0-9A-z]([-_.]?[0-9A-z])*@[0-9A-z]([-_.]?[0-9A-z])*\\.[A-z]{2,}$")
        val namePattern = Regex(pattern = "^[a-zA-Z가-힣0-9]{2,10}$")
        val passwordPattern = Regex(pattern = "^.*(?=^.{8,15}\$)(?=.*\\d)(?=.*[a-zA-Z])(?=.*[`~!@#\$%^&+*()\\-_+=.,<>/?'\";:\\[\\]{}\\\\|]).*\$")

        if (!emailPattern.matches(email)) {
            textViewErrorEmail.visibility = View.VISIBLE
            editTextEmailAddress.isSelected = true
        } else if (!namePattern.matches(name)) {
            textViewErrorName.visibility = View.VISIBLE
            editTextName.isSelected = true
        } else if (!passwordPattern.matches(password)) {
            textViewErrorPassword.visibility = View.VISIBLE
            editTextPassword.isSelected = true
        } else if (password != passwordVerification) {
            textViewErrorPasswordCheck.visibility = View.VISIBLE
            editTextPasswordCheck.isSelected = true
        } else if (!terms) {
            Toast.makeText(this, "이용약관에 동의해주세요.", Toast.LENGTH_SHORT).show()
        } else if (!privacy) {
            Toast.makeText(this, "개인정보처리방침에 동의해주세요..", Toast.LENGTH_SHORT).show()
        } else {
            val service = service
            val getObject = service.checkEmail(CheckEmailRequest(email))
            getObject.enqueue(object : Callback<CheckEmailResponse?> {
                override fun onResponse(call: Call<CheckEmailResponse?>, response: Response<CheckEmailResponse?>) {
                    if (response.isSuccessful) {
                        infor.email = editTextEmailAddress.text.toString()
                        infor.name = editTextName.text.toString()
                        infor.password = editTextPassword.text.toString()
                        val intent = Intent(applicationContext, SignUpPage2Activity::class.java)
                        intent.putExtra("infor", infor)
                        startActivity(intent)
                        overridePendingTransition(R.anim.right_in, R.anim.left_out)
                        finish()
                        return
                    }
                    Toast.makeText(this@SignUpPage1Activity, "해당 이메일로 가입한 계정이 존재합니다.", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<CheckEmailResponse?>, t: Throwable) {
                    Toast.makeText(this@SignUpPage1Activity, "잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }
            })
        }
        return false
    }

    private fun initLayout() {
        editTextEmailAddress.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                val emailPattern = Regex("^[0-9A-z]([-_.]?[0-9A-z])*@[0-9A-z]([-_.]?[0-9A-z])*\\.[A-z]{2,}$")
                if (!emailPattern.matches(charSequence)) {
                    textViewErrorEmail.visibility = View.VISIBLE
                    editTextEmailAddress.isSelected = true
                } else {
                    textViewErrorEmail.visibility = View.GONE
                    editTextEmailAddress.isSelected = false
                }
            }

            override fun afterTextChanged(arg0: Editable) {
                // 입력 끝났을 때
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // 입력하기 전
            }
        })
        editTextName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val namePattern = Regex("^[a-zA-Z가-힣0-9]{2,10}$")
                if (!namePattern.matches(charSequence)) {
                    textViewErrorName.visibility = View.VISIBLE
                    editTextName.setSelected(true)
                } else {
                    textViewErrorName.visibility = View.GONE
                    editTextName.setSelected(false)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        textViewErrorEmail.visibility = View.GONE
        textViewErrorName.visibility = View.GONE
        textViewErrorPassword.visibility = View.GONE
        textViewErrorPasswordCheck.visibility = View.GONE


        editTextEmailAddress.setText(infor.email)
        editTextName.setText(infor.name)
        editTextPassword.setText(infor.password)
        editTextPasswordCheck.setText(infor.password)
    }
}