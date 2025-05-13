package com.example.app_ecosense.info

import android.util.Log
import com.example.app_ecosense.models.Planta
import com.example.app_ecosense.models.PlantaDetailModelo
import com.example.app_ecosense.models.Sensor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface EcosenseApiService {
    @GET("plantes/")
    suspend fun getPlantasPorUsuario(
        @Query("usuari_id") usuari_id: Int
    ): List<Planta>

    @GET("plantes/{planta_id}")
    suspend fun getDetallePlanta(
        @Path("planta_id") planta_id: Int
    ): Response<Planta>

    @GET("plantes/por-zones")
    suspend fun getPlantasPorZonas(
        @Query("usuari_id") usuari_id: Int
    ): List<ZonaResponse>

    @GET("plantas/{id}")
    suspend fun getPlantaDetail(@Path("id") plantaId: Int): PlantaDetailModelo

    @GET("sensors/{sensor_id}")
    suspend fun checkSensorExists(@Path("sensor_id") sensorId: Int): Response<Sensor>

    @POST("plantes/")
    suspend fun crearPlanta(
        @Body planta: PlantaCreateRequest
    ): Response<PlantaResponse>

    @POST("usuaris/login")
    suspend fun loginUsuario(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("usuaris/registre")
    suspend fun registrarUsuario(
        @Body registroRequest: RegistreRequest
    ): Response<RegistreResponse>

    @GET("humitat/{sensor_id}")
    suspend fun getHumitatActual(
        @Path("sensor_id") sensorId: Int
    ): Response<HumitatResponse>
}

data class LoginRequest(
    val email: String,
    val contrasenya: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val usuari_id: Int?,
    val nom: String?,
    val email: String?
)

data class RegistreRequest(
    val nom: String,
    val cognom: String,
    val email: String,
    val contrasenya: String
)

data class RegistreResponse(
    val success: Boolean,
    val message: String?,
    val usuari_id: Int?,
    val nom: String?,
    val email: String?
)

data class ZonaResponse(
    val zona: String,
    val plantas: List<Planta>
)

data class PlantaCreateRequest(
    val nom: String,
    val ubicacio: String,
    val sensor_id: Int,
    val usuari_id: Int? = null,
    val imagen_url: String? = null
)

data class PlantaResponse(
    val id: Int,
    val nom: String,
    val ubicacio: String,
    val sensor_id: Int,
    val usuari_id: Int?,
    val imagen_url: String?
)

data class HumitatResponse(
    val sensor_id: Int,
    val valor: Float,
    val timestamp: String
)

object ApiConfig {
    private const val BASE_URL = "http://18.213.199.248:8000"
    val baseUrl: String
        get() = BASE_URL
}

object EcosenseApiClient {
    //private const val BASE_URL = "http://192.168.5.206:8000"
    //private const val BASE_URL = "http://192.168.17.240:8000"
    private const val BASE_URL = "http://18.213.199.248:8000"


    // Configuración optimizada del cliente HTTP
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .hostnameVerifier { _, _ -> true }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: EcosenseApiService = retrofit.create(EcosenseApiService::class.java)

    // Configuración SSL insegura (solo para desarrollo)
    private val unsafeTrustManager = object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private val unsafeSslSocketFactory by lazy {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(unsafeTrustManager), SecureRandom())
        sslContext.socketFactory
    }

    /**
     * Función de conveniencia para obtener las plantas agrupadas por zona
     */
    suspend fun getPlantasAgrupadasPorZona(usuarioId: Int): Map<String, List<Planta>> {
        return try {
            val plantas = service.getPlantasPorUsuario(usuarioId)
            Log.d("API_DEBUG", "Plantas recibidas: ${plantas.size}")
            plantas.forEach { planta ->
                Log.d("API_DEBUG", "Planta: ${planta.nom} - Ubicación: ${planta.ubicacio}")
            }
            plantas.groupBy { it.ubicacio }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error al obtener plantas", e)
            emptyMap()
        }
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}