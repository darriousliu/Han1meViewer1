package io.github.daisukikaffuchino.han1meviewer.logic.exception

import org.jetbrains.compose.resources.StringResource

class LoginStateExpiredException(
    override val messageResource: StringResource,
) : IllegalStateException("login state expired"), LocalizedThrowable
