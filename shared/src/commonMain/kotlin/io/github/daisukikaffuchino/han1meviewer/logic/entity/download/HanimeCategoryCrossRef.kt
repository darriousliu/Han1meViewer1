package io.github.daisukikaffuchino.han1meviewer.logic.entity.download

import androidx.room3.Entity
import androidx.room3.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    primaryKeys = ["videoId", "categoryId"],
    indices = [Index(value = ["categoryId"])],
)
data class HanimeCategoryCrossRef(
    val videoId: Int,
    val categoryId: Int,
)
