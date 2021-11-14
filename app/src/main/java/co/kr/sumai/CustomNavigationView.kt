package co.kr.sumai

import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import co.kr.sumai.func.InfoByClass
import com.google.android.material.navigation.NavigationView

class CustomNavigationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : NavigationView(context, attrs), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var dlMainDrawerRoot: DrawerLayout

    private val infoByClass = InfoByClass()

    init{
        val header = LayoutInflater.from(context).inflate(R.layout.navi_header_main, this, false)
        addHeaderView(header)
        attrs?.let { getAttrs(it) }
    }

    fun init(drawerLayout: DrawerLayout) {
        dlMainDrawerRoot = drawerLayout
        setNavigationItemSelectedListener(this)
    }

    private fun getAttrs(attrs : AttributeSet){
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomNavigationView)

        setTypeArray(typedArray)
    }

    private fun setTypeArray(typedArray : TypedArray){
        val logoSrc = typedArray.getResourceId(R.styleable.CustomNavigationView_navLogoSrc,
            R.drawable.sumai_logo)
        getHeaderView(0).findViewById<ImageView>(R.id.imageView).setImageResource(logoSrc)

        val logoText = typedArray.getString(R.styleable.CustomNavigationView_navLogoText)
        getHeaderView(0).findViewById<TextView>(R.id.textView).text = logoText
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.home -> {
            }
            R.id.terms -> {
                intent = Intent(context, GuideActivity::class.java)
                intent.putExtra("theme", infoByClass.getTheme(context.javaClass.simpleName))
                intent.putExtra("page", 0)
                context.startActivity(intent)
            }
            R.id.privacy -> {
                intent = Intent(context, GuideActivity::class.java)
                intent.putExtra("theme", infoByClass.getTheme(context.javaClass.simpleName))
                intent.putExtra("page", 1)
                context.startActivity(intent)
            }
            R.id.notice -> {
                intent = Intent(context, GuideActivity::class.java)
                intent.putExtra("theme", infoByClass.getTheme(context.javaClass.simpleName))
                intent.putExtra("page", 2)
                context.startActivity(intent)
            }
            R.id.sendFeedback -> sendMail()
        }
        dlMainDrawerRoot.closeDrawer(GravityCompat.START)
        return false
    }

    private fun sendMail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        try {
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("help@sumai.co.kr"))
            emailIntent.type = "text/html"
            emailIntent.setPackage("com.google.android.gm")
            if (emailIntent.resolveActivity(context.packageManager) != null) context.startActivity(emailIntent)
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            emailIntent.type = "text/html"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("help@sumai.co.kr"))
            context.startActivity(Intent.createChooser(emailIntent, "의견 보내기"))
        }
    }
}