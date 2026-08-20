package io.github.daisukikaffuchino.han1meviewer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Han1meViewer",
    ) {
        App()
    }
}
