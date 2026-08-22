package io.github.daisukikaffuchino.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper

lateinit var applicationContext: Context
    internal set

val application: Application
    get() = applicationContext as Application

val Context.activity: Activity?
    get() {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
