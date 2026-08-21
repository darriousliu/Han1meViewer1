package io.github.daisukikaffuchino.han1meviewer.logic

import io.github.daisukikaffuchino.utils.LogUtil
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.R
import io.github.daisukikaffuchino.han1meviewer.logic.model.Announcement
import io.github.daisukikaffuchino.utils.applicationContext
import io.github.daisukikaffuchino.utils.decodeFromStringByBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.http.HttpHeaders
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.HttpClient

@Serializable
data class AppUpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val updateDescription: String,
    val forceUpdate: Boolean,
)

data class AppUpdateCheckResult(
    val updateInfo: AppUpdateInfo? = null,
    val announcement: Announcement? = null,
)

sealed interface AppUpdateState {
    data object Checking : AppUpdateState
    data object NoUpdate : AppUpdateState
    data class Available(val info: AppUpdateInfo) : AppUpdateState
}

@Serializable
private data class AppUpdatePayload(
    val versionName: String? = null,
    val versionCode: Int = 0,
    val downloadUrl: String? = null,
    val updateDescription: String = "",
    val forceUpdate: Boolean = false,
    val isShowAnnouncement: Boolean = false,
    val announcement: String = "",
)

@OptIn(ExperimentalSerializationApi::class)
object AppUpdateChecker {
    private const val TAG = "AppUpdateChecker"
    private const val ENCODED_UPDATE_URL =
        "aHR0cHM6Ly9obm0tMTI1ODY2NDI3Ni5jb3MuYXAtc2hhbmdoYWkubXlxY2xvdWQuY29tL3VwZGF0ZS5qc29u"
    private const val ENCODED_UPDATE_REFERER = "aG5tdmlld2VydXAuY29t"
    private const val CURRENT_VERSION_CODE = 260805

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        allowTrailingComma = true
    }

    private val client by lazy {
        HttpClient(OkHttp) {
            expectSuccess = false
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 15_000
            }
        }
    }

    suspend fun checkForUpdate(): AppUpdateCheckResult = withContext(Dispatchers.IO) {
        val cachedJson = SettingsRepository.current.cachedUpdateJson

        val responseJson = runCatching { requestUpdateJson() }
            .onFailure { LogUtil.e(TAG, "Failed to check for updates", it) }
            .getOrNull()

        if (responseJson != null) SettingsRepository.setCachedUpdateJson(responseJson)

        val jsonToUse = responseJson ?: cachedJson
        if (responseJson == null) {
            jsonToUse?.let { LogUtil.d(TAG, "Using stale update JSON: $it") }
        }
        jsonToUse.toUpdateCheckResult()
    }

    suspend fun ignoreUpdate(versionCode: Int) = SettingsRepository.setIgnoredVersionCode(versionCode)

    private suspend fun requestUpdateJson(): String {
        val response = client.get(ENCODED_UPDATE_URL.decodeFromStringByBase64()) {
            header(
                HttpHeaders.Referrer,
                ENCODED_UPDATE_REFERER.decodeFromStringByBase64()
            )
        }
        check(response.status.isSuccess()) {
            "Update check failed with HTTP ${response.status.value}"
        }
        return response.bodyAsText().also { json ->
            LogUtil.d(TAG, "Update response JSON: $json")
        }
    }

    private fun String?.toUpdateCheckResult(): AppUpdateCheckResult {
        if (this.isNullOrBlank()) return AppUpdateCheckResult()
        return runCatching {
            val payload = jsonParser.decodeFromString<AppUpdatePayload>(this)
            AppUpdateCheckResult(
                updateInfo = payload.toAvailableUpdateOrNull(),
                announcement = payload.toAnnouncementOrNull(),
            )
        }.onFailure {
            LogUtil.e(TAG, "Invalid update JSON", it)
        }.getOrDefault(AppUpdateCheckResult())
    }

    private fun AppUpdatePayload.toAvailableUpdateOrNull(): AppUpdateInfo? {
        val versionName = versionName?.trim().orEmpty()
        val downloadUrl = downloadUrl?.trim().orEmpty()
        if (versionName.isBlank() || versionCode <= 0 || downloadUrl.isBlank()) return null
        if (runCatching { Url(downloadUrl) }.getOrNull()?.host.isNullOrBlank()) {
            LogUtil.e(TAG, "downloadUrl is invalid")
            return null
        }

        val currentVersionCode = CURRENT_VERSION_CODE
        val ignoredVersionCode = SettingsRepository.current.ignoredVersionCode
        return AppUpdateInfo(
            versionName = versionName,
            versionCode = versionCode,
            downloadUrl = downloadUrl,
            updateDescription = updateDescription,
            forceUpdate = forceUpdate,
        ).takeIf {
            it.versionCode > currentVersionCode &&
                (it.forceUpdate || it.versionCode != ignoredVersionCode)
        }
    }

    private fun AppUpdatePayload.toAnnouncementOrNull(): Announcement? {
        val content = announcement.trim()
        if (!isShowAnnouncement || content.isBlank()) return null
        return Announcement(
            title = applicationContext.getString(R.string.update_announcement_title),
            content = content,
            isActive = true,
        )
    }
}
