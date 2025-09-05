package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapter.CarAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.model.Car
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.myapitest.service.Result
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestLocationPermission()
        setupView()

        // 1- Criar tela de Login com algum provedor do Firebase (Telefone, Google)
        //      Cadastrar o Seguinte celular para login de test: +5511912345678
        //      Código de verificação: 101010

        // 2- Criar Opção de Logout no aplicativo

        // 3- Integrar API REST /car no aplicativo
        //      API será disponibilida no Github
        //      JSON Necessário para salvar e exibir no aplicativo
        //      O Image Url deve ser uma foto armazenada no Firebase Storage
        //      { "id": "001", "imageUrl":"https://image", "year":"2020/2020", "name":"Gaspar", "licence":"ABC-1234", "place": {"lat": 0, "long": 0} }

        // Opcionalmente trabalhar com o Google Maps ara enviar o place
    }

    override fun onResume() {
        super.onResume()
        fetchCars()
    }

    private fun setupView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.fabAddCar.setOnClickListener {
            startActivity(android.content.Intent(this, NewCarActivity::class.java))
        }
    }

    private fun requestLocationPermission() {
        // TODO
    }

    private fun openEditCarActivity(car: Car) {
        val intent = Intent(this, EditCarActivity::class.java).apply {
            putExtra("car_id", car.id)
            putExtra("car_name", car.name)
            putExtra("car_year", car.year)
            putExtra("car_licence", car.licence)
            putExtra("car_image_url", car.imageUrl)
            putExtra("car_lat", car.place.lat)
            putExtra("car_long", car.place.long)
        }
        startActivity(intent)
    }

    private fun fetchCars() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCars() }

            withContext(Dispatchers.Main){
                when(result){
                    is Result.Success -> {
                        val adapter = CarAdapter(result.data) { car ->
                            openEditCarActivity(car)
                        }
                        binding.recyclerView.adapter = adapter
                    }
                    is Result.Error -> {
                        Toast.makeText(this@MainActivity, "Erro", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        }
    }


}
