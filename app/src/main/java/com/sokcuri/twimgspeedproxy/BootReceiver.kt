package com.sokcuri.twimgspeedproxy

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.support.v7.preference.PreferenceManager
import android.util.Log
import android.os.Build



class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BootReceiver", "OK")

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPref.getBoolean("alwaysRun", false)) {
            val intent = Intent(context, ProxyService::class.java)
            intent.action = ProxyService.ActionStartForegroundService
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            MainActivity.setServiceSwitch(true)
        }
    }
}