package com.griffith.perfectclock.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
class SecondsPickerState(
    initialSeconds: Int = 0
) {
    var seconds by mutableStateOf(initialSeconds)
}

//@Composable
//fun rememberSecondsPickerState(
//    initialSeconds: Int = 0
//): SecondsPickerState = remember {
//    SecondsPickerState(initialSeconds)
//}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SecondsInput(
    state: SecondsPickerState,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val seconds = (0..59).toList()
    val itemHeight = 48.dp // Height of each item in the picker

    // Scroll to the selected second when the component is first composed
    LaunchedEffect(state.seconds) {
        val initialIndex = seconds.indexOf(state.seconds)
        if (initialIndex != -1) {
            listState.scrollToItem(initialIndex)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(80.dp) 
                .height(150.dp) // Adjust height to show multiple items
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = itemHeight * 2), // Padding to center the selected item
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight()
            ) {
                items(seconds.size) { index ->
                    val second = seconds[index]
                    val isSelected = second == state.seconds
                    Text(
                        text = String.format("%02d", second),
                        fontSize = if (isSelected) 24.sp else 18.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .height(itemHeight)
                            .clickable { state.seconds = second }
                            .wrapContentSize(Alignment.Center)
                    )
                }
            }
        }
        Text("s", modifier = Modifier.padding(start = 4.dp))
    }
}
