package co.kr.sumai.voi

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityVoiMainBinding
import co.kr.sumai.func.loadPreferences

class VoiMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private var userID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()

        userID = loadPreferences(applicationContext, "loginData", "id")
        binding.content.toolbar.init(userID)
    }

    private fun initLayout() {
        // toolbar, drawer, navigation Component
        setSupportActionBar(binding.content.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.dlMainDrawerRoot,
            binding.content.toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        binding.dlMainDrawerRoot.addDrawerListener(drawerToggle)
        binding.nvMainNavigationRoot.init(binding.dlMainDrawerRoot)

        binding.content.textViewLimitGuide.visibility = View.INVISIBLE
        binding.content.layoutLoading.visibility = View.INVISIBLE
    }

    //뒤로 버튼 두번 연속 클릭 시 종료
    private var time: Long = 0
    override fun onBackPressed() {
        if (binding.dlMainDrawerRoot.isDrawerOpen(GravityCompat.START)) {
            binding.dlMainDrawerRoot.closeDrawer(GravityCompat.START)
        } else {
            if (System.currentTimeMillis() - time >= 2000) {
                time = System.currentTimeMillis()
                Toast.makeText(applicationContext, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
            } else if (System.currentTimeMillis() - time < 2000) {
                this.finish()
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
        drawerToggle.drawerArrowDrawable.color = Color.WHITE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
}