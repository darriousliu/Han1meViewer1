package io.github.daisukikaffuchino.han1meviewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * commonMain 的占位入口，desktop 与 iOS 壳目前只画这一屏。
 *
 * Android 不走这里：`MainActivity` 在 androidMain，自己 setContent 自己的导航图。
 * 等 UI 真正上移 commonMain 时，这里换成共用的根组合体。
 */
@Composable
fun App() {
    MaterialTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Han1meViewer · ${getPlatform().name}")
        }
    }
}
