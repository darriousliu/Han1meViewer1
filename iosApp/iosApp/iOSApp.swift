import SwiftUI

@main
struct iOSApp: App {
    // 后台下载要用 AppDelegate 的 handleEventsForBackgroundURLSession
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}