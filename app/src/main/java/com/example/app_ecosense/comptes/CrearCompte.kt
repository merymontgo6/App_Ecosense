package com.example.app_ecosense.comptes

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_ecosense.R
import com.example.app_ecosense.accesibilitat.ocultarBarra

class CrearCompte : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ocultarBarra(window).hideSystemBar()
        setContentView(R.layout.activity_crear_compte)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val crearCompte: AppCompatButton = findViewById(R.id.cc)
        crearCompte.setOnClickListener {
            val intent = Intent(this, IniciSessio::class.java)
            startActivity(intent)
        }

        val iniciarSessio: TextView = findViewById(R.id.iniciarSessio)
        iniciarSessio.setOnClickListener {
            val intent = Intent(this, IniciSessio::class.java)
            startActivity(intent)
        }
    }
}