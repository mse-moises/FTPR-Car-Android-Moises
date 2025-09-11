package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ViewCarActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private var carLat: Double = 0.0
    private var carLong: Double = 0.0
    private var carName: String = ""
    private lateinit var imgCar: ImageView
    private lateinit var tvCarName: TextView
    private lateinit var tvCarYear: TextView
    private lateinit var tvCarLicence: TextView
    private lateinit var btnEdit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_car)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Detalhes do Carro"
        }

        imgCar = findViewById(R.id.imgCar)
        tvCarName = findViewById(R.id.tvCarName)
        tvCarYear = findViewById(R.id.tvCarYear)
        tvCarLicence = findViewById(R.id.tvCarLicence)
        btnEdit = findViewById(R.id.btnEdit)

        val carId = intent.getStringExtra("car_id") ?: ""
        carName = intent.getStringExtra("car_name") ?: ""
        val carYear = intent.getStringExtra("car_year") ?: ""
        val carLicence = intent.getStringExtra("car_licence") ?: ""
        val carImageUrl = intent.getStringExtra("car_image_url") ?: ""
        carLat = intent.getDoubleExtra("car_lat", 0.0)
        carLong = intent.getDoubleExtra("car_long", 0.0)

        tvCarName.text = carName
        tvCarYear.text = carYear
        tvCarLicence.text = carLicence

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (carImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(carImageUrl)
                .into(imgCar)
        }

        btnEdit.setOnClickListener {
            val intent = Intent(this, EditCarActivity::class.java).apply {
                putExtra("car_id", carId)
                putExtra("car_name", carName)
                putExtra("car_year", carYear)
                putExtra("car_licence", carLicence)
                putExtra("car_image_url", carImageUrl)
                putExtra("car_lat", carLat)
                putExtra("car_long", carLong)
            }
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val carLocation = LatLng(carLat, carLong)
        
        googleMap.apply {
            // Desabilita todos os gestos do mapa
            uiSettings.setAllGesturesEnabled(false)
            // Desabilita os controles de zoom
            uiSettings.isZoomControlsEnabled = false
            
            // Adiciona o marcador
            addMarker(MarkerOptions()
                .position(carLocation)
                .title(carName))
            
            // Move a câmera para a posição do carro com zoom
            moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 15f))
        }
    }
}
