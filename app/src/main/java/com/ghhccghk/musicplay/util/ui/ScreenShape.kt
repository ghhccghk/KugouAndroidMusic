package com.ghhccghk.musicplay.util.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


data class ScreenCornerDataDp(
    val topLeft: Dp,
    val topRight:Dp,
    val bottomLeft: Dp,
    val bottomRight: Dp
)


@Composable
fun rememberScreenCornerDataDp(): ScreenCornerDataDp{
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return remember(windowInfo, density) {
        // Fallback for other platforms
        ScreenCornerDataDp(
            topLeft = 0.dp,
            topRight = 0.dp,
            bottomLeft = 0.dp,
            bottomRight = 0.dp
        )


    }
}