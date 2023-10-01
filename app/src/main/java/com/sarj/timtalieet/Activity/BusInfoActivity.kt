package com.sarj.timtalieet.Activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import com.sarj.timtalieet.Class.BusInfo
import com.sarj.timtalieet.Adapter.BusInfoAdapter
import com.sarj.timtalieet.Class.BusDetailInfo
import com.sarj.timtalieet.R
import com.sarj.timtalieet.databinding.ActivityBusInfoBinding
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class BusInfoActivity : AppCompatActivity() {
    var newData=""
    private lateinit var binding: ActivityBusInfoBinding
    private lateinit var busInfoAdapter: BusInfoAdapter
    private val url = "https://ntcapi.iett.istanbul/service"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sp = this.getSharedPreferences("degerler", MODE_PRIVATE)
        val key = sp.getString("access_key","access_key yok")
        Log.e("key",key.toString())
        val intent = intent
        val data = intent.getIntExtra("deger",1)
        Log.e("New Data",data.toString())
        CoroutineScope(Dispatchers.Main).launch {
            val busInfoList = getBusInfo(data.toString(),key!!)
            Log.e("busInfoList",busInfoList.toString())
            busInfoAdapter.updateBusStops(busInfoList)
        }
        val recyclerView = binding.recyclerView2
        recyclerView.layoutManager = LinearLayoutManager(this)
        busInfoAdapter = BusInfoAdapter(emptyList())
        recyclerView.adapter = busInfoAdapter
    }



    suspend fun getBusInfo(durakkodu:String,accesskey:String):List<BusDetailInfo> {
        val jsonData = """
       {
        "alias": "ybs",
        "data": {
        "data": {
        "password": "n1!t8c7M1",
        "username": "netuce"
    },
        "method": "POST",
        "path": [
        "real-time-information",
        "stop-arrivals",
        "${durakkodu}"
        ]
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
            .addHeader("Authorization", "Bearer $accesskey")
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

                val list = mutableListOf<BusDetailInfo>()
                jsonArray.forEach { element ->
                    val busInfoJson = element.asJsonObject
                    val kod = busInfoJson.get("kod").asInt
                    val kapino = busInfoJson.get("kapino").asString
                    val ototip = busInfoJson.get("ototip").asString
                    val engelli = busInfoJson.get("engelli").asInt
                    val hatkodu = busInfoJson.get("hatkodu").asString
                    val hattip = busInfoJson.get("hattip").asString
                    val usb = busInfoJson.get("usb").asString
                    val wifi = busInfoJson.get("wifi").asString
                    val klima = busInfoJson.get("klima").asString
                    val son_konum= busInfoJson.get("son_konum").asString
                    val son_hiz = busInfoJson.get("son_hiz").asString
                    val saat = busInfoJson.get("saat").asString
                    val dakika = busInfoJson.get("dakika").asString
                    val tahmintipi = busInfoJson.get("tahmintipi").asString
                    val hatadi = busInfoJson.get("hatadi").asString
                    val sistemsaati = busInfoJson.get("sistemsaati").asString
                    val son_konum_saati =busInfoJson.get("son_konum_saati").asString
                    val guzergah= busInfoJson.get("guzergah").asString
                    val durak_adi = busInfoJson.get("durak_adi").asString
                    val kaynak = busInfoJson.get("kaynak").asString
                    val busInfoObject = BusDetailInfo(
                        kod,
                        kapino,
                        ototip,
                        engelli,
                        hatkodu,
                        hattip,
                        usb,
                        wifi,
                        klima,
                        son_konum,
                        son_hiz,
                        saat,
                        dakika,
                        tahmintipi,
                        hatadi,
                        sistemsaati,
                        son_konum_saati,
                        guzergah,
                        durak_adi,
                        kaynak
                    )
                    list.add(busInfoObject)

                }
                return@withContext list
            } else {
                println("Request failed with code: ${response.code}")
                return@withContext emptyList()
            }
        }
    }
}