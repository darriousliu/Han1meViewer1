package io.github.daisukikaffuchino.han1meviewer.logic.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/02 002 13:13
 */
@Serializable
@Entity
data class WatchHistoryEntity(
    val coverUrl: String,
    val title: String,
    val releaseDate: Long,
    val watchDate: Long,
    val videoCode: String,
    val progress: Long = 0L,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {

    val releaseDateDays: Int
        get() = (releaseDate / (24 * 60 * 60 * 1000)).toInt()
}
