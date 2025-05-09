package com.example.app_ecosense.plantes;

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_ecosense.R
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.EcosenseApiClient
import com.example.app_ecosense.models.Planta
import com.example.app_ecosense.models.PlantaDetailModelo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantaDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ocultarBarra(window).hideSystemBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planta_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val plantaId = intent.getIntExtra("planta_id", -1)
        if (plantaId == -1) {
            Toast.makeText(this, "Error: ID de planta no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarDetallsPlanta(plantaId)
    }

    private fun cargarDetallsPlanta(plantaId: Int) {
        // Mostrar progreso (puedes usar un ProgressBar)
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Cargando detalles...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = EcosenseApiClient.service.getDetallePlanta(plantaId)

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()

                    if (response.isSuccessful) {
                        response.body()?.let { planta ->
                            mostrarDetallsPlanta(planta)
                        } ?: run {
                            Toast.makeText(this@PlantaDetail, "No se encontraron datos de la planta", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    else {
                        Toast.makeText(this@PlantaDetail, "Error al cargar detalles", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@PlantaDetail, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("PLANTA_DETAIL", "Error al cargar detalles", e)
                    finish()
                }
            }
        }
    }

    private fun mostrarDetallsPlanta(planta: Planta) {
        try {
            // Configurar toolbar
            findViewById<TextView>(R.id.toolbar_title)?.text = planta.nom
            findViewById<TextView>(R.id.planta_title)?.text = planta.nom

            // Cargar imagen
            val imageView = findViewById<ImageView>(R.id.planta_image)
            if (!planta.imagen_url.isNullOrEmpty()) {
                val imageUrl = planta.imagen_url.replace("localhost", "192.168.5.206")
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.plants)
                    .error(R.drawable.plants)
                    .into(imageView)
            } else {
                imageView?.setImageResource(R.drawable.plants)
            }

            // Configurar otros campos
            findViewById<TextView>(R.id.reg_optim_text)?.text = "Reg òptim, ${planta.humitat_optim}%"
            findViewById<TextView>(R.id.descripcio_text)?.text = planta.descripcio

            // Configurar estado de la planta
            val estatPlanta = findViewById<TextView>(R.id.estat_planta_text)
            estatPlanta?.text = if (planta.estat == "bo") {
                "Felicitats, la teva planta es troba bé!"
            } else {
                "La teva planta necessita atenció!"
            }
        } catch (e: Exception) {
            Log.e("PLANTA_DETAIL", "Error mostrando detalles", e)
            Toast.makeText(this, "Error mostrando información", Toast.LENGTH_SHORT).show()
        }
    }
}