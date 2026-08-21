package io.github.daisukikaffuchino.han1meviewer.util

/**
 * Parcelable/Parcelize 只有 Android 有。这里给出跨平台声明，其它平台是空实现。
 * Android 侧要让 kotlin-parcelize 认得这个注解，靠编译参数 additionalAnnotation
 * 指过去（见 shared/build.gradle.kts）。
 */
expect interface Parcelable

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class Parcelize()
