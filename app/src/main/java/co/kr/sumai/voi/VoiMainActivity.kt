package co.kr.sumai.voi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import co.kr.sumai.GuideActivity
import co.kr.sumai.R
import co.kr.sumai.databinding.ActivityVoiMainBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class VoiMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityVoiMainBinding

    var drawerToggle: ActionBarDrawerToggle? = null
    var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
    }

    private fun initLayout() {
        // toolbar, drawer, navigation Component
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerToggle = ActionBarDrawerToggle(
            this,
            dl_main_drawer_root,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        dl_main_drawer_root.addDrawerListener(drawerToggle!!)
        nv_main_navigation_root.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        val intent: Intent
//        when (item.itemId) {
//            R.id.home -> {
//            }
//            R.id.terms -> {
//                intent = Intent(getApplicationContext(), GuideActivity::class.java)
//                intent.putExtra("page", 0)
//                startActivity(intent)
//            }
//            R.id.privacy -> {
//                intent = Intent(getApplicationContext(), GuideActivity::class.java)
//                intent.putExtra("page", 1)
//                startActivity(intent)
//            }
//            R.id.notice -> {
//                intent = Intent(getApplicationContext(), GuideActivity::class.java)
//                intent.putExtra("page", 2)
//                startActivity(intent)
//            }
//            R.id.sendFeedback -> sendMail()
//        }
//        dl_main_drawer_root.closeDrawer(GravityCompat.START)
        return false
    }
}