package com.sokcuri.twimgspeedproxy

import android.content.Context
import android.support.v7.preference.PreferenceManager
import android.util.Log
import com.google.common.net.HostAndPort
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponse
import org.littleshoot.proxy.HttpFilters
import org.littleshoot.proxy.HttpFiltersAdapter
import org.littleshoot.proxy.HttpFiltersSourceAdapter
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import org.littleshoot.proxy.impl.ThreadPoolConfiguration
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException


class LittleProxy {
    var server: HttpProxyServer? = null
    var context: Context

    companion object {
        var port = 57572
        var connectTimeout = 1000
        var idleConnectionTimeout = 10
        var httpConnectionThread = 8
        var proxyWorkerThread = 16
        var serverWorkerThread = 16
        var cdnServer: String? = null
    }
    constructor(context: Context) {
        this.context = context

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        connectTimeout = Integer.parseInt(sharedPref.getString("netConnectTimeout", "1000")!!)
        idleConnectionTimeout = Integer.parseInt(sharedPref.getString("netIdleConnectionTimeout", "10")!!)
        httpConnectionThread = Integer.parseInt(sharedPref.getString("netHTTPConnectionThread", "8")!!)
        proxyWorkerThread = Integer.parseInt(sharedPref.getString("netProxyWorkerThread", "16")!!)
        serverWorkerThread = Integer.parseInt(sharedPref.getString("netServerWorkerThread", "16")!!)
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
            .withAllowRequestToOriginServer(true)
            .withConnectTimeout(connectTimeout)
            .withIdleConnectionTimeout(idleConnectionTimeout)
            .withThreadPoolConfiguration(
                ThreadPoolConfiguration()
                .withAcceptorThreads(httpConnectionThread)
                .withClientToProxyWorkerThreads(proxyWorkerThread)
                .withProxyToServerWorkerThreads(serverWorkerThread)
            )
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

                        override fun proxyToServerConnectionSSLHandshakeStarted() {
                            Log.d("LittleProxy", "proxyToServerConnectionSSLHandshakeStarted")
                        }
                    }
                }
            })
            .withUseDnsSec(true)
            .start()
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

                Log.d("startProxyServer", "does not started")

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