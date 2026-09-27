package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * اشتراک یک کلاینت شبکه و یک Moshi بین تمام سرویس‌های Retrofit.
 *
 * بهینه‌سازی‌ها نسبت به نسخه قبلی (ساخت OkHttp/Moshi تکراری در هر سرویس):
 * - Pool اتصالات مشترک => حذف handshake مجدد TLS برای vpic/api.nhtsa
 * - Dispatcher با کوئول مشخص => جلوگیری از اشباع اتصال هنگام استعلام‌های موازی
 * - غیرفعال‌سازی لاگ HTTP در نسخه Release (I/O بی‌مورد روی مسیر شبکه)
 * - callTimeout سراسری برای جلوگیری از درخواست‌های معلق بی‌پایان
 */
object HttpClientProvider {

    val sharedMoshi: Moshi by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val sharedClient: OkHttpClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val logging = HttpLoggingInterceptor().apply {
            // فقط در Debug لاگ بزن؛ در Release صفر هزینه I/O
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val dispatcher = Dispatcher().apply {
            maxRequests = 8
            maxRequestsPerHost = 4
        }

        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            .build()
    }
}
