package com.example.app_ecosense.plantes

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.app_ecosense.R
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.EcosenseApiClient
import com.example.app_ecosense.info.PlantaCreateRequest
import com.example.app_ecosense.menu.BottomMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AfegirPlanta : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        ocultarBarra(window).hideSystemBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afegir_planta)

        drawerLayout = findViewById(R.id.main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_bottom, BottomMenu())
            .commit()

        val menuIcon: ImageView = findViewById(R.id.menu_icon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val guardarButton: com.google.android.material.button.MaterialButton = findViewById(R.id.guardarButton)
        guardarButton.setOnClickListener {
            guardarPlanta()
        }
    }

    private fun guardarPlanta() {
        val nomPlantaEditText: com.google.android.material.textfield.TextInputEditText = findViewById(R.id.nomPlantaEditText)
        val ubicacioEditText: com.google.android.material.textfield.TextInputEditText = findViewById(R.id.ubicacioEditText)
        val sensorIdEditText: com.google.android.material.textfield.TextInputEditText = findViewById(R.id.sensorIdEditText)
        val imagenUrlEditText: com.google.android.material.textfield.TextInputEditText = findViewById(R.id.imagenUrlEditText)

        val nom = nomPlantaEditText.text.toString().trim()
        val ubicacio = ubicacioEditText.text.toString().trim()
        val sensorIdStr = sensorIdEditText.text.toString().trim()

        if (nom.isEmpty() || ubicacio.isEmpty() || sensorIdStr.isEmpty()) {
            Toast.makeText(this, "Si us plau, omple tots els camps obligatoris", Toast.LENGTH_SHORT).show()
            return
        }

        val sensorId = sensorIdStr.toIntOrNull()
        if (sensorId == null) {
            Toast.makeText(this, "L'ID del sensor ha de ser un número vàlid", Toast.LENGTH_SHORT).show()
            return
        }

        val usuariId = sharedPref.getInt("user_id", 0)
        if (usuariId == 0) {
            Toast.makeText(this, "Error: No s'ha pogut identificar l'usuari", Toast.LENGTH_SHORT).show()
            return
        }

        val imagenUrl = imagenUrlEditText.text.toString().trim()

        val plantaRequest = PlantaCreateRequest(
            nom = nom,
            ubicacio = ubicacio,
            sensor_id = sensorId,
            usuari_id = usuariId,
            imagen_url = if (imagenUrl.isEmpty()) null else imagenUrl
        )

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Afegint planta...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = EcosenseApiClient.service.crearPlanta(plantaRequest)

                if (response.isSuccessful) {
                    val plantaCreada = response.body()
                    if (plantaCreada != null) {
                        Toast.makeText(
                            this@AfegirPlanta,
                            "Planta ${plantaCreada.nom} creada con ID ${plantaCreada.id}",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@AfegirPlanta,
                            "Error: Respuesta vacía del servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorCode = response.code()
                    val errorMsg = when (errorCode) {
                        400 -> "Datos inválidos (verifica sensor/usuario)"
                        404 -> "Recurso no encontrado"
                        else -> "Error $errorCode: ${response.errorBody()?.string()}"
                    }
                    Toast.makeText(
                        this@AfegirPlanta,
                        errorMsg,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AfegirPlanta,
                    "Error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }
}