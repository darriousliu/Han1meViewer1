package io.github.daisukikaffuchino.han1meviewer.logic.network

/** 代理方式。HProxySelector 只有 androidJvm 有，UI 用这里的常量。 */
object ProxyType {
    const val DIRECT = 0
    const val SYSTEM = 1
    const val HTTP = 2
    const val SOCKS = 3

    private val ipv4Regex =
        Regex("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")

    fun validateIp(ip: String): Boolean = ipv4Regex.matches(ip)

    fun validatePort(port: Int): Boolean = port in 0..65535

    fun isValidEndpoint(type: Int, ip: String, port: Int): Boolean = when (type) {
        DIRECT, SYSTEM -> true
        HTTP, SOCKS -> validateIp(ip) && validatePort(port)
        else -> false
    }
}
