// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.com.google.devtools.ksp) apply false
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization) apply false
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.aboutlibraries) apply false
    alias(libs.plugins.ben.manes) apply false
    alias(libs.plugins.nucleus) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

/**
 * 把 [Config.App] 里的版号同步进 iOS 壳的 `Configuration/Config.xcconfig`。
 *
 * xcconfig 是 Xcode 在构建开始前读走的，构建过程中改它影响不到当次构建，所以没法挂在
 * framework 那条链上，只能做成显式任务：改完 Config.App 里的版号跑一次，CI 在 xcodebuild 之前也会跑。
 */
tasks.register("syncIosVersion") {
    group = "build"
    description = "把 Config.App 的 VERSION_NAME/VERSION_CODE 写进 iOS 壳的 Config.xcconfig"

    val xcconfig = layout.projectDirectory.file("iosApp/Configuration/Config.xcconfig")
    val marketingVersion = Config.App.VERSION_NAME
    val projectVersion = Config.App.VERSION_CODE
    inputs.property("versionName", marketingVersion)
    inputs.property("versionCode", projectVersion)
    outputs.file(xcconfig)

    doLast {
        val file = xcconfig.asFile
        require(file.exists()) { "找不到 ${file.path}" }

        var sawMarketing = false
        var sawProject = false
        val updated = file.readLines().joinToString("\n") { line ->
            when {
                line.startsWith("MARKETING_VERSION") -> {
                    sawMarketing = true
                    "MARKETING_VERSION=$marketingVersion"
                }

                line.startsWith("CURRENT_PROJECT_VERSION") -> {
                    sawProject = true
                    "CURRENT_PROJECT_VERSION=$projectVersion"
                }

                else -> line
            }
        } + "\n"
        require(sawMarketing && sawProject) {
            "${file.name} 里没找到 MARKETING_VERSION / CURRENT_PROJECT_VERSION，" +
                "是不是被改名了？改名后这个任务就同步不到版本号了"
        }

        if (file.readText() != updated) {
            file.writeText(updated)
            logger.lifecycle("Config.xcconfig 已同步为 $marketingVersion($projectVersion)")
        }
    }
}
