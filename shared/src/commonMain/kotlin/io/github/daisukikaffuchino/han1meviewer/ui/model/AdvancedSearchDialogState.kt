package io.github.daisukikaffuchino.han1meviewer.ui.model

import io.github.daisukikaffuchino.han1meviewer.logic.model.SearchOption
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.release_date
import org.jetbrains.compose.resources.StringResource

sealed interface AdvancedSearchDialogState {
    val key: String
    val titleRes: StringResource

    data class SingleChoice(
        override val key: String,
        override val titleRes: StringResource,
        val options: List<SearchOption>,
        val selectedIndex: Int,
        val onSelect: (SearchOption) -> Unit,
        val onReset: () -> Unit,
    ) : AdvancedSearchDialogState

    data class MultiChoice(
        override val key: String,
        override val titleRes: StringResource,
        val scopes: List<SearchScopeSection>,
        val selected: Set<SearchOption>,
        val broad: Boolean,
        val onSave: (Set<SearchOption>, Boolean) -> Unit,
        val onReset: () -> Unit,
    ) : AdvancedSearchDialogState

    data class ReleaseDate(
        override val key: String,
        val options: List<SearchOption>,
        val initialApproximate: String?,
        val initialYear: Int?,
        val initialMonth: Int?,
        val onSaveApproximate: (String?) -> Unit,
        val onSaveSpecific: (Int, Int?) -> Unit,
        val onReset: () -> Unit,
    ) : AdvancedSearchDialogState {
        override val titleRes: StringResource = Res.string.release_date
    }
}