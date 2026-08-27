package io.github.daisukikaffuchino.han1meviewer.ui.screen.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import io.github.daisukikaffuchino.han1meviewer.ui.component.HapticTextButton as TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.daisukikaffuchino.han1meviewer.ui.component.appbar.HanimeScaffold
import io.github.daisukikaffuchino.han1meviewer.ui.preview.ComponentPreview
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.cancel
import han1meviewer.shared.generated.resources.email
import han1meviewer.shared.generated.resources.ic_export
import han1meviewer.shared.generated.resources.login
import han1meviewer.shared.generated.resources.password
import han1meviewer.shared.generated.resources.scan_for_cookies
import han1meviewer.shared.generated.resources.try_login_here
import io.github.daisukikaffuchino.han1meviewer.ui.component.rememberHapticPerformer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.backhandler.BackHandler
import kotlinx.coroutines.launch
import io.github.kdroidfilter.webview.request.RequestInterceptor
import io.github.kdroidfilter.webview.request.WebRequest
import io.github.kdroidfilter.webview.request.WebRequestInterceptResult
import io.github.kdroidfilter.webview.web.LoadingState
import io.github.kdroidfilter.webview.web.WebView
import io.github.kdroidfilter.webview.web.WebViewNavigator
import io.github.kdroidfilter.webview.web.rememberWebViewNavigator
import io.github.kdroidfilter.webview.web.rememberWebViewState
import io.github.daisukikaffuchino.han1meviewer.HANIME_LOGIN_URL
import io.github.daisukikaffuchino.han1meviewer.HanimeConstants.HANIME_URL
import io.github.daisukikaffuchino.han1meviewer.USER_AGENT
import io.github.daisukikaffuchino.han1meviewer.util.NativeWebViewHolder
import io.github.daisukikaffuchino.han1meviewer.util.enableDomStorage
import io.github.daisukikaffuchino.han1meviewer.util.readWebViewCookies
import io.github.kdroidfilter.webview.web.NativeWebView

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    isLoggingIn: Boolean,
    onBack: () -> Unit,
    onCookiesCaptured: (String) -> Unit,
    onPasswordLogin: (username: String, password: String) -> Unit,
    onOpenQrScanner: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showLoginDialog by remember { mutableStateOf(false) }

    val state = rememberWebViewState("about:blank") {
        customUserAgentString = USER_AGENT
        isJavaScriptEnabled = true
    }

    // 桌面端只能顺着原生对象够到 WebView 的 cookie 存储
    val webViewHolder = remember { NativeWebViewHolder() }

    // 登录成功会重定向回站内首页，只取一次（重定向会带出多个请求）
    var captured by remember { mutableStateOf(false) }

    suspend fun captureCookies(url: String) {
        if (captured) return
        val cookies = readWebViewCookies(webViewHolder.value, url)
            ?: state.cookieManager.getCookies(url).joinToString("; ") { "${it.name}=${it.value}" }
        // 空串说明还没落盘，别当成捕获成功——否则会「登录成功、立刻被判失效又登出」
        if (cookies.isBlank()) return
        captured = true
        onCookiesCaptured(cookies)
    }

    val navigator = rememberWebViewNavigator(
        requestInterceptor = remember {
            object : RequestInterceptor {
                override fun onInterceptUrlRequest(
                    request: WebRequest,
                    navigator: WebViewNavigator,
                ): WebRequestInterceptResult {
                    if (request.isRedirect && HANIME_URL.contains(request.url) && !captured) {
                        scope.launch { captureCookies(request.url) }
                        return WebRequestInterceptResult.Reject
                    }
                    return WebRequestInterceptResult.Allow
                }
            }
        },
    )

    // 上面的拦截器只在 Android 上稳定命中：WKWebView 不会把登录后的跳转报成
    // isRedirect，拦不到就一直停在 WebView 上。这里再补一条与触发方式无关的路径
    // —— 页面加载完、且落到站内首页（不是 /login），就按同样的条件取一次 cookie。
    // Android 上重定向已被 Reject，走不到首页，所以这条不会重复触发。
    LaunchedEffect(state.lastLoadedUrl, state.loadingState) {
        val url = state.lastLoadedUrl ?: return@LaunchedEffect
        if (state.loadingState !is LoadingState.Finished) return@LaunchedEffect
        if (HANIME_URL.none { it.trimEnd('/') == url.trimEnd('/') }) return@LaunchedEffect
        captureCookies(url)
    }

    LaunchedEffect(Unit) {
        state.cookieManager.removeAllCookies()
        navigator.loadUrl(HANIME_LOGIN_URL)
    }

    // 主框架加载失败 -> 账密登录兜底
    LaunchedEffect(state.errorsForCurrentRequest.size) {
        if (state.errorsForCurrentRequest.any { it.isFromMainFrame }) showLoginDialog = true
    }

    BackHandler(enabled = navigator.canGoBack) { navigator.navigateBack() }

    if (showLoginDialog) {
        LoginDialog(
            isLoggingIn = isLoggingIn,
            onDismiss = { showLoginDialog = false },
            onLogin = onPasswordLogin,
        )
    }

    val refreshingState = rememberPullToRefreshState()
    val haptic = rememberHapticPerformer()
    HanimeScaffold(
        title = stringResource(Res.string.login),
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(Res.string.scan_for_cookies)) },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_export),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                onClick = {
                    haptic()
                    onOpenQrScanner()
                },
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { navigator.loadUrl(HANIME_LOGIN_URL) },
            state = refreshingState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = refreshingState,
                    isRefreshing = state.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        ) {
            WebView(
                state = state,
                modifier = Modifier.fillMaxSize(),
                navigator = navigator,
                onCreated = { webView: NativeWebView ->
                    webViewHolder.value = webView
                    webView.enableDomStorage()
                },
            )
        }
    }
}

@Composable
fun LoginDialog(
    isLoggingIn: Boolean,
    onDismiss: () -> Unit,
    onLogin: (username: String, password: String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.try_login_here)) },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(Res.string.email)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoggingIn,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(Res.string.password)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoggingIn,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onLogin(username, password) },
                enabled = username.isNotBlank() && password.isNotBlank() && !isLoggingIn,
            ) {
                Text(stringResource(Res.string.login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoggingIn) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ComponentPreview {
        LoginScreen(
            isLoggingIn = false,
            onBack = {},
            onCookiesCaptured = {},
            onPasswordLogin = { _, _ -> },
            onOpenQrScanner = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginDialogPreview() {
    ComponentPreview {
        LoginDialog(
            isLoggingIn = false,
            onDismiss = {},
            onLogin = { _, _ -> },
        )
    }
}
