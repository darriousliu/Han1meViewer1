package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.ai_decensored
import han1meviewer.shared.generated.resources.ai_generated
import han1meviewer.shared.generated.resources.amateur_nomask
import han1meviewer.shared.generated.resources.animation_2_5d
import han1meviewer.shared.generated.resources.animation_2d
import han1meviewer.shared.generated.resources.category_3d_animation
import han1meviewer.shared.generated.resources.category_cosplay
import han1meviewer.shared.generated.resources.category_instant_noodle
import han1meviewer.shared.generated.resources.category_motion_anime
import han1meviewer.shared.generated.resources.china_av
import han1meviewer.shared.generated.resources.chinese_amateur
import han1meviewer.shared.generated.resources.chinese_subtitle
import han1meviewer.shared.generated.resources.hd_uncensored
import han1meviewer.shared.generated.resources.latest_av
import han1meviewer.shared.generated.resources.latest_hanime
import han1meviewer.shared.generated.resources.latest_release
import han1meviewer.shared.generated.resources.latest_upload
import han1meviewer.shared.generated.resources.mmd
import han1meviewer.shared.generated.resources.ranking_this_month
import han1meviewer.shared.generated.resources.ranking_today
import han1meviewer.shared.generated.resources.they_watched
import org.jetbrains.compose.resources.StringResource

const val HOME_CATEGORY_LATEST_HANIME = "latest_hanime"
const val HOME_CATEGORY_LATEST_RELEASE = "latest_release"
const val HOME_CATEGORY_LATEST_UPLOAD = "latest_upload"
const val HOME_CATEGORY_WATCHING_NOW = "watching_now"
const val HOME_CATEGORY_SHORT_EPISODE = "short_episode"
const val HOME_CATEGORY_MOTION_ANIME = "motion_anime"
const val HOME_CATEGORY_3D_CG = "3d_cg"
const val HOME_CATEGORY_2_5D = "2_5d"
const val HOME_CATEGORY_2D_ANIME = "2d_anime"
const val HOME_CATEGORY_AI_GENERATED = "ai_generated"
const val HOME_CATEGORY_MMD = "mmd"
const val HOME_CATEGORY_COSPLAY = "cosplay"

data class HomeCategoryPreferenceItem(
    val key: String,
    val normalTitleRes: StringResource,
    val avTitleRes: StringResource? = null,
)

val defaultHomeCategoryPreferenceItems = listOf(
    HomeCategoryPreferenceItem(HOME_CATEGORY_LATEST_HANIME, Res.string.latest_hanime, Res.string.latest_av),
    HomeCategoryPreferenceItem(HOME_CATEGORY_LATEST_RELEASE, Res.string.latest_release),
    HomeCategoryPreferenceItem(HOME_CATEGORY_LATEST_UPLOAD, Res.string.latest_upload),
    HomeCategoryPreferenceItem(HOME_CATEGORY_WATCHING_NOW, Res.string.they_watched),
    HomeCategoryPreferenceItem(HOME_CATEGORY_SHORT_EPISODE, Res.string.category_instant_noodle, Res.string.amateur_nomask),
    HomeCategoryPreferenceItem(HOME_CATEGORY_MOTION_ANIME, Res.string.category_motion_anime, Res.string.hd_uncensored),
    HomeCategoryPreferenceItem(HOME_CATEGORY_3D_CG, Res.string.category_3d_animation, Res.string.ai_decensored),
    HomeCategoryPreferenceItem(HOME_CATEGORY_2_5D, Res.string.animation_2_5d, Res.string.china_av),
    HomeCategoryPreferenceItem(HOME_CATEGORY_2D_ANIME, Res.string.animation_2d, Res.string.chinese_amateur),
    HomeCategoryPreferenceItem(HOME_CATEGORY_AI_GENERATED, Res.string.ai_generated, Res.string.chinese_subtitle),
    HomeCategoryPreferenceItem(HOME_CATEGORY_MMD, Res.string.mmd, Res.string.ranking_today),
    HomeCategoryPreferenceItem(HOME_CATEGORY_COSPLAY, Res.string.category_cosplay, Res.string.ranking_this_month),
)

val defaultHomeCategoryOrder: List<String>
    get() = defaultHomeCategoryPreferenceItems.map { it.key }

val homeCategoryOrder: List<String>
    get() = normalizeHomeCategoryKeys(SettingsRepository.current.homeCategoryOrder)

val hiddenHomeCategoryKeys: Set<String>
    get() = SettingsRepository.current.hiddenHomeCategoryKeys

suspend fun saveHomeCategoryPreferences(order: List<String>, hiddenKeys: Set<String>) =
    SettingsRepository.setHomeCategories(
        normalizeHomeCategoryKeys(order),
        hiddenKeys.filterTo(linkedSetOf()) { it in defaultHomeCategoryOrder },
    )

private fun normalizeHomeCategoryKeys(keys: List<String>): List<String> {
    val defaults = defaultHomeCategoryOrder
    return keys.distinct().filter { it in defaults } + defaults.filterNot { it in keys }
}
