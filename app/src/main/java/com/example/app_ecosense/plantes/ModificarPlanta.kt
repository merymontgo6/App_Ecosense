package com.example.app_ecosense.plantes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.app_ecosense.PantallaHome
import com.example.app_ecosense.R
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.ApiConfig
import com.example.app_ecosense.info.EcosenseApiClient
import com.example.app_ecosense.info.PlantaUpdateRequest
import com.example.app_ecosense.models.PlantasViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModificarPlanta : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var etNombrePlanta: EditText
    private lateinit var etUbicacionPlanta: EditText
    private lateinit var etSensorId: EditText
    private lateinit var ivPlanta: ImageView
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: AppCompatButton
    private var plantaId: Int = 0
    private var usuarioId: Int = 0
    private var imagenUrl: String = ""
    private val viewModel: PlantasViewModel by viewModels()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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

        findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            finish()
        }

        initViews()
        setupDrawerLayout()
        swipeRefreshLayout = findViewById(R.id.swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener { cargarDatosPlanta() }
        cargarDatosPlanta()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.main)
        etNombrePlanta = findViewById(R.id.et_nombre_planta)
        etUbicacionPlanta = findViewById(R.id.ubi_planta)
        etSensorId = findViewById(R.id.id_Sensor_planta)
        ivPlanta = findViewById(R.id.iv_planta)
        btnGuardar = findViewById(R.id.btn_guardar_canvis)
        btnEliminar = findViewById(R.id.btn_eliminar_planta)
        btnGuardar.setOnClickListener { guardarCambios() }
        btnEliminar.setOnClickListener { mostrarDialogoConfirmacion() }
    }

    private fun setupDrawerLayout() {
        val menuIcon: ImageView = findViewById(R.id.menu_icon)
        menuIcon.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
    }

    private fun cargarDatosPlanta() {
        plantaId = intent.getIntExtra("planta_id", 0)
        usuarioId = intent.getIntExtra("usuario_id", 0)

        if (plantaId == 0) {
            Toast.makeText(this, "ID de planta inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = EcosenseApiClient.service.obtenerPlanta(plantaId)
                if (response.isSuccessful) {
                    val planta = response.body()
                    planta?.let {
                        etNombrePlanta.setText(it.nom)
                        etUbicacionPlanta.setText(it.ubicacio)
                        etSensorId.setText(it.sensor_id.toString())
                        imagenUrl = it.imagen_url ?: ""

                        if (imagenUrl.isNotEmpty()) {
                            val baseUrl = ApiConfig.baseUrl
                            val imageUrl = if (imagenUrl.startsWith("http")) {
                                imagenUrl
                            } else {
                                "$baseUrl${if (imagenUrl.startsWith("/")) "" else "/"}$imagenUrl"
                            }
                            Glide.with(this@ModificarPlanta)
                                .load(imageUrl)
                                .placeholder(R.drawable.plants)
                                .error(R.drawable.plants)
                                .into(ivPlanta)
                        }
                    }
                } else {
                    Toast.makeText(this@ModificarPlanta, "Error al cargar planta", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ModificarPlanta, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }


    private fun guardarCambios() {
        val nuevoNombre = etNombrePlanta.text.toString()
        val nuevaUbicacion = etUbicacionPlanta.text.toString()
        val nuevoSensorId = etSensorId.text.toString().toIntOrNull() ?: 0

        if (nuevoNombre.isEmpty() || nuevaUbicacion.isEmpty() || nuevoSensorId == 0) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val sensorResponse = EcosenseApiClient.service.checkSensorExists(nuevoSensorId)
                if (!sensorResponse.isSuccessful) {
                    Toast.makeText(this@ModificarPlanta, "Error: El sensor $nuevoSensorId no existe", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val plantaUpdate = PlantaUpdateRequest(
                    nom = nuevoNombre,
                    ubicacio = nuevaUbicacion,
                    sensor_id = nuevoSensorId,
                    imagen_url = if (imagenUrl.isEmpty()) null else imagenUrl
                )

                val updateResponse = EcosenseApiClient.service.actualizarPlanta(plantaId, plantaUpdate)

                if (updateResponse.isSuccessful) {
                    Toast.makeText(this@ModificarPlanta, "¡Planta actualizada!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ModificarPlanta, PantallaHome::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("usuario_id", usuarioId)
                    }
                    startActivity(intent)
                } else {
                    val errorMsg = updateResponse.errorBody()?.string() ?: "Error desconocido"
                    Toast.makeText(this@ModificarPlanta, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) { Toast.makeText(this@ModificarPlanta, "Error de red: ${e.message}", Toast.LENGTH_LONG).show() }
        }
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(this).setTitle("Eliminar Planta")
            .setMessage("¿Estás seguro de que quieres eliminar esta planta?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarPlanta() }
            .setNegativeButton("Cancelar", null)
            .setCancelable(true)
            .show()
    }

    private fun eliminarPlanta() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = EcosenseApiClient.service.eliminarPlanta(plantaId)
                if (response.isSuccessful) {
                    viewModel.notifyDataUpdated()
                    Toast.makeText(this@ModificarPlanta, "Planta eliminada correctamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ModificarPlanta, PantallaHome::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("usuario_id", usuarioId)
                    }
                    startActivity(intent)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Toast.makeText(this@ModificarPlanta, "Error al eliminar: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) { Toast.makeText(this@ModificarPlanta, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show() }
        }
    }
}