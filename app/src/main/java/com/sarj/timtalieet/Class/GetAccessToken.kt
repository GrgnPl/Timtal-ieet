package com.sarj.timtalieet.Class

import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GetAccessToken {
    private var accessToken: String? = null

    private val accessTokenJob = Job()
    private val accessTokenScope = CoroutineScope(Dispatchers.IO + accessTokenJob)
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getAccessTokenFromApi(): String {
        val jsonData = """
            {
              "client_id": "thAwizrcxoSgzWUzRRzhSyaiBQwQlOqA",
              "client_secret": "jRUTfAItVHYctPULyQFjbzTyLFxHklykujPWXKqRntSKTLEr",
              "grant_type": "client_credentials",
              "scope": "ntc_da.mobietttestenv service"
            }
        """.trimIndent()

        val url = "https://ntcapi.iett.istanbul/oauth2/v2/auth"

        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, jsonData)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Accept-Encoding", "gzip")
            .addHeader("Content-Type", "application/json; charset=UTF-8")
            .build()

        return accessTokenScope.async {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JsonParser.parseString(responseBody).asJsonObject
                    val newAccessToken = jsonResponse.get("access_token").asString
                    accessToken = newAccessToken
                    return@async newAccessToken
                } else {
                    println("Request failed with code: ${response.code}")
                    return@async ""
                }
            } catch (e: IOException) {
                println("Exception during network request: ${e.message}")
                return@async ""
            }
        }.await()
    }

    fun cancelAccessTokenJob() {
        accessTokenJob.cancel()
    }
}