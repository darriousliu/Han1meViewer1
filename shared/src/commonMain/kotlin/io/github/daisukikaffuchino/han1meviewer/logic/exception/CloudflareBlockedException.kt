package io.github.daisukikaffuchino.han1meviewer.logic.exception

import org.jetbrains.compose.resources.StringResource

/**
 * 检测到爬虫被封鎖
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/07 007 12:45
 */
open class CloudflareBlockedException(
    override val messageResource: StringResource,
) : RuntimeException("cloudflare blocked"), LocalizedThrowable
