package io.github.daisukikaffuchino.han1meviewer.logic.network.service

import io.ktor.client.statement.HttpResponse
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST

interface HanimeSubscriptionService {

    @FormUrlEncoded
    @POST("subscribe")
    suspend fun subscribeArtist(
        @Field("_token") csrfToken: String?,
        @Field("subscribe-user-id") userId: String,
        @Field("subscribe-artist-id") artistId: String,
        // 如果当前未订阅会发送空字符串，否则发1
        @Field("subscribe-status") status: String,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse
}