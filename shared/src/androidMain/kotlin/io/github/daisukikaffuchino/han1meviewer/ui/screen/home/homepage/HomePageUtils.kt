package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage

/**
 * 将首页分类转换为高级搜索请求参数。
 *
 * 仅写入分类中存在的参数，避免向搜索页传递空值。
 *
 * @receiver 首页分类数据
 * @return 可直接用于高级搜索的参数映射
 */
internal fun HomeCategory.toAdvancedSearchParams(): Map<String, String> = buildMap {
    genre?.let { put("genre", it) }
    sort?.let { put("sort", it) }
    tags?.let { put("tags", it) }
}
