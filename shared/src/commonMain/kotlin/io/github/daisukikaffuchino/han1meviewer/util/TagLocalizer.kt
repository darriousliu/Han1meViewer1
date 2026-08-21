package io.github.daisukikaffuchino.han1meviewer.util

import io.github.daisukikaffuchino.han1meviewer.logic.model.SearchOption
import io.github.daisukikaffuchino.utils.LanguageHelper
import io.github.daisukikaffuchino.utils.loadAssetAs

object TagLocalizer {

    private const val SEARCH_PREFIX = "search."

    private data class TagMappings(
        val labels: Map<String, String>,
        val searchKeys: Map<String, String>,
    )

    private var tagOptions: List<SearchOption> = emptyList()

    private var cachedLanguageTag: String? = null
    private var cachedMappings: TagMappings? = null

    /** 资源读取是 suspend，用之前先在协程里灌一次；重复调用不重复读。 */
    suspend fun preload() {
        if (tagOptions.isNotEmpty()) return
        tagOptions = loadAssetAs<Map<String, List<SearchOption>>>("search_options/tags.json")
            .orEmpty()
            .values
            .flatten() + loadAssetAs<List<SearchOption>>("search_options/genre.json").orEmpty()
        cachedMappings = null
    }

    // 未 preload 时映射表为空，localizeTag 回落到原字符串，与遇到未知 tag 的行为一致
    private val tagMappings: TagMappings
        get() {
            val languageTag = LanguageHelper.preferredLanguage.toLanguageTag()
            val mappings = cachedMappings
            if (cachedLanguageTag == languageTag && mappings != null) return mappings
            return buildTagMappings(tagOptions).also {
                cachedLanguageTag = languageTag
                cachedMappings = it
            }
        }

    fun localizeTags(tags: List<String>): List<String> {
        if (tags.isEmpty()) return tags
        return tags.map(::localizeTag)
    }

    fun localizeTag(tag: String): String {
        val normalizedTag = tag.normalizeTag()
        return tagMappings.labels[normalizedTag] ?: normalizedTag
    }

    fun resolveSearchKey(tag: String): String {
        val normalizedTag = tag.normalizeTag()
        return tagMappings.searchKeys[normalizedTag] ?: normalizedTag
    }

    private fun buildTagMappings(options: List<SearchOption>): TagMappings {
        val labels = mutableMapOf<String, String>()
        val searchKeys = mutableMapOf<String, String>()
        options.forEach { option ->
            val label = option.value.normalizeTag().takeIf { it.isNotBlank() } ?: return@forEach
            val searchKey = option.searchKey
                ?.normalizeTag()
                ?.takeIf { it.isNotBlank() }
                ?: return@forEach
            listOfNotNull(
                option.searchKey,
                option.name,
                option.lang?.zhrCN,
                option.lang?.zhrTW,
                option.lang?.en,
                option.lang?.ja,
            ).forEach { rawTag ->
                val normalizedTag = rawTag.normalizeTag()
                // putIfAbsent 是 JVM 专有
                if (normalizedTag !in labels) labels[normalizedTag] = label
                if (normalizedTag !in searchKeys) searchKeys[normalizedTag] = searchKey
            }
        }
        return TagMappings(labels = labels, searchKeys = searchKeys)
    }

    private fun String.normalizeTag(): String = removePrefix(SEARCH_PREFIX)
}
