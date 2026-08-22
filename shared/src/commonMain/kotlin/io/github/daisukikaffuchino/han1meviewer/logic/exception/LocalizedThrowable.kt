package io.github.daisukikaffuchino.han1meviewer.logic.exception

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString

/**
 * 带资源 id 的异常。CMP 的 getString 是 suspend 的，Repo/Parser 里没法同步取字符串，
 * 所以只携带资源，等展示时再解析。
 */
interface LocalizedThrowable {
    val messageResource: StringResource?
}

/** 没有专门类型、只是要带一句本地化文案的场合。 */
class LocalizedException(
    override val messageResource: StringResource,
) : IllegalStateException("localized: ${messageResource.key}"), LocalizedThrowable

/** 展示用：带资源的解析资源，否则退回 message。 */
@Composable
fun Throwable.localizedText(): String {
    val res = (this as? LocalizedThrowable)?.messageResource
    return if (res != null) stringResource(res) else message.orEmpty()
}

/** 同上，给协程里用（比如 collect 到错误后弹 toast）。 */
suspend fun Throwable.localizedString(): String {
    val res = (this as? LocalizedThrowable)?.messageResource
    return if (res != null) getString(res) else message.orEmpty()
}
