package com.example.myapitest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.example.myapitest.service.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class EditCarActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private lateinit var edtModel: EditText
    private lateinit var edtYear: EditText
    private lateinit var edtPrice: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnSelectLocation: Button
    private lateinit var btnSelectImage: Button
    private lateinit var imgPreview: ImageView
    private lateinit var tvCoordinates: TextView
    private lateinit var uploadProgressContainer: LinearLayout
    private lateinit var uploadProgressBar: ProgressBar
    private lateinit var tvUploadProgress: TextView
    private var carId: String = ""
    private var selectedLat: Double = 0.0
    private var selectedLong: Double = 0.0
    private var googleMap: GoogleMap? = null
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            imgPreview.setImageURI(it)
        }
    }

    private val mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { intent ->
                selectedLat = intent.getDoubleExtra("latitude", 0.0)
                selectedLong = intent.getDoubleExtra("longitude", 0.0)
                tvCoordinates.text = "Coordenadas: $selectedLat, $selectedLong"
                updateMapLocation()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_form)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Editar Carro"
        }

        edtModel = findViewById(R.id.edtModel)
        edtYear = findViewById(R.id.edtYear)
        edtPrice = findViewById(R.id.edtPrice)
        btnUpdate = findViewById(R.id.btnSave)
        btnUpdate.text = "Atualizar"
        btnSelectLocation = findViewById(R.id.btnSelectLocation)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        imgPreview = findViewById(R.id.imgPreview)
        tvCoordinates = findViewById(R.id.tvCoordinates)
        uploadProgressContainer = findViewById(R.id.uploadProgressContainer)
        uploadProgressBar = findViewById(R.id.uploadProgressBar)
        tvUploadProgress = findViewById(R.id.tvUploadProgress)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        selectedLat = intent.getDoubleExtra("car_lat", 0.0)
        selectedLong = intent.getDoubleExtra("car_long", 0.0)
        tvCoordinates.text = "Coordenadas: $selectedLat, $selectedLong"
        updateMapLocation()

        btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("latitude", selectedLat)
            intent.putExtra("longitude", selectedLong)
            mapLauncher.launch(intent)
        }

        // Get car data from intent
        carId = intent.getStringExtra("car_id") ?: ""
        edtModel.setText(intent.getStringExtra("car_name"))
        edtYear.setText(intent.getStringExtra("car_year"))
        edtPrice.setText(intent.getStringExtra("car_licence"))

        // Carrega a imagem atual do carro
        val currentImageUrl = intent.getStringExtra("car_image_url")
        if (!currentImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(currentImageUrl)
                .into(imgPreview)
        }

        if (carId.isEmpty()) {
            Toast.makeText(this, "Erro: ID do carro não fornecido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnUpdate.setOnClickListener {
            val name = edtModel.text.toString()
            val year = edtYear.text.toString()
            val license = edtPrice.text.toString()

            if (name.isBlank() || year.isBlank() || license.isBlank()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val car = Car(
                id = carId,
                name = name,
                year = year,
                licence = license,
                imageUrl = intent.getStringExtra("car_image_url") ?: "",
                place = Place(
                    lat = selectedLat,
                    long = selectedLong
                )
            )
            updateCar(car)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.setAllGesturesEnabled(false)
        updateMapLocation()
    }

    private fun updateMapLocation() {
        googleMap?.let { map ->
            val location = LatLng(selectedLat, selectedLong)
            map.clear()
            map.addMarker(MarkerOptions()
                .position(location)
                .title("Localização do Carro"))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    private fun updateCar(car: Car) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var imageUrl = car.imageUrl
                selectedImageUri?.let { uri ->
                    withContext(Dispatchers.Main) {
                        uploadProgressContainer.visibility = View.VISIBLE
                        btnUpdate.isEnabled = false
                        uploadProgressBar.progress = 0
                        tvUploadProgress.text = "Iniciando upload..."
                    }

                    try {
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference
                        val imageRef = storageRef.child("cars/${System.currentTimeMillis()}_${uri.lastPathSegment}")
                        
                        imageUrl = withContext(Dispatchers.IO) {
                            val uploadTask = imageRef.putFile(uri)
                            
                            uploadTask.addOnProgressListener { taskSnapshot ->
                                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                                runOnUiThread {
                                    uploadProgressBar.progress = progress
                                    tvUploadProgress.text = "Fazendo upload: $progress%"
                                }
                            }

                            uploadTask.await()
                            imageRef.downloadUrl.await().toString()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditCarActivity, "Erro no upload: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        throw e
                    } finally {
                        withContext(Dispatchers.Main) {
                            uploadProgressContainer.visibility = View.GONE
                            btnUpdate.isEnabled = true
                        }
                    }
                }

                val updatedCar = car.copy(imageUrl = imageUrl)
                RetrofitClient.apiService.updateCar(car.id, updatedCar)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditCarActivity, "Carro atualizado com sucesso", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditCarActivity, "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
