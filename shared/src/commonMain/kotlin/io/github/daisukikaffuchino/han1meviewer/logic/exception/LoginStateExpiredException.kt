package io.github.daisukikaffuchino.han1meviewer.logic.exception

import org.jetbrains.compose.resources.StringResource

/**
 * 带资源 id 而不是解析好的文案：CMP 的 getString 是 suspend 的，
 * Parser 里没法同步取字符串，交给 UI 层解析。
 */
class LoginStateExpiredException(
    val resource: StringResource? = null,
) : IllegalStateException("Login state expired")
