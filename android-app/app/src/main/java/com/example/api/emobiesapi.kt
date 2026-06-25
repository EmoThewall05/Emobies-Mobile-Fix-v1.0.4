package com.example.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// ─── Request / Response Models ───────────────────────────

data class LoginRequest(val password: String)

data class LoginResponse(
    val token: String?,
    val role: String?,
    val error: String?
)

data class RepairApiModel(
    val device: String,
    val customerLocation: String?,
    val status: String
)

// ─── API Interface ────────────────────────────────────────

interface EmobiesApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/repairs")
    suspend fun getRepairs(): List<RepairApiModel>
}

// ─── Retrofit Client ──────────────────────────────────────

object RetrofitClient {
    private const val BASE_URL = "https://emobies-server.onrender.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val apiService: EmobiesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EmobiesApiService::class.java)
    }
}
