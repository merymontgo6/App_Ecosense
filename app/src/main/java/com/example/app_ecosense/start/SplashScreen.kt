package com.example.app_ecosense.start

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.app_ecosense.R
import com.example.app_ecosense.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    lateinit var binding:ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val logo = findViewById<ImageView>(R.id.logo)
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotacio)
        logo.startAnimation(rotateAnimation)

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}