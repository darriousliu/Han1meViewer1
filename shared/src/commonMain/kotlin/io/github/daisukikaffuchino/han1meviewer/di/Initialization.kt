package io.github.daisukikaffuchino.han1meviewer.di

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.datastore.DataStoreManager
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.module

/**
 * Koin 的唯一启动入口，三端宿主各自调一次。
 *
 * [platformDeclaration] 给平台补自己的东西 —— Android 要在这里传 `androidContext(...)`，
 * 桌面和 iOS 目前不需要。
 */
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
}

private var appInitialized = false

/**
 * 桌面 / iOS 的一次性启动序列：先装存储，再起 Koin。
 *
 * 顺序不能反 —— Koin 的定义里有直接读 SettingsRepository 的。
 *
 * 幂等：iOS 的 MainViewController() 可能被 SwiftUI 调多次，重复 startKoin 会抛
 * KoinAppAlreadyStartedException，重复 install 会把 SettingsRepository 的流重置。
 *
 * Android 不用这个 —— HanimeApplication.onCreate() 里还要穿插 Context 注入和
 * 语言、代理的初始化，那边自己按顺序调 initOthers() 与 initKoin()。
 */
fun initAppOnce(platformDeclaration: KoinAppDeclaration = {}) {
    if (appInitialized) return
    appInitialized = true
    initOthers()
    initKoin(platformDeclaration)
}
