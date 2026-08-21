package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.CloudflareRoute
import io.github.daisukikaffuchino.han1meviewer.util.NavigationEvent
import io.ktor.http.Url
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource
import io.github.daisukikaffuchino.utils.LogUtil

/**
 * Coalesces simultaneous challenges for the same host and gives every waiting request a
 * definitive result. A cancelled verification must not retry its original request without a
 * clearance cookie.
 */
@OptIn(InternalCoroutinesApi::class)
object CloudflareVerificationCoordinator {

    private val verificationTimeout = 5.minutes

    private class Verification {
        // 不关联任何单个网络请求的 Job：
        // 某个等待者取消时，不能取消其他请求共享的验证。
        val result = CompletableDeferred<Boolean>()
        val startedAt = TimeSource.Monotonic.markNow()
    }

    private val lock = SynchronizedObject()
    private val verifications = mutableMapOf<String, Verification>()

    suspend fun verify(url: String): Boolean {
        val host = runCatching { Url(url).host.lowercase() }.getOrNull() ?: return false

        val (verification, shouldLaunch) = synchronized(lock) {
            val existing = verifications[host]
            if (existing != null) {
                existing to false
            } else {
                Verification().let { created ->
                    verifications[host] = created
                    created to true
                }
            }
        }

        if (shouldLaunch) {
            // tryEmit 投递失败不抛异常，只返回 false，这里必须自己判，
            // 否则没人拉起过盾页，调用方要一直挂到超时。
            val launched = try {
                NavigationEvent.navigation(CloudflareRoute(url, host))
            } catch (_: Exception) {
                false
            }
            if (!launched) {
                LogUtil.e("Cloudflare", "过盾页拉起失败，没有导航订阅者？host=$host")
                failed(host = host, expected = verification)
                return verification.result.await()
            }
        }

        // 同一轮验证的所有等待者共享同一个五分钟期限，
        // 后加入者不会重新获得完整的五分钟。
        val remainingTime =
            verificationTimeout - verification.startedAt.elapsedNow()

        val result = withTimeoutOrNull(remainingTime) {
            verification.result.await()
        }

        if (result != null) {
            return result
        }

        // 超时是整轮验证失败，而不只是当前等待者失败。
        // 完成 Deferred 会一次性唤醒所有等待者。
        failed(host = host, expected = verification)

        // 处理超时和 UI 完成同时发生的竞态，返回真正胜出的结果。
        return verification.result.await()
    }

    fun complete(host: String, succeeded: Boolean) {
        val key = host.lowercase()
        val verification = synchronized(lock) {
            verifications.remove(key)
        } ?: return

        verification.result.complete(succeeded)
    }

    private fun failed(host: String, expected: Verification) {
        val verification = synchronized(lock) {
            if (verifications[host] === expected) {
                verifications.remove(host)
            } else {
                null
            }
        }

        verification?.result?.complete(false)
    }
}