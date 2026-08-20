@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// ============================================================================
// :app —— Android 应用壳，零 Kotlin 源码。
//
// 只剩四样东西：AndroidManifest、cpp/JNI（签名校验，Android 专属）、
// res/resources.properties（generateLocaleConfig 要求它在 application 模块）、
// 以及签名/打包/版号配置。业务代码全在 :shared 的 androidMain。
// ============================================================================
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ben.manes)
    // 依赖清单的真正来源：只有 application 模块能解析到完整的 runtimeClasspath
    // （:shared 里那些 implementation 依赖也在其中）。:shared 上那个同名插件只产出
    // 空壳，靠这里生成的 res/raw/aboutlibraries.json 在资源合并时覆盖它。
    // **别摘这一行**，摘了开源许可页会静默变成空白。
    alias(libs.plugins.aboutlibraries)
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = Config.App.APPLICATION_ID
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = Config.App.VERSION_CODE
        versionName = Config.App.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
                abiFilters += "arm64-v8a"
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    splits {
        abi {
            isEnable = gradle.startParameter.taskRequests.toString().contains("Release")
            reset()
            include("arm64-v8a")
            isUniversalApk = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_new"
        }

        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_debug"
        }
    }
    buildFeatures {
        // BuildConfig 改由 :shared 的 BuildKonfig 生成（AGP 的 KMP 库插件没有这个开关），
        // 这里再生成一份只会多出一个没人用的同名类。
        buildConfig = false
        compose = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    lint {
        disable += setOf("EnsureInitializerMetadata")
    }
    // 让位给 :shared——AGP 不允许两个模块用同一个 namespace，而 R/BuildConfig
    // 必须留在 ...han1meviewer 下面才不用改 128 处 import。applicationId 不受影响。
    namespace = "io.github.daisukikaffuchino.han1meviewer.app"

    androidResources {
        generateLocaleConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.value(JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-jvm-default=enable"
        )
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val apkName = "Han1meViewer-v${output.versionName.get()}.apk"
            (output as VariantOutputImpl).outputFileName = apkName
        }
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(project.dependencies.platform(libs.compose.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.ui.tooling.preview)
    debugImplementation(libs.compose.ui.ui.tooling)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    androidTestImplementation(libs.test.junit)
}
