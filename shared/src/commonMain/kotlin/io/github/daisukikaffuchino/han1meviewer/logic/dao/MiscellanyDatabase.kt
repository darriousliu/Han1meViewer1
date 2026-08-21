package io.github.daisukikaffuchino.han1meviewer.logic.dao

import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.daisukikaffuchino.han1meviewer.logic.entity.HKeyframeEntity
import androidx.room3.ConstructedBy
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * 这是各种 有数据库需求的小功能 的聚集地，
 * 如果这个功能需要数据库就放到这里。
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/12 012 12:28
 */
@ConstructedBy(MiscellanyDatabaseConstructor::class)
@Database(
    entities = [HKeyframeEntity::class],
    version = 1, exportSchema = false
)
abstract class MiscellanyDatabase : RoomDatabase() {

    abstract val hKeyframeDao: HKeyframeDao

    companion object {
        val instance by lazy {
            Room.databaseBuilder<MiscellanyDatabase>(name = roomDatabasePath("miscellany.db"))
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO).build()
        }
    }
}


@Suppress("KotlinNoActualForExpect")
expect object MiscellanyDatabaseConstructor : RoomDatabaseConstructor<MiscellanyDatabase> {
    override fun initialize(): MiscellanyDatabase
}
