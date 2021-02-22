package co.kr.sumai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.kr.sumai.spinner.HintSpinner;

public class SignUpPage2Activity extends AppCompatActivity {
    private SignUpInfor infor;

    private EditText editTextYear;
    private EditText editTextMonth;
    private EditText editTextDay;

    private Spinner spinnerGender;
    private EditText editTextGender;

    private TextView textViewErrorBirthday;
    private TextView textViewErrorGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page_2);

        initLayout();

        Button buttonNext = findViewById(R.id.button);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!verify()) return;
                infor.setBirthday("");
                infor.setSex("");
                Intent intent = new Intent(getApplicationContext(), SignUpPage3Activity.class);
                intent.putExtra("infor", infor);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.left_out);
                finish();
            }
        });

        infor = (SignUpInfor)getIntent().getSerializableExtra("infor");

    }

    private void initLayout() {
        editTextYear = findViewById(R.id.editTextYear);
        editTextMonth = findViewById(R.id.editTextMonth);
        editTextDay = findViewById(R.id.editTextDay);

        spinnerGender = findViewById(R.id.spinnerGender);

        HintSpinner<CharSequence> adapter = new HintSpinner<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.gender_array));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        spinnerGender.setSelection(adapter.getCount());

        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if("사용자 지정".equals(spinnerGender.getItemAtPosition(i))) {
                    editTextGender.setVisibility(View.VISIBLE);
                } else {
                    editTextGender.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        editTextGender = findViewById(R.id.editTextGender);
        editTextGender.setVisibility(View.GONE);

        textViewErrorBirthday = findViewById(R.id.textViewErrorBirthday);
        textViewErrorGender = findViewById(R.id.textViewErrorGender);

        textViewErrorBirthday.setVisibility(View.GONE);
        textViewErrorGender.setVisibility(View.GONE);
    }

    private boolean verify() {
        textViewErrorBirthday.setVisibility(View.GONE);
        textViewErrorGender.setVisibility(View.GONE);

        String year = editTextYear.getText().toString();
        String month = editTextMonth.getText().toString();
        String day = editTextDay.getText().toString();
        String gender = (String) spinnerGender.getSelectedItem();

        int yearInt = 0;
        int monthInt = 0;
        int dayInt = 0;

        try {
            yearInt = Integer.parseInt(year);
            monthInt = Integer.parseInt(month);
            dayInt = Integer.parseInt(day);
        } catch (NumberFormatException e) {};

        if(year.equals("") && month.equals("") && day.equals("")) {
        } else if(year.equals("") || month.equals("") || day.equals("")) {
            textViewErrorBirthday.setText("생년월일을 정확히 입력해 주세요.");
            textViewErrorBirthday.setVisibility(View.VISIBLE);
        } else if(yearInt < 1000 || 10000 <= yearInt) {
            textViewErrorBirthday.setText("4자리 연도를 입력해 주세요.");
            textViewErrorBirthday.setVisibility(View.VISIBLE);
        } else if(yearInt < 1890) {
            textViewErrorBirthday.setText("올바른 연도를 입력해 주세요.");
            textViewErrorBirthday.setVisibility(View.VISIBLE);
        } else if(!(1 <= dayInt && dayInt <= 31)) {
            textViewErrorBirthday.setText("올바른 일을 입력해 주세요.");
            textViewErrorBirthday.setVisibility(View.VISIBLE);
        } else {
            String birthdayString = String.format("%04d%02d%02d", yearInt, monthInt, dayInt);

            SimpleDateFormat dateVerify = new SimpleDateFormat("yyyyMMdd");

            Date birthday;
            try {
                dateVerify.setLenient(false);
                birthday = dateVerify.parse(birthdayString);

                Date date = new Date();

                // 현재 날짜와 비교
                if (birthday.compareTo(date) > 0) {
                    textViewErrorBirthday.setText("올바른 생년월일을 입력해 주세요.");
                    textViewErrorBirthday.setVisibility(View.VISIBLE);
                    return false;
                }

            } catch (ParseException e) {
                textViewErrorBirthday.setText("올바른 생년월일을 입력해 주세요.");
                textViewErrorBirthday.setVisibility(View.VISIBLE);
                return false;
            }
        }

        if(gender.equals("사용자 지정") && editTextGender.getText().toString().equals("")) {
            textViewErrorGender.setText("사용자 지정 성별을 입력해주세요.");
            textViewErrorGender.setVisibility(View.VISIBLE);
        } else {
            return false;
        }
        return false;
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