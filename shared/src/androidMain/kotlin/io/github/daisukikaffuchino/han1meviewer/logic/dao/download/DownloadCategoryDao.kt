package io.github.daisukikaffuchino.han1meviewer.logic.dao.download

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.RewriteQueriesToDropUnusedColumns
import androidx.room3.Transaction
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.DownloadCategoryEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeCategoryCrossRef
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeDownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DownloadCategoryDao {
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM HanimeDownloadEntity " +
                "INNER JOIN HanimeCategoryCrossRef ON HanimeDownloadEntity.id = HanimeCategoryCrossRef.videoId " +
                "WHERE HanimeCategoryCrossRef.categoryId = :categoryId AND HanimeDownloadEntity.downloadedLength == HanimeDownloadEntity.length"
    )
    abstract fun getVideosForCategory(categoryId: Int): Flow<List<HanimeDownloadEntity>>

    @Query("SELECT * FROM DownloadCategoryEntity")
    abstract fun getAllCategories(): Flow<MutableList<DownloadCategoryEntity>>

    @Query("SELECT * FROM DownloadCategoryEntity")
    abstract suspend fun getAllCategoriesOnce(): List<DownloadCategoryEntity>

    @Query("SELECT * FROM HanimeCategoryCrossRef")
    abstract suspend fun getAllCrossRefs(): List<HanimeCategoryCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllCategories(categories: List<DownloadCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllCrossRefs(crossRefs: List<HanimeCategoryCrossRef>)

    @Query("DELETE FROM HanimeCategoryCrossRef")
    abstract suspend fun deleteAllCrossRefs()

    @Query("DELETE FROM DownloadCategoryEntity")
    abstract suspend fun deleteAllCategories()
}
