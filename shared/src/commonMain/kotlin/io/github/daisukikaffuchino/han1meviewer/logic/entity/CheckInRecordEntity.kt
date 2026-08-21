package io.github.daisukikaffuchino.han1meviewer.logic.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "check_in_records")
data class CheckInRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val time: String,
    val type: String,
    val feeling: String
)
