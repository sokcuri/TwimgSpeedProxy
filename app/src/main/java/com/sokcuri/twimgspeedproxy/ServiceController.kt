package com.sokcuri.twimgspeedproxy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v4.content.ContextCompat.startForegroundService
import android.os.Build
import android.support.v7.preference.PreferenceManager

class ServiceController {
    private var context: Context
    private var cls: Class<*>

    companion object {
        var isServiceRunning = false
    }

    constructor(activity: Context, service: Class<*>) {
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
        isServiceRunning = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(getIntent()))
        } else {
            context.startService(Intent(getIntent()))
        }
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        var editor = sharedPref.edit()
        editor.putBoolean("serviceRun", true)
        editor.commit()
    }

    fun stopService() {
        isServiceRunning = false
        context.stopService(getIntent())
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        var editor = sharedPref.edit()
        editor.putBoolean("serviceRun", false)
        editor.commit()
    }

    fun bindService(serviceConnection: ServiceConnection) {
        context.bindService(getIntent(), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(serviceConnection: ServiceConnection) {
        context.unbindService(serviceConnection)
    }
}