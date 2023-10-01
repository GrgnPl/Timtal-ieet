package com.sarj.timtalieet.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import com.sarj.timtalieet.Adapter.BusDetailAdapter
import com.sarj.timtalieet.Class.BusDetailInfo
import com.sarj.timtalieet.Class.BusInfo
import com.sarj.timtalieet.Class.BusStop
import com.sarj.timtalieet.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class BusDetailActivity : AppCompatActivity() {
    private lateinit var busDetailAdapter: BusDetailAdapter

    private val url = "https://ntcapi.iett.istanbul/service"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_detail)
        val sp = this.getSharedPreferences("degerler", MODE_PRIVATE)
        val key = sp.getString("access_key", "access_key yok")
        val intent = intent
        val data = intent.getStringExtra("gonderilecek_deger")
        Log.e("New Data",data.toString())
        Log.e("key", key.toString())
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView4)
        recyclerView.layoutManager = LinearLayoutManager(this)
        busDetailAdapter = BusDetailAdapter(emptyList())
        recyclerView.adapter = busDetailAdapter

        CoroutineScope(Dispatchers.IO).launch {
            val busDetailList = getBusDetailInfo(data.toString(), key!!)

            // Update the UI on the main thread
           runOnUiThread {
                busDetailAdapter.updateBusStops(busDetailList)
            }
        }

    }


    suspend fun getBusDetailInfo(kapino:String,accessKey: String):List<BusInfo> {
        val jsonData = """
     {
  "alias": "mainGetBusLocation_basic",
  "data": {
    "AKYOLBILYENI.K_ARAC.KAPINUMARASI":"${kapino}"
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
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonArray = JsonParser.parseString(responseBody).asJsonArray
                Log.e("json_Data", jsonArray.toString())

                val list = mutableListOf<BusInfo>()

                jsonArray.forEach { element ->
                    val busStopJson = element.asJsonObject

                    val arac_id= busStopJson.get("H_OTOBUSKONUM_ARACID").asInt
                    val boylam = busStopJson.get("H_OTOBUSKONUM_BOYLAM").asDouble
                    val enlem = busStopJson.get("H_OTOBUSKONUM_ENLEM").asDouble
                    val hiz = busStopJson.get("H_OTOBUSKONUM_HIZ").asInt
                    val kayit_zamani = busStopJson.get("H_OTOBUSKONUM_KAYITZAMANI").asString
                    val otobus_rakimi = busStopJson.get("H_OTOBUSKONUM_RAKIM").asInt
                    val arac_kapi_no = busStopJson.get("K_ARAC_KAPINUMARASI").asString
                    val arac_plaka = busStopJson.get("K_ARAC_PLAKA").asString
                    val busStopObject = BusInfo(arac_id, boylam, enlem, hiz, kayit_zamani,otobus_rakimi,arac_kapi_no,arac_plaka)
                    list.add(busStopObject)

                }
                return@withContext list
            } else {
                println("Request failed with code: ${response.code}")
                return@withContext emptyList()
            }
        }
    }
}