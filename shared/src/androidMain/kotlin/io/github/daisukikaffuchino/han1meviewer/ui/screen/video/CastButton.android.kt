package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.util.StateSet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.enable_google_cast
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun CastButton(modifier: Modifier) {
    val context = LocalContext.current
    val contentDescription = stringResource(Res.string.enable_google_cast)
    AndroidView(
        modifier = modifier,
        factory = {
            MediaRouteButton(it).also { button ->
                button.minimumWidth = 0
                button.minimumHeight = 0
                button.contentDescription = contentDescription
                CastButtonFactory.setUpMediaRouteButton(context, button)
                button.setRemoteIndicatorDrawable(createGoogleCastIndicator(context))
            }
        },
    )
}

private fun createGoogleCastIndicator(context: Context): Drawable = StateListDrawable().apply {
    addState(
        intArrayOf(android.R.attr.state_checked),
        whiteDrawable(context, androidx.media3.cast.R.drawable.media_route_button_connected),
    )
    addState(
        intArrayOf(android.R.attr.state_checkable),
        whiteDrawable(context, androidx.media3.cast.R.drawable.media_route_button_disconnected),
    )
    addState(
        StateSet.WILD_CARD,
        whiteDrawable(context, androidx.media3.cast.R.drawable.media_route_button_disconnected),
    )
}

private fun whiteDrawable(context: Context, drawableRes: Int): Drawable =
    requireNotNull(ContextCompat.getDrawable(context, drawableRes)).mutate().also { drawable ->
        DrawableCompat.setTint(drawable, android.graphics.Color.WHITE)
    }
