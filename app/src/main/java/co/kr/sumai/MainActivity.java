package co.kr.sumai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import co.kr.sumai.net.NetRetrofitStore;
import co.kr.sumai.net.SummaryRequest;
import co.kr.sumai.net.SummaryResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    Toolbar toolbar;

    ImageButton imageButtonClear;
    ImageButton imageButtonSummary;

    EditText editTextSummary;
    TextView textViewSummaryResult;

    private FrameLayout adContainerView;
    private AdView adView; // 애드몹 배너

    private FirebaseAnalytics mFirebaseAnalytics;

    String ID = "";
    int record = 1;

    //뒤로 버튼 두번 연속 클릭 시 종료
    private long time= 0;
    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis()-time>=2000){
            time=System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }else if(System.currentTimeMillis()-time<2000){
            MainActivity.this.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();

        clickEvent();

        // Firebase
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // AdMob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) { }
        });

        adContainerView = findViewById(R.id.ad_view_container);
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.adaptive_banner_ad_unit_id));
        adContainerView.addView(adView);
        loadBanner();
    }

    private void initLayout() {
        // toolbar, drawer, navigation Component
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.dl_main_drawer_root);
        navigationView = (NavigationView) findViewById(R.id.nv_main_navigation_root);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        navigationView.setNavigationItemSelectedListener(this);


        // main Component
        imageButtonClear = (ImageButton) findViewById(R.id.imageButtonClear);
        imageButtonClear.setVisibility(View.INVISIBLE);
        imageButtonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSummary.setText("");
                imageButtonClear.setVisibility(View.INVISIBLE);
            }
        });

        imageButtonSummary = (ImageButton) findViewById(R.id.imageButtonSummary);
        imageButtonSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                summaryRequest(editTextSummary.getText().toString());
            }
        });

        editTextSummary = (EditText) findViewById(R.id.editTextSummary);
        editTextSummary.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력 변화 시
                if(0 < editTextSummary.getText().toString().length()) {
                    if(imageButtonClear.getVisibility() == View.INVISIBLE)
                        imageButtonClear.setVisibility(View.VISIBLE);
                }
                else {
                    textViewSummaryResult.setVisibility(View.INVISIBLE);
                    if(imageButtonClear.getVisibility() == View.VISIBLE)
                        imageButtonClear.setVisibility(View.INVISIBLE);
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

        textViewSummaryResult = (TextView) findViewById(R.id.textViewSummaryResult);
        textViewSummaryResult.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
        drawerToggle.getDrawerArrowDrawable().setColor(Color.WHITE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                break;
            case R.id.terms:
//                intent = new Intent(getApplicationContext(), GuideActivity.class);
//                intent.putExtra("tabs", 1);
//                startActivity(intent);
                break;
            case R.id.privacy:
//                intent = new Intent(getApplicationContext(), GuideActivity.class);
//                intent.putExtra("tabs", 2);
//                startActivity(intent);
                break;
            case R.id.notice:
//                intent = new Intent(getApplicationContext(), GuideActivity.class);
//                intent.putExtra("tabs", 3);
//                startActivity(intent);
                break;
            case R.id.sendFeedback:
                sendMail();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private void clickEvent() {
        // toolbar
        ImageButton buttonNews = findViewById(R.id.buttonNews);
        buttonNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://news.sumai.co.kr"));
                startActivity(intent);
            }
        });

        LinearLayout layoutLogin = findViewById(R.id.layoutLogin);
        layoutLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, LoginActivity);
//                startActivity(intent);
            }
        });
    }

    private void summaryRequest(String data) {

        Call<SummaryResponse> res = NetRetrofitStore.getInstance().getService().SUMMARY(new SummaryRequest(data, ID, record));
        res.enqueue(new Callback<SummaryResponse>() {
            @Override
            public void onResponse(Call<SummaryResponse> call, Response<SummaryResponse> response) {
                textViewSummaryResult.setText(response.body().getSummarize());
                textViewSummaryResult.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<SummaryResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), call + "\n" + t, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadBanner() {
        AdRequest adRequest = new AdRequest.Builder().build();
        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void sendMail() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        try {
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"help@sumai.co.kr"});

            emailIntent.setType("text/html");
            emailIntent.setPackage("com.google.android.gm");
            if(emailIntent.resolveActivity(getPackageManager())!=null)
                startActivity(emailIntent);

            startActivity(emailIntent);
        } catch (Exception e) {
            e.printStackTrace();

            emailIntent.setType("text/html");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"help@sumai.co.kr"});

            startActivity(Intent.createChooser(emailIntent, "의견 보내기"));
        }
    }
}