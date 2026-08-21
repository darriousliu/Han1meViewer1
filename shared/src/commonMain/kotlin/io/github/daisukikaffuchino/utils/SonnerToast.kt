package io.github.daisukikaffuchino.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.luminance
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterDefaults
import com.dokar.sonner.ToasterState
import com.dokar.sonner.rememberToasterState
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * 应用内唯一的 Sonner Toast 入口。
 *
 * 调用方可以位于 Compose、Activity、ViewModel、Worker 或传统 View 中；请求会切换到主线程，
 * 并在下一个可用的 Activity 宿主显示。不要直接使用 Android [android.widget.Toast]。
 */
object SonnerToast {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private data class Request(
        val message: String,
        val type: ToastType,
        val duration: Duration,
    )

    private const val MAX_PENDING_REQUESTS = 10

    private val pendingRequests = ArrayDeque<Request>()
    private var toasterState: ToasterState? = null

    /** Adds the shared toast host to an Activity's root Compose content. */
    @Composable
    fun Host() {
        val state = rememberToasterState()
        val darkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f

        DisposableEffect(state) {
            attach(state)
            onDispose { detach(state) }
        }

        Toaster(
            state = state,
            maxVisibleToasts = 3,
            richColors = true,
            darkTheme = darkTheme,
            showCloseButton = true,
        )
    }

    fun show(
        message: String?,
        type: ToastType = ToastType.Normal,
        duration: Duration = ToasterDefaults.DurationDefault,
    ) {
        val text = message?.trim().orEmpty()
        if (text.isEmpty()) return
        scope.launch { deliver(Request(text, type, duration)) }
    }

    // CMP 的 getString 是 suspend 的，这里起个协程解析完再走原来的显示路径
    fun show(
        resource: StringResource,
        vararg formatArgs: Any,
        type: ToastType = ToastType.Normal,
        duration: Duration = ToasterDefaults.DurationDefault,
    ) {
        scope.launch {
            show(getString(resource, *formatArgs), type, duration)
        }
    }

    fun success(message: String?) = show(message, ToastType.Success)

    fun success(resource: StringResource, vararg formatArgs: Any) =
        show(resource, *formatArgs, type = ToastType.Success)

    fun info(message: String?) = show(message, ToastType.Info)

    fun info(resource: StringResource, vararg formatArgs: Any) =
        show(resource, *formatArgs, type = ToastType.Info)

    fun warning(message: String?) = show(message, ToastType.Warning)

    fun warning(resource: StringResource, vararg formatArgs: Any) =
        show(resource, *formatArgs, type = ToastType.Warning)

    fun error(message: String?) = show(message, ToastType.Error, ToasterDefaults.DurationLong)

    fun error(resource: StringResource, vararg formatArgs: Any) =
        show(resource, *formatArgs, type = ToastType.Error, duration = ToasterDefaults.DurationLong)

    @Suppress("UNUSED_PARAMETER")
    fun dismissAll() {
        scope.launch { toasterState?.dismissAll() }
    }

    private fun attach(state: ToasterState) {
        toasterState = state
        while (pendingRequests.isNotEmpty()) {
            val request = pendingRequests.removeFirst()
            state.show(request.message, type = request.type, duration = request.duration)
        }
    }

    private fun detach(state: ToasterState) {
        if (toasterState === state) toasterState = null
    }

    private fun deliver(request: Request) {
        val state = toasterState
        if (state == null) {
            if (pendingRequests.size == MAX_PENDING_REQUESTS) pendingRequests.removeFirst()
            pendingRequests.addLast(request)
            return
        }
        state.show(request.message, type = request.type, duration = request.duration)
    }
}
