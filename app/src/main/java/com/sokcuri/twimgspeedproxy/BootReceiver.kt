package com.sokcuri.twimgspeedproxy

import android.widget.Toast
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.support.v7.preference.PreferenceManager
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BootReceiver", "OK")

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPref.getBoolean("alwaysRun", false)) {
            val intent = Intent(context, ProxyService::class.java)
            intent.action = ProxyService.ActionStartForegroundService
            context.startService(intent)
            MainActivity.setServiceSwitch(true)
        }
    }
}