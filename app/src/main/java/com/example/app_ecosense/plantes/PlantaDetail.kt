package com.example.app_ecosense.plantes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_ecosense.R
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.ApiConfig
import com.example.app_ecosense.info.EcosenseApiClient
import com.example.app_ecosense.models.PlantaDetailModelo
import com.example.app_ecosense.models.PlantasViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantaDetail : AppCompatActivity() {
    private val viewModel: PlantasViewModel by viewModels()

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

        findViewById<ImageView>(R.id.edit_icon).setOnClickListener {
            val intent = Intent(this, ModificarPlanta::class.java)
            intent.putExtra("planta_id", plantaId)
            startActivity(intent)
        }

        viewModel.dataUpdateEvent.observe(this) { updateType ->
            if (updateType == PlantasViewModel.UpdateType.PLANTA_ACTUALIZADA) {
                cargarDetallsPlanta(plantaId)
            }
        }
        cargarDetallsPlanta(plantaId)
    }

    private fun cargarDetallsPlanta(plantaId: Int) {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Cargando detalles...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = EcosenseApiClient.service.getDetallePlanta(plantaId)
                if (response.isSuccessful) {
                    val rawBody = response.body()
                    if (rawBody != null) {
                        withContext(Dispatchers.Main) { mostrarDetallsPlanta(rawBody) }
                    }
                }

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (response.isSuccessful) {
                        response.body()?.let { plantaDetail ->
                            mostrarDetallsPlanta(plantaDetail)
                        } ?: run {
                            Toast.makeText(this@PlantaDetail, "No se encontraron datos de la planta", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                        Toast.makeText(this@PlantaDetail, "Error al cargar detalles: $errorBody", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@PlantaDetail, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun mostrarDetallsPlanta(data: PlantaDetailModelo) {
        try {
            findViewById<TextView>(R.id.toolbar_title)?.text = data.nom
            findViewById<TextView>(R.id.planta_title)?.text = data.nom

            val imageView = findViewById<ImageView>(R.id.planta_image)
            data.imagen_url?.takeIf { it.isNotBlank() }?.let { url ->
                val baseUrl = ApiConfig.baseUrl
                val fullUrl = if (url.startsWith("http")) url else "$baseUrl${if (url.startsWith("/")) "" else "/"}$url"
                Glide.with(this).load(fullUrl).placeholder(R.drawable.plants).error(R.drawable.plants).into(imageView)
            } ?: imageView.setImageResource(R.drawable.plants)

            val valorHumitat = data.humitat_valor
            val humitatText = "Lectura actual d'humetat: %.1f%%".format(valorHumitat)
            findViewById<TextView>(R.id.reg_optim_text)?.text = humitatText

            val (textoEstado, iconoEstado) = when {
                valorHumitat < 30 -> "La planta necesita agua!" to R.drawable.ic_sick_plant
                valorHumitat in 30.0..70.0 -> "Felicitats, la teva planta es troba bé!" to R.drawable.ic_healthy_plant
                valorHumitat > 70 -> "La planta tiene exceso de agua!" to R.drawable.ic_sick_plant
                else -> "Estado desconegut" to R.drawable.ic_sick_plant
            }
            findViewById<TextView>(R.id.estat_planta_text)?.text = textoEstado
            findViewById<ImageView>(R.id.estat_planta_icon)?.setImageResource(iconoEstado)
        } catch (e: Exception) { Toast.makeText(this, "Error mostrando datos", Toast.LENGTH_SHORT).show() }
    }
}