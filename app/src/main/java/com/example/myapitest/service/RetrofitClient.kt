package com.example.myapitest.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.104:3000/"

    private val instance: Retrofit by lazy {
        Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
    }

    val apiService = instance.create(ApiService::class.java)
}