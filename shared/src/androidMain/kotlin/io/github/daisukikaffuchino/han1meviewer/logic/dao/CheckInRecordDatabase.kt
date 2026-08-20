package io.github.daisukikaffuchino.han1meviewer.logic.dao

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import io.github.daisukikaffuchino.han1meviewer.logic.entity.CheckInRecordEntity

@Database(
    entities = [CheckInRecordEntity::class],
    version = 5,
    exportSchema = false
)
abstract class CheckInRecordDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInRecordDao

    companion object {
        @Volatile
        private var INSTANCE: CheckInRecordDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE check_in_records_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        type TEXT NOT NULL DEFAULT '自慰',
                        feeling TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
                // 旧表一行 (date, count) 展开成 count 行，上限 20 条，与迁移前逐字一致
                val records = buildList {
                    connection.prepare("SELECT date, count FROM check_in_records")
                        .use { statement ->
                            while (statement.step()) {
                                add(statement.getText(0) to statement.getInt(1))
                            }
                        }
                }
                connection.prepare(
                    "INSERT INTO check_in_records_new (date, type, feeling) VALUES (?, '自慰', '')"
                ).use { statement ->
                    records.forEach { (date, count) ->
                        repeat(count.coerceAtMost(20)) {
                            statement.bindText(1, date)
                            statement.step()
                            statement.reset()
                            statement.clearBindings()
                        }
                    }
                }
                connection.execSQL("DROP TABLE check_in_records")
                connection.execSQL("ALTER TABLE check_in_records_new RENAME TO check_in_records")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE check_in_records ADD COLUMN time TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override suspend fun migrate(connection: SQLiteConnection) = Unit
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE check_in_records_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        time TEXT NOT NULL,
                        type TEXT NOT NULL,
                        feeling TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                connection.execSQL(
                    """
                    INSERT INTO check_in_records_new (id, date, time, type, feeling)
                    SELECT id, date, time, type, feeling FROM check_in_records
                    """.trimIndent()
                )
                connection.execSQL("DROP TABLE check_in_records")
                connection.execSQL("ALTER TABLE check_in_records_new RENAME TO check_in_records")
                connection.execSQL("DROP TABLE IF EXISTS sidedishes")
            }
        }

        fun getDatabase(context: Context): CheckInRecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CheckInRecordDatabase::class.java,
                    "check_in_records"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                    )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
