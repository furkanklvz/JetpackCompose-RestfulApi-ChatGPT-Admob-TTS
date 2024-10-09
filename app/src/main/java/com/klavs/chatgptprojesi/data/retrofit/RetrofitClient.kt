package com.klavs.chatgptprojesi.data.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {
    companion object {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // Bağlantı zaman aşımı
            .readTimeout(30, TimeUnit.SECONDS)     // Yanıt okuma zaman aşımı
            .writeTimeout(30, TimeUnit.SECONDS)    // Veri yazma zaman aşımı
            .build()

        fun GetClient(baseUrl: String): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}