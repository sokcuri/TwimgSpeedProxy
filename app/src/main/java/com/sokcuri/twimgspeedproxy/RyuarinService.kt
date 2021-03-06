package com.sokcuri.twimgspeedproxy

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.okhttp.*
import java.util.*


class RyuarinService {
    companion object {
        private const val RyuarinServiceTag = "RyuarinService"
        var table: RyuarinTable? = null
        var expire: Long = 0
        var confirmIncoming = false;

        fun update(context: Context): Boolean {
            if (!confirmIncoming) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (ProxyService.notificationBuilder != null) {
                    val builder = ProxyService.notificationBuilder!!;
                    builder.setContentTitle("트위터 이미지 프록시가 연결됨")
                    builder.setContentText("트위터 이미지 프록시가 작동하고 있습니다")
                    notificationManager.notify(1, builder.build())
                    confirmIncoming = true
                }
            }
            if (expire > System.currentTimeMillis()) {
                // Log.d(RyuarinServiceTag, "Not Expired")
                return false
            }

            expire = System.currentTimeMillis() + 1000 * 60 * 15
            val client = OkHttpClient()

            val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA
                )
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