package com.sokcuri.twimgspeedproxy

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.okhttp.*
import java.util.*


class RyuarinService {
    companion object {
        private const val RyuarinServiceTag = "RyuarinService"
        var table: RyuarinTable? = null
        var expire: Long = 0

        fun update(): Boolean {
            if (expire > System.currentTimeMillis()) {
                // Log.d(RyuarinServiceTag, "Not Expired")
                return false
            }
            expire = System.currentTimeMillis() + 1000 * 60 * 5
            val client = OkHttpClient()

            val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .allEnabledCipherSuites()
                .allEnabledTlsVersions()
                .build()

            client.connectionSpecs = Collections.singletonList(spec);

            val request = Request.Builder()
                .url("https://twimg.ryuar.in/json")
                .build()
            val asyncTask = @SuppressLint("StaticFieldLeak")
            object : AsyncTask<Void, Void, String>() {
                override fun doInBackground(vararg params: Void): String? {
                    return try {
                        val response = client.newCall(request).execute()
                        if (!response.isSuccessful) {
                            null
                        } else response.body().string()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }

            val body = asyncTask.execute().get()
            if (body == null) {
                Log.d(RyuarinServiceTag, "서버 리스트를 가져올 수 없습니다")
                expire = System.currentTimeMillis() + 1000 * 60 * 5
                return false
            }

            val gson = Gson()
            try {
                table = gson.fromJson(body, RyuarinTable::class.java)
            } catch (exception: Exception) {
                Log.d(RyuarinServiceTag, "gson exception raised")
                expire = System.currentTimeMillis() + 1000 * 60 * 5
            }
            return true
        }

    }
}

data class RyuarinTable(
    @SerializedName("best_cdn") val bestCdn : Map<String, String>,
    @SerializedName("detail") val detail : Map<String, List<RyuarinDetail>>,
    @SerializedName("updated_at") val updatedAt : String
)

data class RyuarinDetail(
    @SerializedName("HTTPSuccess") val httpSuccess : Boolean,
    @SerializedName("PingSuccess") val pingSuccess : Boolean,
    @SerializedName("default_cdn") val defaultCdn : Boolean,
    @SerializedName("domain") val domain : String,
    @SerializedName("geoip") val geoip : RyuarinGeoip,
    @SerializedName("http") val http : RyuarinHttpStatus,
    @SerializedName("ping") val ping : RyuarinPingStatus,
    @SerializedName("ip") val ip: String
)

data class RyuarinGeoip (
    @SerializedName("city") val city: String,
    @SerializedName("country") val country: String
)

data class RyuarinHttpStatus (
    @SerializedName("bps_avg") val avg: Double,
    @SerializedName("bps_max") val max: Double,
    @SerializedName("bps_min") val min: Double,
    @SerializedName("request") val request: Int,
    @SerializedName("response") val response: Int
)

data class RyuarinPingStatus (
    @SerializedName("rtt_avg") val avg: Double,
    @SerializedName("rtt_max") val max: Double,
    @SerializedName("rtt_min") val min: Double,
    @SerializedName("recv") val recv: Int,
    @SerializedName("sent") val sent: Int
)