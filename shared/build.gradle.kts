@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import Config.isRelease
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ktorfit)
    // 只为让 R.raw.aboutlibraries 有定义；KMP 模块下它收集不到依赖，产出是空壳，
    // 真正有内容的那份由 :app 生成并在资源合并时覆盖
    alias(libs.plugins.aboutlibraries)
}

val releaseBuild = isRelease

// AGP 的 KMP 库插件没有 buildFeatures.buildConfig，BuildConfig 由 BuildKonfig 生成
buildkonfig {
    packageName = "io.github.daisukikaffuchino.han1meviewer"
    // 必须是 internal（objectName 而非 exposeObjectWithName）：public 会导出进
    // ObjC 头，其中的 DEBUG 字段撞 Xcode 的 DEBUG=1 宏，iOS 编译当场崩
    objectName = "BuildConfig"

    defaultConfigs {
        buildConfigField(BOOLEAN, "DEBUG", (!releaseBuild).toString(), const = true)
        // 必须与 manifest 里 ${applicationId} 展开后逐字一致，.debug 后缀不能省
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
        // CMP 的 BackHandler 标了 @ExperimentalComposeUiApi，用到的地方会越来越多，
        // 在模块级别开一次比逐文件 @OptIn 好维护
        freeCompilerArgs.add("-opt-in=androidx.compose.ui.ExperimentalComposeUiApi")
    }

    android {
        // 与原 :app 的 namespace 逐字相同，R/BuildConfig 才留在原包下；
        // 代价是 :app 让位成 ...han1meviewer.app（applicationId 不变）
        namespace = "io.github.daisukikaffuchino.han1meviewer"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            // 让 kotlin-parcelize 认 commonMain 里那个 expect 注解
            freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=io.github.daisukikaffuchino.han1meviewer.util.Parcelize",
            )
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
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "io.github.daisukikaffuchino.han1meviewer.shared")
        }
    }

    applyDefaultHierarchyTemplate()

    // android 与 jvm 共用的中间源集：DoH、ProxySelector、磁盘缓存、限速这些
    // Ktor 没有对应物、只能直接用 OkHttp API 的东西放这里，iOS 走 Darwin 另写
    val androidJvmMain = sourceSets.create("androidJvmMain") {
        dependsOn(sourceSets.getByName("commonMain"))
    }
    sourceSets.getByName("androidMain").dependsOn(androidJvmMain)
    sourceSets.getByName("jvmMain").dependsOn(androidJvmMain)
    androidJvmMain.dependencies {
        implementation(libs.ktor.client.okhttp)
        implementation(libs.okhttp)
        implementation(libs.okhttp.dns.over.https)
    }

    sourceSets {
        commonMain {
            // KSP 生成到 commonMain metadata，各目标共用一份
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(libs.bundles.compose.multiplatform)
                implementation(libs.bundles.lifecycle)

                implementation(libs.coroutines.core)
                implementation(libs.serialization.json)
                implementation(libs.datetime)
                implementation(libs.datastore.core)
                implementation(libs.datastore.preferences.core)
                implementation(libs.bundles.filekit)
                implementation(libs.coil.compose)
                // 跨平台 sprintf：CMP 的 stringResource 只认 %N$d/%N$s，处理不了 %.1f
                implementation(libs.mp.stools)
                implementation(libs.kermit)
                implementation(libs.ksoup)
                implementation(libs.ktor.client.core)
                implementation(libs.ktorfit.lib.light)
                implementation(libs.jb.navigation3.ui)
            }
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

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
            implementation(libs.coil.network.ktor3)
            implementation(libs.aboutlibraries.compose.m3)
            implementation(libs.compose.avatar.cropper)
            implementation(libs.kyant.m3color)
            implementation(libs.sonner)

            implementation(libs.datetime)
            implementation(libs.serialization.json)
            implementation(libs.ksoup)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.okhttp)


            implementation(libs.media3.exoplayer)
            implementation(libs.media3.exoplayer.hls)
            implementation(libs.media3.cast)
            implementation(libs.mpv.lib)
        }
    }
}

dependencies {
    // entity/DAO 与 service 目前都在 androidMain，只需要 android 那条处理器
    add("kspAndroid", libs.room.compiler)
    kspCommonMainMetadata(libs.ktorfit.ksp)

    androidRuntimeClasspath(libs.compose.ui.ui.tooling)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

// 编译任务与其它 ksp 任务都读 commonMain 的生成目录，必须排在它后面
val kspMetadataTask = tasks.matching { it.name == "kspCommonMainKotlinMetadata" }
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn(kspMetadataTask)
    }
}
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }
    .configureEach { dependsOn(kspMetadataTask) }
