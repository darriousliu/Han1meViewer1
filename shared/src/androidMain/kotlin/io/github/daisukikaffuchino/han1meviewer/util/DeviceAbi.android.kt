package io.github.daisukikaffuchino.han1meviewer.util

import android.os.Build

actual val isX86_64Device: Boolean
    get() = Build.SUPPORTED_ABIS.any { it == "x86_64" }
