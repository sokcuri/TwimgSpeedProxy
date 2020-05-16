package com.sokcuri.twimgspeedproxy

import android.content.Context
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import android.support.v7.preference.PreferenceManager
import android.widget.Toast
import com.google.common.net.HostAndPort
import org.littleshoot.proxy.HttpFiltersAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import org.littleshoot.proxy.HttpFilters
import org.littleshoot.proxy.HttpFiltersSourceAdapter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException


class LittleProxy {
    var server: HttpProxyServer? = null
    var context: Context

    companion object {
        var port = 57572
        var cdnServer: String? = null
    }
    constructor(context: Context) {
        this.context = context

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        port = Integer.parseInt(sharedPref.getString("proxyPort", "57572")!!)
        val serverArray = context.resources.getStringArray(R.array.servers_value)
        cdnServer = serverArray.find {
            it == sharedPref.getString("cdnServer", "")
        }
        if (cdnServer == null) {
            cdnServer = serverArray[0]

            var editor = sharedPref.edit()
            editor.putString("cdnServer", cdnServer)
            editor.commit()
        }
    }

    private fun startProxyServer() {
        this.server = DefaultHttpProxyServer.bootstrap()
            .withPort(port)
            .withFiltersSource(object : HttpFiltersSourceAdapter() {
                override fun filterRequest(originalRequest: HttpRequest, ctx: ChannelHandlerContext): HttpFilters {
                    return object : HttpFiltersAdapter(originalRequest) {
                        override fun proxyToServerResolutionStarted(hostAndPort: String): InetSocketAddress? {
                            RyuarinService.update(context)

                            var parsedHostAndPort: HostAndPort
                            try {
                                parsedHostAndPort = HostAndPort.fromString(hostAndPort)
                            } catch (e: IllegalArgumentException) {
                                throw UnknownHostException(hostAndPort)
                            }

                            val host = HostPatch.changeHost(parsedHostAndPort.hostText, cdnServer!!)
                            val port = parsedHostAndPort.getPortOrDefault(80)
                            val address = InetAddress.getByName(host)
                            return InetSocketAddress(address, port)
                        }
                    }
                }
            }).start()
    }

    private fun stopProxyServer() {
        this.server?.stop()
    }

    private fun abortProxyServer() {
        this.server?.abort()
    }

    fun start() {
        Thread {
            try {
                startProxyServer()
            } catch (exception: Exception) {

            }
        }.start()
    }

    fun stop() {
        Thread {
            stopProxyServer()
        }.start()
    }

    fun abort() {
        Thread {
            abortProxyServer()
        }.start()
    }

    fun restart() {
        Thread {
            try {
                stopProxyServer()
                startProxyServer()
            } catch (exception: Exception) {

            }
        }.start()
    }
}