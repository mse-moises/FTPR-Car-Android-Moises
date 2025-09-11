package com.example.myapitest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.example.myapitest.service.RetrofitClient
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NewCarActivity : AppCompatActivity(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private val mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { intent ->
                selectedLat = intent.getDoubleExtra("latitude", 0.0)
                selectedLong = intent.getDoubleExtra("longitude", 0.0)
                tvCoordinates.text = "Coordenadas: $selectedLat, $selectedLong"
                updateMapLocation()
                validateFields()
            }
        }
    }
    private lateinit var edtModel: EditText
    private lateinit var edtYear: EditText
    private lateinit var edtPrice: EditText
    private lateinit var btnSave: Button
    private lateinit var btnSelectLocation: Button
    private lateinit var btnSelectImage: Button
    private lateinit var imgPreview: ImageView
    private lateinit var tvCoordinates: TextView
    private lateinit var uploadProgressContainer: LinearLayout
    private lateinit var uploadProgressBar: ProgressBar
    private lateinit var tvUploadProgress: TextView
    private var selectedLat: Double = 0.0
    private var selectedLong: Double = 0.0
    private var selectedImageUri: Uri? = null
    
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            imgPreview.setImageURI(it)
            validateFields()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_form)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Novo Carro"
        }

        edtModel = findViewById(R.id.edtModel)
        edtYear = findViewById(R.id.edtYear)
        edtPrice = findViewById(R.id.edtPrice)
        btnSave = findViewById(R.id.btnSave)
        btnSelectLocation = findViewById(R.id.btnSelectLocation)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        imgPreview = findViewById(R.id.imgPreview)
        tvCoordinates = findViewById(R.id.tvCoordinates)
        uploadProgressContainer = findViewById(R.id.uploadProgressContainer)
        uploadProgressBar = findViewById(R.id.uploadProgressBar)
        tvUploadProgress = findViewById(R.id.tvUploadProgress)

        // Inicializa o mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Desabilita o botão inicialmente
        btnSave.isEnabled = false

        // Adiciona listeners para validar os campos
        edtModel.addTextChangedListener { validateFields() }
        edtYear.addTextChangedListener { validateFields() }
        edtPrice.addTextChangedListener { validateFields() }
        
        btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

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

    private suspend fun uploadImage(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("cars/${System.currentTimeMillis()}.jpg")
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
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

    private fun validateFields() {
        val isNameValid = edtModel.text.toString().isNotBlank()
        val isYearValid = edtYear.text.toString().isNotBlank()
        val isLicenseValid = edtPrice.text.toString().isNotBlank()
        val isLocationValid = selectedLat != 0.0 || selectedLong != 0.0
        val isImageValid = selectedImageUri != null

        btnSave.isEnabled = isNameValid && isYearValid && isLicenseValid && isLocationValid && isImageValid
    }

    private fun saveCar(car: Car) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Selecione uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        // Desabilita o botão e mostra loading
        btnSave.isEnabled = false
        uploadProgressContainer.visibility = View.VISIBLE
        uploadProgressBar.progress = 0
        tvUploadProgress.text = "Iniciando upload..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Upload da imagem com progresso
                val imageUrl = withContext(Dispatchers.IO) {
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef = storageRef.child("cars/${System.currentTimeMillis()}_${selectedImageUri!!.lastPathSegment}")
                    val uploadTask = imageRef.putFile(selectedImageUri!!)

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

                val carWithImage = car.copy(imageUrl = imageUrl)
                RetrofitClient.apiService.createCar(carWithImage)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NewCarActivity, "Carro salvo com sucesso", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NewCarActivity, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSave.isEnabled = true
                    uploadProgressContainer.visibility = View.GONE
                }
            }
        }
    }
}
