package com.sokcuri.twimgspeedproxy

import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.random.Random

class HostPatch {
    companion object {
        var twimgHost: ArrayList<String> = ArrayList()
        fun twimg(addr: InetSocketAddress): InetSocketAddress {
            if (addr.hostName == "pbs.twimg.com") {
                val netAddr = InetAddress.getByAddress(
                    addr.hostName,
                    InetAddress.getByName(
                        HostPatch.twimgHost[Random.nextInt(HostPatch.twimgHost.size)]
                    ).address
                )
                return InetSocketAddress(netAddr, addr.port)
            }
        return addr
        }
    }
}