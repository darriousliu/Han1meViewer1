package io.github.daisukikaffuchino.han1meviewer.logic

import io.github.daisukikaffuchino.han1meviewer.BuildConfig
import io.github.daisukikaffuchino.han1meviewer.logic.dao.CheckInRecordDatabase
import io.github.daisukikaffuchino.han1meviewer.logic.dao.DownloadDatabase
import io.github.daisukikaffuchino.han1meviewer.logic.dao.HistoryDatabase
import io.github.daisukikaffuchino.han1meviewer.logic.dao.MiscellanyDatabase
import io.github.daisukikaffuchino.han1meviewer.logic.datastore.DataStoreManager
import io.github.daisukikaffuchino.han1meviewer.logic.entity.CheckInRecordEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.HKeyframeEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.WatchHistoryEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.DownloadCategoryEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.DownloadGroupEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeCategoryCrossRef
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeDownloadEntity
import io.github.daisukikaffuchino.han1meviewer.logic.network.HanimeNetwork
import io.github.daisukikaffuchino.han1meviewer.logic.network.rebuildPlatformNetworking
import io.github.daisukikaffuchino.han1meviewer.logic.platform.updateCheckInWidget
import io.github.daisukikaffuchino.utils.selectAppLanguage
import io.github.daisukikaffuchino.han1meviewer.logic.platform.setMaxConcurrentDownloadCount
import io.github.daisukikaffuchino.han1meviewer.util.switchLauncherIcon
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * 备份的读写全部走 FileKit 的 [PlatformFile]，Android 上就是 SAF uri，
 * 桌面/iOS 上是普通路径，所以整个实现可以放在 commonMain。
 */
object BackupManager {
    private const val BACKUP_VERSION = 1

    // 不用公共的 HJson：备份文件是给用户看的，要缩进；也必须写出默认值，
    // 否则 version 这类字段会被省掉。
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    @Serializable
    private data class BackupData(
        val version: Int = BACKUP_VERSION,
        val appVersionCode: Int = BuildConfig.VERSION_CODE,
        val appVersionName: String = BuildConfig.VERSION_NAME,
        val exportedAt: Long = Clock.System.now().toEpochMilliseconds(),
        val settings: Map<String, PreferenceValue>? = null,
        val hKeyframes: List<HKeyframeEntity>? = null,
        val checkInRecords: List<CheckInRecordEntity>? = null,
        val watchHistories: List<WatchHistoryEntity>? = null,
        val downloadGroups: List<DownloadGroupEntity>? = null,
        val downloads: List<HanimeDownloadEntity>? = null,
        val downloadCategories: List<DownloadCategoryEntity>? = null,
        val downloadCategoryCrossRefs: List<HanimeCategoryCrossRef>? = null,
    )

    @Serializable
    private sealed interface PreferenceValue {
        @Serializable
        data class BooleanValue(val value: Boolean) : PreferenceValue

        @Serializable
        data class FloatValue(val value: Float) : PreferenceValue

        @Serializable
        data class IntValue(val value: Int) : PreferenceValue

        @Serializable
        data class LongValue(val value: Long) : PreferenceValue

        @Serializable
        data class StringValue(val value: String) : PreferenceValue

        @Serializable
        data class StringSetValue(val value: Set<String>) : PreferenceValue
    }

    suspend fun exportTo(file: PlatformFile) {
        val backup = BackupData(
            settings = DataStoreManager.exportBackup().mapValuesNotNull { (_, value) ->
                value.toPreferenceValue()
            },
            hKeyframes = MiscellanyDatabase.instance.hKeyframeDao.getAll(),
            checkInRecords = CheckInRecordDatabase.instance.checkInDao().getAllRecords(),
            watchHistories = HistoryDatabase.instance.watchHistory.getAll(),
            downloadGroups = DownloadDatabase.instance.downloadGroupDao.getAllGroupsOnce(),
            downloads = DownloadDatabase.instance.hanimeDownloadDao.getAll(),
            downloadCategories = DownloadDatabase.instance.downloadCategoryDao.getAllCategoriesOnce(),
            downloadCategoryCrossRefs = DownloadDatabase.instance.downloadCategoryDao.getAllCrossRefs(),
        )
        file.write(json.encodeToString(backup).encodeToByteArray())
    }

    suspend fun importFrom(file: PlatformFile) {
        val backup = json.decodeFromString<BackupData>(file.readBytes().decodeToString())

        backup.hKeyframes?.let { hKeyframes ->
            MiscellanyDatabase.instance.hKeyframeDao.apply {
                deleteAll()
                insertAll(hKeyframes)
            }
        }

        backup.checkInRecords?.let { checkInRecords ->
            CheckInRecordDatabase.instance.checkInDao().apply {
                deleteAll()
                insertAll(checkInRecords)
            }
        }

        backup.watchHistories?.let { watchHistories ->
            HistoryDatabase.instance.watchHistory.apply {
                deleteAll()
                insertAll(watchHistories)
            }
        }

        if (backup.downloadGroups != null || backup.downloads != null ||
            backup.downloadCategories != null || backup.downloadCategoryCrossRefs != null
        ) {
            val downloadGroups = backup.downloadGroups.orEmpty()
            val groupIds = downloadGroups.mapTo(mutableSetOf()) { it.id } +
                    DownloadGroupEntity.DEFAULT_GROUP_ID
            val downloads = backup.downloads.orEmpty().map { download ->
                if (download.groupId in groupIds) {
                    download
                } else {
                    download.copy(groupId = DownloadGroupEntity.DEFAULT_GROUP_ID)
                }
            }
            val downloadCategories = backup.downloadCategories.orEmpty()
            val downloadIds = downloads.mapTo(mutableSetOf()) { it.id }
            val categoryIds = downloadCategories.mapTo(mutableSetOf()) { it.id }
            val crossRefs = backup.downloadCategoryCrossRefs.orEmpty().filter { crossRef ->
                crossRef.videoId in downloadIds && crossRef.categoryId in categoryIds
            }

            DownloadDatabase.instance.apply {
                downloadCategoryDao.deleteAllCrossRefs()
                hanimeDownloadDao.deleteAll()
                downloadCategoryDao.deleteAllCategories()
                downloadGroupDao.deleteAll()
                downloadGroupDao.insertAll(downloadGroups)
                downloadGroupDao.insertDefaultGroup()
                downloadCategoryDao.insertAllCategories(downloadCategories)
                hanimeDownloadDao.insertAll(downloads)
                downloadCategoryDao.insertAllCrossRefs(crossRefs)
            }
        }

        backup.settings?.let {
            DataStoreManager.restoreBackup(it.mapValues { (_, value) -> value.rawValue })
            // 下面几项是「设置变了要立刻生效」的副作用，平台做不到的那端各自是空实现
            selectAppLanguage(SettingsRepository.current.appLanguage)
            rebuildPlatformNetworking()
            HanimeNetwork.rebuildNetwork()
            setMaxConcurrentDownloadCount(SettingsRepository.current.downloadCountLimit)
            switchLauncherIcon(SettingsRepository.current.fakeLauncherIcon)
        }

        runCatching { updateCheckInWidget() }
    }

    private inline fun <K, V, R : Any> Map<K, V>.mapValuesNotNull(
        transform: (Map.Entry<K, V>) -> R?
    ): Map<K, R> {
        return mapNotNull { entry -> transform(entry)?.let { entry.key to it } }.toMap()
    }

    private fun Any?.toPreferenceValue(): PreferenceValue? {
        return when (this) {
            is Boolean -> PreferenceValue.BooleanValue(this)
            is Float -> PreferenceValue.FloatValue(this)
            is Int -> PreferenceValue.IntValue(this)
            is Long -> PreferenceValue.LongValue(this)
            is String -> PreferenceValue.StringValue(this)
            is Set<*> -> PreferenceValue.StringSetValue(this.filterIsInstance<String>().toSet())
            else -> null
        }
    }

    private val PreferenceValue.rawValue: Any
        get() = when (this) {
            is PreferenceValue.BooleanValue -> value
            is PreferenceValue.FloatValue -> value
            is PreferenceValue.IntValue -> value
            is PreferenceValue.LongValue -> value
            is PreferenceValue.StringSetValue -> value
            is PreferenceValue.StringValue -> value
        }
}
