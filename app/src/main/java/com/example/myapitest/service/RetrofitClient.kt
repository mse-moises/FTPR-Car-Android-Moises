package com.example.myapitest.service

import retrofit2.Retrofit

object RetrofitClient {
    private const val BASE_URL = "https://localhost:3000"

    private val instance: Retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).build()
    }

    val apiService = instance.create(ApiService::class.java)
}