package io.github.daisukikaffuchino.han1meviewer.logic.exception

import org.jetbrains.compose.resources.StringResource

/**
 * 解析錯誤
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/05 005 16:20
 */
class ParseException : RuntimeException, LocalizedThrowable {

    override val messageResource: StringResource?

    constructor(funcName: String, varName: String) :
        super("[Parse::$funcName => $varName] parse error!") {
        messageResource = null
    }

    constructor(reason: String) : super(reason) {
        messageResource = null
    }

    constructor(resource: StringResource) : super("parse error") {
        messageResource = resource
    }
}
