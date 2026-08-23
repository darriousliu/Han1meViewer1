package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.github.daisukikaffuchino.han1meviewer.util.monotonicMillis
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.AF_UNSPEC
import platform.posix.AI_NUMERICHOST
import platform.posix.EINPROGRESS
import platform.posix.F_GETFL
import platform.posix.F_SETFL
import platform.posix.NI_MAXHOST
import platform.posix.NI_NUMERICHOST
import platform.posix.O_NONBLOCK
import platform.posix.POLLOUT
import platform.posix.SOCK_STREAM
import platform.posix.SOL_SOCKET
import platform.posix.SO_ERROR
import platform.posix.addrinfo
import platform.posix.close
import platform.posix.connect
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.freeaddrinfo
import platform.posix.getaddrinfo
import platform.posix.getnameinfo
import platform.posix.getsockopt
import platform.posix.memset
import platform.posix.poll
import platform.posix.pollfd
import platform.posix.socket

/**
 * iOS 侧的网络探测。NSURLSession 不暴露连接层，Ktor 也没有跨平台的
 * 「测某个 IP 的延迟」接口，所以这里直接走 BSD socket。
 */

/** 用系统 DNS 解析域名，返回数字形式的地址。 */
@OptIn(ExperimentalForeignApi::class)
internal fun resolveAddresses(host: String): List<String> = memScoped {
    val hints = alloc<addrinfo>()
    memset(hints.ptr, 0, sizeOf<addrinfo>().convert())
    hints.ai_family = AF_UNSPEC
    hints.ai_socktype = SOCK_STREAM

    val head = allocPointerTo<addrinfo>()
    if (getaddrinfo(host, null, hints.ptr, head.ptr) != 0) return emptyList()
    try {
        val buffer = allocArray<ByteVar>(NI_MAXHOST)
        val addresses = mutableListOf<String>()
        var node = head.value
        while (node != null) {
            val info = node.pointed
            val ok = getnameinfo(
                info.ai_addr, info.ai_addrlen,
                buffer, NI_MAXHOST.convert(),
                null, 0u,
                NI_NUMERICHOST,
            ) == 0
            if (ok) addresses += buffer.toKString()
            node = info.ai_next
        }
        addresses.distinct()
    } finally {
        freeaddrinfo(head.value)
    }
}

/**
 * 对字面 IP 做一次非阻塞 TCP 连接并计时，返回毫秒；-1 表示不可达。
 * 用 getaddrinfo(AI_NUMERICHOST) 拼 sockaddr，省得自己处理 v4/v6 和字节序。
 */
@OptIn(ExperimentalForeignApi::class)
internal fun tcpConnectMillis(ip: String, port: Int, timeoutMillis: Int): Int = memScoped {
    val hints = alloc<addrinfo>()
    memset(hints.ptr, 0, sizeOf<addrinfo>().convert())
    hints.ai_family = AF_UNSPEC
    hints.ai_socktype = SOCK_STREAM
    hints.ai_flags = AI_NUMERICHOST

    val head = allocPointerTo<addrinfo>()
    if (getaddrinfo(ip, port.toString(), hints.ptr, head.ptr) != 0) return -1
    val info = head.value?.pointed
    if (info == null) {
        freeaddrinfo(head.value)
        return -1
    }

    val fd = socket(info.ai_family, info.ai_socktype, info.ai_protocol)
    if (fd < 0) {
        freeaddrinfo(head.value)
        return -1
    }

    val start = monotonicMillis()
    try {
        // 非阻塞才能自己控制超时
        fcntl(fd, F_SETFL, fcntl(fd, F_GETFL, 0) or O_NONBLOCK)

        if (connect(fd, info.ai_addr, info.ai_addrlen) != 0) {
            if (errno != EINPROGRESS) return -1

            val poller = alloc<pollfd>()
            poller.fd = fd
            poller.events = POLLOUT.toShort()
            if (poll(poller.ptr, 1u, timeoutMillis) <= 0) return -1

            val socketError = alloc<IntVar>()
            val length = alloc<UIntVar>()
            length.value = sizeOf<IntVar>().convert()
            val queried = getsockopt(fd, SOL_SOCKET, SO_ERROR, socketError.ptr, length.ptr)
            if (queried != 0 || socketError.value != 0) return -1
        }
        (monotonicMillis() - start).toInt()
    } finally {
        close(fd)
        freeaddrinfo(head.value)
    }
}
