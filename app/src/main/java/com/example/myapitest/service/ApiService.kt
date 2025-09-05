package com.example.myapitest.service

import com.example.myapitest.model.Car
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface ApiService {
    @GET("car") suspend fun getCars(): List<Car>
    @POST("car") suspend fun createCar(@Body car: Car): Car
}