package com.example.app_ecosense.plantes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.app_ecosense.R
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.ApiConfig
import com.example.app_ecosense.menu.BottomMenu

class ModificarPlanta : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var etNombrePlanta: EditText
    private lateinit var ivPlanta: ImageView
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ocultarBarra(window).hideSystemBar()
        setContentView(R.layout.activity_modificar_planta)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar el menú inferior
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_bottom, BottomMenu())
            .commit()

        // Inicializar vistas
        initViews()

        // Configurar el menú lateral
        setupDrawerLayout()

        // Cargar datos de la planta
        cargarDatosPlanta()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.main)
        etNombrePlanta = findViewById(R.id.et_nombre_planta)
        ivPlanta = findViewById(R.id.iv_planta)
        btnGuardar = findViewById(R.id.btn_guardar)

        btnGuardar.setOnClickListener {
            guardarCambios()
        }
        findViewById<Button>(R.id.btn_cambiar_imagen).setOnClickListener {
        }
    }

    private fun setupDrawerLayout() {
        val menuIcon: ImageView = findViewById(R.id.menu_icon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun cargarDatosPlanta() {
        // Obtener datos de la planta del Intent
        val nombrePlanta = intent.getStringExtra("planta_nombre") ?: ""
        val imagenUrl = intent.getStringExtra("planta_imagen") ?: ""
        val ubicacion = intent.getStringExtra("planta_ubicacion") ?: ""
        val sensorId = intent.getStringExtra("sensor_id") ?: ""

        // Mostrar los datos en la interfaz
        etNombrePlanta.setText(nombrePlanta)

        // Cargar la imagen usando Glide
        if (imagenUrl.isNotEmpty()) {
            val baseUrl = ApiConfig.baseUrl
            val imageUrl = if (imagenUrl.startsWith("http")) {
                imagenUrl
            } else {
                "$baseUrl${if (imagenUrl.startsWith("/")) "" else "/"}${imagenUrl}"
            }

            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.plants)
                .error(R.drawable.plants)
                .into(ivPlanta)
        }

        // Aquí podrías cargar más datos según sea necesario
    }

    private fun guardarCambios() {
        val nuevoNombre = etNombrePlanta.text.toString()
        // Aquí iría la lógica para guardar los cambios en la base de datos/API

        // Mostrar mensaje de éxito y cerrar la actividad
        // Toast.makeText(this, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
        finish()
    }
}