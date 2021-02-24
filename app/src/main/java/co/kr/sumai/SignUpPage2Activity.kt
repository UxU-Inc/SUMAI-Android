package co.kr.sumai

import kotlinx.android.synthetic.main.activity_sign_up_page_2.*
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import co.kr.sumai.spinner.HintSpinner
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class SignUpPage2Activity : AppCompatActivity() {
    private lateinit var infor: SignUpInfor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page_2)
        initLayout()
        buttonNext.setOnClickListener(View.OnClickListener {
            if (!verify()) return@OnClickListener
            infor.birthday = ""
            infor.sex = ""
            val intent = Intent(applicationContext, SignUpPage3Activity::class.java)
            intent.putExtra("infor", infor)
            startActivity(intent)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
            finish()
        })
        infor = intent.getSerializableExtra("infor") as SignUpInfor
    }

    private fun initLayout() {
        val adapter = HintSpinner<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.gender_array))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = adapter
        spinnerGender.setSelection(adapter.count)
        spinnerGender.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                if ("사용자 지정" == spinnerGender.getItemAtPosition(i)) {
                    editTextGender.visibility = View.VISIBLE
                } else {
                    editTextGender.visibility = View.GONE
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        editTextGender.visibility = View.GONE
        textViewErrorBirthday.visibility = View.GONE
        textViewErrorGender.visibility = View.GONE
    }

    private fun verify(): Boolean {
        textViewErrorBirthday.visibility = View.GONE
        textViewErrorGender.visibility = View.GONE
        editTextYear.isSelected = false
        editTextMonth.isSelected = false
        editTextDay.isSelected = false
        editTextGender.isSelected = false
        val year = editTextYear.text.toString()
        val month = editTextMonth.text.toString()
        val day = editTextDay.text.toString()
        val gender = spinnerGender.selectedItem as String
        var yearInt = 0
        var monthInt = 0
        var dayInt = 0
        try {
            yearInt = year.toInt()
            monthInt = month.toInt()
            dayInt = day.toInt()
        } catch (e: NumberFormatException) {
        }
        if (year == "" && month == "" && day == "") {
        } else if (year == "" || month == "" || day == "") {
            textViewErrorBirthday.text = "생년월일을 정확히 입력해 주세요."
            textViewErrorBirthday.visibility = View.VISIBLE
        } else if (yearInt < 1000 || 10000 <= yearInt) {
            textViewErrorBirthday.text = "4자리 연도를 입력해 주세요."
            textViewErrorBirthday.visibility = View.VISIBLE
            editTextYear.isSelected = true
        } else if (yearInt < 1890) {
            textViewErrorBirthday.text = "올바른 연도를 입력해 주세요."
            textViewErrorBirthday.visibility = View.VISIBLE
            editTextYear.isSelected = true
        } else if (dayInt !in 1..31) {
            textViewErrorBirthday.text = "올바른 일을 입력해 주세요."
            textViewErrorBirthday.visibility = View.VISIBLE
            editTextDay.isSelected = true
        } else {
            val birthdayString = String.format("%04d%02d%02d", yearInt, monthInt, dayInt)
            val dateVerify = SimpleDateFormat("yyyyMMdd")
            val birthday: Date
            try {
                dateVerify.isLenient = false
                birthday = dateVerify.parse(birthdayString)
                val date = Date()

                // 현재 날짜와 비교
                if (birthday > date) {
                    textViewErrorBirthday.text = "올바른 생년월일을 입력해 주세요."
                    textViewErrorBirthday.visibility = View.VISIBLE
                    editTextYear.isSelected = true
                    editTextMonth.isSelected = true
                    editTextDay.isSelected = true
                    return false
                }
            } catch (e: ParseException) {
                textViewErrorBirthday.text = "올바른 생년월일을 입력해 주세요."
                textViewErrorBirthday.visibility = View.VISIBLE
                editTextYear.isSelected = true
                editTextMonth.isSelected = true
                editTextDay.isSelected = true
                return false
            }
        }
        if (gender == "사용자 지정" && editTextGender.text.toString() == "") {
            textViewErrorGender.text = "사용자 지정 성별을 입력해주세요."
            textViewErrorGender.visibility = View.VISIBLE
            editTextGender.isSelected = true
        } else {
            return false
        }
        return false
    }

    private fun back() {
        val intent = Intent(applicationContext, SignUpPage1Activity::class.java)
        intent.putExtra("infor", infor)
        startActivity(intent)
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        back()
    }
}