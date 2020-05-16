package com.sokcuri.twimgspeedproxy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.preference.PreferenceManager
import android.util.Log


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BootReceiver", "OK")

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPref.getBoolean("alwaysRun", false)) {
            val intent = Intent(context, ProxyService::class.java)
            intent.action = ProxyService.ActionStartForegroundService

            // https://stackoverflow.com/questions/44425584/context-startforegroundservice-did-not-then-call-service-startforeground
            val connection = ProxyService.getServiceConnection(context)
            try {
                context.bindService(
                    intent, connection,
                    Context.BIND_AUTO_CREATE
                )
            } catch (ignored: RuntimeException) {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
            MainActivity.setServiceSwitch(true)
        }
    }
}