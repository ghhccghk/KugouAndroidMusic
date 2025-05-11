package com.ghhccghk.musicplay.ui.widgets.basic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

@Composable
fun YosWrapper(content: @Composable () -> Unit) =
    key(content.hashCode()) {
        content()
    }