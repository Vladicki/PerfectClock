package com.griffith.perfectclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.griffith.perfectclock.components.AppTopAppBar
import com.griffith.perfectclock.components.BottomTabBar
import com.griffith.perfectclock.ui.theme.PerfectClockTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerfectClockTheme {
                val pagerState = rememberPagerState(pageCount = { 3 })
                val titles = listOf("Timers", "Alerts", "Stopwatches")

                Scaffold(
                    topBar = {
                        AppTopAppBar(title = titles[pagerState.currentPage])
                    },
                    bottomBar = {
                        BottomTabBar(pagerState = pagerState, titles = titles, )
                    }
                ) { paddingValues ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) { page ->
                        when (page) {
                            0 -> TimersScreen()
                            1 -> AlertsScreen()
                            2 -> StopwatchesScreen()
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PerfectClockTheme {
        // You can add a preview of your main screen here if you want
    }
}
