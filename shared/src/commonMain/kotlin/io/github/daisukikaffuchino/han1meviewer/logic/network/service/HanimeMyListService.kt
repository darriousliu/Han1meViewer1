package io.github.daisukikaffuchino.han1meviewer.logic.network.service

import androidx.annotation.IntRange
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.HttpResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.HTTP
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

/**
 * MyList 是指 喜欢的影片 + 稍后再看
 *
 * Playlist 是指 自定义的播放列表
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/26 026 16:30
 */
interface HanimeMyListService {
    @GET("user/{userid}/{type}")
    suspend fun getMyListItems(
        @Path("userid") userId: String,
        @Path("type") listType: String,
        @Query("page") page: Int
    ): HttpResponse

    @GET("user/{userid}/histories")
    suspend fun getOnlineWatchHistories(
        @Path("userid") userId: String,
        @Query("sort") sort: String,
        @Query("page") page: Int,
    ): HttpResponse

    @GET("user/{userid}/edit")
    suspend fun getUserAccountPage(
        @Path("userid") userId: String,
    ): HttpResponse

    @FormUrlEncoded
    @POST("user/{userid}")
    suspend fun updateUserAccountProfile(
        @Path("userid") userId: String,
        @Field("_token") csrfToken: String?,
        @Field("_method") method: String = "patch",
        @Field("type") type: String = "profile",
        @Field("name") name: String,
        @Field("email") email: String,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse

    @FormUrlEncoded
    @POST("user/{userid}")
    suspend fun updateUserAccountPassword(
        @Path("userid") userId: String,
        @Field("_token") csrfToken: String?,
        @Field("_method") method: String = "patch",
        @Field("type") type: String = "password",
        @Field("password_old") oldPassword: String,
        @Field("password_new") newPassword: String,
        @Field("password_new_confirm") newPasswordConfirm: String,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse

    @POST("user/{userid}")
    suspend fun updateUserAccountAvatar(
        @Path("userid") userId: String,
        @Body form: MultiPartFormDataContent,
    ): HttpResponse

    // 带 body 的 DELETE：Ktorfit 的 @FormUrlEncoded 只认 @POST 这类，表单只能自己传
    @HTTP(method = "DELETE", path = "user/tab-item/{id}", hasBody = true)
    suspend fun deleteOnlineWatchHistory(
        @Path("id") videoCode: String,
        @Body form: FormDataContent,
        @Header("X-CSRF-TOKEN") csrfToken: String?,
    ): HttpResponse

    @GET("playlist")
    suspend fun getMyPlayListItems(
        @Query("list") listCode: String,
        @Query("page") page: Int
    ): HttpResponse

    @FormUrlEncoded
    @POST("deletePlayitem")
    suspend fun deleteMyListItems(
        @Field("playlist_id") listType: String,
        @Field("video_id") videoCode: String,
        @Field("count") count: Int = 1, // 隨便傳一個就行
        @Header("X-CSRF-TOKEN") csrfToken: String?,
    ): HttpResponse

    @FormUrlEncoded
    @POST("like")
    suspend fun addToMyFavVideo(
        @Field("like-foreign-id") videoCode: String,
        @Field("like-status") likeStatus: String,
        @Field("_token") csrfToken: String?,
        @Field("like-user-id") userId: String?,
        @Field("like-is-positive") isPositive: Int = 1,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse

    @FormUrlEncoded
    @POST("like")
    suspend fun rateVideo(
        @Field("like-foreign-id") videoCode: String,
        @Field("like-is-positive") isPositive: Int,
        @Field("like-status") likeStatus: String,
        @Field("unlike-status") unlikeStatus: String,
        @Field("likes-count") likesCount: Int,
        @Field("unlikes-count") unlikesCount: Int,
        @Field("_token") csrfToken: String?,
        @Field("like-user-id") userId: String?,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse

    @GET("user/{userid}/playlists")
    suspend fun getPlaylists(
        @Path("userid") userId: String,
        @Query("page") @IntRange(from = 1) page: Int
    ): HttpResponse

    @FormUrlEncoded
    @POST("createPlaylist")
    suspend fun createPlaylist(
        @Field("_token") csrfToken: String?,
        @Field("create-playlist-video-id") videoCode: String,
        @Field("playlist-title") title: String,
        @Field("playlist-description") description: String,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse

    @FormUrlEncoded
    @POST("save")
    suspend fun addToMyList(
        @Field("_token") csrfToken: String?,
        @Field("input_id") listCode: String,
        @Field("video_id") videoCode: String,
        @Field("is_checked") isChecked: Boolean,
        @Field("user_id") userId: String = "",
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse

    @FormUrlEncoded
    @POST("playlist/{list_code}")
    suspend fun modifyPlaylist(
        @Path("list_code") listCode: String,
        @Field("playlist-title") title: String,
        @Field("playlist-description") description: String,
        @Field("playlist-delete") delete: String?, // 删除 "on"，不删除 null
        @Field("_token") csrfToken: String?,
        @Field("_method") method: String? = "PUT",
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse
}
