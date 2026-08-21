package io.github.daisukikaffuchino.han1meviewer.logic.model

import io.github.daisukikaffuchino.han1meviewer.util.Parcelable
import io.github.daisukikaffuchino.han1meviewer.util.Parcelize

@Parcelize
data class MySubscriptions(
    val subscriptions: List<SubscriptionItem>,
    val subscriptionsVideos: List<SubscriptionVideosItem>,
    val maxPage: Int
) : Parcelable

@Parcelize
data class SubscriptionItem(
    val artistName: String,
    val avatar: String
) : Parcelable

@Parcelize
data class SubscriptionVideosItem(
    override val title: String,
    override val coverUrl: String,
    override val videoCode: String,
    override val duration: String? = null,
    override val views: String? = null,
    override val reviews: String? = null,
    override val currentArtist: String? = null,
    override val uploadTime: String?= null,
) : Parcelable, VideoItemType
