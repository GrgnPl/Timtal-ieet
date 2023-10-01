package com.sarj.timtalieet.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonParser
import com.sarj.timtalieet.Adapter.BusStopAdapter
import com.sarj.timtalieet.Class.BusStop
import com.sarj.timtalieet.Class.GetAccessToken
import com.sarj.timtalieet.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val url = "https://ntcapi.iett.istanbul/service"
    private val accessTokenHelper = GetAccessToken()
    private lateinit var busStopAdapter: BusStopAdapter
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the RecyclerView and set its layout manager and adapter

        val recyclerView =binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        busStopAdapter = BusStopAdapter(emptyList())
        recyclerView.adapter = busStopAdapter
        // Check for location permission and start location updates
        if (checkLocationPermission()) {
            startLocationUpdates()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            false
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                println("latitude $latitude")
                println("longitude $longitude")

                lifecycleScope.launch {
                    val accessToken = accessTokenHelper.getAccessTokenFromApi()
                    val sp = getSharedPreferences("degerler", MODE_PRIVATE)
                    val editor = sp.edit()
                    editor.putString("access_key",accessToken)
                    editor.apply()
                    if (accessToken.isNotEmpty()) {
                        val busStops = getBusStop(accessToken, latitude, longitude)
                        busStopAdapter.updateBusStops(busStops)
                        val nearestBusStop = findNearestBusStop(latitude, longitude, busStops)
                        binding.entext.text = latitude.toString()
                        binding.boytext.text = longitude.toString()
                        CoroutineScope(Dispatchers.Main).launch {
                            if (nearestBusStop != null) {
                                binding.yakinduraktext.text = "En Yakın Durak : ${nearestBusStop.DURAK_ADI}"
                            } else {
                                binding.yakinduraktext.text = "Boş"
                            }
                        }

                        println("Bus Stops: $busStops")
                    } else {
                        Log.e("hata", "Access Token alınamadı.")
                    }
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }

        // Request location updates

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            locationListener
        )
    }

    // Fetch bus stops based on the location
    private suspend fun getBusStop(accessKey: String, lat: Double, long: Double): List<BusStop> {
        val jsonData = """
        {
            "alias": "mainGetBusStopNearby",
            "data": {
                "HATYONETIM.DURAK.GEOLOC": {
                    "fromSRID": "7932",
                    "lat": "$lat",
                    "long": "$long",
                    "r": "0.5"
                }
            }
        }
    """.trimIndent()

        val client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, jsonData)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Accept-Encoding", "gzip")
            .addHeader("Authorization", "Bearer $accessKey")
            .addHeader("Connection", "Keep-Alive")
            .addHeader("Content-Length", "189")
            .addHeader("Content-Type", "application/json; charset=UTF-8")
            .addHeader("Host", "ntcapi.iett.istanbul")
            .addHeader("User-Agent", "okhttp/5.0.0-alpha.11 https://ntcapi.iett.istanbul/service")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonArray = JsonParser.parseString(responseBody).asJsonArray

                    val busStops = mutableListOf<BusStop>()

                    jsonArray.forEach { element ->
                        val busStopJson = element.asJsonObject

                        val durakAdi = busStopJson.get("DURAK_ADI").asString
                        val durakKodu = busStopJson.get("DURAK_DURAK_KODU").asString
                        val konum = Pair(busStopJson.getAsJsonObject("DURAK_GEOLOC").get("y").asDouble, busStopJson.getAsJsonObject("DURAK_GEOLOC").get("x").asDouble)
                        val durakID = busStopJson.get("DURAK_ID").asInt
                        val durakYonBilgisi = busStopJson.get("DURAK_YON_BILGISI").asString

                        val busStopObject = BusStop(durakAdi, durakKodu.toInt(), konum, durakID, durakYonBilgisi)
                        busStops.add(busStopObject)
                    }

                    return@withContext busStops
                } else {
                    println("Request failed with code: ${response.code}")
                    return@withContext emptyList()
                }
            } catch (e: IOException) {
                println("Exception during network request: ${e.message}")
                return@withContext emptyList()
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
    private fun findNearestBusStop(latitude: Double, longitude: Double, busStops: List<BusStop>): BusStop? {
        var nearestBusStop: BusStop? = null
        var shortestDistance = Float.MAX_VALUE // distance'ı Float türünde tanımla

        val currentLocation = Location("")
        currentLocation.latitude = latitude
        currentLocation.longitude = longitude

        for (busStop in busStops) {
            val busStopLatitude = busStop.DURAK_GEOLOC.first  // Enlem (Latitude)
            val busStopLongitude = busStop.DURAK_GEOLOC.second  // Boylam (Longitude

            val busStopLocation = Location("")
            busStopLocation.latitude = busStopLatitude
            busStopLocation.longitude = busStopLongitude

            val distanceArray = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                busStopLocation.latitude, busStopLocation.longitude,
                distanceArray
            )
            val distance = distanceArray[0]

            if (distance < shortestDistance) {
                shortestDistance = distance
                nearestBusStop = busStop
            }
        }

        return nearestBusStop
    }


}