package com.example.app_ecosense.comptes

import android.app.ProgressDialog
import android.content.Context
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
import com.example.app_ecosense.PantallaHome
import com.example.app_ecosense.R
import com.example.app_ecosense.accesibilitat.ocultarBarra
import com.example.app_ecosense.info.EcosenseApiClient
import com.example.app_ecosense.info.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IniciSessio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ocultarBarra(window).hideSystemBar()
        setContentView(R.layout.activity_inici_sessio)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val oblidarContrassenya: TextView = findViewById(R.id.oblidarC)
        oblidarContrassenya.setOnClickListener {
            val intent = Intent(this, RecuperarContrassenya::class.java)
            startActivity(intent)
        }

        val iniciSessio: AppCompatButton = findViewById(R.id.iniciarSessio)
        iniciSessio.setOnClickListener {
            val username = findViewById<EditText>(R.id.usernameEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            iniciarSesion(username, password)
        }

        val crearCompte: TextView = findViewById(R.id.cc)
        crearCompte.setOnClickListener {
            val intent = Intent(this, CrearCompte::class.java)
            startActivity(intent)
        }
    }

    private fun iniciarSesion(email: String, password: String) {
        // Mostrar diálogo de progreso
        val progressDialog = ProgressDialog(this).apply {
            setTitle("Iniciando sesión")
            setMessage("Por favor espere...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = EcosenseApiClient.service.loginUsuario(
                    LoginRequest(email, password))

                            withContext(Dispatchers.Main) {
                        progressDialog.dismiss()

                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            when {
                                loginResponse?.success == true -> {
                                    // Guardar datos del usuario
                                    val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        putInt("user_id", loginResponse.usuari_id ?: 0)
                                        putString("user_name", loginResponse.nom ?: "")
                                        putString("user_email", loginResponse.email ?: "")
                                        apply()
                                    }

                                    // Ir a la pantalla principal
                                    startActivity(Intent(this@IniciSessio, PantallaHome::class.java))
                                    finish()
                                }
                                !loginResponse?.message.isNullOrEmpty() -> {
                                    showToast(loginResponse?.message ?: "Credenciales incorrectas")
                                }
                                else -> {
                                    showToast("Error en las credenciales")
                                }
                            }
                        } else {
                            showToast("Error del servidor: ${response.code()}")
                        }
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    showToast("Error de conexión: ${e.message}")
                    Log.e("LoginError", "Error en inicio de sesión", e)
                }
            }
        }
    }

    // Función auxiliar para mostrar Toast
    private fun showToast(message: String) {
        Toast.makeText(this@IniciSessio, message, Toast.LENGTH_LONG).show()
    }
}