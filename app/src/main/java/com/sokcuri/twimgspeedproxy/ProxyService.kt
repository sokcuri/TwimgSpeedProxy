package com.sokcuri.twimgspeedproxy

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.support.v4.app.NotificationCompat
import android.support.v7.preference.PreferenceManager
import android.util.Log

class ProxyService: Service() {
    companion object {
        private const val ProxyServiceTag = "FOREGROUND_SERVICE"
        const val ActionStartForegroundService = "ACTION_START_FOREGROUND_SERVICE"
        const val ActionRestartForegroundService = "ACTION_RESTART_FOREGROUND_SERVICE"
        const val ActionAbortForegroundService = "ACTION_ABORT_FOREGROUND_SERVICE"
        const val ActionStopForegroundService = "ACTION_STOP_FOREGROUND_SERVICE"

        var IsServiceRunning = false
    }

    private var littleProxy: LittleProxy? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(ProxyServiceTag, "Foreground service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action

            when (action) {
                ActionStartForegroundService -> startProxyService()
                ActionRestartForegroundService -> restartProxyService()
                ActionAbortForegroundService -> abortProxyService()
                ActionStopForegroundService -> stopProxyService()
            }
        }
        return START_NOT_STICKY
    }

    private fun startProxyService() {
        if (littleProxy == null) {
            littleProxy = LittleProxy(applicationContext)
        }
        littleProxy?.start()
        IsServiceRunning = true

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var editor = sharedPref.edit()
        editor.putBoolean("serviceRun", true)
        editor.commit()

        makeNotification()
    }

    private fun makeNotification() {
        val channelID = "com.sokcuri.twimgspeedproxy"
        val channelName = "TwimgSpeedProxy Background Service"

        val notificationTitle = "트위터 이미지 고속 프록시가 활성화됨"
        val notificationDesc = "트위터 이미지 고속 프록시가 동작중입니다"

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                channelID,
                channelName, NotificationManager.IMPORTANCE_NONE
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            // Build the notification.
            val notification = Notification.Builder(this, channelID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDesc)
                .setSmallIcon(R.drawable.ic_action_twitter)
                .setContentIntent(contentIntent)
                .build()
            startForeground(1, notification)
        } else {
            val notification = NotificationCompat.Builder(this, "default")
                .setContentTitle(notificationTitle)
                .setContentText(notificationDesc)
                .setSmallIcon(R.drawable.ic_action_twitter)
                .setContentIntent(contentIntent)
                .build()
            startForeground(2001, notification)
        }
    }

    private fun restartProxyService() {
        littleProxy?.restart()
    }

    private fun abortProxyService() {
        littleProxy?.abort()
    }

    private fun stopProxyService() {
        Log.d(ProxyServiceTag, "Stop proxy service")
        stopForeground(true)
        stopSelf()

        littleProxy?.stop()
        IsServiceRunning = false

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var editor = sharedPref.edit()
        editor.putBoolean("serviceRun", false)
        editor.commit()
    }
}