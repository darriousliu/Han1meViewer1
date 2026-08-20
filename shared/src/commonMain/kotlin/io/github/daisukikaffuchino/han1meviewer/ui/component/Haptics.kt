package io.github.daisukikaffuchino.han1meviewer.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository

/**
 * 替代原来的 VibrationUtil + LocalView：CMP 的 LocalHapticFeedback 三端都有实现，
 * 不需要 expect/actual。返回的 lambda 内部读设置开关，调用点直接 `haptic()`。
 */
@Composable
fun rememberHapticPerformer(
    type: HapticFeedbackType = HapticFeedbackType.ContextClick,
): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(haptic, type) {
        {
            if (SettingsRepository.hapticFeedbackEnabled) {
                haptic.performHapticFeedback(type)
            }
        }
    }
}
