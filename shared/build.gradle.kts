@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

// ============================================================================
// :shared —— 唯一的 KMP 模块，三个平台壳（:app / :desktopApp / iosApp）都只认它。
//
// 目前 commonMain 里只有占位 App()；app 的业务代码会整体搬进 androidMain（S3），
// 之后再逐块上移 commonMain。多模块拆分（core/feature）留到后面几轮。
// ============================================================================
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    compilerOptions {
        // expect/actual class 目前仍是实验特性
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        // S3 搬代码时这里会改成 io.github.daisukikaffuchino.han1meviewer，
        // 好让 R / BuildConfig 仍生成在原包名下，128 个 R import 一行不用改；
        // 届时 :app 的 namespace 让位改成 ...han1meviewer.app（applicationId 不变）。
        namespace = "io.github.daisukikaffuchino.han1meviewer.shared"
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
            // 目录不存在时无害，所以现在就挂上是零成本的。
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(libs.bundles.compose.multiplatform)
            }
        }
    }
}

// 所有编译任务和其它 ksp 任务都会读 commonMain 的生成目录，都得排在它后面，
// 否则 Gradle 报 "uses this output ... without declaring an explicit dependency"。
// 用 tasks.matching 惰性引用：还没挂处理器时该任务不存在，按名字 dependsOn 会直接报错，
// matching 出来的空集合则无害。
val kspMetadataTask = tasks.matching { it.name == "kspCommonMainKotlinMetadata" }
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn(kspMetadataTask)
    }
}
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }
    .configureEach { dependsOn(kspMetadataTask) }
