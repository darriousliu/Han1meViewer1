import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            // 自定义 scheme 和「文件」App 的打开方式都走这里，解析交给 Kotlin 侧
            .onOpenURL { url in
                IosDeepLink.shared.handle(url: url.absoluteString)
            }
    }
}