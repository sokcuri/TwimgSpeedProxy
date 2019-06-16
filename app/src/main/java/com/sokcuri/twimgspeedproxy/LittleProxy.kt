package com.sokcuri.twimgspeedproxy

import android.content.Context
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.impl.DefaultHttpProxyServer

class LittleProxy {
    lateinit var server: HttpProxyServer
    var context: Context

    companion object {
        var port = 57572
    }
    constructor(context: Context) {
        this.context = context

        val sharedPref = context.getSharedPreferences("global", Context.MODE_PRIVATE) ?: return
        val defaultValue = Integer.parseInt(context.resources.getString(R.string.proxy_port))
        port = sharedPref.getInt(context.getString(R.string.proxy_port), defaultValue)
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