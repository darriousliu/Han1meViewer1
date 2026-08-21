package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.HanimeScreen
import kotlinx.serialization.Serializable
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.about
import han1meviewer.shared.generated.resources.developer_options
import han1meviewer.shared.generated.resources.download_settings
import han1meviewer.shared.generated.resources.h_keyframe_manage
import han1meviewer.shared.generated.resources.h_keyframe_settings
import han1meviewer.shared.generated.resources.mpv_advanced_settings
import han1meviewer.shared.generated.resources.network_settings
import han1meviewer.shared.generated.resources.open_source_license
import han1meviewer.shared.generated.resources.player_settings
import han1meviewer.shared.generated.resources.settings
import han1meviewer.shared.generated.resources.settings_appearance
import han1meviewer.shared.generated.resources.settings_data_privacy
import han1meviewer.shared.generated.resources.settings_interface_interaction
import han1meviewer.shared.generated.resources.settings_network_download
import han1meviewer.shared.generated.resources.settings_video_playback
import han1meviewer.shared.generated.resources.shared_h_keyframe_manage
import org.jetbrains.compose.resources.StringResource

@Serializable
object HomeSettingsRoute : HanimeScreen

@Serializable
object VideoPlaybackSettingsRoute : HanimeScreen

@Serializable
object NetworkDownloadSettingsRoute : HanimeScreen

@Serializable
object AppearanceSettingsRoute : HanimeScreen

@Serializable
object InterfaceInteractionSettingsRoute : HanimeScreen

@Serializable
object DataPrivacySettingsRoute : HanimeScreen

@Serializable
object DeveloperOptionsSettingsRoute : HanimeScreen

@Serializable
object AboutSettingsRoute : HanimeScreen

@Serializable
object OpenSourceLicensesRoute : HanimeScreen

@Serializable
object PlayerSettingsRoute : HanimeScreen

@Serializable
object NetworkSettingsRoute : HanimeScreen

@Serializable
object DownloadSettingsRoute : HanimeScreen

@Serializable
object MpvPlayerSettingsRoute : HanimeScreen

@Serializable
object HKeyframesRoute : HanimeScreen

@Serializable
object SharedHKeyframesRoute : HanimeScreen

@Serializable
object HKeyframeSettingsRoute : HanimeScreen

enum class SettingsDestinationSpec(
    val titleRes: StringResource,
    val showToolbar: Boolean = true,
) {
    Home(
        titleRes = Res.string.settings,
    ),
    VideoPlayback(
        titleRes = Res.string.settings_video_playback,
    ),
    NetworkDownload(
        titleRes = Res.string.settings_network_download,
    ),
    Appearance(
        titleRes = Res.string.settings_appearance,
    ),
    InterfaceInteraction(
        titleRes = Res.string.settings_interface_interaction,
    ),
    DataPrivacy(
        titleRes = Res.string.settings_data_privacy,
    ),
    DeveloperOptions(
        titleRes = Res.string.developer_options,
    ),
    About(
        titleRes = Res.string.about,
    ),
    OpenSourceLicenses(
        titleRes = Res.string.open_source_license,
    ),
    Player(
        titleRes = Res.string.player_settings,
    ),
    Network(
        titleRes = Res.string.network_settings,
    ),
    Download(
        titleRes = Res.string.download_settings,
    ),
    Mpv(
        titleRes = Res.string.mpv_advanced_settings,
    ),
    HKeyframes(
        titleRes = Res.string.h_keyframe_manage,
    ),
    SharedHKeyframes(
        titleRes = Res.string.shared_h_keyframe_manage,
    ),
    HKeyframeSettings(
        titleRes = Res.string.h_keyframe_settings,
    );

    val route: HanimeScreen
        get() = when (this) {
            Home -> HomeSettingsRoute
            VideoPlayback -> VideoPlaybackSettingsRoute
            NetworkDownload -> NetworkDownloadSettingsRoute
            Appearance -> AppearanceSettingsRoute
            InterfaceInteraction -> InterfaceInteractionSettingsRoute
            DataPrivacy -> DataPrivacySettingsRoute
            DeveloperOptions -> DeveloperOptionsSettingsRoute
            About -> AboutSettingsRoute
            OpenSourceLicenses -> OpenSourceLicensesRoute
            Player -> PlayerSettingsRoute
            Network -> NetworkSettingsRoute
            Download -> DownloadSettingsRoute
            Mpv -> MpvPlayerSettingsRoute
            HKeyframes -> HKeyframesRoute
            SharedHKeyframes -> SharedHKeyframesRoute
            HKeyframeSettings -> HKeyframeSettingsRoute
        }
}
