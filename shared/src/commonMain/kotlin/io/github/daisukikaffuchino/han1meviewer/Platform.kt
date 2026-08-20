package io.github.daisukikaffuchino.han1meviewer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
