package com.example.app_ecosense.comptes

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_ecosense.R
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.EcosenseApiClient
import com.example.app_ecosense.info.RegistreRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CrearCompte : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ocultarBarra(window).hideSystemBar()
        setContentView(R.layout.activity_crear_compte)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val crearCompte: AppCompatButton = findViewById(R.id.cc)
        crearCompte.setOnClickListener {
            registrarUsuario()
        }

        val iniciarSessio: TextView = findViewById(R.id.iniciarSessio)
        iniciarSessio.setOnClickListener {
            val intent = Intent(this, IniciSessio::class.java)
            startActivity(intent)
        }
    }

    private fun registrarUsuario() {
        val nom = findViewById<EditText>(R.id.usernameEditText).text.toString().trim()
        val cognom = findViewById<EditText>(R.id.cognom).text.toString().trim()
        val email = findViewById<EditText>(R.id.adreca).text.toString().trim()
        val contrasenya = findViewById<EditText>(R.id.contra).text.toString().trim()
        val confirmarContrasenya = findViewById<EditText>(R.id.confirmarc).text.toString().trim()

        // Validaciones
        if (nom.isEmpty() || cognom.isEmpty() || email.isEmpty() || contrasenya.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, ingrese un email válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (contrasenya != confirmarContrasenya) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        if (contrasenya.length < 6) {
            Toast.makeText(
                this,
                "La contraseña debe tener al menos 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Registrando usuario...")
            setCancelable(false)
            show()
        }

        // Llamada a la API
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = EcosenseApiClient.service.registrarUsuario(
                    RegistreRequest(nom, cognom, email, contrasenya)
                )

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()

                    if (response.isSuccessful) {
                        val registroResponse = response.body()
                        when {
                            registroResponse?.success == true -> {
                                Toast.makeText(
                                    this@CrearCompte,
                                    "Registro exitoso. Por favor inicie sesión.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this@CrearCompte, IniciSessio::class.java))
                                finish()
                            }

                            !registroResponse?.message.isNullOrEmpty() -> {
                                Toast.makeText(
                                    this@CrearCompte,
                                    registroResponse?.message ?: "Error en el registro",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                Toast.makeText(
                                    this@CrearCompte,
                                    "Error desconocido en el registro",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@CrearCompte,
                            "Error del servidor: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@CrearCompte,
                        "Error de conexión: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("API_ERROR", "Error al registrar usuario", e)
                }
            }
        }
    }
}