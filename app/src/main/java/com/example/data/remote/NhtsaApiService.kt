package com.example.data.remote

import com.example.data.model.NhtsaDecodeResponse
import com.example.data.model.NhtsaRecallsResponse
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NhtsaApiService {

    @GET("api/vehicles/decodevinvalues/{vin}?format=json")
    suspend fun decodeVin(
        @Path("vin") vin: String
    ): NhtsaDecodeResponse

    companion object {
        private const val VPIC_BASE_URL = "https://vpic.nhtsa.dot.gov/"

        fun create(): NhtsaApiService = Retrofit.Builder()
            .baseUrl(VPIC_BASE_URL)
            // اشتراک کلاینت و Moshi (بهینه‌سازی: حذف ساخت تکراری OkHttp/Moshi)
            .client(HttpClientProvider.sharedClient)
            .addConverterFactory(MoshiConverterFactory.create(HttpClientProvider.sharedMoshi))
            .build()
            .create(NhtsaApiService::class.java)
    }
}

interface NhtsaRecallsApiService {

    @GET("recalls/recallsByVin?format=json")
    suspend fun getRecalls(
        @Query("vin") vin: String
    ): NhtsaRecallsResponse

    companion object {
        private const val RECALLS_BASE_URL = "https://api.nhtsa.gov/"

        fun create(): NhtsaRecallsApiService = Retrofit.Builder()
            .baseUrl(RECALLS_BASE_URL)
            .client(HttpClientProvider.sharedClient)
            .addConverterFactory(MoshiConverterFactory.create(HttpClientProvider.sharedMoshi))
            .build()
            .create(NhtsaRecallsApiService::class.java)
    }
}
