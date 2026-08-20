package io.github.daisukikaffuchino.han1meviewer.logic.network.service

import io.ktor.client.statement.HttpResponse
import de.jensklingenberg.ktorfit.http.*

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/09/19 019 17:44
 */
interface HanimeCommentService {
    @GET("loadComment")
    suspend fun getComments(
        @Query("type") type: String, // 類似 "video", "preview"
        @Query("id") code: String,
    ): HttpResponse

    @GET("loadReplies")
    suspend fun getCommentReply(
        @Query("id") commentId: String,
    ): HttpResponse

    @FormUrlEncoded
    @POST("createComment")
    suspend fun postComment(
        @Field("_token") csrfToken: String?,
        @Field("comment-user-id") currentUserId: String,
        @Field("comment-type") type: String, // 類似 "video", "preview"
        @Field("comment-foreign-id") targetUserId: String,
        @Field("comment-text") text: String,
        @Field("comment-count") count: Int = 1, // 感觉没什么用，仅前端用
        @Field("comment-is-political") isPolitical: Int = 0, // 感觉没什么用，仅前端用
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse

    @FormUrlEncoded
    @POST("replyComment")
    suspend fun postCommentReply(
        @Field("_token") csrfToken: String?,
        @Field("reply-comment-id") replyCommentId: String,
        @Field("reply-comment-text") text: String,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse

    @FormUrlEncoded
    @POST("commentLike")
    suspend fun likeComment(
        @Field("_token") csrfToken: String?,
        @Field("foreign_type") foreignType: String,
        @Field("foreign_id") foreignId: String?,
        @Field("is_positive") isPositive: Int, // 你選擇的是讚還是踩，1是讚，0是踩
        @Field("comment-like-user-id") likeUserId: String?,
        @Field("comment-likes-count") commentLikesCount: Int,
        @Field("comment-likes-sum") commentLikesSum: Int,
        @Field("like-comment-status") likeCommentStatus: Int, // 你之前有沒有點過讚，1是0否
        @Field("unlike-comment-status") unlikeCommentStatus: Int, // 你之前有沒有點過踩，1是0否
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse

    @FormUrlEncoded
    @POST("user/{userId}/report")
    suspend fun submitReport(
        @Path("userId") userId: String,
        @Field("_token") csrfToken: String?,
        @Field("redirect-url") redirectUrl: String,
        @Field("reportable-id") reportableId: String?,
        @Field("reportable-type") reportableType: String?,
        @Field("reason") reason: String,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken
    ): HttpResponse
}