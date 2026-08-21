package io.github.daisukikaffuchino.han1meviewer.logic

import io.github.daisukikaffuchino.utils.LogUtil
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.dao.DownloadDatabase
import io.github.daisukikaffuchino.han1meviewer.logic.dao.HistoryDatabase
import io.github.daisukikaffuchino.han1meviewer.logic.dao.MiscellanyDatabase
import io.github.daisukikaffuchino.han1meviewer.logic.entity.HKeyframeEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.HKeyframeHeader
import io.github.daisukikaffuchino.han1meviewer.logic.entity.HKeyframeType
import io.github.daisukikaffuchino.han1meviewer.logic.entity.HanimeAdvancedSearchHistoryEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.SearchHistoryEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.WatchHistoryEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.DownloadGroupEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeDownloadEntity
import io.github.daisukikaffuchino.han1meviewer.logic.model.SearchOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import han1meviewer.shared.generated.resources.Res
import io.github.daisukikaffuchino.han1meviewer.HJson
import io.github.daisukikaffuchino.han1meviewer.logic.H_KEYFRAME_FILES

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/22 022 23:00
 */
object DatabaseRepo {

    object HKeyframe {
        private val hKeyframeDao = MiscellanyDatabase.instance.hKeyframeDao

        fun loadAll(keyword: String? = null) =
            if (keyword != null) hKeyframeDao.loadAll(keyword)
            else hKeyframeDao.loadAll()

        // #issue-106: 剧集分类
        fun loadAllShared(): Flow<List<HKeyframeType>> = flow {
            // Res.readBytes 是 suspend，不能放进 Sequence lambda，这里用普通循环
            val entities = mutableListOf<HKeyframeEntity>()
            for (fileName in H_KEYFRAME_FILES) {
                runCatching {
                    HJson.decodeFromString<HKeyframeEntity>(
                        Res.readBytes("files/h_keyframes/$fileName").decodeToString()
                    )
                }.onSuccess(entities::add).onFailure { it.printStackTrace() }
            }
            val res = entities
                .sortedWith(compareBy<HKeyframeEntity> { it.group }.thenBy { it.episode })
                .groupBy { it.group ?: "???" }
                .flatMap { (group, list) ->
                    listOf(HKeyframeHeader(title = group, attached = list)) + list
                }
            emit(res)
        }

        suspend fun findBy(videoCode: String) =
            hKeyframeDao.findBy(videoCode)

        fun observe(videoCode: String): Flow<HKeyframeEntity?> {
            if (SettingsRepository.sharedHKeyframesEnable) {
                return flow t@{
                    val find = hKeyframeDao.findBy(videoCode)
                    if (find == null || SettingsRepository.sharedHKeyframesUseFirst) {
                        runCatching {
                            val bytes = Res.readBytes("files/h_keyframes/$videoCode.json")
                            this@t.emit(HJson.decodeFromString<HKeyframeEntity>(bytes.decodeToString()))
                        }.onFailure { e ->
                            // 没有这个共享关键帧文件是正常情况，不当错误报
                            LogUtil.w("HKeyframe", "读取关键帧失败: $videoCode.json (${e.message})")
                        }
                    } else {
                        hKeyframeDao.observe(videoCode).collect {
                            this@t.emit(it)
                        }
                    }
                }.catch t@{ e ->
                    e.printStackTrace()
                    hKeyframeDao.observe(videoCode).collect {
                        this@t.emit(it)
                    }
                }
            }
            return hKeyframeDao.observe(videoCode)
        }

        suspend fun insert(entity: HKeyframeEntity) = hKeyframeDao.insert(entity)

        suspend fun update(entity: HKeyframeEntity) = hKeyframeDao.update(entity)

        suspend fun delete(entity: HKeyframeEntity) =
            hKeyframeDao.delete(entity)

        suspend fun modifyKeyframe(
            videoCode: String,
            oldKeyframe: HKeyframeEntity.Keyframe, keyframe: HKeyframeEntity.Keyframe,
        ) = hKeyframeDao.modifyKeyframe(videoCode, oldKeyframe, keyframe)

        suspend fun appendKeyframe(
            videoCode: String, title: String,
            keyframe: HKeyframeEntity.Keyframe,
        ) = hKeyframeDao.appendKeyframe(videoCode, title, keyframe)

        suspend fun removeKeyframe(
            videoCode: String,
            keyframe: HKeyframeEntity.Keyframe,
        ) = hKeyframeDao.removeKeyframe(videoCode, keyframe)
    }

    object SearchHistory {
        private val searchHistoryDao = HistoryDatabase.instance.searchHistory

        @JvmOverloads
        fun loadAll(keyword: String? = null) =
            if (keyword.isNullOrBlank()) searchHistoryDao.loadAll()
            else searchHistoryDao.loadAll(keyword)

        suspend fun delete(history: SearchHistoryEntity) =
            searchHistoryDao.delete(history)

        suspend fun insert(history: SearchHistoryEntity) =
            searchHistoryDao.insertOrUpdate(history)

        suspend fun deleteByKeyword(query: String) =
            searchHistoryDao.deleteByKeyword(query)
    }

    object HanimeAdvancedSearchRepo {
        private val dao = HistoryDatabase.instance.hanimeAdvancedSearchHistory

        suspend fun saveSearch(
            query: String?,
            genre: String?,
            sort: String?,
            broad: Boolean?,
            date: String?,
            duration: String?,
            tags: Set<SearchOption>?,
            brands: Set<SearchOption>?
        ) {
            val entity = HanimeAdvancedSearchHistoryEntity(
                query = query,
                genre = genre,
                sort = sort,
                broad = broad,
                date = date,
                duration = duration,
                tags = tags?.toDbString(),
                brands = brands?.toDbString()
            )
            dao.insertHistory(entity)
        }

        fun getSearchHistories(limit: Int = 20) = dao.loadHistories(limit)
        suspend fun deleteHistory(id: Long) = dao.deleteHistory(id)
        fun Set<SearchOption>.toDbString(): String =
            mapNotNull { it.searchKey }.joinToString(",")
        fun String.toSearchOptionSet(): Set<SearchOption> =
            if (isBlank()) emptySet()
            else split(",").map { SearchOption(searchKey = it) }.toSet()
    }

    object WatchHistory {
        private val watchHistoryDao = HistoryDatabase.instance.watchHistory

        fun loadAll() =
            watchHistoryDao.loadAll()

        suspend fun delete(history: WatchHistoryEntity) =
            watchHistoryDao.delete(history)

        suspend fun deleteAll() =
            watchHistoryDao.deleteAll()

        suspend fun update(history: WatchHistoryEntity) =
            watchHistoryDao.update(history)

        suspend fun updateProgress(videoCode: String,progress: Long) =
            watchHistoryDao.updateProgress(videoCode, progress)

        suspend fun insert(history: WatchHistoryEntity) =
            watchHistoryDao.insertOrUpdate(history)

        suspend fun findBy(videoCode: String) =
            watchHistoryDao.findBy(videoCode)

        suspend fun getWatched(resultList: List<String>) =
            watchHistoryDao.getWatchedCodes(resultList)
    }

    object HanimeDownload {
        private val hanimeDownloadDao = DownloadDatabase.instance.hanimeDownloadDao
        private val downloadGroupDao = DownloadDatabase.instance.downloadGroupDao
        fun loadAllDownloadingHanime() =
            hanimeDownloadDao.loadAllDownloadingHanime()

        /**
         * 查询所有视频，并且每个视频要有当前他在的分类
         */
        fun loadAllDownloadedHanime(
            sortedBy: HanimeDownloadEntity.SortedBy,
            ascending: Boolean,
        ) = when (sortedBy) {
            HanimeDownloadEntity.SortedBy.TITLE ->
                hanimeDownloadDao.loadAllDownloadedHanimeByTitle(ascending)

            HanimeDownloadEntity.SortedBy.ID ->
                hanimeDownloadDao.loadAllDownloadedHanimeById(ascending)
        }
        suspend fun delete(videoCode: String, quality: String) =
            hanimeDownloadDao.delete(videoCode, quality)

        suspend fun delete(videoCode: String) =
            hanimeDownloadDao.delete(videoCode)

        suspend fun pauseAll() =
            hanimeDownloadDao.pauseAll()

        suspend fun delete(entity: HanimeDownloadEntity) =
            hanimeDownloadDao.delete(entity)

        suspend fun insert(entity: HanimeDownloadEntity) =
            hanimeDownloadDao.insert(entity)

        suspend fun update(entity: HanimeDownloadEntity) =
            hanimeDownloadDao.update(entity)

        suspend fun find(videoCode: String, quality: String) =
            hanimeDownloadDao.find(videoCode, quality)

        suspend fun find(videoCode: String) =
            hanimeDownloadDao.find(videoCode)

        suspend fun insertDefaultGroup() =
            downloadGroupDao.insertDefaultGroup()

        fun getAllGroups()=
            downloadGroupDao.getAllGroups()

        suspend fun getGroupById(id: Int)=
            downloadGroupDao.getGroupById(id)

        suspend fun updateVideoGroup(videoCode: String, newGroupId: Int)=
            hanimeDownloadDao.updateVideoGroup(videoCode, newGroupId)

        suspend fun createNewGroup(name: String): Long{
            val maxIndex = downloadGroupDao.getMaxOrderIndex() ?: 0
            val newIndex = maxIndex + 1
            val newGroup = DownloadGroupEntity(
                name = name,
                orderIndex = newIndex
            )
            return downloadGroupDao.insert(newGroup)
        }

        suspend fun getOrCreateGroup(name: String): Int =
            downloadGroupDao.getOrCreateGroup(name)

        suspend fun deleteGroup(group: DownloadGroupEntity) {
            downloadGroupDao.deleteGroup(group)
        }

        suspend fun updateGroup(group: DownloadGroupEntity)=
            downloadGroupDao.update(group)
    }
}
