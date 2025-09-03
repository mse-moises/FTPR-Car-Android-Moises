package com.example.myapitest.model

data class Place(
    val lat: Double,
    val long: Double
)

data class Car(
    val imageUrl: String,
    val year: String,
    val name: String,
    val license: String,
    val place: Place
)