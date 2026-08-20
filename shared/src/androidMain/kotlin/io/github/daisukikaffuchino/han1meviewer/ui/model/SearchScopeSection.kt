package io.github.daisukikaffuchino.han1meviewer.ui.model

import io.github.daisukikaffuchino.han1meviewer.logic.model.SearchOption
import org.jetbrains.compose.resources.StringResource

data class SearchScopeSection(
    val titleRes: StringResource,
    val options: List<SearchOption>,
    val spanCount: Int = 3,
)