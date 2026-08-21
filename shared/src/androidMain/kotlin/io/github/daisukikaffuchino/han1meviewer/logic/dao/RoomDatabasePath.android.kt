package io.github.daisukikaffuchino.han1meviewer.logic.dao

import androidx.room3.Room
import androidx.room3.RoomDatabase

internal actual inline fun <reified T : RoomDatabase> Room.databaseBuilder(name: String): RoomDatabase.Builder<T> {
    return Room.databaseBuilder(name = name)
}