package com.sokcuri.twimgspeedproxy

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.annotation.Nullable
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

        fun getServiceConnection(context: Context): ServiceConnection {
            return object : ServiceConnection {
                val TAG = "ProxyService";
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    // The binder of the service that returns the instance that is created.
                    val binder = service as LocalBinder

                    // The getter method to acquire the service.
                    val proxyService: ProxyService = binder.service

                    // getServiceIntent(context) returns the relative service intent

                    val intent = Intent(context, ProxyService::class.java)
                    intent.action = ProxyService.ActionStartForegroundService

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }

                    // This is the key: Without waiting Android Framework to call this method
                    // inside Service.onCreate(), immediately call here to post the notification.
                    proxyService.startForeground(1, getNotification(context))

                    // Release the connection to prevent leaks.
                    context.unbindService(this)
                }

                override fun onBindingDied(name: ComponentName) {
                    Log.w(TAG, "Binding has dead.")
                }

                override fun onNullBinding(name: ComponentName) {
                    Log.w(TAG, "Bind was null.")
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    Log.w(TAG, "Service is disconnected..")
                }
            }
        }
        private fun getNotification(context: Context): Notification {
            val channelID = "com.sokcuri.twimgspeedproxy"
            val channelName = "TwimgSpeedProxy Background Service"

            val notificationTitle = "트위터 이미지 고속 프록시가 활성화됨"
            val notificationDesc = "트위터 이미지 고속 프록시가 동작중입니다"

            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (android.os.Build.VERSION.SDK_INT >= 26) {
                val notificationChannel = NotificationChannel(
                    channelID,
                    channelName, NotificationManager.IMPORTANCE_NONE
                )
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)

                // Build the notification.
                return Notification.Builder(context, channelID)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationDesc)
                    .setSmallIcon(R.drawable.ic_action_twitter)
                    .setContentIntent(contentIntent)
                    .build()
            } else {
                return NotificationCompat.Builder(context, "default")
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationDesc)
                    .setSmallIcon(R.drawable.ic_action_twitter)
                    .setContentIntent(contentIntent)
                    .build()
            }
        }
    }

    private var littleProxy: LittleProxy? = null

    inner class LocalBinder : Binder() {
        val service: ProxyService
            get() = this@ProxyService;
    }

    // Create the instance on the service.
    private val binder = LocalBinder()

    // Return this instance from onBind method.
    // You may also return new LocalBinder() which is
    // basically the same thing.
    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(ProxyServiceTag, "Foreground service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {

            when (intent.action) {
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
            startForeground(1, notification)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else {
            stopSelf();
        }

        littleProxy?.stop()
        IsServiceRunning = false

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var editor = sharedPref.edit()
        editor.putBoolean("serviceRun", false)
        editor.commit()
    }
}