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

class RecuperarContrassenya : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ocultarBarra(window).hideSystemBar()
        setContentView(R.layout.activity_recuperar_contrassenya)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val cancelar: AppCompatButton = findViewById(R.id.cancelarBtn)
        cancelar.setOnClickListener {
            val intent = Intent(this, IniciSessio::class.java)
            startActivity(intent)
        }

        val restablir: AppCompatButton = findViewById(R.id.restablirBtn)
        restablir.setOnClickListener {
            val intent = Intent(this, NovaContrassenya::class.java)
            startActivity(intent)
        }
    }
}