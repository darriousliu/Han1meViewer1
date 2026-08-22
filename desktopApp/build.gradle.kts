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

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    // Compose Desktop 的事件循环跑在 Swing EDT 上，Dispatchers.Main 需要这个才有实现
    implementation(libs.coroutines.swing)
    implementation(libs.nucleus.application)
    implementation(libs.compose.multiplatform.ui.tooling.preview)
}

compose.desktop {
    application {
        mainClass = "io.github.daisukikaffuchino.han1meviewer.MainKt"

        // Tao 后端起不来时窗口是静默不显示的，加 -PtaoDebug 打开它自己的诊断日志
        if (project.hasProperty("taoDebug")) {
            jvmArgs += "-Dnucleus.tao.debug=true"
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Han1meViewer"
            packageVersion = "1.0.0"
        }
    }
}
