package io.github.daisukikaffuchino.han1meviewer.logic.network

/** ping 一个 IP，返回毫秒；-1 表示不可达。 */
expect suspend fun measureIpDelay(ip: String): Int

/** 拿域名的 CDN 候选 IP 列表。 */
expect suspend fun resolveCdnIps(host: String): List<String>

/** 只走 DoH 解析，返回 IP 字符串。 */
expect suspend fun lookupByDohOnly(host: String): List<String>

/**
 * 平台的 HTTP 栈是否能接管 DNS。
 *
 * iOS 用的是 NSURLSession，没有 DNS 钩子，内建 hosts / 自定义 hosts / DoH
 * 都影响不到实际请求，所以整组设置在那边不渲染。
 */
expect val isCustomDnsSupported: Boolean
