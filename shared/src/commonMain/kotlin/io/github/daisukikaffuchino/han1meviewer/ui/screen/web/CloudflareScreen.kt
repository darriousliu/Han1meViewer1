package io.github.daisukikaffuchino.han1meviewer.ui.screen.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.daisukikaffuchino.han1meviewer.ui.component.appbar.HanimeScaffold
import io.github.daisukikaffuchino.han1meviewer.ui.preview.ComponentPreview
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.complete_cloudflare_verification
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import io.github.kdroidfilter.webview.web.LoadingState
import io.github.kdroidfilter.webview.web.WebView
import io.github.kdroidfilter.webview.web.WebViewNavigator
import io.github.kdroidfilter.webview.web.rememberWebViewNavigator
import io.github.kdroidfilter.webview.web.rememberWebViewState
import io.github.daisukikaffuchino.han1meviewer.USER_AGENT
import org.jetbrains.compose.resources.getString
import han1meviewer.shared.generated.resources.complete_cloudflare_verification_with_warning
import han1meviewer.shared.generated.resources.current_webview_version
import han1meviewer.shared.generated.resources.webview_version_unknown
import han1meviewer.shared.generated.resources.webview_version_too_low
import han1meviewer.shared.generated.resources.version_check_failed
import io.github.daisukikaffuchino.han1meviewer.util.NativeWebViewHolder
import io.github.daisukikaffuchino.han1meviewer.util.enableDomStorage
import io.github.daisukikaffuchino.han1meviewer.util.readWebViewCookies
import io.github.kdroidfilter.webview.web.NativeWebView
import io.github.daisukikaffuchino.utils.LogUtil
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

/** 轮询间隔：过盾成功到 cookie 落盘之间有几百毫秒的空档，一秒一查足够跟上。 */
private val CHECK_INTERVAL = 1.seconds

/** evaluateJavaScript 的回调有可能不回来（页面正在跳转、WebView 正在重建），别把轮询挂死。 */
private val JS_TIMEOUT = 5.seconds

/** 过盾页 `<head>` 里的 challenge 标记，只要还在就说明盾没过完。 */
private val CHALLENGE_MARKERS =
    listOf("#challenge-form", "#challenge-success-text", "#challenge-error-text")

@Composable
fun CloudflareScreen(
    url: String,
    onSolved: (cookies: String, completedUrl: String) -> Unit,
    onClose: () -> Unit,
) {
    val state = rememberWebViewState(url) {
        customUserAgentString = USER_AGENT
        isJavaScriptEnabled = true
    }
    val navigator = rememberWebViewNavigator()

    val baseWarning = stringResource(Res.string.complete_cloudflare_verification_with_warning)
    var tipText by remember { mutableStateOf(baseWarning) }

    // 桌面端只能顺着原生对象够到 WebView 的 cookie 存储
    val webViewHolder = remember { NativeWebViewHolder() }

    var solved by remember { mutableStateOf(false) }

    // 过盾成功的唯一判据是拿到 cf_clearance，所以整页存续期间按固定节奏轮询。
    //
    // 原来是挂在 loadingState 变化上一次性地查：桌面端的 loadingState 是每 250ms 轮询原生
    // isLoading() 现算的，过完盾落到终态后就再也不动了，那一次查空（cookie 还没落盘、
    // 或者 evaluateJavaScript 的回调没回来）就永远没有第二次机会——表现正是「盾过了，
    // 页面不关」。轮询没有这个问题，顺带也覆盖了「本来就有有效 cf_clearance、
    // 这次压根没弹验证」那条路径。
    LaunchedEffect(Unit) {
        var missLogged = false
        while (!solved) {
            delay(CHECK_INTERVAL)
            // WebView 还没建起来时 evaluateJavaScript 会直接回空串，等它
            if (state.loadingState is LoadingState.Initializing) continue

            val head = navigator.awaitJavaScript("document.head.innerHTML") ?: continue
            if (CHALLENGE_MARKERS.any { it in head }) continue

            // 过盾常带重定向，cookie 要按落地页的 URL 取，host 也从落地页推
            val currentUrl = state.lastLoadedUrl ?: url
            val cookies = readWebViewCookies(webViewHolder.value, currentUrl)
                ?: state.cookieManager.getCookies(currentUrl)
                    .joinToString("; ") { "${it.name}=${it.value}" }
            if (cookies.contains("cf_clearance")) {
                solved = true
                onSolved(cookies, currentUrl)
            } else if (!missLogged) {
                missLogged = true
                LogUtil.w("Cloudflare", "页面已无 challenge 标记但读不到 cf_clearance，继续等：$currentUrl")
            }
        }
    }

    // WebView 版本提示。与上面那条分开跑，取不到 UA 也不能耽误过盾检测。
    LaunchedEffect(Unit) {
        repeat(UA_CHECK_ATTEMPTS) {
            delay(CHECK_INTERVAL)
            val output = navigator.awaitJavaScript("navigator.userAgent") ?: return@repeat
            val userAgent = output.removeSurrounding("\"")
                .replace("\\\"", "\"").replace("\\\\", "\\")
            val versionCode = CHROME_VERSION_REGEX.find(userAgent)
                ?.groupValues?.getOrNull(1) ?: userAgent
            var t = baseWarning + getString(Res.string.current_webview_version, versionCode)
            t += try {
                val parts = versionCode.split(".").map { part -> part.toIntOrNull() ?: 0 }
                when {
                    parts.size < 4 -> getString(Res.string.webview_version_unknown)
                    parts[0] < 120 -> getString(Res.string.webview_version_too_low)
                    else -> ""
                }
            } catch (_: Exception) {
                getString(Res.string.version_check_failed)
            }
            tipText = t
            return@LaunchedEffect
        }
    }

    val progress = ((state.loadingState as? LoadingState.Loading)?.progress ?: 0f).times(100).toInt()
    HanimeScaffold(
        title = stringResource(Res.string.complete_cloudflare_verification),
        onBack = onClose,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            WebView(
                state = state,
                modifier = Modifier.fillMaxSize(),
                navigator = navigator,
                // 显式标类型：另一个 WebView 重载的 onCreated 是 () -> Unit，会歧义
                onCreated = { webView: NativeWebView ->
                    webViewHolder.value = webView
                    webView.enableDomStorage()
                },
            )

            if (progress in 1..99) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    strokeCap = StrokeCap.Round,
                )
            }

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = lerp(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.surface,
                        0.6f
                    )
                ),
            ) {
                Text(
                    text = tipText,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private const val UA_CHECK_ATTEMPTS = 20

private val CHROME_VERSION_REGEX = "Chrome/(\\d+\\.\\d+\\.\\d+\\.\\d+)".toRegex()

/**
 * [WebViewNavigator.evaluateJavaScript] 的挂起版。
 *
 * 结果为空串表示这次没执行成功（WebView 还没建好时库就是直接回空串的），统一当没拿到。
 */
private suspend fun WebViewNavigator.awaitJavaScript(script: String): String? =
    withTimeoutOrNull(JS_TIMEOUT) {
        suspendCancellableCoroutine { continuation ->
            evaluateJavaScript(script) { result ->
                if (continuation.isActive) continuation.resume(result)
            }
        }
    }?.takeIf { it.isNotEmpty() }

@Preview(showBackground = true)
@Composable
fun CloudflareScreenPreview() {
    ComponentPreview {
        CloudflareScreen(
            url = "https://example.com/",
            onSolved = { _, _ -> },
            onClose = {},
        )
    }
}
