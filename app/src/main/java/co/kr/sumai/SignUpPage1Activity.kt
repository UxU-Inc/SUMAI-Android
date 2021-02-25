package co.kr.sumai

import kotlinx.android.synthetic.main.activity_sign_up_page_1.*
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import co.kr.sumai.net.CheckEmailRequest
import co.kr.sumai.net.CheckEmailResponse
import co.kr.sumai.net.SignUpInforRequest
import co.kr.sumai.net.service
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up_page_1.buttonPrivacy
import kotlinx.android.synthetic.main.activity_sign_up_page_1.buttonTerms
import kotlinx.android.synthetic.main.activity_sign_up_page_1.editTextPassword
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpPage1Activity : AppCompatActivity() {
    private lateinit var inforRequest: SignUpInforRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page_1)
        inforRequest = intent.getSerializableExtra("infor") as SignUpInforRequest
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
            service.checkEmail(CheckEmailRequest(email)).enqueue(object : Callback<CheckEmailResponse?> {
                override fun onResponse(call: Call<CheckEmailResponse?>, response: Response<CheckEmailResponse?>) {
                    if (response.isSuccessful) {
                        inforRequest.email = editTextEmailAddress.text.toString()
                        inforRequest.name = editTextName.text.toString()
                        inforRequest.password = editTextPassword.text.toString()
                        val intent = Intent(applicationContext, SignUpPage2Activity::class.java)
                        intent.putExtra("infor", inforRequest)
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
                if (!emailPattern.matches(charSequence) && charSequence.toString() != "") {
                    textViewErrorEmail.visibility = View.VISIBLE
                    editTextEmailAddress.isSelected = true
                } else {
                    textViewErrorEmail.visibility = View.GONE
                    editTextEmailAddress.isSelected = false
                }
            }

            override fun afterTextChanged(arg0: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        })
        editTextName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val namePattern = Regex("^[a-zA-Z가-힣0-9]{2,10}$")
                if (!namePattern.matches(charSequence) && charSequence.toString() != "") {
                    textViewErrorName.visibility = View.VISIBLE
                    editTextName.isSelected = true
                } else {
                    textViewErrorName.visibility = View.GONE
                    editTextName.isSelected = false
                }
            }
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
        })
        textViewErrorEmail.visibility = View.GONE
        textViewErrorName.visibility = View.GONE
        textViewErrorPassword.visibility = View.GONE
        textViewErrorPasswordCheck.visibility = View.GONE


        buttonTerms.setOnClickListener{ startActivity(Intent(applicationContext, GuideActivity::class.java)) }
        buttonPrivacy.setOnClickListener {
            val intent = Intent(applicationContext, GuideActivity::class.java)
            intent.putExtra("page", 1)
            startActivity(intent)
        }


        editTextEmailAddress.setText(inforRequest.email)
        editTextName.setText(inforRequest.name)
        editTextPassword.setText(inforRequest.password)
        editTextPasswordCheck.setText(inforRequest.password)
    }
}