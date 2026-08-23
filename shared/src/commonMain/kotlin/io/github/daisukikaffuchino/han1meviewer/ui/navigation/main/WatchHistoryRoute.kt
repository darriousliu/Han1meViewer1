package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.compose.runtime.Composable
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.WatchHistoryTabScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.HomePageViewModel
import io.github.daisukikaffuchino.han1meviewer.ui.viewmodel.OnlineWatchHistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WatchHistoryRouteScreen(
    onBack: () -> Unit,
    onNavigateToVideo: (String) -> Unit,
) {
    val localViewModel: HomePageViewModel = koinViewModel()
    val onlineViewModel: OnlineWatchHistoryViewModel = koinViewModel()
    WatchHistoryTabScreen(
        localHistoriesFlow = localViewModel.loadAllWatchHistories(),
        onlineItems = onlineViewModel.items,
        onlineState = onlineViewModel.state,
        onlineSort = onlineViewModel.selectedSort,
        onlineLoadedPageCount = onlineViewModel.loadedPageCount,
        onlineIsLoadingMore = onlineViewModel.isLoadingMore,
        onlineRefreshing = onlineViewModel::isRefreshing,
        onlineDeleteStateFlow = onlineViewModel.deleteFlow,
        onBack = onBack,
        onOpenLocalVideo = { onNavigateToVideo(it.videoCode) },
        onDeleteLocalHistory = localViewModel::deleteWatchHistory,
        onDeleteAllLocalHistories = localViewModel::deleteAllWatchHistories,
        onOpenOnlineVideo = { onNavigateToVideo(it.videoCode) },
        onDeleteOnlineVideo = onlineViewModel::deleteItem,
        onRefreshOnline = onlineViewModel::refresh,
        onLoadMoreOnline = onlineViewModel::loadNextPage,
    )
}
