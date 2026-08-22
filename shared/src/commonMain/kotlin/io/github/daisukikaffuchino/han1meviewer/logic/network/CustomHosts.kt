package io.github.daisukikaffuchino.han1meviewer.logic.network

/** 自定义 hosts 的解析与校验。HDns 本体依赖 InetAddress，只有 androidJvm 有。 */
object CustomHosts {
    private val ipv4 = Regex("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")

    fun parse(raw: String): List<String> =
        raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    /** @return 无效 IP 的错误信息列表，为空表示全部有效 */
    fun validate(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val ips = parse(raw)
        if (ips.isEmpty()) return listOf("No IP addresses entered")
        return ips.filterNot { isValidIpAddress(it) }.map { "Invalid IP address: \"$it\"" }
    }

    fun isValidIpAddress(ip: String): Boolean {
        val text = ip.removePrefix("[").removeSuffix("]")
        return ipv4.matches(text) || isValidIpv6(text)
    }

    // 只做字面量校验：8 组十六进制，允许一次 :: 省略，末组可以是 IPv4
    private fun isValidIpv6(text: String): Boolean {
        if (text.isEmpty() || ':' !in text) return false
        if (text.count { it == ':' } > 7 && "::" !in text) return false
        val compressed = text.split("::")
        if (compressed.size > 2) return false
        var groups = 0
        compressed.forEachIndexed { index, part ->
            if (part.isEmpty()) return@forEachIndexed
            val pieces = part.split(":")
            pieces.forEachIndexed { i, piece ->
                val isLast = index == compressed.lastIndex && i == pieces.lastIndex
                when {
                    isLast && ipv4.matches(piece) -> groups += 2
                    piece.length in 1..4 && piece.all { it.isHexDigit() } -> groups += 1
                    else -> return false
                }
            }
        }
        return if (compressed.size == 2) groups <= 8 else groups == 8
    }

    private fun Char.isHexDigit() = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
}
