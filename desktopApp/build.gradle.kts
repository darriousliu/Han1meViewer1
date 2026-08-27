import dev.nucleusframework.desktop.application.dsl.CompressionLevel
import dev.nucleusframework.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    // 仍然保留 JetBrains 的 compose 插件：Nucleus 只接管打包，compose 的依赖访问器
    // （compose.desktop.currentOs）和 IDE 集成还得靠它
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.nucleus)
}

kotlin {
    // 桌面端独立钉在 25，比 :shared 的 21 高一档：AOT cache 是 JDK 25 的能力。
    // 只有本模块受影响——它是叶子模块，产物不被任何人消费；:shared 编出来的 21 字节码
    // 在 25 的运行时上照跑。
    //
    // ⚠️ jlink 出发行版运行时、以及跑 AOT 训练轮用的是 **Gradle 守护进程那份 JDK**
    // （Nucleus 的 javaHome 默认取它），不是这里的 toolchain。所以 Gradle JDK 必须是
    // **25 且自带 jmods/**，否则 jlink 报「此 JDK 不包含打包模块」。
    //
    // 坑在于 **Adoptium 从 JDK 25 起不再随包发 jmods**（mac/aarch64 与 win/x64 均核实为 0 个），
    // 而 JDK 21 时代它是带的——所以「换个大版本就挂」。可用的：Corretto 25（69 个，不含 JavaFX）、
    // Oracle OpenJDK 25（69 个）、Zulu 25（注意别取 ca-fx 变体，那个会把 JavaFX 也 jlink 进来）。
    // 自查：ls "$JAVA_HOME/jmods" | wc -l
    jvmToolchain(25)
}

/**
 * 当前构建机的 OS + 架构，形如 `windows-x64` / `macos-arm64`。
 *
 * libmpv 的原生库按这个维度分包，发行版只带自己这一份——mediamp-mpv 本身不含任何 .dll/.dylib/.so。
 * 本工程不走 mediamp 那条「运行时从 classpath 上的 runtime jar 里解出来」的默认路径，
 * 而是在打包期就摊进应用资源目录（见下面的 [mpvNativeRuntime] / unpackMpvNatives）。
 * 少引一条就是播放页报「播放内核初始化失败」，多引几条只是白白把安装包撑大几百 MB。
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

/**
 * libmpv 原生库的解包来源。**不进运行时 classpath**。
 *
 * mediamp 默认那条路是「运行时从 classpath 上的 runtime jar 里解出来再 dlopen」，而它的解压目标是
 * `createTempDirectory("mediamp-mpv")`——每次启动都是新目录，macOS 要为「新出现的文件」重跑一次
 * 代码签名评估，实测 6.2 秒（解压只占 0.17 秒），而且这一整段发生在播放页的组合期上。
 *
 * 改成打包期就把 38 个 .dylib/.dll/.so 摊进应用资源目录，运行时只剩一次 dlopen（实测 9 毫秒），
 * 也就不用再往用户机器上写一份 21MB 的副本。安装包大小基本不变：同一批文件，jar 里本来也是压过的。
 */
val mpvNativeRuntime: Configuration = configurations.create("mpvNativeRuntime") {
    isCanBeConsumed = false
    isCanBeResolved = true
    // runtime jar 没有依赖，关掉传递解析免得把 mediamp 的 API 也拖进来
    isTransitive = false
}

/**
 * Nucleus 会把 `appResourcesRootDir` 下 `common/` + `<os>/` + `<os>-<arch>/` 三个目录的内容
 * 同步进应用的 `resources/`，并用 `compose.application.resources.dir` 把路径告诉运行时。
 * `./gradlew run`、`runDistributable`、装好的应用三种形态都会设这个属性。
 *
 * 这里只放 `common/`：上面按 [osTriple] 只拉了构建机这一个平台的 runtime，
 * 摊出来的本来就只有一份，再按平台分目录没有意义。
 */
val appResourcesRoot: Provider<Directory> = layout.buildDirectory.dir("appResources")

val unpackMpvNatives = tasks.register<Sync>("unpackMpvNatives") {
    description = "把 libmpv 的原生库摊进应用资源目录，免得运行时再从 jar 里解一次"
    from(provider { mpvNativeRuntime.files.map(::zipTree) }) {
        // 只要原生库本身：清单是给「运行时解压」那条路用的，这里不走那条路
        exclude("META-INF/**", "mpv-natives-*.txt")
    }
    into(appResourcesRoot.map { it.dir("common") })
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.coroutines.swing)
    implementation(libs.compose.multiplatform.ui.tooling.preview)

    implementation(libs.filekit.core)

    // AOT cache 的运行时判定（AotRuntime.isTraining），训练轮要靠它自杀退出
    implementation(libs.nucleus.aot.runtime)

    // 桌面播放内核 libmpv 的原生库。只有当前平台这一条，跨平台打包要在目标 OS 上各构建一次。
    // 注意是 mpvNativeRuntime 不是 runtimeOnly：它只作为打包期的解包来源，不进运行时 classpath。
    when (val triple = osTriple()) {
        "windows-x64" -> mpvNativeRuntime(libs.mediamp.mpv.runtime.windows.x64)
        "windows-arm64" -> mpvNativeRuntime(libs.mediamp.mpv.runtime.windows.arm64)
        "linux-x64" -> mpvNativeRuntime(libs.mediamp.mpv.runtime.linux.x64)
        "macos-x64" -> mpvNativeRuntime(libs.mediamp.mpv.runtime.macos.x64)
        "macos-arm64" -> mpvNativeRuntime(libs.mediamp.mpv.runtime.macos.arm64)
        // 上游没发 linux-arm64 的 mpv runtime，那台机器上只能构建出没有播放能力的包
        else -> logger.warn("mediamp 没有 $triple 的 mpv runtime，本次构建产物无法播放视频")
    }
}

configurations.configureEach {
    exclude(group = "org.jetbrains.compose.material", module = "material-icons-extended")
}

/**
 * 打包配置。Nucleus 注册的任务名与 compose.desktop 那套完全重名，两个块并存会直接 error()，
 * 所以这里是「搬过来」而不是「加一块」。
 *
 * 安装包不再走 jpackage 的 msi/dmg 分支，而是 jpackage 出 app-image 后交给 electron-builder，
 * 因此**构建机必须有 Node.js**（Windows 上也就不再需要 WiX v3 了）。
 */
nucleus.application {
    mainClass = "io.github.daisukikaffuchino.han1meviewer.MainKt"

    nativeDistributions {
        appResourcesRootDir.set(appResourcesRoot)
        includeAllModules = true
        // Zip 有两个用处：给装不了安装包的机器当免安装版（原来是 CI 里用 PowerShell 手工压的），
        // 以及 macOS 侧自动更新的必需品——差分更新的 blockmap 是基于 Zip 而不是 Dmg 生成的。
        // 只出 Windows 与 macOS。Linux 不在发行范围内（CI 也没有 Linux job），
        // 要加的话是 TargetFormat.Deb / Rpm / AppImage，届时 homepage 变成必填项。
        targetFormats(
            TargetFormat.Dmg,
            TargetFormat.Zip,
            TargetFormat.Nsis,
        )
        packageName = "Han1meViewer"
        // 跟着 Config.App.VERSION_NAME 走
        packageVersion = Config.App.desktopPackageVersion
        // 构建期跑一轮训练，把类加载与 JIT profile 落成 app.aot 随包发。
        // 冷启动省掉 JVM 预热，代价是每次打包多一轮约 45 秒的训练运行。
        enableAotCache = true
        homepage = "https://github.com/darriousliu/Han1meViewer1"
        // 产物直接按发布名出，CI 里那串 cp 改名必须去掉——latest*.yml 的 url 字段就是
        // 这里定的文件名，发布时再改名更新器会 404。
        // 不用 ${name}：它取的是 electron-builder 的小写 name（han1meviewer）。
        artifactName = "Han1meViewer-\${version}-\${os}-\${arch}.\${ext}"
        compressionLevel = CompressionLevel.Maximum
        // 依赖里带了多平台原生库（filekit 的 JNA、skiko），只留当前平台那份
        cleanupNativeLibs = true
        // 深链：写进 macOS 的 CFBundleURLTypes / Windows 注册表 / Linux .desktop 的 MimeType。
        // scheme 与 DeepLinkTarget.kt 里的 DEEP_LINK_SCHEME 必须一致。
        protocol("Han1meViewer", "han1meviewer")

        // 只生成 electron-builder 的发布配置与 latest*.yml/.blockmap，**不负责上传**，
        // 上传是 CI 的事（见 .github/workflows）。运行时那侧由
        // shared/jvmMain 的 InAppUpdater.jvm.kt 读同一个仓库的 Release。
        publish {
            github {
                enabled = true
                owner = "darriousliu"
                repo = "Han1meViewer1"
            }
        }

        windows {
            iconFile.set(file("icons/han1meviewer.ico"))
            menuGroup = rootProject.name
            // 取代原来的 shortcut / perUserInstall / dirChooser。
            // 不再需要 upgradeUuid：覆盖升级由 NSIS 自己按 packageName 判定，
            // 差分更新也走这条链，不是 MSI 那套 ProductCode 比对。
            nsis {
                oneClick = false
                perMachine = false
                allowElevation = true
                allowToChangeInstallationDirectory = true
                createDesktopShortcut = true
                createStartMenuShortcut = true
                runAfterFinish = true
            }
        }
        macOS {
            iconFile.set(file("icons/han1meviewer.icns"))
            // Nucleus 默认按 SDK 26 构建，等于给 macOS 26+ 开 Liquid Glass。
            // 应用自己是 Material 3 全自绘，系统新拟态只会作用在窗口装饰与系统控件上，
            // 两套观感混在一起并不好看，这里显式退回旧行为。
            macOsSdkVersion = null
        }
    }
}

// appResourcesRootDir 是个普通目录属性，Gradle 推不出它的内容来自哪个任务，显式挂一下。
// 名字对上 Nucleus 的 prepareAppResources / prepareSandboxedAppResources 两个 Sync。
tasks.matching { it.name.endsWith("AppResources") }.configureEach {
    dependsOn(unpackMpvNatives)
}
