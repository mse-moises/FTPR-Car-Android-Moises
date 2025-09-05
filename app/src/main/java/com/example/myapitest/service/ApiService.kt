package com.example.myapitest.service

import com.example.myapitest.model.Car
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Body
import retrofit2.http.Path

interface ApiService {
    @GET("car") suspend fun getCars(): List<Car>
    @POST("car") suspend fun createCar(@Body car: Car): Car
    @PATCH("car/{id}") suspend fun updateCar(@Path("id") id: String, @Body car: Car): Car
}