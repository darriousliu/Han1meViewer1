package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.utils.LogUtil
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import io.github.daisukikaffuchino.han1meviewer.ui.component.HapticTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.daisukikaffuchino.han1meviewer.EMPTY_STRING
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.Parser
import io.github.daisukikaffuchino.han1meviewer.logic.network.DohConfig
import io.github.daisukikaffuchino.han1meviewer.logic.network.HanimeNetwork
import io.github.daisukikaffuchino.han1meviewer.logic.network.ServiceCreator
import io.github.daisukikaffuchino.han1meviewer.logic.state.WebsiteState
import io.github.daisukikaffuchino.han1meviewer.logout
import io.github.daisukikaffuchino.han1meviewer.ui.component.ConfirmDialog
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.DelayResultUi
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.DohTestResultUi
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.NetworkSettingsScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.NetworkSettingsUiState
import io.github.daisukikaffuchino.utils.SonnerToast
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import io.ktor.http.isSuccess
import io.ktor.client.statement.request
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.get
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.custom
import han1meviewer.shared.generated.resources.custom_mirror_site_test_failed
import han1meviewer.shared.generated.resources.custom_mirror_site_test_failed_http
import han1meviewer.shared.generated.resources.custom_mirror_site_test_parse_failed
import han1meviewer.shared.generated.resources.custom_mirror_site_test_partial_success
import han1meviewer.shared.generated.resources.custom_mirror_site_test_success
import han1meviewer.shared.generated.resources.custom_mirror_site_watch_test_failed
import han1meviewer.shared.generated.resources.custom_mirror_site_watch_test_failed_http
import han1meviewer.shared.generated.resources.direct
import han1meviewer.shared.generated.resources.doh_disabled_summary
import han1meviewer.shared.generated.resources.http_proxy
import han1meviewer.shared.generated.resources.loading
import han1meviewer.shared.generated.resources.node_latency_sum
import han1meviewer.shared.generated.resources.socks_proxy
import han1meviewer.shared.generated.resources.system_proxy
import han1meviewer.shared.generated.resources.unknow
import han1meviewer.shared.generated.resources.attention
import han1meviewer.shared.generated.resources.cancel
import han1meviewer.shared.generated.resources.confirm
import han1meviewer.shared.generated.resources.custom_mirror_site_invalid
import han1meviewer.shared.generated.resources.custom_mirror_site_testing
import han1meviewer.shared.generated.resources.custom_mirror_site_warning
import han1meviewer.shared.generated.resources.doh_conflict_message
import han1meviewer.shared.generated.resources.domain_change_tips
import han1meviewer.shared.generated.resources.mpv_socks5_warning
import han1meviewer.shared.generated.resources.network_timeout_text
import han1meviewer.shared.generated.resources.restart_or_not_working
import han1meviewer.shared.generated.resources.warning
import han1meviewer.shared.generated.resources.invalid_ip_or_port
import io.github.daisukikaffuchino.han1meviewer.logic.network.CustomHosts
import io.github.daisukikaffuchino.han1meviewer.logic.network.ProxyType
import io.github.daisukikaffuchino.han1meviewer.util.monotonicMillis
import io.ktor.http.Url
import io.ktor.http.protocolWithAuthority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import io.github.daisukikaffuchino.han1meviewer.util.restartApplication

private enum class DohConflictTarget {
    EnableDoH,
    EnableBuiltInHosts,
}

@Composable
fun NetworkSettingsRouteScreen(embedded: Boolean = false) {
    val coroutineScope = rememberCoroutineScope()
    val settings by SettingsRepository.settings.collectAsStateWithLifecycle()
    var currentHost by remember { mutableStateOf(SettingsRepository.baseUrl) }
    var isDelayTesting by remember { mutableStateOf(false) }
    var isDohTesting by remember { mutableStateOf(false) }
    var isCustomMirrorTesting by remember { mutableStateOf(false) }
    var customMirrorTestResult by remember { mutableStateOf<String?>(null) }
    var showDomainRestartConfirm by remember { mutableStateOf(false) }
    var showHostsRestartConfirm by remember { mutableStateOf(false) }
    var showCustomHostsValidationError by remember { mutableStateOf<List<String>?>(null) }
    var showCustomMirrorValidationError by remember { mutableStateOf(false) }
    var showCustomMirrorWarningConfirm by remember { mutableStateOf(false) }
    var showDohConflictConfirm by remember { mutableStateOf(false) }
    var showSocksWarning by remember { mutableStateOf(false) }
    var pendingDomainValue by remember { mutableStateOf("") }
    var pendingUseCustomMirrorSite by remember { mutableStateOf(SettingsRepository.useCustomMirrorSite) }
    var pendingCustomMirrorSite by remember { mutableStateOf(SettingsRepository.customMirrorSite) }
    var pendingAppendCustomMirrorPath by remember { mutableStateOf(SettingsRepository.appendCustomMirrorPath) }
    var pendingDohConflictTarget by remember { mutableStateOf(DohConflictTarget.EnableDoH) }
    var pendingDohEnabled by remember { mutableStateOf(SettingsRepository.useDoH) }
    var pendingDohPreset by remember { mutableStateOf(SettingsRepository.dohPreset) }
    var pendingDohCustomUrl by remember { mutableStateOf(SettingsRepository.dohCustomUrl) }
    var pendingDohBootstrapIps by remember { mutableStateOf(SettingsRepository.dohBootstrapIps) }
    var pendingDohTimeoutSeconds by remember { mutableIntStateOf(SettingsRepository.dohTimeoutSeconds) }
    val delayResults = remember { mutableStateListOf<DelayResultUi>() }
    val dohTestResults = remember { mutableStateListOf<DohTestResultUi>() }
    // 延迟测试与 DoH 测试各占一个 job，退出页面时一起取消
    var delayJob by remember { mutableStateOf<Job?>(null) }
    var dohJob by remember { mutableStateOf<Job?>(null) }
    val uiState = remember(settings) { buildNetworkSettingsUiState() }
    val networkTimeoutText = stringResource(Res.string.network_timeout_text)
    val customMirrorInvalidText = stringResource(Res.string.custom_mirror_site_invalid)
    val customMirrorTestingText = stringResource(Res.string.custom_mirror_site_testing)
    val unknownText = stringResource(Res.string.unknow)
    fun stopDelayTest() {
        isDelayTesting = false
        delayJob?.cancel()
        delayJob = null
    }

    fun stopDohTest() {
        isDohTesting = false
        dohJob?.cancel()
        dohJob = null
    }

    fun startDelayTest(ipList: List<String>) {
        delayJob?.cancel()
        delayJob = coroutineScope.launch {
            while (isActive && isDelayTesting) {
                ipList.forEach { ip ->
                    launch {
                        val delay = withContext(Dispatchers.IO) { measureIpDelay(ip) }
                        val index = delayResults.indexOfFirst { it.ip == ip }
                        if (index >= 0) delayResults[index] = DelayResultUi(ip, delay)
                    }
                }
                delay(2000)
            }
        }
    }

    fun runDohTest() {
        if (isDohTesting) return
        val host = hostOf(SettingsRepository.baseUrl) ?: unknownText
        currentHost = SettingsRepository.baseUrl
        dohTestResults.clear()
        isDohTesting = true
        dohJob = coroutineScope.launch {
            val start = monotonicMillis()
            val result = withContext(Dispatchers.IO) { runCatching { lookupByDohOnly(host) } }
            val delay = (monotonicMillis() - start).toInt()
            dohTestResults.clear()
            result.onSuccess { list ->
                dohTestResults.add(
                    DohTestResultUi(
                        host = host,
                        ips = list.distinct(),
                        delay = delay,
                        message = "",
                    )
                )
            }.onFailure { throwable ->
                LogUtil.w("DOH_TEST", "lookup failed for $host: ${throwable.message}")
                dohTestResults.add(
                    DohTestResultUi(
                        host = host,
                        ips = emptyList(),
                        delay = -1,
                        message = throwable.message?.ifBlank { networkTimeoutText }
                            ?: networkTimeoutText,
                    )
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopDelayTest()
            stopDohTest()
        }
    }

    NetworkSettingsScreen(
        state = uiState,
        domainOptions = remember { runBlocking { buildDomainOptions() } },
        currentHost = currentHost,
        delayResults = delayResults,
        dohTestResults = dohTestResults,
        isDelayTesting = isDelayTesting,
        isDohTesting = isDohTesting,
        proxyType = SettingsRepository.proxyType,
        proxyIp = SettingsRepository.proxyIp,
        proxyPort = SettingsRepository.proxyPort,
        dohEnabled = SettingsRepository.useDoH,
        dohPreset = SettingsRepository.dohPreset,
        dohCustomUrl = SettingsRepository.dohCustomUrl,
        dohBootstrapIps = SettingsRepository.dohBootstrapIps,
        dohTimeoutSeconds = SettingsRepository.dohTimeoutSeconds,
        useCustomMirrorSite = SettingsRepository.useCustomMirrorSite,
        customMirrorSite = SettingsRepository.customMirrorSite,
        appendCustomMirrorPath = SettingsRepository.appendCustomMirrorPath,
        customMirrorTestResult = customMirrorTestResult,
        isCustomMirrorTesting = isCustomMirrorTesting,
        onDomainChange = { newValue ->
            val origin = SettingsRepository.baseUrl
            if (newValue != origin) {
                pendingDomainValue = newValue
                pendingUseCustomMirrorSite = false
                pendingCustomMirrorSite = SettingsRepository.customMirrorSite
                pendingAppendCustomMirrorPath = SettingsRepository.appendCustomMirrorPath
                showDomainRestartConfirm = true
            }
        },
        onSaveCustomMirrorSite = { enabled, url, appendPath ->
            val normalizedUrl = normalizeCustomMirrorSite(url)
            if (enabled && normalizedUrl == null) {
                showCustomMirrorValidationError = true
                return@NetworkSettingsScreen
            }
            val customMirrorSite = normalizedUrl.orEmpty()
            if (enabled != SettingsRepository.useCustomMirrorSite ||
                customMirrorSite != SettingsRepository.customMirrorSite ||
                appendPath != SettingsRepository.appendCustomMirrorPath
            ) {
                pendingUseCustomMirrorSite = enabled
                pendingCustomMirrorSite = customMirrorSite
                pendingAppendCustomMirrorPath = appendPath
                if (enabled) {
                    showCustomMirrorWarningConfirm = true
                } else {
                    showDomainRestartConfirm = true
                }
            }
        },
        onTestCustomMirrorSite = { url, appendPath ->
            val normalizedUrl = normalizeCustomMirrorSite(url)
            if (normalizedUrl == null) {
                customMirrorTestResult = customMirrorInvalidText
                return@NetworkSettingsScreen
            }
            if (isCustomMirrorTesting) return@NetworkSettingsScreen
            isCustomMirrorTesting = true
            customMirrorTestResult = customMirrorTestingText
            coroutineScope.launch {
                customMirrorTestResult = testCustomMirrorSite(normalizedUrl, appendPath)
                isCustomMirrorTesting = false
            }
        },
        onUseBuiltInHostsChange = { value ->
            if (value && SettingsRepository.useDoH) {
                showDohConflictConfirm = true
                pendingDohConflictTarget = DohConflictTarget.EnableBuiltInHosts
                return@NetworkSettingsScreen
            }
            coroutineScope.launch {
                SettingsRepository.update { it.copy(useBuiltInHosts = value) }
                showHostsRestartConfirm = true
            }
        },
        onSaveCustomHosts = { data ->
            val errors = CustomHosts.validate(data)
            if (errors.isNotEmpty()) {
                showCustomHostsValidationError = errors
                return@NetworkSettingsScreen
            }
            coroutineScope.launch {
                SettingsRepository.update { it.copy(customHostsData = data) }
                if (SettingsRepository.useBuiltInHosts) HanimeNetwork.rebuildNetwork()
            }
        },
        customHostsData = SettingsRepository.customHostsData,
        onSaveDohSettings = { enabled, preset, url, bootstrapIps, timeoutSeconds ->
            pendingDohEnabled = enabled
            pendingDohPreset = preset
            pendingDohCustomUrl = url
            pendingDohBootstrapIps = bootstrapIps
            pendingDohTimeoutSeconds = timeoutSeconds
            if (enabled && SettingsRepository.useBuiltInHosts) {
                showDohConflictConfirm = true
                pendingDohConflictTarget = DohConflictTarget.EnableDoH
                return@NetworkSettingsScreen
            }
            coroutineScope.launch {
                SettingsRepository.update { it.copy(useDoH = enabled, dohPreset = preset, dohCustomUrl = url, dohBootstrapIps = bootstrapIps, dohTimeoutSeconds = timeoutSeconds.coerceIn(1, 60)) }
                currentHost = SettingsRepository.baseUrl
                HanimeNetwork.rebuildNetwork()
            }
        },
        onOpenDelayTest = {
            val host = hostOf(SettingsRepository.baseUrl) ?: unknownText
            currentHost = SettingsRepository.baseUrl
            delayResults.clear()
            isDelayTesting = true
            coroutineScope.launch {
                val ipList = withContext(Dispatchers.IO) { resolveCdnIps(host) }
                LogUtil.i("delayTest", ipList.toString())
                delayResults.clear()
                delayResults.addAll(ipList.map { DelayResultUi(it, -1) })
                startDelayTest(ipList)
            }
        },
        onOpenDohTest = { runDohTest() },
        onDismissDelayTest = { stopDelayTest() },
        onDismissDohTest = { stopDohTest() },
        onApplyProxy = { type, ip, port ->
            if (!ProxyType.isValidEndpoint(type, ip, port)) {
                SonnerToast.warning(Res.string.invalid_ip_or_port)
                return@NetworkSettingsScreen
            }
            if (type == ProxyType.SOCKS) {
                showSocksWarning = true
            }
            coroutineScope.launch {
                SettingsRepository.update { it.copy(proxyType = io.github.daisukikaffuchino.han1meviewer.logic.model.ProxyType.fromId(type), proxyIp = ip, proxyPort = port) }
                applyProxyToSystem()
                HanimeNetwork.rebuildNetwork()
            }
        },
        embedded = embedded,
    )

    ConfirmDialog(
        visible = showDomainRestartConfirm,
        title = stringResource(Res.string.attention),
        message = stringResource(Res.string.domain_change_tips).trimIndent(),
        confirmText = stringResource(Res.string.confirm),
        dismissText = stringResource(Res.string.cancel),
        cancelable = false,
        onConfirm = {
            coroutineScope.launch {
                SettingsRepository.update {
                    it.copy(
                        domainName = pendingDomainValue.ifEmpty { it.domainName },
                        selectedBaseUrl = pendingDomainValue.ifEmpty { it.selectedBaseUrl },
                        useCustomMirrorSite = pendingUseCustomMirrorSite,
                        customMirrorSite = pendingCustomMirrorSite,
                        appendCustomMirrorPath = pendingAppendCustomMirrorPath,
                    )
                }
                logout()
                restartApplication()
            }
        },
        onDismiss = {
            pendingDomainValue = ""
            pendingUseCustomMirrorSite = SettingsRepository.useCustomMirrorSite
            pendingCustomMirrorSite = SettingsRepository.customMirrorSite
            pendingAppendCustomMirrorPath = SettingsRepository.appendCustomMirrorPath
            showDomainRestartConfirm = false
        },
    )

    if (showCustomMirrorValidationError) {
        AlertDialog(
            onDismissRequest = { showCustomMirrorValidationError = false },
            title = { Text(stringResource(Res.string.attention)) },
            text = { Text(stringResource(Res.string.custom_mirror_site_invalid)) },
            confirmButton = {
                TextButton(onClick = { showCustomMirrorValidationError = false }) {
                    Text(stringResource(Res.string.confirm))
                }
            },
        )
    }

    ConfirmDialog(
        visible = showHostsRestartConfirm,
        title = stringResource(Res.string.attention),
        message = stringResource(Res.string.restart_or_not_working, EMPTY_STRING),
        confirmText = stringResource(Res.string.confirm),
        dismissText = stringResource(Res.string.cancel),
        cancelable = false,
        onConfirm = { restartApplication() },
        onDismiss = { showHostsRestartConfirm = false },
    )

    val validationErrors = showCustomHostsValidationError
    if (validationErrors != null) {
        AlertDialog(
            onDismissRequest = { showCustomHostsValidationError = null },
            title = { Text(stringResource(Res.string.attention)) },
            text = { Text(validationErrors.joinToString("\n")) },
            confirmButton = {
                TextButton(onClick = { showCustomHostsValidationError = null }) {
                    Text(stringResource(Res.string.confirm))
                }
            },
        )
    }

    ConfirmDialog(
        visible = showCustomMirrorWarningConfirm,
        title = stringResource(Res.string.attention),
        message = stringResource(Res.string.custom_mirror_site_warning),
        confirmText = stringResource(Res.string.confirm),
        dismissText = stringResource(Res.string.cancel),
        cancelable = false,
        onConfirm = {
            showCustomMirrorWarningConfirm = false
            showDomainRestartConfirm = true
        },
        onDismiss = {
            pendingUseCustomMirrorSite = SettingsRepository.useCustomMirrorSite
            pendingCustomMirrorSite = SettingsRepository.customMirrorSite
            pendingAppendCustomMirrorPath = SettingsRepository.appendCustomMirrorPath
            showCustomMirrorWarningConfirm = false
        },
    )

    ConfirmDialog(
        visible = showDohConflictConfirm,
        title = stringResource(Res.string.attention),
        message = stringResource(Res.string.doh_conflict_message),
        confirmText = stringResource(Res.string.confirm),
        dismissText = stringResource(Res.string.cancel),
        cancelable = false,
        onConfirm = {
            coroutineScope.launch {
                SettingsRepository.update {
                    when (pendingDohConflictTarget) {
                        DohConflictTarget.EnableDoH -> it.copy(useBuiltInHosts = false, useDoH = pendingDohEnabled, dohPreset = pendingDohPreset, dohCustomUrl = pendingDohCustomUrl, dohBootstrapIps = pendingDohBootstrapIps, dohTimeoutSeconds = pendingDohTimeoutSeconds.coerceIn(1, 60))
                        DohConflictTarget.EnableBuiltInHosts -> it.copy(useDoH = false, useBuiltInHosts = true)
                    }
                }
                showDohConflictConfirm = false
                HanimeNetwork.rebuildNetwork()
            }
        },
        onDismiss = { showDohConflictConfirm = false },
    )

    ConfirmDialog(
        visible = showSocksWarning,
        title = stringResource(Res.string.warning),
        message = stringResource(Res.string.mpv_socks5_warning),
        confirmText = stringResource(Res.string.confirm),
        dismissText = stringResource(Res.string.cancel),
        onConfirm = { showSocksWarning = false },
        onDismiss = { showSocksWarning = false },
    )
}

private fun buildNetworkSettingsUiState(): NetworkSettingsUiState = runBlocking {
    NetworkSettingsUiState(
        domainName = SettingsRepository.baseUrl,
        domainDisplay = buildDomainOptions().firstOrNull { it.second == SettingsRepository.baseUrl }?.first
            ?: SettingsRepository.baseUrl,
        proxySummary = when (SettingsRepository.proxyType) {
            ProxyType.DIRECT -> getString(Res.string.direct)
            ProxyType.SYSTEM -> getString(Res.string.system_proxy)
            ProxyType.HTTP -> getString(
                Res.string.http_proxy,
                SettingsRepository.proxyIp,
                SettingsRepository.proxyPort
            )

            ProxyType.SOCKS -> getString(
                Res.string.socks_proxy,
                SettingsRepository.proxyIp,
                SettingsRepository.proxyPort
            )

            else -> getString(Res.string.direct)
        },
        useBuiltInHosts = SettingsRepository.useBuiltInHosts,
        useCustomMirrorSite = SettingsRepository.useCustomMirrorSite,
        customMirrorSite = SettingsRepository.customMirrorSite,
        appendCustomMirrorPath = SettingsRepository.appendCustomMirrorPath,
        useDoH = SettingsRepository.useDoH,
        dohSummary = buildDohSummary(),
        delaySummary = getString(Res.string.node_latency_sum),
    )
}

private fun normalizeCustomMirrorSite(url: String): String? {
    val trimmed = url.trim().trimEnd('/')
    val uri = runCatching { Url(trimmed) }.getOrNull() ?: return null
    if (uri.protocol.name != "https" || uri.host.isBlank()) return null
    if (uri.encodedQuery.isNotBlank() || uri.fragment.isNotBlank()) return null
    return url.trim()
}

private fun hostOf(url: String): String? =
    runCatching { Url(url).host }.getOrNull()?.takeIf { it.isNotBlank() }

private suspend fun testCustomMirrorSite(
    homeUrl: String,
    appendPath: Boolean,
): String {
    return runCatching {
        run {
            val response = ServiceCreator.hClient.get(homeUrl)
            val finalUrl = response.request.url.toString()
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return getString(
                Res.string.custom_mirror_site_test_failed_http,
                    response.status.value,
                    finalUrl,
                )
            }

            val apiBaseUrl = buildCustomMirrorApiBaseUrl(homeUrl, appendPath)
            val watchTestResult = testCustomMirrorWatchUrl(apiBaseUrl)
            when (val parseResult = Parser.homePageVer2(body)) {
                is WebsiteState.Success -> if (watchTestResult == null) {
                    getString(
                Res.string.custom_mirror_site_test_success,
                        finalUrl,
                        apiBaseUrl,
                    )
                } else {
                    getString(
                Res.string.custom_mirror_site_test_partial_success,
                        finalUrl,
                        apiBaseUrl,
                        watchTestResult,
                    )
                }

                is WebsiteState.Error -> getString(
                Res.string.custom_mirror_site_test_parse_failed,
                    finalUrl,
                    parseResult.throwable.message ?: parseResult.throwable::class.simpleName.orEmpty(),
                )

                WebsiteState.Loading -> getString(
                Res.string.custom_mirror_site_test_parse_failed,
                    finalUrl,
                    getString(Res.string.loading),
                )
            }
        }
    }.getOrElse { throwable ->
        getString(
                Res.string.custom_mirror_site_test_failed,
            throwable.message ?: throwable::class.simpleName.orEmpty(),
        )
    }
}

private suspend fun testCustomMirrorWatchUrl(apiBaseUrl: String): String? {
    return runCatching {
        val response = ServiceCreator.hClient.get(apiBaseUrl + "search")
        if (response.status.isSuccess()) {
            null
        } else {
            getString(
                Res.string.custom_mirror_site_watch_test_failed_http,
                response.status.value,
                response.request.url.toString(),
            )
        }
    }.getOrElse { throwable ->
        getString(
                Res.string.custom_mirror_site_watch_test_failed,
            throwable.message ?: throwable::class.simpleName.orEmpty(),
        )
    }
}

private fun buildCustomMirrorApiBaseUrl(homeUrl: String, appendPath: Boolean): String {
    val url = if (appendPath) homeUrl else {
        Url(homeUrl).protocolWithAuthority
    }
    return if (url.endsWith('/')) url else "$url/"
}

private suspend fun buildDohSummary(): String {
    if (!SettingsRepository.useDoH) return getString(Res.string.doh_disabled_summary)
    if (SettingsRepository.useBuiltInHosts) return getString(Res.string.doh_conflict_message)
    val core = if (SettingsRepository.dohPreset == "custom") {
        SettingsRepository.dohCustomUrl.ifBlank { getString(Res.string.custom) }
    } else {
        DohConfig.selectedPreset().title
    }
    val bootstrap = DohConfig.bootstrapIps().takeIf { it.isNotEmpty() }?.joinToString()
    return if (bootstrap != null) "$core\nBootstrap: $bootstrap" else core
}

/** ping 一个 IP，返回毫秒；-1 表示不可达。 */
expect suspend fun measureIpDelay(ip: String): Int

/** 拿域名的 CDN 候选 IP 列表。 */
expect suspend fun resolveCdnIps(host: String): List<String>

/** 只走 DoH 解析，返回 IP 字符串。 */
expect suspend fun lookupByDohOnly(host: String): List<String>

/** 把代理设置写进系统属性，WebView 才能跟着走。 */
expect fun applyProxyToSystem()

/**
 * 平台的 HTTP 栈是否能接管 DNS。
 *
 * iOS 用的是 NSURLSession，没有 DNS 钩子，内建 hosts / 自定义 hosts / DoH
 * 都影响不到实际请求，所以整组设置在那边不渲染。
 */
expect val isCustomDnsSupported: Boolean
