package com.sokcuri.twimgspeedproxy

import android.content.Context
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import android.support.v7.preference.PreferenceManager
import android.util.Log


class LittleProxy {
    lateinit var server: HttpProxyServer
    var context: Context

    companion object {
        var port = 57572
        var twimg = true
        var twvideo = true
        var twabs = true
    }
    constructor(context: Context) {
        this.context = context

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        port = Integer.parseInt(sharedPref.getString("proxyPort", "57572"))
        twimg = sharedPref.getBoolean("enableTwimgSpeed", true)
        twvideo = sharedPref.getBoolean("enableTwvideoSpeed", true)
        twabs = sharedPref.getBoolean("enableTwabsSpeed", true)
    }

    fun start() {
        this.server = DefaultHttpProxyServer.bootstrap()
            .withPort(port)
            .start()
    }

    fun stop() {
        this.server.stop()
        this.server.abort()
    }
}