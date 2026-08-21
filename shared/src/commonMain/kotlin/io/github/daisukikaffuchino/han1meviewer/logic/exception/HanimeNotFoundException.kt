package io.github.daisukikaffuchino.han1meviewer.logic.exception

import org.jetbrains.compose.resources.StringResource

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/07 007 13:08
 */
class HanimeNotFoundException(
    override val messageResource: StringResource,
) : RuntimeException("hanime not found"), LocalizedThrowable