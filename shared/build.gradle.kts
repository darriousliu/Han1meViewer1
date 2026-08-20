@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import Config.isRelease
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

// ============================================================================
// :shared —— 唯一的 KMP 模块，三个平台壳（:app / :desktopApp / iosApp）都只认它。
//
// 全部业务代码现在住在 androidMain（325 个 kt + res + assets），commonMain 只有
// 占位 App()。后续按依赖逐块上移 commonMain。多模块拆分留到后面几轮。
// ============================================================================
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.buildkonfig)
    // ⚠️ 这里挂它**只是为了让 R.raw.aboutlibraries 有定义**，它在 KMP 模块下收集不到
    // 任何依赖，产出恒为 30 字节空壳 {"libraries":[],"licenses":{}}（试过
    // collect { all = true }，没用）。真正有内容的那份由 :app 生成并在资源合并时
    // 覆盖掉这份空壳——**所以 :app 的 aboutlibraries 插件不能摘**，摘了开源许可页
    // 会整页空白，而且编译期毫无征兆。
    // 等 S5 资源搬去 Compose Resources、改用 Res.readBytes 之后，这份空壳才能去掉。
    alias(libs.plugins.aboutlibraries)
}

val releaseBuild = isRelease

// AGP 的 KMP 库插件没有 buildFeatures.buildConfig，BuildConfig 换成 BuildKonfig 生成。
// packageName 与原来的 namespace 一致，所以 11 处 `import ...han1meviewer.BuildConfig`
// 和 Constants.kt 的同包引用全都不用改。
buildkonfig {
    packageName = "io.github.daisukikaffuchino.han1meviewer"
    // 用 objectName 而不是 exposeObjectWithName：前者生成 internal object，后者生成
    // public object。必须是 internal——public 会被导出进 Shared.framework 的 ObjC 头，
    // 而里面那个 `DEBUG` 字段撞上 Xcode Debug 配置预定义的 `DEBUG=1` 宏，头文件被预处理
    // 成 `@property (readonly) BOOL 1;`，iOS 侧编译直接崩在 module PCM 生成阶段。
    // BuildConfig 本来也只是模块内部实现，不该出现在对外 framework 的 API 里。
    objectName = "BuildConfig"

    defaultConfigs {
        buildConfigField(BOOLEAN, "DEBUG", (!releaseBuild).toString(), const = true)
        // FILE_PROVIDER_AUTHORITY 直接拼它，必须与 manifest 里 ${applicationId} 展开后
        // 逐字一致，所以 debug 的 .debug 后缀不能省。
        buildConfigField(
            STRING, "APPLICATION_ID", Config.App.applicationId(releaseBuild), const = true
        )
        buildConfigField(STRING, "VERSION_NAME", Config.App.VERSION_NAME, const = true)
        buildConfigField(INT, "VERSION_CODE", Config.App.VERSION_CODE.toString(), const = true)
        buildConfigField(INT, "SEARCH_YEAR_RANGE_END", Config.thisYear.toString(), const = true)
    }
}

kotlin {
    compilerOptions {
        // expect/actual class 目前仍是实验特性
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        // 与原 :app 的 namespace 逐字相同：R 和 BuildConfig 仍生成在这个包下，
        // 128 处 `import ...han1meviewer.R` 与 958 处 stringResource 一行不用改。
        // 代价是 :app 的 namespace 要让位成 ...han1meviewer.app（applicationId 不变）。
        namespace = "io.github.daisukikaffuchino.han1meviewer"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            // iosApp/ContentView.swift 里的 `import Shared` 认的就是这个名字
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "io.github.daisukikaffuchino.han1meviewer.shared")
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            // KSP（S4 的 Ktorfit / Room3）生成到 commonMain metadata，各目标共用一份。
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(libs.bundles.compose.multiplatform)
            }
        }

        // 依赖原样从 :app 搬过来，一个不增不减；替换要等 S4 一项一项来。
        androidMain.dependencies {
            implementation(libs.aboutlibraries.core)
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.androidx.documentfile)
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.datastore.preferences)

            implementation(libs.bundles.android.base)
            implementation(libs.bundles.android.jetpack)

            implementation(project.dependencies.platform(libs.compose.compose.bom))
            implementation(libs.compose.ui.graphics)
            implementation(libs.compose.material3)
            implementation(libs.androidx.activity.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.compose.ui.ui.tooling.preview)
            implementation(libs.androidx.ui)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.okhttp)
            implementation(libs.aboutlibraries.compose.m3)
            implementation(libs.compose.avatar.cropper)
            implementation(libs.kyant.m3color)
            implementation(libs.sonner)

            implementation(libs.datetime)
            implementation(libs.serialization.json)
            implementation(libs.ksoup)

            implementation(libs.retrofit)
            implementation(libs.converter.serialization)
            implementation(libs.okhttp)
            implementation(libs.okhttp.dns.over.https)

            implementation(libs.coil)

            implementation(libs.media3.exoplayer)
            implementation(libs.media3.exoplayer.hls)
            implementation(libs.media3.cast)
            implementation(libs.mpv.lib)
        }
    }
}

dependencies {
    // Room 的 entity/DAO 目前全在 androidMain，只需要 android 目标那条处理器。
    // S4 升 room3 上移 commonMain 时再补 kspJvm / kspIos*。
    add("kspAndroid", libs.room.compiler)

    androidRuntimeClasspath(libs.compose.ui.ui.tooling)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

// 所有编译任务和其它 ksp 任务都会读 commonMain 的生成目录，都得排在它后面，
// 否则 Gradle 报 "uses this output ... without declaring an explicit dependency"。
val kspMetadataTask = tasks.matching { it.name == "kspCommonMainKotlinMetadata" }
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn(kspMetadataTask)
    }
}
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }
    .configureEach { dependsOn(kspMetadataTask) }
