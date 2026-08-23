package io.github.daisukikaffuchino.han1meviewer.di

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.datastore.DataStoreManager
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.module
import io.github.daisukikaffuchino.han1meviewer.logic.network.installPlatformNetworking
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.applyStoredAppLanguage

fun initKoin(platformDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        platformDeclaration()
        modules(AppModule().module)
    }
}

/**
 * Koin 之外的启动项：存储要先装好，Koin 的定义里有直接读 SettingsRepository 的。
 *
 * Android 在 HanimeApplication.onCreate() 里单独调这个（那边还要穿插
 * Context 注入、语言和代理的初始化）；桌面和 iOS 走 [initAppOnce]。
 */
private fun initOthers() {
    DataStoreManager.initialize()
    SettingsRepository.install(DataStoreManager)
    // 下面两个都要读用户配置，必须排在 SettingsRepository 之后
    applyStoredAppLanguage()
    installPlatformNetworking()
}

private var appInitialized = false

fun initAppOnce(platformDeclaration: KoinAppDeclaration = {}) {
    if (appInitialized) return
    appInitialized = true
    initOthers()
    initKoin(platformDeclaration)
}
