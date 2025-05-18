package com.example.app_ecosense

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.ApiConfig
import com.example.app_ecosense.info.EcosenseApiClient
import com.example.app_ecosense.info.ZonaResponse
import com.example.app_ecosense.menu.BaseActivity
import com.example.app_ecosense.menu.BottomMenu
import com.example.app_ecosense.models.Planta
import com.example.app_ecosense.models.PlantasViewModel
import com.example.app_ecosense.models.Zona
import com.example.app_ecosense.plantes.AfegirPlanta
import com.example.app_ecosense.plantes.PlantaDetail
import com.example.app_ecosense.plantes.ZonaDetail
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PantallaHome : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var zonaContainer: LinearLayout
    private val viewModel: PlantasViewModel by viewModels()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        ocultarBarra(window).hideSystemBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_home)

        val fabAdd: FloatingActionButton = findViewById(R.id.fab_add_planta)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AfegirPlanta::class.java)
            startActivity(intent)
        }

        supportFragmentManager.beginTransaction().replace(R.id.fragment_bottom, BottomMenu()).commit()
        drawerLayout = findViewById(R.id.main)
        zonaContainer = findViewById(R.id.zona_container)

        val menuIcon: ImageView = findViewById(R.id.menu_icon)
        menuIcon.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        carregarPlantasPorZonas()

        viewModel.dataUpdateEvent.observe(this) { updateType ->
            when (updateType) {
                PlantasViewModel.UpdateType.PLANTA_ACTUALIZADA,
                PlantasViewModel.UpdateType.TODOS_LOS_DATOS -> {
                    carregarPlantasPorZonas()
                }else -> {}
            }
        }
        swipeRefreshLayout = findViewById(R.id.swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener { carregarPlantasPorZonas() }

    }

    private fun carregarPlantasPorZonas() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val usuarioId = obtenerIdUsuario()
                val zonasResponse = EcosenseApiClient.service.getPlantasPorZonas(usuarioId)
                if (zonasResponse.isEmpty()) {
                    mostrarMensajeSinPlantas()
                    return@launch
                }
                mostrarZonas(zonasResponse)
            } catch (e: Exception) { mostrarMensajeError("Error al cargar las plantas: ${e.localizedMessage}") }
            finally {
                swipeRefreshLayout.isRefreshing = false
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
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(16, 32, 16, 0) }
            }
            zonaContainer.addView(errorText)
        }
    }

    private fun obtenerIdUsuario(): Int {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", 0) // Devuelve 0 si no encuentra nada
    }

    private fun mostrarMensajeSinPlantas() {
        val emptyText = TextView(this).apply {
            text = "No hay plantas registradas"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.green_800))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 32, 0, 0) }
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            setOnClickListener {
                val intent = Intent(this@PantallaHome, ZonaDetail::class.java).apply {
                    putExtra("zona_nombre", nombreZona)
                    putExtra("usuario_id", obtenerIdUsuario())
                }
                startActivity(intent)
            }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0)
            compoundDrawablePadding = 16
        }
        zonaContainer.addView(title)
    }

    private fun agregarPlantasDeZona(plantas: List<Planta>) {
        val scroll = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 32) }
        }

        val plantasContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        plantas.forEach { planta -> plantasContainer.addView(crearVistaPlanta(planta)) }
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
            layoutParams = LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(16, 0, 16, 0) }
            setPadding(16, 16, 16, 16)
            background = ContextCompat.getDrawable(context, R.drawable.planta_item_background)

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

            addView(TextView(context).apply {
                text = planta.nom
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.green_800))
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            })

            setOnClickListener {
                val intent = Intent(context, PlantaDetail::class.java).apply { putExtra("planta_id", planta.id) }
                context.startActivity(intent)
            }
        }
    }
}