package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.calendar_desc
import han1meviewer.shared.generated.resources.calendar_location
import han1meviewer.shared.generated.resources.calendar_permission_denied
import han1meviewer.shared.generated.resources.calendar_title
import han1meviewer.shared.generated.resources.no_calendar_app
import io.github.daisukikaffuchino.utils.SonnerToast
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.getString
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventAvailabilityFree
import platform.EventKit.EKEventStore
import platform.EventKitUI.EKEventEditViewAction
import platform.EventKitUI.EKEventEditViewController
import platform.EventKitUI.EKEventEditViewDelegateProtocol
import platform.Foundation.NSDate
import platform.Foundation.NSSelectorFromString
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.UIKit.popoverPresentationController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * 走 EventKit 真正把事件写进系统日历。
 *
 * 用 [EKEventEditViewController] 而不是 `EKEventStore.saveEvent()` 直接落库，
 * 是为了和 Android 的 `ACTION_INSERT` 对齐：都弹系统的「新建事件」界面，由用户确认、
 * 并自己选写进哪个日历。静默写别人的日历不合适，也没法处理「没有默认日历」的情况。
 */
@Composable
actual fun rememberAddCalendarEvent(): (LocalDate) -> Unit {
    val scope = rememberCoroutineScope()
    return { date ->
        scope.launch {
            // 字符串要 suspend 取，先在协程里取好再进 EventKit 的回调
            val title = getString(Res.string.calendar_title, date.month.number, date.day)
            val description = getString(Res.string.calendar_desc)
            val location = getString(Res.string.calendar_location)
            requestAccessThen { granted ->
                if (granted) {
                    presentEventEditor(date, title, description, location)
                } else {
                    SonnerToast.warning(Res.string.calendar_permission_denied)
                }
            }
        }
    }
}

/**
 * iOS 17 起加事件只要「只写」权限，弹窗措辞比全量访问轻得多；
 * 部署目标还包含 iOS 15/16，那两版只有全量权限一条路，用 respondsToSelector 分流。
 */
@OptIn(ExperimentalForeignApi::class)
private fun requestAccessThen(onResult: (Boolean) -> Unit) {
    val store = EKEventStore()
    val deliver: (Boolean) -> Unit = { granted ->
        // EventKit 的回调在任意队列上，UI 与 Toast 都必须回主线程
        dispatch_async(dispatch_get_main_queue()) { onResult(granted) }
    }
    val writeOnly = NSSelectorFromString("requestWriteOnlyAccessToEventsWithCompletion:")
    if (store.respondsToSelector(writeOnly)) {
        store.requestWriteOnlyAccessToEventsWithCompletion { granted, _ -> deliver(granted) }
    } else {
        store.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { granted, _ ->
            deliver(granted)
        }
    }
}

private fun presentEventEditor(
    date: LocalDate,
    title: String,
    description: String,
    location: String,
) {
    val host = topMostViewController() ?: run {
        SonnerToast.warning(Res.string.no_calendar_app)
        return
    }
    val store = EKEventStore()
    val event = EKEvent.eventWithEventStore(store).apply {
        setTitle(title)
        setNotes(description)
        setLocation(location)
        // 全天事件：结束日期是开区间，取当天零点到次日零点
        setStartDate(date.atStartOfDayEpochMillis().toNSDate())
        setEndDate(date.plusDays(1).atStartOfDayEpochMillis().toNSDate())
        setAllDay(true)
        // 打卡提醒不该把这一天标成「忙」
        setAvailability(EKEventAvailabilityFree)
        setCalendar(store.defaultCalendarForNewEvents)
    }

    val controller = EKEventEditViewController().apply {
        eventStore = store
        setEvent(event)
    }
    // editViewDelegate 是 weak 的：不自己留一份强引用，delegate 立刻被回收，
    // 用户点「加入」或「取消」都不回调，界面关不掉
    val delegate = EventEditDelegate()
    retainedEditDelegate = delegate
    controller.editViewDelegate = delegate
    // iPad 上 popover 没有锚点会直接崩
    controller.popoverPresentationController?.sourceView = host.view
    host.presentViewController(controller, animated = true, completion = null)
}

/**
 * 撑住上面那个 weak 的 editViewDelegate。
 *
 * 放顶层而不是 EventEditDelegate 的 companion：Kotlin/Native 不允许 ObjC 子类的
 * companion 带字段（Fields are not supported for Companion of subclass of ObjC type）。
 * 只在主线程读写。
 */
private var retainedEditDelegate: EventEditDelegate? = null

private class EventEditDelegate : NSObject(), EKEventEditViewDelegateProtocol {
    override fun eventEditViewController(
        controller: EKEventEditViewController,
        didCompleteWithAction: EKEventEditViewAction,
    ) {
        controller.dismissViewControllerAnimated(true) { retainedEditDelegate = null }
    }
}

private fun Long.toNSDate(): NSDate =
    NSDate.dateWithTimeIntervalSince1970(this / 1000.0)
