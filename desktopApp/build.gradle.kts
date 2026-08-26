import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

/**
 * 当前构建机的 OS + 架构，形如 `windows-x64` / `macos-arm64`。
 *
 * libmpv 的原生库按这个维度分包，发行版只带自己这一份——mediamp-mpv 本身不含任何 .dll/.dylib/.so，
 * 运行时由 mediamp-native-loader 从 classpath 上的 runtime jar 里解出来。少引一条就是启动即报
 * 「找不到 libmpv」，多引几条只是白白把安装包撑大几百 MB。
 */
fun osTriple(): String {
    val os = System.getProperty("os.name").lowercase()
    val arch = when (val raw = System.getProperty("os.arch").lowercase()) {
        "x86_64", "amd64" -> "x64"
        "aarch64", "arm64" -> "arm64"
        else -> throw GradleException("不支持的 CPU 架构: $raw")
    }
    return when {
        os.startsWith("windows") -> "windows-$arch"
        os.startsWith("mac") || os.startsWith("darwin") -> "macos-$arch"
        os.startsWith("linux") -> "linux-$arch"
        else -> throw GradleException("不支持的操作系统: $os")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.coroutines.swing)
    implementation(libs.compose.multiplatform.ui.tooling.preview)

    implementation(libs.filekit.core)

    // 桌面播放内核 libmpv 的原生库。只有当前平台这一条，跨平台打包要在目标 OS 上各构建一次。
    when (val triple = osTriple()) {
        "windows-x64" -> runtimeOnly(libs.mediamp.mpv.runtime.windows.x64)
        "windows-arm64" -> runtimeOnly(libs.mediamp.mpv.runtime.windows.arm64)
        "linux-x64" -> runtimeOnly(libs.mediamp.mpv.runtime.linux.x64)
        "macos-x64" -> runtimeOnly(libs.mediamp.mpv.runtime.macos.x64)
        "macos-arm64" -> runtimeOnly(libs.mediamp.mpv.runtime.macos.arm64)
        // 上游没发 linux-arm64 的 mpv runtime，那台机器上只能构建出没有播放能力的包
        else -> logger.warn("mediamp 没有 $triple 的 mpv runtime，本次构建产物无法播放视频")
    }
}

compose.desktop {
    application {
        mainClass = "io.github.daisukikaffuchino.han1meviewer.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Han1meViewer"
            // 跟着 Config.App.VERSION_NAME 走。Windows 靠这个版本号判断能否覆盖升级，
            // 写死不动的话新版 MSI 装到旧版上会被当成「已安装」。
            packageVersion = Config.App.desktopPackageVersion
        }
    }
}
