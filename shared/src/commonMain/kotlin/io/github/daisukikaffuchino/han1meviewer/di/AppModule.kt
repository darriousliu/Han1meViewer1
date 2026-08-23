package io.github.daisukikaffuchino.han1meviewer.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Koin 定义的总入口。
 *
 * `@ComponentScan` 扫整个 `io.github.daisukikaffuchino.han1meviewer` 包，
 * 所以新增 `@KoinViewModel` / `@Single` / `@Factory` 不用回来登记，写完注解就生效。
 */
@Module
@ComponentScan("io.github.daisukikaffuchino.han1meviewer")
class AppModule
