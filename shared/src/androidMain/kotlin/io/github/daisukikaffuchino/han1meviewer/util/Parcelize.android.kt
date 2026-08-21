package io.github.daisukikaffuchino.han1meviewer.util

actual typealias Parcelable = android.os.Parcelable

/**
 * 不能 typealias 到 kotlinx.parcelize.Parcelize：additionalAnnotation 已经把本名字
 * 注册成 parcelize 注解，再取别名会让使用点解析歧义。声明成自己的注解即可，
 * 代码生成由编译器插件按 additionalAnnotation 完成。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
actual annotation class Parcelize
