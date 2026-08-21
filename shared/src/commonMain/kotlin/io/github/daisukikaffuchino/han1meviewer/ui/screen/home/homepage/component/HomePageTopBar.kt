package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.github.daisukikaffuchino.han1meviewer.ui.component.IconButton
import io.github.daisukikaffuchino.han1meviewer.ui.component.appbar.HanimeTopAppBar
import io.github.daisukikaffuchino.han1meviewer.ui.preview.ComponentPreview
import io.github.daisukikaffuchino.han1meviewer.ui.theme.HanimeDefaults
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.global_search
import han1meviewer.shared.generated.resources.hanime_list
import han1meviewer.shared.generated.resources.ic_menu
import han1meviewer.shared.generated.resources.ic_newspaper
import han1meviewer.shared.generated.resources.ic_search
import han1meviewer.shared.generated.resources.open_menu
import han1meviewer.shared.generated.resources.roboto

/**
 * 渲染首页顶部栏，包含抽屉入口、搜索入口和新番列表入口。
 * @param onOpenDrawer 点击抽屉按钮时调用。
 * @param onSearchClick 点击搜索框时调用。
 * @param onNavigateToPreview 点击新番按钮时调用。
 * @param modifier 应用于顶部栏根布局的修饰符。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageTopBar(
    onOpenDrawer: () -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToPreview: () -> Unit,
    modifier: Modifier = Modifier,
    showNavigationIcon: Boolean = true,
    containerColor: Color = HanimeDefaults.Colors.pageSurface,
) {
    HanimeTopAppBar(
        modifier = modifier,
        title = {
            Han1meViewerText()
        },
        navigationIcon = if (showNavigationIcon) {
            {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_menu),
                        contentDescription = stringResource(Res.string.open_menu),
                    )
                }
            }
        } else {
            {}
        },
        actions = {
            Row {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = stringResource(Res.string.global_search),
                    )
                }
                IconButton(onClick = onNavigateToPreview) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_newspaper),
                        contentDescription = stringResource(Res.string.hanime_list),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
private fun Han1meViewerText(
    modifier: Modifier = Modifier,
    fontSize: Int = 20,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    // CMP 的 Font(FontResource) 是 @Composable（内部按需异步加载), 不能再作为顶层 val 初始化,
    // 迁移为在组合内按需构建（Font() 内部会自行按 resource 做 remember 缓存）。
    val robotoFont = FontFamily(Font(Res.font.roboto))

    val annotatedString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontFamily = robotoFont,
                fontWeight = FontWeight.Bold,
            )
        ) {
            append("H")
        }
        withStyle(
            style = SpanStyle(
                color = onSurface,
                fontFamily = robotoFont,
                fontWeight = FontWeight.Bold,
            )
        ) {
            append("an1me")
        }
        withStyle(
            style = SpanStyle(
                color = onSurfaceVariant,
                fontFamily = robotoFont,
                fontWeight = FontWeight.Normal,
            )
        ) {
            append("Viewer")
        }
    }

    BasicText(
        text = annotatedString,
        modifier = modifier,
        style = androidx.compose.ui.text.TextStyle(
            fontSize = fontSize.sp,
        ),
        softWrap = false,
        maxLines = maxLines,
        overflow = overflow,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 8.sp,
            maxFontSize = fontSize.sp,
        ),
    )
}

@Preview
@Composable
private fun HomePageTopBarPreview() {
    ComponentPreview {
        HomePageTopBar(
            onOpenDrawer = {},
            onSearchClick = {},
            onNavigateToPreview = {}
        )
    }
}

@Preview
@Composable
private fun HomePageTopBarLandPreview() {
    ComponentPreview {
        HomePageTopBar(
            onOpenDrawer = {},
            onSearchClick = {},
            onNavigateToPreview = {},
            showNavigationIcon = false
        )
    }
}
