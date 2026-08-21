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
import dev.nucleusframework.webview.request.RequestInterceptor
import dev.nucleusframework.webview.request.WebRequest
import dev.nucleusframework.webview.request.WebRequestInterceptResult
import dev.nucleusframework.webview.web.WebView
import dev.nucleusframework.webview.web.WebViewNavigator
import dev.nucleusframework.webview.web.rememberWebViewNavigator
import dev.nucleusframework.webview.web.rememberWebViewState
import io.github.daisukikaffuchino.han1meviewer.HANIME_LOGIN_URL
import io.github.daisukikaffuchino.han1meviewer.HanimeConstants.HANIME_URL
import io.github.daisukikaffuchino.han1meviewer.USER_AGENT

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
        androidWebSettings.domStorageEnabled = true
    }

    // 重定向命中站内域即登录成功，只取一次（重定向会带出多个请求）
    var captured by remember { mutableStateOf(false) }
    val navigator = rememberWebViewNavigator(
        requestInterceptor = remember {
            object : RequestInterceptor {
                override fun onInterceptUrlRequest(
                    request: WebRequest,
                    navigator: WebViewNavigator,
                ): WebRequestInterceptResult {
                    if (request.isRedirect && HANIME_URL.contains(request.url) && !captured) {
                        captured = true
                        scope.launch {
                            onCookiesCaptured(
                                state.cookieManager.getCookies(request.url)
                                    .joinToString("; ") { "${it.name}=${it.value}" }
                            )
                        }
                        return WebRequestInterceptResult.Reject
                    }
                    return WebRequestInterceptResult.Allow
                }
            }
        },
    )

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

@Preview
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

@Preview
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
