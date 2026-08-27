package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.update_downloading
import han1meviewer.shared.generated.resources.update_downloading_differential
import han1meviewer.shared.generated.resources.update_installing
import han1meviewer.shared.generated.resources.update_now
import io.github.daisukikaffuchino.han1meviewer.logic.update.InAppUpdateStage
import org.jetbrains.compose.resources.stringResource

/**
 * 桌面端应用内更新的进度对话框。
 *
 * 刻意不可取消：安装那一步会把进程交给系统安装器再重启，中途关掉窗口只会让用户以为出了事。
 * 下载阶段本身是可以中断的，但一个 150MB 级别的包在差分命中时通常只有几 MB，
 * 给取消按钮的收益不抵多出来的状态管理。
 */
@Composable
fun UpdateProgressDialog(stage: InAppUpdateStage) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        confirmButton = {},
        title = { Text(stringResource(Res.string.update_now)) },
        text = {
            Column {
                Text(
                    when (stage) {
                        is InAppUpdateStage.Downloading -> stringResource(
                            if (stage.differential) Res.string.update_downloading_differential
                            else Res.string.update_downloading
                        )

                        InAppUpdateStage.Installing -> stringResource(Res.string.update_installing)
                    }
                )
                Spacer(Modifier.height(16.dp))
                when (stage) {
                    is InAppUpdateStage.Downloading -> LinearProgressIndicator(
                        progress = { stage.percent },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    InAppUpdateStage.Installing -> LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    )
}
