package com.example.myapitest.service

import com.example.myapitest.model.Car

interface ApiService {
    fun getCars(): List<Car>
}