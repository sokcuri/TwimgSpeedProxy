package com.sokcuri.twimgspeedproxy

import java.net.InetAddress
import java.net.InetSocketAddress

class HostPatch {
    companion object {
        fun resolve(host: String, port: Int): InetSocketAddress  {
            var netAddr = InetAddress.getByName(host)
            return InetSocketAddress(netAddr, port)
        }
        fun twimg(addr: InetSocketAddress): InetSocketAddress {
            when {
                addr.hostName == "pbs.twimg.com" && LittleProxy.twimg -> return resolve("pbs-ak.twimg.com", addr.port)
                addr.hostName == "video.twimg.com" && LittleProxy.twvideo  -> return resolve("video-ak.twimg.com", addr.port)
                addr.hostName == "abs.twimg.com" && LittleProxy.twabs -> return resolve("abs-ak.twimg.com", addr.port)
                else -> return addr
            }
        }
    }
}