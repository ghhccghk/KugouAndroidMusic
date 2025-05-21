package com.ghhccghk.musicplay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PlaylistBottomSheetPreview() {
    val dummyVisible = remember { mutableStateOf(true) } // 模拟显示状态
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (dummyVisible.value) {
        ModalBottomSheet(
            onDismissRequest = {
                dummyVisible.value = false
            },
            sheetState = sheetState
        ) {
            Text(
                "播放列表",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            Divider()
            LazyColumn {
                items(5) { index ->
                    ListItem(
                        headlineContent = { Text("假歌曲 $index") },
                        supportingContent = { Text("假歌手") }
                    )
                }
            }
        }
    }
}