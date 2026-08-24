package io.github.daisukikaffuchino.han1meviewer.logic.platform

/**
 * 系统在后台下载完成后会把 app 唤回来，并通过
 * `application(_:handleEventsForBackgroundURLSession:completionHandler:)`
 * 递一个 completion handler 过来，我们把队列里的事做完之后必须调它，
 * 否则系统会认为 app 卡住。
 *
 * Swift 侧的 AppDelegate 负责把 handler 放进来（见 iosApp/AppDelegate.swift）。
 */
object BackgroundDownloadEvents {

    /** Swift 侧设置；由 [NsUrlSessionDownloadController] 在会话事件处理完后调用并清空。 */
    var onBackgroundSessionFinished: (() -> Unit)? = null

    /**
     * Swift 侧在收到后台会话事件时调一次，确保下载控制器（以及它的会话与委托）
     * 已经建起来——否则 app 是在后台被拉起的，界面还没创建，没人接管那些任务。
     */
    fun ensureControllerReady() {
        platformDownloadWorkController.hashCode()
    }
}
