package io.github.daisukikaffuchino.utils

import android.app.Activity
import android.content.Intent
import java.lang.ref.WeakReference
import kotlin.system.exitProcess

/**
 * 顶层 expect 函数（非 @Composable）拿不到 LocalActivity，只能从这里取。
 * @Composable 的场合一律优先用 LocalActivity，别走这里。
 */
object ActivityManager {
    @JvmStatic
    var currentActivity: WeakReference<Activity?> = WeakReference(null)

    /** 已经 finishing / destroyed 的不再交出去，避免拿到僵尸 Activity。 */
    @JvmStatic
    val activeActivity: Activity?
        get() = currentActivity.get()?.takeIf { !it.isFinishing && !it.isDestroyed }

    @JvmStatic
    fun onActivityResumed(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    @JvmStatic
    fun onActivityDestroyed(activity: Activity) {
        if (currentActivity.get() === activity) currentActivity = WeakReference(null)
    }

    @JvmStatic
    fun restart(killProcess: Boolean = true) {
        val intent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent != null) {
            applicationContext.startActivity(intent)
        }
        if (killProcess) exitProcess(0)
    }
}

/**
 * 优先用前台 Activity 启动，退化到 application context 时才补 NEW_TASK。
 *
 * 差别不只是崩不崩：用 Activity 起 chooser 才留在同一个 task 里，
 * 临时 uri 授权也才跟着 Activity 走。
 */
fun startActivitySafely(intent: Intent): Boolean = runCatching {
    val activity = ActivityManager.activeActivity
    if (activity != null) {
        activity.startActivity(intent)
    } else {
        applicationContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    true
}.getOrDefault(false)
