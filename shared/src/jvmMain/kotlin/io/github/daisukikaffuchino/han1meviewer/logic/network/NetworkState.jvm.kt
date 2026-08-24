package io.github.daisukikaffuchino.han1meviewer.logic.network

// 桌面走以太网/Wi-Fi，恒 false 就是正确语义
actual fun isActiveNetworkMetered(): Boolean = false
