package io.github.daisukikaffuchino.han1meviewer.logic.model

import io.github.daisukikaffuchino.han1meviewer.util.Parcelable
import androidx.compose.ui.text.intl.Locale
import io.github.daisukikaffuchino.utils.CHINESE
import io.github.daisukikaffuchino.utils.ENGLISH
import io.github.daisukikaffuchino.utils.JAPANESE
import io.github.daisukikaffuchino.utils.LanguageHelper
import io.github.daisukikaffuchino.utils.SIMPLIFIED_CHINESE
import io.github.daisukikaffuchino.han1meviewer.util.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.appearance_and_figure
import han1meviewer.shared.generated.resources.characteristics
import han1meviewer.shared.generated.resources.relationship
import han1meviewer.shared.generated.resources.sex_position
import han1meviewer.shared.generated.resources.story_location
import han1meviewer.shared.generated.resources.story_plot
import han1meviewer.shared.generated.resources.video_attr
import org.jetbrains.compose.resources.StringResource

@Suppress("EqualsOrHashCode")
@Serializable
@Parcelize
data class SearchOption(
    @SerialName("lang")
    val lang: Language? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("search_key")
    val searchKey: String? = null,
) : Parcelable {

    companion object {
        fun <K> Map<K, Set<SearchOption>>.flatten(): Set<String> = buildSet {
            values.forEach { options ->
                val res = options.mapNotNullTo(mutableSetOf()) { it.searchKey }
                addAll(res)
            }
        }

        operator fun Map<String, List<SearchOption>>.get(scopeNameRes: StringResource): List<SearchOption> {
            return when (scopeNameRes) {
                Res.string.video_attr -> this["video_attributes"].orEmpty()
                Res.string.relationship -> this["character_relationships"].orEmpty()
                Res.string.characteristics -> this["characteristics"].orEmpty()
                Res.string.appearance_and_figure -> this["appearance_and_figure"].orEmpty()
                Res.string.story_plot -> this["story_plot"].orEmpty()
                Res.string.story_location -> this["story_location"].orEmpty()
                Res.string.sex_position -> this["sex_positions"].orEmpty()
                else -> error("Unknown scope name res: $scopeNameRes")
            }
        }

    }

    @Serializable
    @Parcelize
    data class Language(
        @SerialName("zh-rCN")
        val zhrCN: String? = null,
        @SerialName("zh-rTW")
        val zhrTW: String? = null,
        @SerialName("en")
        val en: String? = null,
        @SerialName("ja")
        val ja: String? = null,
    ) : Parcelable

    override fun hashCode(): Int = searchKey.hashCode()

    val value: String
        get() = when {
            lang == null -> name.orEmpty()
            else -> LanguageHelper.preferredLanguage.let { pl ->
                when (pl.language) {
                    Locale.CHINESE.language -> when (pl.region) {
                        Locale.SIMPLIFIED_CHINESE.region -> lang.zhrCN
                        else -> lang.zhrTW
                    }

                    Locale.ENGLISH.language -> lang.en
                    Locale.JAPANESE.language -> lang.ja
                    else -> lang.zhrTW
                }
            } ?: lang.zhrTW.orEmpty()
        }
}
