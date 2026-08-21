package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.compose.runtime.Composable
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.DailyCheckInScreen

@Composable
fun DailyCheckInRouteScreen(
    onBack: () -> Unit,
) {
    DailyCheckInScreen(
        onBack = onBack,
    )
}
