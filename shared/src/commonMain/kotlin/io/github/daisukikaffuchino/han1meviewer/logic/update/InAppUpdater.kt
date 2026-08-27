package io.github.daisukikaffuchino.han1meviewer.logic.update

/**
 * 应用内更新的阶段回调。
 *
 * 只有桌面端实现了这条路（Nucleus 的 updater-runtime，读 GitHub Release 上的 `latest*.yml`，
 * 有 blockmap 时走差分下载）。Android / iOS 的 [supportsInAppUpdate] 是 false，
 * 调用方退回原来那条「打开浏览器到下载页」的老路。
 *
 * 注意这与 [io.github.daisukikaffuchino.han1meviewer.logic.AppUpdateChecker] 是两件事：
 * 「有没有新版、公告、要不要强制」仍然由 AppUpdateChecker 从腾讯云 COS 的 update.json 判定，
 * 三端行为一致；这里只负责桌面端「点了更新之后怎么把新版装上」。
 */
sealed interface InAppUpdateStage {
    /** [percent] 为 0f..1f。[differential] 为真表示命中了 blockmap 差分，只下增量块。 */
    data class Downloading(val percent: Float, val differential: Boolean) : InAppUpdateStage

    /** 下载完毕，正在交给系统安装器。成功的话进程会被重启，不会再有后续回调。 */
    data object Installing : InAppUpdateStage
}

/** 当前平台与当前发行形态是否支持应用内下载安装。false 时调用方必须退回打开下载页。 */
expect val supportsInAppUpdate: Boolean

/**
 * 下载并安装最新版。
 *
 * 成功时进程会被安装器接管并重启，**正常路径下这个函数不返回**。返回 failure 表示这条路
 * 走不通（没有匹配当前平台的产物、下载校验失败、当前是 dev/免安装形态等），调用方应退回打开下载页。
 */
expect suspend fun runInAppUpdate(onStage: (InAppUpdateStage) -> Unit): Result<Unit>
