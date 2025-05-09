package com.example.app_ecosense.plantes

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_ecosense.R

import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.EcosenseApiClient
import com.example.app_ecosense.models.Planta
import com.example.app_ecosense.models.PlantaDetailModelo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZonaDetail : AppCompatActivity() {

    private lateinit var plantasContainer: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        ocultarBarra(window).hideSystemBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zona_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        plantasContainer = findViewById(R.id.plantas_container)

        val zonaNombre = intent.getStringExtra("zona_nombre") ?: ""
        val usuarioId = intent.getIntExtra("usuario_id", 1)

        // Configurar título de la zona
        findViewById<TextView>(R.id.zona_title).text = zonaNombre

        cargarPlantasDeZona(usuarioId, zonaNombre)
    }

    private fun cargarPlantasDeZona(usuarioId: Int, zonaNombre: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val plantas = obtenerPlantasDeZona(usuarioId, zonaNombre)
                mostrarPlantas(plantas)
            } catch (e: Exception) {
                Log.e("ZONA_DETAIL", "Error al cargar plantas", e)
                mostrarError("Error al cargar plantas: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun obtenerPlantasDeZona(usuarioId: Int, zonaNombre: String): List<Planta> {
        val zonas = EcosenseApiClient.service.getPlantasPorZonas(usuarioId)
        return zonas.find { it.zona == zonaNombre }?.plantas ?: emptyList()
    }

    private fun mostrarPlantas(plantas: List<Planta>) {
        plantasContainer.removeAllViews()

        if (plantas.isEmpty()) {
            mostrarMensajeSinPlantas()
            return
        }

        plantas.forEach { planta ->
            plantasContainer.addView(crearVistaPlanta(planta))
        }
    }

    private fun crearVistaPlanta(planta: Planta): LinearLayout {
        val context = this@ZonaDetail

        // Contenedor principal de la tarjeta
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 24)
            }
            setPadding(24, 24, 24, 24)
            background = ContextCompat.getDrawable(context, R.drawable.card_background)
            elevation = 8f

            // Añadir esto para feedback visual
            foreground = ContextCompat.getDrawable(context, R.drawable.ripple_effect)
            isClickable = true
            isFocusable = true

            // ImageView para la planta
            addView(ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    300
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                adjustViewBounds = true

                // Cargar imagen
                if (!planta.imagen_url.isNullOrEmpty()) {
                    val baseUrl = "http://18.213.199.248:8000"
                    val imageUrl = if (planta.imagen_url.startsWith("http")) {
                        planta.imagen_url
                    } else {
                        // Asegurar que la URL tenga el formato correcto
                        "$baseUrl${if (planta.imagen_url.startsWith("/")) "" else "/"}${planta.imagen_url}"
                    }

                    Log.d("IMAGE_URL", "URL completa: $imageUrl")

                    Glide.with(context)
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Para desarrollo
                        .skipMemoryCache(true) // Para desarrollo
                        .placeholder(R.drawable.plants)
                        .error(R.drawable.plants)
                        .into(this)
                } else {
                    setImageResource(R.drawable.plants)
                }
            })

            // Nombre de la planta
            addView(TextView(context).apply {
                text = planta.nom
                textSize = 24f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.green_800))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }
            })

            // Separador
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                ).apply {
                    setMargins(0, 0, 0, 12)
                }
                setBackgroundColor(ContextCompat.getColor(context, R.color.green_200))
            })

            // Información de la planta
            addView(TextView(context).apply {
                text = "Ubicación: ${planta.ubicacio}\nSensor ID: ${planta.sensor_id}"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.green_700))
            })

            // Click listener para toda la tarjeta
            setOnClickListener {
                try {
                    val intent = Intent(this@ZonaDetail, PlantaDetailModelo::class.java).apply {
                        putExtra("planta_id", planta.id)
                        putExtra("planta_nombre", planta.nom)
                        putExtra("planta_imagen", planta.imagen_url)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("NAVIGATION", "Error al abrir PlantaDetail", e)
                }
            }
        }
    }

    private fun mostrarMensajeSinPlantas() {
        val emptyText = TextView(this).apply {
            text = "No hay plantas en esta zona"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.green_800))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 32, 0, 0)
            }
        }
        plantasContainer.addView(emptyText)
    }

    private fun mostrarError(mensaje: String) {
        val errorText = TextView(this).apply {
            text = mensaje
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 32, 16, 0)
            }
        }
        plantasContainer.addView(errorText)
    }
}