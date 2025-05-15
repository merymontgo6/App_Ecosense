package com.example.app_ecosense.info

import com.example.app_ecosense.models.Planta
import com.example.app_ecosense.models.PlantaDetailModelo
import com.example.app_ecosense.models.Sensor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Path
import java.sql.Date
import java.util.concurrent.TimeUnit

interface EcosenseApiService {

    @GET("plantes/complet/{planta_id}")
    suspend fun getDetallePlanta(@Path("planta_id") plantaId: Int): Response<PlantaDetailModelo>

    @GET("plantes/por-zones")
    suspend fun getPlantasPorZonas(
        @Query("usuari_id") usuari_id: Int
    ): List<ZonaResponse>

    @GET("plantes/{id}")
    suspend fun obtenerPlanta(@Path("id") id: Int): Response<Planta>

    @PUT("plantes/{planta_id}")
    suspend fun actualizarPlanta(
        @Path("planta_id") plantaId: Int,
        @Body planta: PlantaUpdateRequest
    ): Response<PlantaResponse>

    @DELETE("plantes/{planta_id}")
    suspend fun eliminarPlanta(
        @Path("planta_id") plantaId: Int
    ): Response<Unit>

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

data class PlantaUpdateRequest(
    val nom: String? = null,
    val ubicacio: String? = null,
    val sensor_id: Int? = null,
    val imagen_url: String? = null
)

data class HumitatResponse(
    val sensor_id: Int,
    val valor: Float,
    val timestamp: Date
)

object ApiConfig {
    //private const val BASE_URL = "http://192.168.5.206:8000"
    //private const val BASE_URL = "http://18.213.199.248:8000"
    private const val BASE_URL = "http://192.168.17.240:8000"
    val baseUrl: String
        get() = BASE_URL
}

object EcosenseApiClient {
    //private const val BASE_URL = "http://18.213.199.248:8000"
    private const val BASE_URL = "http://192.168.17.240:8000"
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
}