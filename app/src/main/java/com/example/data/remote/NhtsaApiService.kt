package com.example.data.remote

import com.example.data.model.NhtsaDecodeResponse
import com.example.data.model.NhtsaRecallsResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface NhtsaApiService {

    @GET("api/vehicles/decodevinvalues/{vin}?format=json")
    suspend fun decodeVin(
        @Path("vin") vin: String
    ): NhtsaDecodeResponse

    companion object {
        private const val VPIC_BASE_URL = "https://vpic.nhtsa.dot.gov/"

        fun create(): NhtsaApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(VPIC_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(NhtsaApiService::class.java)
        }
    }
}

interface NhtsaRecallsApiService {

    @GET("recalls/recallsByVin?format=json")
    suspend fun getRecalls(
        @Query("vin") vin: String
    ): NhtsaRecallsResponse

    companion object {
        private const val RECALLS_BASE_URL = "https://api.nhtsa.gov/"

        fun create(): NhtsaRecallsApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(RECALLS_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(NhtsaRecallsApiService::class.java)
        }
    }
}
