@file:Suppress("UnstableApiUsage")

import org.gradle.api.Project
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * @author Yenaly Liew
 * @time 2023/11/25 025 17:55
 */
object Config {

    /**
     * 版号与 applicationId 的唯一来源。
     *
     * :app 拿它填 defaultConfig，:shared 拿它喂 BuildKonfig，:desktopApp 拿它当安装包版本，
     * iOS 壳靠根项目的 syncIosVersion 任务同步过去——都读这里，
     * 别在任何 build.gradle.kts 或源码里再写一份字面量。
     */
    object App {
        const val APPLICATION_ID = "io.github.daisukikaffuchino.han1meviewer"
        const val VERSION_CODE = 260805
        const val VERSION_NAME = "26.3.2"

        /** debug 构建带 `.debug` 后缀，与 :app 的 applicationIdSuffix 一致。 */
        fun applicationId(isRelease: Boolean) =
            if (isRelease) APPLICATION_ID else "$APPLICATION_ID.debug"

        /**
         * jpackage 能吃的版本号。它只认纯数字的 `主.次.修订`，Windows 的 MSI 还要求主版本号
         * 在 1..255 之间，所以这里把 `26.3.2-beta` 一类的后缀截掉，只留数字段。
         */
        val desktopPackageVersion: String
            get() {
                val numeric = VERSION_NAME.substringBefore('-').substringBefore('+')
                val nums = numeric.split('.')
                    .map { it.trim() }
                    .takeWhile { it.toIntOrNull() != null }
                    .map { it.toInt() }
                require(nums.isNotEmpty() && nums.first() in 1..255) {
                    "VERSION_NAME=$VERSION_NAME 转不出 jpackage 能用的版本号：" +
                        "主版本号必须是 1..255 的整数"
                }
                return nums.take(3).joinToString(".")
            }
    }

    val Project.isRelease: Boolean
        get() = gradle.startParameter.taskNames.any { it.contains("Release") }

    object Version {

        const val DEBUG = "debug"
        const val RELEASE = "release"

        fun Project.createVersion(
            major: Int, minor: Int, patch: Int
        ): Pair<Int, String> {
            val source = this.source
            val versionCode: Int
            val versionName: String
            when (source) {
                DEBUG -> {
                    versionCode = 1
                    versionName = "$DEBUG+$versionCode"
                }

                else -> {
                    versionCode = LocalDateTime.now(Clock.systemUTC()).format(
                        DateTimeFormatter.ofPattern("yyMMddHH")
                    ).toInt()
                    versionName = "${major}.${minor}.${patch}-$source+$versionCode"
                }
            }
            return versionCode to versionName
        }

        val Project.source: String
            get() = if (isRelease) RELEASE else DEBUG
    }

    val thisYear: Int
        get() = LocalDateTime.now(Clock.system(ZoneId.of("UTC+8"))).year
}
