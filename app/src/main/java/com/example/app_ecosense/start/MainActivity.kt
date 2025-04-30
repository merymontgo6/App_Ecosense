package com.example.app_ecosense.start

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_ecosense.comptes.CrearCompte
import com.example.app_ecosense.R
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.comptes.IniciSessio

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ocultarBarra(window).hideSystemBar()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val startButton: AppCompatButton = findViewById(R.id.startBtn)
        startButton.setOnClickListener {
            val intent = Intent(this, IniciSessio::class.java)
            startActivity(intent)
        }

        val crearCompte: AppCompatButton = findViewById(R.id.cc)
        crearCompte.setOnClickListener {
            val intent = Intent(this, CrearCompte::class.java)
            startActivity(intent)
        }
    }
}