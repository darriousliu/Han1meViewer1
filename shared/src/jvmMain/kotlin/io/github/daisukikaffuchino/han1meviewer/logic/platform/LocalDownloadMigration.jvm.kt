package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.utils.LogUtil
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

// 同卷是改名，跨卷时 JDK 自己退化成复制加删除
internal actual suspend fun movePlatformFile(from: PlatformFile, to: PlatformFile): Boolean =
    runCatching {
        Files.move(Path.of(from.path), Path.of(to.path), StandardCopyOption.REPLACE_EXISTING)
        true
    }.onFailure { LogUtil.e("Migrate", "搬运失败: ${from.path}", it) }.getOrDefault(false)
