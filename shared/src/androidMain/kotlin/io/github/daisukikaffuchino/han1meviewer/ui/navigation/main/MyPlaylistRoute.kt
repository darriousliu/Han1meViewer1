package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.daisukikaffuchino.han1meviewer.getHanimeShareText
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.myplaylist.PlaylistScreen
import io.github.daisukikaffuchino.han1meviewer.ui.viewmodel.MyPlayListViewModel
import io.github.daisukikaffuchino.utils.rememberCopyTextToClipboard
import io.github.daisukikaffuchino.utils.SonnerToast
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.copy_to_clipboard

@Composable
fun MyPlaylistRouteScreen(
    onBack: () -> Unit,
    onNavigateToVideo: (String) -> Unit,
) {
    val viewModel: MyPlayListViewModel = viewModel()
    val copyTextToClipboard = rememberCopyTextToClipboard()
    PlaylistScreen(
        viewModel = viewModel,
        navigateBack = onBack,
        onClickItem = onNavigateToVideo,
        onLongClickItem = { videoCode, title ->
            copyTextToClipboard(getHanimeShareText(title, videoCode))
            SonnerToast.success(Res.string.copy_to_clipboard)
        },
    )
}
