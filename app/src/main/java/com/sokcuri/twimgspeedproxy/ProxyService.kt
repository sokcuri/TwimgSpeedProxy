package com.sokcuri.twimgspeedproxy

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.StrictMode
import android.support.v4.app.NotificationCompat
import android.widget.Toast


class ProxyService: Service() {

    lateinit var context: Context
    lateinit var littleProxy: LittleProxy
    lateinit var packageReceiver: PackageReceiver
    private var currentIntent: Intent? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundServiceForOreo()
        else
            startForegroundService()

        context = applicationContext
        littleProxy = LittleProxy(context)

        if (Build.VERSION.SDK_INT > 9) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        Toast.makeText(this, "TwimgSpeedProxy by @sokcuri", Toast.LENGTH_LONG).show()

        packageReceiver = PackageReceiver()
        val pFilter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
        pFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        pFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        pFilter.addDataScheme("package")
        registerReceiver(packageReceiver, pFilter)
    }

    private fun startForegroundServiceForOreo() {
        val CHANNEL_ID = "com.sokcuri.twimgspeedproxy"
        val CHANNEL_NAME = "TwimgSpeedProxy Background Service"
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            val contentIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("트위터 이미지 고속 프록시가 활성화됨")
                .setContentText("트위터 이미지 고속 프록시가 동작중입니다")
                .setSmallIcon(R.drawable.ic_action_twitter)
                .setContentIntent(contentIntent)
                .build()
            startForeground(2001, notification)
        }
    }

    private fun startForegroundService() {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "default")
            .setContentTitle("트위터 이미지 고속 프록시가 활성화됨")
            .setContentText("트위터 이미지 고속 프록시가 동작중입니다")
            .setSmallIcon(R.drawable.ic_action_twitter)
            .setContentIntent(contentIntent)
            .build()
        startForeground(2001, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(context, "트위터 이미지 고속 프록시 서비스 시작: Port " + LittleProxy.port.toString(), Toast.LENGTH_LONG).show();
        this.currentIntent = currentIntent
        this.littleProxy.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(packageReceiver)

        super.onDestroy()
        littleProxy.stop()
    }

}