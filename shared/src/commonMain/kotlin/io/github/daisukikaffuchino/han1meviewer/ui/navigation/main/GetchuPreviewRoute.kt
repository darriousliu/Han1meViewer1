package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.compose.runtime.Composable
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.preview.getchupreview.GetchuPreviewDetailScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.preview.getchupreview.GetchuPreviewScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.preview.getchupreview.GetchuPreviewViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GetchuPreviewRouteScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
) {
    val viewModel: GetchuPreviewViewModel = koinViewModel()
    GetchuPreviewScreen(
        onBack = onBack,
        onNavigateToDetail = onNavigateToDetail,
        viewModel = viewModel,
    )
}

@Composable
fun GetchuPreviewDetailRouteScreen(
    route: GetchuPreviewDetailRoute,
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToVideoUrl: (String) -> Unit,
) {
    val viewModel: GetchuPreviewViewModel = koinViewModel()
    GetchuPreviewDetailScreen(
        id = route.id,
        onBack = onBack,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToVideoUrl = onNavigateToVideoUrl,
        viewModel = viewModel,
    )
}
