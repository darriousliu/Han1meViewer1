package io.github.daisukikaffuchino.han1meviewer.logic.model

/**
 * 假应用图标。设置里存的一直是 Android 的 activity-alias 类名，
 * iOS 侧的备用图标名（Assets.xcassets 里的 appiconset）在这里一并对上。
 */
enum class LauncherIconOption(val alias: String, val iosIconName: String?) {
    Default("io.github.daisukikaffuchino.han1meviewer.LauncherAliasDefault", null),
    Calc("io.github.daisukikaffuchino.han1meviewer.LauncherFakeCalc", "AppIconCalc"),
    Cornhub("io.github.daisukikaffuchino.han1meviewer.LauncherFakeCornhub", "AppIconCornhub"),
    Xxt("io.github.daisukikaffuchino.han1meviewer.LauncherFakeXxt", "AppIconXxt");

    companion object {
        fun fromAlias(alias: String): LauncherIconOption =
            entries.firstOrNull { it.alias == alias } ?: Default
    }
}
