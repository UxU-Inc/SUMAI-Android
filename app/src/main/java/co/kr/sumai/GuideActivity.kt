package co.kr.sumai

import kotlinx.android.synthetic.main.activity_guide.*
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener

class GuideActivity : AppCompatActivity() {
    private val urls = arrayOf("https://www.sumai.co.kr/terms/content", "https://www.sumai.co.kr/privacy/content", "https://www.sumai.co.kr/notices/content")
    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = intent.getIntExtra("theme", R.style.AppVoiTheme)
        setTheme(theme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        termsButton.tag = 0
        privacyButton.tag = 1
        noticesButton.tag = 2
        termsButton.setOnClickListener { guideViewPager.currentItem = 0 }
        privacyButton.setOnClickListener { guideViewPager.currentItem = 1 }
        noticesButton.setOnClickListener { guideViewPager.currentItem = 2 }
        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        guideViewPager.adapter = pagerAdapter
        guideViewPager.offscreenPageLimit = 2
        guideViewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                var button: Button
                for (i in 0..2) {
                    button = buttonLinearLayout.findViewWithTag<View>(i) as Button
                    if (position == i) {
                        buttonLinearLayout.findViewWithTag<View>(i).isSelected = true
                        button.setTextColor(Color.WHITE)
                    } else {
                        buttonLinearLayout.findViewWithTag<View>(i).isSelected = false
                        button.setTextColor(Color.BLACK)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    public override fun onStart() {
        super.onStart()
        guideViewPager.currentItem = 2
        val b = intent.extras
        var page = 0
        if (b != null) page = b.getInt("page")
        guideViewPager.currentItem = page
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            return WebViewFragment(urls[position])
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }
    }

    companion object {
        private const val NUM_PAGES = 3
    }
}