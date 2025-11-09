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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.griffith.perfectclock.components.AppTopAppBar
import com.griffith.perfectclock.components.BottomTabBar
import com.griffith.perfectclock.ui.theme.PerfectClockTheme
import com.griffith.perfectclock.SettingsDialogContent
import android.os.Build
import androidx.annotation.RequiresApi

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerfectClockTheme {
                val pagerState = rememberPagerState(pageCount = { 3 })
                val titles = listOf("Timers", "Alarms", "Stopwatches")
                var showSettingsDialog by remember { mutableStateOf(false) }
                val context = LocalContext.current
                val gridConfigStorage = remember { GridConfigStorage(context) }
                var gridConfig by remember { mutableStateOf(gridConfigStorage.loadGridConfig()) }

                Scaffold(
                    topBar = {
                        AppTopAppBar(title = titles[pagerState.currentPage], onSettingsClick = { showSettingsDialog = true })
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
                            1 -> AlarmsScreen()
                            2 -> StopwatchesScreen()
                        }
                    }
                }

                if (showSettingsDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            gridConfigStorage.saveGridConfig(gridConfig)
                            showSettingsDialog = false
                        },
                        title = { Text("Settings") },
                        text = {
                            SettingsDialogContent(
                                gridConfig = gridConfig,
                                onGridConfigChange = { newConfig -> gridConfig = newConfig }
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                gridConfigStorage.saveGridConfig(gridConfig)
                                showSettingsDialog = false
                            }) {
                                Text("Close")
                            }
                        }
                    )
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
