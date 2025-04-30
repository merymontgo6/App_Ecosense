package com.example.app_ecosense.info

import android.util.Log
import com.example.app_ecosense.models.Planta
import com.example.app_ecosense.models.ZonaResponse
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
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
}

object EcosenseApiClient {
    private const val BASE_URL = "http://192.168.17.240:8000"

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