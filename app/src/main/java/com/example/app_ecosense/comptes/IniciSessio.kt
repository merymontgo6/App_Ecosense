package com.example.app_ecosense.comptes

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_ecosense.PantallaHome
import com.example.app_ecosense.R
import com.example.app_ecosense.accesibilitat.ocultarBarra

class IniciSessio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ocultarBarra(window).hideSystemBar()
        setContentView(R.layout.activity_inici_sessio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val oblidarContrassenya: TextView = findViewById(R.id.oblidarC)
        oblidarContrassenya.setOnClickListener {
            val intent = Intent(this, RecuperarContrassenya::class.java)
            startActivity(intent)
        }

        val iniciSessio: AppCompatButton = findViewById(R.id.iniciarSessio)
        iniciSessio.setOnClickListener {
            val intent = Intent(this, PantallaHome::class.java)
            startActivity(intent)
        }

        val crearCompte: TextView = findViewById(R.id.cc)
        crearCompte.setOnClickListener {
            val intent = Intent(this, CrearCompte::class.java)
            startActivity(intent)
        }
    }
}