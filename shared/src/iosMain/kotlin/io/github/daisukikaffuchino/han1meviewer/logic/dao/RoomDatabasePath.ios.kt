package io.github.daisukikaffuchino.han1meviewer.logic.dao

import androidx.room3.Room
import androidx.room3.RoomDatabase

// ⚠️ 这份实现两端一字不差，但**不能**挪进 jvmIosMain 共用一份。
// 底下调的 Room.databaseBuilder(name) 是 Room 各平台各自提供的重载，在中间源集的
// metadata 视图里看不见，解析会落到上面这个 actual 自己身上，编译器报
// 「Inline function ... cannot be recursive」。只有 compileJvmIosMainKotlinMetadata
// 会报这个错，叶子 target 的编译（那里平台重载可见）是过得去的。
internal actual inline fun <reified T : RoomDatabase> Room.databaseBuilder(
    name: String
): RoomDatabase.Builder<T> {
    return Room.databaseBuilder(name = name)
}
