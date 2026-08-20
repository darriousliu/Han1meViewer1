@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.daisukikaffuchino.han1meviewer.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import io.github.daisukikaffuchino.utils.VibrationUtil

/** Icon button variants with a shared expressive pressed-shape transition. */
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    shapes: IconButtonShapes = IconButtonDefaults.shapes(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    androidx.compose.material3.IconButton(
        onClick = {
            VibrationUtil.performHapticFeedback(view)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shapes = shapes,
        content = content,
    )
}

@Composable
fun FilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
    shapes: IconButtonShapes = IconButtonDefaults.shapes(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    androidx.compose.material3.FilledIconButton(
        onClick = {
            VibrationUtil.performHapticFeedback(view)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shapes = shapes,
        content = content,
    )
}

@Composable
fun FilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(),
    shapes: IconButtonShapes = IconButtonDefaults.shapes(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    androidx.compose.material3.FilledTonalIconButton(
        onClick = {
            VibrationUtil.performHapticFeedback(view)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shapes = shapes,
        content = content,
    )
}

@Composable
fun OutlinedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.outlinedIconButtonColors(),
    shapes: IconButtonShapes = IconButtonDefaults.shapes(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    androidx.compose.material3.OutlinedIconButton(
        onClick = {
            VibrationUtil.performHapticFeedback(view)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shapes = shapes,
        content = content,
    )
}

/** Filled tonal button with app-wide haptic feedback and pressed-shape transition. */
@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    shapes: ButtonShapes = ButtonDefaults.shapes(),
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val view = LocalView.current
    val resolvedShapes = shape?.let { shapes.copy(shape = it) } ?: shapes
    androidx.compose.material3.FilledTonalButton(
        onClick = {
            VibrationUtil.performHapticFeedback(view)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shapes = resolvedShapes,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

/** Outlined button with app-wide haptic feedback and pressed-shape transition. */
@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    shapes: ButtonShapes = ButtonDefaults.shapes(),
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val view = LocalView.current
    val resolvedShapes = shape?.let { shapes.copy(shape = it) } ?: shapes
    androidx.compose.material3.OutlinedButton(
        onClick = {
            VibrationUtil.performHapticFeedback(view)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shapes = resolvedShapes,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}
