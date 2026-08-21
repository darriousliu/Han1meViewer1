package io.github.daisukikaffuchino.han1meviewer.logic.dao

import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.databasesDir
import io.github.vinceglb.filekit.div

internal expect inline fun <reified T : RoomDatabase> Room.databaseBuilder(name: String): RoomDatabase.Builder<T>

/**
 * Android 上 FileKit.databasesDir 就是 context.getDatabasePath(..).parentFile，
 * 与原来 Room.databaseBuilder(context, .., name) 落的位置逐字相同，老库不会丢。
 */
internal fun roomDatabasePath(name: String): String =
    (FileKit.databasesDir / name).absolutePath()
