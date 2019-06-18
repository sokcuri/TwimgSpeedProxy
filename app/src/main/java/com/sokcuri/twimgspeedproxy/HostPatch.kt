package com.sokcuri.twimgspeedproxy

import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.random.Random

class HostPatch {
    companion object {
        var twimgHost: ArrayList<String> = ArrayList()

        fun resolve(host: String, port: Int): InetSocketAddress  {
            var netAddr = InetAddress.getByName(host)
            return InetSocketAddress(netAddr, port)
        }
        fun twimg(addr: InetSocketAddress): InetSocketAddress {
            when {
                addr.hostName == "pbs.twimg.com" -> return resolve("pbs-ak.twimg.com", addr.port)
                addr.hostName == "video.twimg.com" -> return resolve("video-ak.twimg.com", addr.port)
                addr.hostName == "abs.twimg.com" -> return resolve("abs-ak.twimg.com", addr.port)
                else -> return addr
            }
        }
    }
}