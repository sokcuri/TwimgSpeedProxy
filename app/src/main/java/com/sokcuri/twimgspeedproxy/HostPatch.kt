package com.sokcuri.twimgspeedproxy

import android.util.Log

class HostPatch {
    companion object {
        fun changeHost(host: String, cdnServer: String): String {
            val changedHost = when {
                host == "pbs.twimg.com" && LittleProxy.twimg -> {
                    pbs(cdnServer)
                }
                host == "video.twimg.com" && LittleProxy.twvideo -> {
                    video(cdnServer)
                }
                host == "abs.twimg.com" && LittleProxy.twabs -> {
                    abs(cdnServer)
                }
                else -> host
            }
            if (host != changedHost) {
                Log.d("HostPatch::changeHost", "Request host: $host -> $changedHost")
            } else {
                Log.d("HostPatch::changeHost", "Not changed host: $host ($cdnServer)")
            }
            return changedHost
        }

        private fun auto(host: String): String {
            return try {
                RyuarinService.table?.bestCdn!!.getValue(host)
            } catch (exception: Exception) {
                // Log.d("HostPatch::auto", "cdn host not found - $host")
                changeHost(host, "Akamai@eip-ntt")
            }
        }
        private fun pbs(cdnServer: String): String {
            return when (cdnServer) {
                "Auto@twimg.ryuar.in" -> auto("pbs.twimg.com")
                "Akamai@eip-ntt" -> "eip-ntt.pbs.twimg.com.akahost.net"
                "Akamai@eip-tata" -> "eip-tata.pbs.twimg.com.akahost.net"
                "Akamai@ak" -> "pbs-ak.twimg.com"
                "Zero" -> "pbs-zero.twimg.com"
                "Edgecast@ec" -> "pbs-ec.twimg.com"
                "Fastly@ft" -> "pbs-ft.twimg.com"
                else -> "pbs-ak.twimg.com"
            }
        }

        private fun video(cdnServer: String): String {
            return when (cdnServer) {
                "Auto@twimg.ryuar.in" -> auto("video.twimg.com")
                "Akamai@eip-ntt" -> "eip-ntt.video.twimg.com.akahost.net"
                "Akamai@eip-tata" -> "eip-tata.video.twimg.com.akahost.net"
                "Akamai@ak" -> "video-ak.twimg.com"
                "Zero" -> "video-zero.twimg.com"
                "Edgecast@ec" -> "video-ec.twimg.com"
                "Fastly@ft" -> "video-ft.twimg.com"
                else -> "video-ak.twimg.com"
            }
        }

        private fun abs(cdnServer: String): String {
            return when (cdnServer) {
                "Auto@twimg.ryuar.in" -> auto("abs.twimg.com")
                "Akamai@eip-ntt" -> "abs-ak.twimg.com"
                "Akamai@eip-tata" -> "abs-ak.twimg.com"
                "Akamai@ak" -> "abs-ak.twimg.com"
                "Zero" -> "abs-zero.twimg.com"
                "Edgecast@ec" -> "abs-ec.twimg.com"
                "Fastly@ft" -> "abs-ft.twimg.com"
                else -> "abs-ak.twimg.com"
            }
        }
    }
}
