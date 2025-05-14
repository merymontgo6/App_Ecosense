package com.example.app_ecosense.plantes

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_ecosense.R

import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.ApiConfig
import com.example.app_ecosense.info.EcosenseApiClient
import com.example.app_ecosense.models.Planta
import com.example.app_ecosense.models.PlantasViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZonaDetail : AppCompatActivity() {

    private lateinit var plantasContainer: LinearLayout
    private val viewModel: PlantasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        ocultarBarra(window).hideSystemBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zona_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fabAdd: FloatingActionButton = findViewById(R.id.fab_add_planta)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AfegirPlanta::class.java)
            startActivity(intent)
        }

        plantasContainer = findViewById(R.id.plantas_container)
        val zonaNombre = intent.getStringExtra("zona_nombre") ?: ""
        val usuarioId = intent.getIntExtra("usuario_id", 1)

        findViewById<TextView>(R.id.zona_title).text = zonaNombre
        cargarPlantasDeZona(usuarioId, zonaNombre)

        viewModel.dataUpdateEvent.observe(this) { updateType ->
            when (updateType) {
                PlantasViewModel.UpdateType.PLANTA_ACTUALIZADA,
                PlantasViewModel.UpdateType.ZONA_ACTUALIZADA,
                PlantasViewModel.UpdateType.TODOS_LOS_DATOS -> {
                    val usuarioId = intent.getIntExtra("usuario_id", 0)
                    val zonaNombre = intent.getStringExtra("zona_nombre") ?: ""
                    cargarPlantasDeZona(usuarioId, zonaNombre)
                }
            }
        }
    }

    private fun cargarPlantasDeZona(usuarioId: Int, zonaNombre: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val plantas = obtenerPlantasDeZona(usuarioId, zonaNombre)
                mostrarPlantas(plantas)
            } catch (e: Exception) { mostrarError("Error al cargar plantas: ${e.localizedMessage}") }
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
        plantas.forEach { planta -> plantasContainer.addView(crearVistaPlanta(planta)) }
    }

    private fun crearVistaPlanta(planta: Planta): LinearLayout {
        val context = this@ZonaDetail

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(16, 8, 16, 24) }
            setPadding(24, 24, 24, 24)
            background = ContextCompat.getDrawable(context, R.drawable.card_background)
            elevation = 8f
            foreground = ContextCompat.getDrawable(context, R.drawable.ripple_effect)
            isClickable = true
            isFocusable = true

            addView(ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300).apply { setMargins(0, 0, 0, 16) }
                scaleType = ImageView.ScaleType.CENTER_CROP
                adjustViewBounds = true

                if (!planta.imagen_url.isNullOrEmpty()) {
                    val baseUrl = ApiConfig.baseUrl
                    val imageUrl = if (planta.imagen_url.startsWith("http")) { planta.imagen_url
                    } else { "$baseUrl${if (planta.imagen_url.startsWith("/")) "" else "/"}${planta.imagen_url}" }
                    Glide.with(context).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.plants).error(R.drawable.plants).into(this)
                } else { setImageResource(R.drawable.plants) }
            })

            val headerLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                gravity = Gravity.CENTER_VERTICAL
            }

            val plantNameTextView = TextView(context).apply {
                text = planta.nom
                textSize = 24f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.green_800))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            headerLayout.addView(plantNameTextView)

            val editButton = ImageView(context).apply {
                setImageResource(R.drawable.ic_edit)
                contentDescription = "Editar planta"
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setPadding(16, 16, 16, 16)
                setOnClickListener {
                    val intent = Intent(this@ZonaDetail, ModificarPlanta::class.java).apply {
                        putExtra("planta_id", planta.id)
                        putExtra("planta_nombre", planta.nom)
                        putExtra("planta_ubicacion", planta.ubicacio)
                        putExtra("planta_imagen", planta.imagen_url)
                        putExtra("sensor_id", planta.sensor_id)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                }
            }
            headerLayout.addView(editButton)
            addView(headerLayout)

            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply { setMargins(0, 0, 0, 12) }
                setBackgroundColor(ContextCompat.getColor(context, R.color.green_200))
            })

            addView(TextView(context).apply {
                text = "Ubicación: ${planta.ubicacio}\nSensor ID: ${planta.sensor_id}"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.green_700))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 8) }
            })

            val humitatTextView = TextView(context).apply {
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.green_700))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            addView(humitatTextView)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = EcosenseApiClient.service.getHumitatActual(planta.sensor_id)
                    when {
                        response.isSuccessful -> {
                            response.body()?.let { humitat ->
                                val humedadFormateada = "%.1f".format(humitat.valor)
                                humitatTextView.text = "Humedad: $humedadFormateada%"

                                val color = when {
                                    humitat.valor > 70 -> R.color.humidity_high
                                    humitat.valor > 30 -> R.color.humidity_medium
                                    else -> R.color.humidity_low
                                }
                                humitatTextView.setTextColor(ContextCompat.getColor(context, color))
                            } ?: run {
                                humitatTextView.text = "Humedad: No disponible"
                                humitatTextView.setTextColor(ContextCompat.getColor(context, R.color.green_700))
                            }
                        }
                        response.code() == 404 -> { humitatTextView.text = "Sensor no encontrado" }
                        else -> {
                            val errorMsg = try { response.errorBody()?.string() ?: "Error desconocido"
                            } catch (e: Exception) { "Error al leer respuesta" }
                            humitatTextView.text = "Error: $errorMsg"
                        }
                    }
                } catch (e: Exception) { humitatTextView.text = "Error de conexión" }
            }

            setOnClickListener {
                val intent = Intent(this@ZonaDetail, PlantaDetail::class.java).apply {
                    putExtra("planta_id", planta.id)
                    putExtra("planta_nombre", planta.nom)
                    putExtra("planta_imagen", planta.imagen_url)
                    putExtra("sensor_id", planta.sensor_id)
                }
                startActivity(intent)
            }
        }
    }

    private fun mostrarMensajeSinPlantas() {
        val emptyText = TextView(this).apply {
            text = "No hay plantas en esta zona"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.green_800))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 32, 0, 0) }
        }
        plantasContainer.addView(emptyText)
    }

    private fun mostrarError(mensaje: String) {
        val errorText = TextView(this).apply {
            text = mensaje
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(16, 32, 16, 0) }
        }
        plantasContainer.addView(errorText)
    }
}