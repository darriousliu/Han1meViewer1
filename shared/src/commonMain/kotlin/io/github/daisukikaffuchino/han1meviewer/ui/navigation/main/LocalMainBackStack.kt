package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 主导航栈。组件里要跳转时用它，不要再去 cast Activity。
 */
val LocalMainBackStack = staticCompositionLocalOf<TopLevelBackStack<HanimeScreen>> {
    error("LocalMainBackStack 未提供")
}
