package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.HomeSettingsRoute
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.check_in_feature_name
import han1meviewer.shared.generated.resources.download
import han1meviewer.shared.generated.resources.fav_video
import han1meviewer.shared.generated.resources.home_page
import han1meviewer.shared.generated.resources.ic_access_time
import han1meviewer.shared.generated.resources.ic_download
import han1meviewer.shared.generated.resources.ic_favorite_border
import han1meviewer.shared.generated.resources.ic_format_list_bulleted
import han1meviewer.shared.generated.resources.ic_history
import han1meviewer.shared.generated.resources.ic_home
import han1meviewer.shared.generated.resources.ic_settings
import han1meviewer.shared.generated.resources.ic_subscribtion
import han1meviewer.shared.generated.resources.ic_thumb_up_off_alt
import han1meviewer.shared.generated.resources.my_subscribe
import han1meviewer.shared.generated.resources.play_list
import han1meviewer.shared.generated.resources.settings
import han1meviewer.shared.generated.resources.watch_history
import han1meviewer.shared.generated.resources.watch_later
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.DrawableResource

enum class MainDrawerDestination(
    val route: HanimeScreen,
    val iconRes: DrawableResource,
    val titleRes: StringResource,
) {
    Home(
        route = HomeRoute,
        iconRes = Res.drawable.ic_home,
        titleRes = Res.string.home_page,
    ),
    Settings(
        route = HomeSettingsRoute,
        iconRes = Res.drawable.ic_settings,
        titleRes = Res.string.settings,
    ),
    DailyCheckIn(
        route = DailyCheckInRoute,
        iconRes = Res.drawable.ic_thumb_up_off_alt,
        titleRes = Res.string.check_in_feature_name,
    ),
    WatchLater(
        route = MyWatchLaterRoute,
        iconRes = Res.drawable.ic_access_time,
        titleRes = Res.string.watch_later,
    ),
    FavVideo(
        route = MyFavVideoRoute,
        iconRes = Res.drawable.ic_favorite_border,
        titleRes = Res.string.fav_video,
    ),
    Playlist(
        route = MyPlaylistRoute,
        iconRes = Res.drawable.ic_format_list_bulleted,
        titleRes = Res.string.play_list,
    ),
    Subscription(
        route = SubscriptionRoute,
        iconRes = Res.drawable.ic_subscribtion,
        titleRes = Res.string.my_subscribe,
    ),
    WatchHistory(
        route = WatchHistoryRoute,
        iconRes = Res.drawable.ic_history,
        titleRes = Res.string.watch_history,
    ),
    Download(
        route = DownloadRoute,
        iconRes = Res.drawable.ic_download,
        titleRes = Res.string.download,
    );

    companion object {
        fun fromRoute(route: HanimeScreen?): MainDrawerDestination? =
            entries.firstOrNull { it.route == route }
    }
}
