package co.kr.sumai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class GuideActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private PagerAdapter pagerAdapter;
    private LinearLayout buttonLinearLayout;
    private Button termsButton;
    private Button privacyButton;
    private Button noticesButton;

    private String[] urls = {"https://www.sumai.co.kr/terms/content", "https://www.sumai.co.kr/privacy/content", "https://www.sumai.co.kr/notices/content"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        buttonLinearLayout = (LinearLayout) findViewById(R.id.buttonLinearLayout);
        termsButton = (Button) findViewById(R.id.termsButton);
        privacyButton = (Button) findViewById(R.id.privacyButton);
        noticesButton = (Button) findViewById(R.id.noticesButton);
        termsButton.setTag(0);
        privacyButton.setTag(1);
        noticesButton.setTag(2);
        termsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPager.setCurrentItem(0);
            }
        });
        privacyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPager.setCurrentItem(1);
            }
        });
        noticesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPager.setCurrentItem(2);
            }
        });

        mPager = (ViewPager) findViewById(R.id.guideViewPager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.setOffscreenPageLimit(2);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                Button button;
                for(int i=0; i<3; i++) {
                    button = (Button) buttonLinearLayout.findViewWithTag(i);
                    if(position == i) {
                        buttonLinearLayout.findViewWithTag(i).setSelected(true);
                        button.setTextColor(Color.WHITE);
                    }
                    else {
                        buttonLinearLayout.findViewWithTag(i).setSelected(false);
                        button.setTextColor(Color.BLACK);
                    }
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        mPager.setCurrentItem(2);
        Bundle b = getIntent().getExtras();
        int page = 0;
        if(b != null) page = b.getInt("page");
        mPager.setCurrentItem(page);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }



        @Override
        public Fragment getItem(int position) {
            return new WebViewFragment(urls[position]);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}