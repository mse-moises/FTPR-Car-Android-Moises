package com.example.myapitest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private var selectedLocation: LatLng? = null
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            map.isMyLocationEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            selectedLocation?.let { location ->
                val intent = Intent().apply {
                    putExtra("latitude", location.latitude)
                    putExtra("longitude", location.longitude)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        map.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        
        val initialLat = intent.getDoubleExtra("latitude", -23.5505)
        val initialLong = intent.getDoubleExtra("longitude", -46.6333)
        val initialLocation = LatLng(initialLat, initialLong)
        
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))
        map.addMarker(MarkerOptions().position(initialLocation))
        selectedLocation = initialLocation
        
        checkLocationPermission()

        map.setOnMapClickListener { latLng ->
            map.clear()
            selectedLocation = latLng
            map.addMarker(MarkerOptions().position(latLng))
        }
    }
}
