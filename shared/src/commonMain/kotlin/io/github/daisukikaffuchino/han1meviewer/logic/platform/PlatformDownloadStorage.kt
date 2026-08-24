package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.vinceglb.filekit.PlatformFile

/** 当前下载目录的展示名；没设过返回 null。 */
expect fun getDownloadPath(): String?

/**
 * 选好目录后持久化下来：Android 是 SAF 的 uri 授权，桌面/iOS 是 bookmark。
 * 返回 false 表示没存住，调用侧不该当成设置成功。
 */
expect suspend fun persistDownloadDirectory(file: PlatformFile): Boolean

/** 已保存的下载目录当前是否还可写。 */
expect fun hasDownloadDirectoryPermission(): Boolean

/** 删掉某个视频的下载目录。 */
expect suspend fun deleteDownloadVideoFolder(videoCode: String)

/** 扫描用户选的下载目录，把已有文件导入数据库。 */
expect suspend fun importDownloadedVideos(dao: HanimeDownloadDao): Boolean

/**
 * 现在能不能扫描导入。
 *
 * Android 要先选好公共目录才有东西可扫；桌面/iOS 的下载目录没选过就落在应用目录里，
 * 始终可扫，不该拿 Android 那套 SAF 前提去挡。
 */
expect fun canImportDownloadedVideos(): Boolean

/** 只有 Android 分「应用私有目录 / 公共目录」，其余平台隐藏迁移入口。 */
expect val isDownloadMigrationSupported: Boolean

/**
 * 把私有目录里的下载迁到用户选的公共目录。
 * onProgress 的 total 为 0 表示没有可迁移的文件，-1 表示没权限。
 */
expect fun migrateDownloadsToPublicStorage(
    dao: HanimeDownloadDao?,
    onProgress: (migrated: Int, total: Int) -> Unit,
)
