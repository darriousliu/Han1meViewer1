package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.account_or_password_wrong
import han1meviewer.shared.generated.resources.login_failed
import han1meviewer.shared.generated.resources.login_success
import io.github.daisukikaffuchino.han1meviewer.logic.NetworkRepo
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.network.CloudflareVerificationCoordinator
import io.github.daisukikaffuchino.han1meviewer.logic.state.WebsiteState
import io.github.daisukikaffuchino.han1meviewer.login
import io.github.daisukikaffuchino.han1meviewer.ui.screen.login.LoginScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.login.ManualInputCookiesScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.web.CloudflareScreen
import io.github.daisukikaffuchino.utils.SonnerToast
import kotlinx.coroutines.launch

@Composable
fun LoginRouteScreen(
    onBack: () -> Unit,
    onOpenManualCookies: () -> Unit,
    onLoginSucceeded: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isLoggingIn by remember { mutableStateOf(false) }

    LoginScreen(
        isLoggingIn = isLoggingIn,
        onBack = onBack,
        onCookiesCaptured = { cookies ->
            scope.launch {
                login(cookies)
                onLoginSucceeded()
            }
        },
        onPasswordLogin = { username, password ->
            isLoggingIn = true
            scope.launch {
                NetworkRepo.login(username, password).collect { state ->
                    when (state) {
                        WebsiteState.Loading -> Unit
                        is WebsiteState.Error -> {
                            isLoggingIn = false
                            state.throwable.printStackTrace()
                            if (state.throwable is IllegalStateException) {
                                SonnerToast.error(Res.string.account_or_password_wrong)
                            } else {
                                SonnerToast.error(Res.string.login_failed)
                            }
                        }

                        is WebsiteState.Success -> {
                            login(state.info)
                            isLoggingIn = false
                            SonnerToast.success(Res.string.login_success)
                            onLoginSucceeded()
                        }
                    }
                }
            }
        },
        onOpenQrScanner = onOpenManualCookies,
    )
}

@Composable
fun ManualCookiesRouteScreen(
    onBack: () -> Unit,
    onLoginSucceeded: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    ManualInputCookiesScreen(
        onBack = onBack,
        onCookieScanned = { cookie ->
            scope.launch {
                login(cookie)
                onLoginSucceeded()
            }
        },
    )
}

@Composable
fun CloudflareRouteScreen(
    route: CloudflareRoute,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val finalized = remember(route.host) { mutableStateOf(false) }

    fun finishVerification(succeeded: Boolean) {
        if (finalized.value) return
        finalized.value = true
        CloudflareVerificationCoordinator.complete(route.host, succeeded)
        onBack()
    }

    CloudflareScreen(
        url = route.url,
        onSolved = { cookies, completedUrl ->
            scope.launch {
                val host = completedUrl.substringAfter("://").substringBefore('/')
                    .substringBefore(':').lowercase().ifEmpty { route.host }
                SettingsRepository.setCloudFlareCookie(cookies, host)
                finishVerification(true)
            }
        },
        onClose = { finishVerification(false) },
    )

    // 不管怎么离开这一页，都要放行等待中的请求
    DisposableEffect(route.host) {
        onDispose {
            if (!finalized.value) {
                CloudflareVerificationCoordinator.complete(route.host, succeeded = false)
            }
        }
    }
}
