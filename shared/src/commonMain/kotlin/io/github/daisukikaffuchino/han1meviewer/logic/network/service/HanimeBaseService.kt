package io.github.daisukikaffuchino.han1meviewer.logic.network.service

import androidx.annotation.IntRange
import io.ktor.client.statement.HttpResponse
import de.jensklingenberg.ktorfit.http.*

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:10
 */
interface HanimeBaseService {

    @GET
    suspend fun getHomePage(@Url url: String): HttpResponse

    @GET("search")
    suspend fun getHanimeSearchResult(
        @Query("page") @IntRange(from = 1) page: Int = 1,
        @Query("query") query: String? = null,
        @Query("genre") genre: String? = null,
        @Query("sort") sort: String? = null,
        @Query("broad") broad: String? = null,
//        @Query("year") year: Int? = null,
//        @Query("month") month: Int? = null,
        @Query("date") date: String? = null,
        @Query("duration") duration: String? = null,
        // 必须是 List：Ktorfit 只对 List 展开成重复参数，Set 会被整体 toString 成 "[a, b]"
        @Query("tags[]") tags: List<String> = emptyList(),
        @Query("brands[]") brands: List<String> = emptyList(),
    ): HttpResponse

    @GET("watch")
    suspend fun getHanimeVideo(
        @Query("v") videoCode: String,
    ): HttpResponse

    @GET("previews/{date}")
    suspend fun getHanimePreview(
        @Path("date") date: String, // 类似 202206. 202012
    ): HttpResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("_token") csrfToken: String?,
        @Field("email") email: String,
        @Field("password") password: String,
        @Header("X-CSRF-TOKEN") csrfToken_1: String? = csrfToken,
    ): HttpResponse

    @GET("login")
    suspend fun getLoginPage(): HttpResponse

    @GET("subscriptions")
    suspend fun getMySubscriptions(
        @Query("page") page: Int
    ): HttpResponse

}
