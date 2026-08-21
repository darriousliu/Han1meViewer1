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
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay
import dev.nucleusframework.webview.web.LoadingState
import dev.nucleusframework.webview.web.WebView
import dev.nucleusframework.webview.web.rememberWebViewNavigator
import dev.nucleusframework.webview.web.rememberWebViewState
import io.github.daisukikaffuchino.han1meviewer.USER_AGENT
import org.jetbrains.compose.resources.getString
import han1meviewer.shared.generated.resources.complete_cloudflare_verification_with_warning
import han1meviewer.shared.generated.resources.current_webview_version
import han1meviewer.shared.generated.resources.webview_version_unknown
import han1meviewer.shared.generated.resources.webview_version_too_low
import han1meviewer.shared.generated.resources.version_check_failed


@Composable
fun CloudflareScreen(
    url: String,
    onSolved: (cookies: String, completedUrl: String) -> Unit,
    onClose: () -> Unit,
) {
    val state = rememberWebViewState(url) {
        customUserAgentString = USER_AGENT
        isJavaScriptEnabled = true
        androidWebSettings.domStorageEnabled = true
    }
    val navigator = rememberWebViewNavigator()

    val baseWarning = stringResource(Res.string.complete_cloudflare_verification_with_warning)
    var tipText by remember { mutableStateOf(baseWarning) }

    // evaluateJavaScript 回调不在协程里，结果先落 state，再由 LaunchedEffect 消费
    var pendingVersionCode by remember { mutableStateOf<String?>(null) }
    var pendingCookieCheck by remember { mutableIntStateOf(0) }
    var uaChecked by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingState, uaChecked) {
        if (uaChecked || state.loadingState !is LoadingState.Loading) return@LaunchedEffect
        uaChecked = true
        navigator.evaluateJavaScript("navigator.userAgent") { output ->
            val userAgent = output.removeSurrounding("\"")
                .replace("\\\"", "\"").replace("\\\\", "\\")
            pendingVersionCode = "Chrome/(\\d+\\.\\d+\\.\\d+\\.\\d+)".toRegex()
                .find(userAgent)?.groupValues?.getOrNull(1) ?: userAgent
        }
    }
    LaunchedEffect(pendingVersionCode) {
        val versionCode = pendingVersionCode ?: return@LaunchedEffect
        var t = baseWarning + getString(Res.string.current_webview_version, versionCode)
        t += try {
            val parts = versionCode.split(".").map { it.toIntOrNull() ?: 0 }
            when {
                parts.size < 4 -> getString(Res.string.webview_version_unknown)
                parts[0] < 120 -> getString(Res.string.webview_version_too_low)
                else -> ""
            }
        } catch (_: Exception) {
            getString(Res.string.version_check_failed)
        }
        tipText = t
    }

    // 进度 >= 90% 或加载完成后延迟 1 秒查 head 里的 challenge 标记
    var solved by remember { mutableStateOf(false) }
    LaunchedEffect(state.loadingState) {
        val loading = state.loadingState
        val ready = loading is LoadingState.Finished ||
                (loading is LoadingState.Loading && loading.progress >= 0.9f)
        if (!ready || solved) return@LaunchedEffect
        delay(1000)
        navigator.evaluateJavaScript("document.head.innerHTML") { html ->
            if (!html.contains("#challenge-form") &&
                !html.contains("#challenge-success-text") &&
                !html.contains("#challenge-error-text")
            ) pendingCookieCheck++
        }
    }
    LaunchedEffect(pendingCookieCheck) {
        if (pendingCookieCheck == 0 || solved) return@LaunchedEffect
        val cookies = state.cookieManager.getCookies(url)
            .joinToString("; ") { "${it.name}=${it.value}" }
        if (cookies.contains("cf_clearance")) {
            solved = true
            onSolved(cookies, url)
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

@Preview
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

