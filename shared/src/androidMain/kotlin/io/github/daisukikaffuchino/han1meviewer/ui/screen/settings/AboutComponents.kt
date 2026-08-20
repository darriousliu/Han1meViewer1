package io.github.daisukikaffuchino.han1meviewer.ui.screen.settings

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import io.github.daisukikaffuchino.han1meviewer.ui.component.HapticTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.confirm
import han1meviewer.shared.generated.resources.usage_notice_content
import han1meviewer.shared.generated.resources.user_terms

@Composable
fun UsageTermsDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.user_terms)) },
        text = {
            SelectionContainer {
                Text(
                    text = stringResource(Res.string.usage_notice_content),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.confirm))
            }
        },
    )
}
