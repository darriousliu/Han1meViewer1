package io.github.daisukikaffuchino.han1meviewer.logic.entity.download

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation

data class VideoWithCategories(
    @Embedded
    val video: HanimeDownloadEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["id"],
        associateBy = Junction(
            value = HanimeCategoryCrossRef::class,
            parentColumns = ["videoId"],
            entityColumns = ["categoryId"]
        )
    )
    val categories: List<DownloadCategoryEntity>,
)
