package io.github.daisukikaffuchino.han1meviewer.util

import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <R> ListenableFuture<R>.await(): R {
    // Fast path
    if (isDone) {
        try {
            return get()
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }
    return suspendCancellableCoroutine { cancellableContinuation ->
        addListener(
            {
                try {
                    cancellableContinuation.resume(get())
                } catch (throwable: Throwable) {
                    val cause = throwable.cause ?: throwable
                    when (throwable) {
                        is java.util.concurrent.CancellationException ->
                            cancellableContinuation.cancel(cause)

                        else -> cancellableContinuation.resumeWithException(cause)
                    }
                }
            },
            DirectExecutor
        )

        cancellableContinuation.invokeOnCancellation {
            cancel(false)
        }
    }
}

/**
 * Run suspend catching
 *
 * @param block suspend block
 */
inline fun <R> runSuspendCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

private data object DirectExecutor : Executor {

    override fun execute(command: Runnable) {
        command.run()
    }
}
