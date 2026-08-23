package io.github.daisukikaffuchino.han1meviewer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.daisukikaffuchino.han1meviewer.BuildConfig
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.utils.SonnerToast

/**
 * 三端共用的 UI 根：显示密度、主题、Toast 宿主。
 *
 * 以前这三样只写在 Android 的 BaseActivity.setHanimeContent 里，iOS 和桌面直接调
 * App()，于是那两端整个界面走的是 Material3 的默认基线配色（一片淡紫灰，看起来
 * 像蒙了一层），Toast 也从来没有宿主。
 *
 * 平台入口一律用这个包住自己的内容，别再各写各的。
 */
@Composable
fun HanimeAppRoot(content: @Composable () -> Unit) {
    val settings by SettingsRepository.settings.collectAsStateWithLifecycle()
    val systemDensity = LocalDensity.current
    val densityScale = if (BuildConfig.DEBUG) settings.displayDensity.scale else 1f
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = systemDensity.density * densityScale,
            fontScale = systemDensity.fontScale,
        ),
    ) {
        HanimeTheme {
            content()
            SonnerToast.Host()
        }
    }
}
