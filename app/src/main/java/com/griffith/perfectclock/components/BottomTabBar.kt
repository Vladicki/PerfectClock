package com.griffith.perfectclock.components

import android.R.attr.bottom
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BottomTabBar(
    pagerState: androidx.compose.foundation.pager.PagerState,
    titles: List<String>
) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier = Modifier.height(70.dp)
                .padding(bottom = 30.dp),
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                text = { Text(title) },
                modifier = if (pagerState.currentPage == index) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else Modifier
            )
        }
    }
}
