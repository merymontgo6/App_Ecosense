package com.example.app_ecosense.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.app_ecosense.PantallaHome
import com.example.app_ecosense.R
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.empresa.Contacte
import com.example.app_ecosense.empresa.InfoNosaltres
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {
    override fun setContentView(layoutResID: Int) {
        ocultarBarra(window).hideSystemBar()

        val drawerLayout = layoutInflater.inflate(R.layout.activity_base, null) as DrawerLayout
        val contentFrame = drawerLayout.findViewById<FrameLayout>(R.id.content_frame)
        val activityView = LayoutInflater.from(this).inflate(layoutResID, contentFrame, false)
        contentFrame.addView(activityView)
        super.setContentView(drawerLayout)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout,
            R.string.nd_open, R.string.nd_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_home -> startActivity(Intent(this, PantallaHome::class.java))
                R.id.item_info -> startActivity(Intent(this, InfoNosaltres::class.java))
                R.id.item_contacte -> startActivity(Intent(this, Contacte::class.java))
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}