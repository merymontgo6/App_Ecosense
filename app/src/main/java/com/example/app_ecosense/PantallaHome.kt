package com.example.app_ecosense

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.EcosenseApiClient
import com.example.app_ecosense.menu.BaseActivity
import com.example.app_ecosense.menu.BottomMenu
import com.example.app_ecosense.models.Planta
import com.example.app_ecosense.models.PlantaDetail
import com.example.app_ecosense.models.ZonaResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PantallaHome : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var zonaContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        ocultarBarra(window).hideSystemBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_home)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_bottom, BottomMenu())
            .commit()

        drawerLayout = findViewById(R.id.main)
        zonaContainer = findViewById(R.id.zona_container)

        val menuIcon: ImageView = findViewById(R.id.menu_icon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        carregarPlantasPorZonas()
    }

    private fun carregarPlantasPorZonas() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val usuarioId = obtenerIdUsuario() // Usa el método para obtener el ID real
                Log.d("DEBUG", "Cargando plantas para usuario: $usuarioId")

                // Usa el endpoint específico para plantas por zonas
                val zonasResponse = EcosenseApiClient.service.getPlantasPorZonas(usuarioId)
                Log.d("DEBUG", "Respuesta de API: ${zonasResponse.size} zonas")

                if (zonasResponse.isEmpty()) {
                    mostrarMensajeSinPlantas()
                    return@launch
                }

                mostrarZonas(zonasResponse)
            } catch (e: Exception) {
                Log.e("ERROR", "Error al cargar plantas", e)
                mostrarMensajeError("Error al cargar las plantas: ${e.localizedMessage}")
            }
        }
    }

    private fun mostrarZonas(zonas: List<ZonaResponse>) {
        zonaContainer.removeAllViews()
        if (zonas.isEmpty()) {
            mostrarMensajeSinPlantas()
            return
        }
        zonas.forEach { zona ->
            if (zona.plantas.isNotEmpty()) {
                agregarTituloZona(zona.zona)
                agregarPlantasDeZona(zona.plantas)
            }
        }
    }

    private fun mostrarMensajeError(mensaje: String) {
        runOnUiThread {
            zonaContainer.removeAllViews()
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
            zonaContainer.addView(errorText)
        }
    }

    private fun obtenerIdUsuario(): Int {
        return 1
    }



    private fun mostrarMensajeSinPlantas() {
        val emptyText = TextView(this).apply {
            text = "No hay plantas registradas"
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
        zonaContainer.addView(emptyText)
    }

    private fun agregarTituloZona(nombreZona: String) {
        val title = TextView(this).apply {
            text = "${nombreZona.capitalize()} ${obtenerEmojiZona(nombreZona)}"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.green_800))
            setPadding(0, 32, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        zonaContainer.addView(title)
    }

    private fun agregarPlantasDeZona(plantas: List<Planta>) {
        val scroll = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 32)
            }
        }

        val plantasContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        plantas.forEach { planta ->
            plantasContainer.addView(crearVistaPlanta(planta))
        }

        scroll.addView(plantasContainer)
        zonaContainer.addView(scroll)
    }

    private fun obtenerEmojiZona(nombreZona: String): String {
        return when {
            nombreZona.contains("casa", ignoreCase = true) -> "🏠"
            nombreZona.contains("oficina", ignoreCase = true) -> "💼"
            nombreZona.contains("jardín", ignoreCase = true) -> "🌳"
            else -> ""
        }
    }

    private fun crearVistaPlanta(planta: Planta): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                300, // Ancho fijo
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 0, 16, 0)
            }
            setPadding(16, 16, 16, 16)
            background = ContextCompat.getDrawable(context, R.drawable.planta_item_background)

            // Imagen de la planta
            addView(ImageView(context).apply {
                setImageResource(R.drawable.plants)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    200
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                setColorFilter(ContextCompat.getColor(context, R.color.green_700))
            })

            // Nombre de la planta
            addView(TextView(context).apply {
                text = planta.nom
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.green_800))
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            })

            // Click listener
            setOnClickListener {
                val intent = Intent(this@PantallaHome, PlantaDetail::class.java).apply {
                    putExtra("planta_id", planta.id)
                }
                startActivity(intent)
            }
        }
    }
}