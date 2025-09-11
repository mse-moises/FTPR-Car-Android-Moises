package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.example.myapitest.service.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewCarActivity : AppCompatActivity() {
    private val mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { intent ->
                selectedLat = intent.getDoubleExtra("latitude", 0.0)
                selectedLong = intent.getDoubleExtra("longitude", 0.0)
                tvCoordinates.text = "Coordenadas: $selectedLat, $selectedLong"
            }
        }
    }
    private lateinit var edtModel: EditText
    private lateinit var edtYear: EditText
    private lateinit var edtPrice: EditText
    private lateinit var btnSave: Button
    private lateinit var btnSelectLocation: Button
    private lateinit var tvCoordinates: TextView
    private var selectedLat: Double = 0.0
    private var selectedLong: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_car)

        edtModel = findViewById(R.id.edtModel)
        edtYear = findViewById(R.id.edtYear)
        edtPrice = findViewById(R.id.edtPrice)
        btnSave = findViewById(R.id.btnSave)
        btnSelectLocation = findViewById(R.id.btnSelectLocation)
        tvCoordinates = findViewById(R.id.tvCoordinates)

        btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            mapLauncher.launch(intent)
        }

        btnSave.setOnClickListener {
            val name = edtModel.text.toString()
            val year = edtYear.text.toString()
            val license = edtPrice.text.toString()

            if (name.isBlank() || year.isBlank() || license.isBlank()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val car = Car(
                id = System.currentTimeMillis().toString(),
                name = name,
                year = year,
                licence = license,
                imageUrl = "https://cdn.motor1.com/images/mgl/0eJY29/s1/os-50-carros-mais-caros-do-mundo.webp",
                place = Place(lat = selectedLat, long = selectedLong)
            )
            saveCar(car)
        }
    }

    private fun saveCar(car: Car) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.apiService.createCar(car)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NewCarActivity, "Carro salvo com sucesso", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NewCarActivity, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
