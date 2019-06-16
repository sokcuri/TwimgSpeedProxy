package com.sokcuri.twimgspeedproxy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v4.content.ContextCompat.startForegroundService
import android.os.Build

class ServiceController {
    private var context: Activity
    private var cls: Class<*>

    companion object {
        var isServiceRunning = false
    }

    constructor(activity: Activity, service: Class<*>) {
        this.context = activity
        this.cls = service
    }

    fun getIntent(): Intent {
        return Intent(context, cls)
    }

    fun startWithBind(serviceConnection: ServiceConnection) {
        startService()
        bindService(serviceConnection)
    }

    fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(getIntent()))
        } else {
            context.startService(Intent(getIntent()))
        }
        isServiceRunning = true
    }

    fun stopService() {
        context.stopService(getIntent())
        isServiceRunning = false
    }

    fun bindService(serviceConnection: ServiceConnection) {
        context.bindService(getIntent(), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(serviceConnection: ServiceConnection) {
        context.unbindService(serviceConnection)
    }
}