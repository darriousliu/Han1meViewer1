import UIKit
import Shared

/// 后台下载完成后系统会把 app 唤回来（界面可能根本没创建），
/// 通过这个回调递一个 completion handler 过来。我们必须：
///   1. 确保 Kotlin 侧的下载控制器已经建起来——它一建就会用同一个 identifier
///      重连后台会话，从而重新拿到委托回调；
///   2. 把 handler 交给 Kotlin，等会话事件处理完再调，否则系统认为 app 卡住。
class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        handleEventsForBackgroundURLSession identifier: String,
        completionHandler: @escaping () -> Void
    ) {
        BackgroundDownloadEvents.shared.ensureControllerReady()
        BackgroundDownloadEvents.shared.onBackgroundSessionFinished = {
            DispatchQueue.main.async { completionHandler() }
        }
    }
}
