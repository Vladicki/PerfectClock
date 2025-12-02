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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.griffith.perfectclock.components.AppTopAppBar
import com.griffith.perfectclock.components.BottomTabBar
import com.griffith.perfectclock.ui.theme.PerfectClockTheme
import com.griffith.perfectclock.SettingsDialogContent
import com.griffith.perfectclock.AlarmStorage
import com.griffith.perfectclock.TimerStorage
import com.griffith.perfectclock.CustomPageStorage
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Collections.emptyList
import androidx.compose.runtime.mutableStateListOf
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import com.griffith.perfectclock.AddAlarmDialog
import com.griffith.perfectclock.AlarmsScreen
import com.griffith.perfectclock.Alarm
import com.griffith.perfectclock.AddTimerDialog
import com.griffith.perfectclock.CustomScreen
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request SCHEDULE_EXACT_ALARM permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:$packageName")
                ).also(::startActivity)
            }
        }
        setContent {
            PerfectClockTheme { 
                DragOverlay {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                val pageConfigStorage = remember { PageConfigStorage(context) }
                var pageConfig by remember { mutableStateOf(pageConfigStorage.loadPageConfig()) }
                val enabledPages = remember(pageConfig) { pageConfig.filter { it.isEnabled } }

                val pagerState = rememberPagerState(pageCount = { enabledPages.size })
                val titles = enabledPages.map { it.title }
                var showSettingsDialog by remember { mutableStateOf(false) }
                var showAddSelectionDialog by remember { mutableStateOf(false) }
                var showAddTimerDialog by remember { mutableStateOf(false) }
                var showAddAlarmDialog by remember { mutableStateOf(false) }

                val gridConfigStorage = remember { GridConfigStorage(context) }
                var gridConfig by remember { mutableStateOf(gridConfigStorage.loadGridConfig()) }
                val alarmStorage = remember { AlarmStorage(context) }
                var alarms by remember { mutableStateOf(alarmStorage.loadAlarms()) }
                val timerStorage = remember { TimerStorage(context) }
                var timers by remember { mutableStateOf(timerStorage.loadTimers()) }
                val customPageStorage = remember { CustomPageStorage(context) }
                var customGridItems by remember { mutableStateOf(customPageStorage.loadCustomGridItems()) }

                val onAddCustomGridItem: (GridItem) -> Unit = { newItem ->
                    val updatedList = customGridItems.toMutableList().apply { add(newItem) }
                    customGridItems = updatedList
                    customPageStorage.saveCustomGridItems(updatedList)
                }
                var showClearAlarmsDialog by remember { mutableStateOf(false) }
                var showClearTimersDialog by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        val title = if (enabledPages.isNotEmpty()) titles[pagerState.currentPage] else "PerfectClock"
                        AppTopAppBar(title = title, onSettingsClick = { showSettingsDialog = true })
                    },
                    bottomBar = {
                        if (enabledPages.isNotEmpty()) {
                            BottomTabBar(pagerState = pagerState, titles = titles)
                        }
                    },
                    floatingActionButton = {
                        if (enabledPages.isNotEmpty() && enabledPages[pagerState.currentPage].isCustom) {
                            FloatingActionButton(onClick = { showAddSelectionDialog = true }) {
                                Icon(Icons.Filled.Add, "Add new alarm or timer")
                            }
                        }
                    }
                ) { paddingValues ->
                    if (enabledPages.isNotEmpty()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) { pageIndex ->
                            when (enabledPages[pageIndex].id) {
                                "timers" -> TimersScreen(
                                    gridConfig = gridConfig,
                                    timers = timers,
                                    onUpdateTimer = { updatedTimer ->
                                        val updatedTimers = timers.toMutableList()
                                        val index = updatedTimers.indexOfFirst { it.id == updatedTimer.id }
                                        if (index != -1) {
                                            updatedTimers[index] = updatedTimer
                                            timers = updatedTimers
                                            timerStorage.saveTimers(updatedTimers)
                                        }
                                    },
                                    onAddTimer = { newTimer ->
                                        val updatedTimers = timers.toMutableList().apply { add(newTimer) }
                                        timers = updatedTimers
                                        timerStorage.saveTimers(updatedTimers)
                                    },
                                    onDeleteTimer = { timerToDelete ->
                                        val updatedTimers = timers.toMutableList().apply { remove(timerToDelete) }
                                        timers = updatedTimers
                                        timerStorage.saveTimers(updatedTimers)
                                    }
                                )
                                "alarms" -> AlarmsScreen(
                                    gridConfig = gridConfig,
                                    alarms = alarms,
                                    onUpdateAlarm = { updatedAlarm ->
                                        val updatedAlarms = alarms.toMutableList()
                                        val index = updatedAlarms.indexOfFirst { it.id == updatedAlarm.id }
                                        if (index != -1) {
                                            updatedAlarms[index] = updatedAlarm
                                            alarms = updatedAlarms
                                            alarmStorage.saveAlarms(updatedAlarms)
                                        }
                                    },
                                    onAddAlarm = { newAlarm ->
                                        val updatedAlarms = alarms.toMutableList().apply { add(newAlarm) }
                                        alarms = updatedAlarms
                                        alarmStorage.saveAlarms(updatedAlarms)
                                    },
                                    onDeleteAlarm = { alarmToDelete ->
                                        val updatedAlarms = alarms.toMutableList().apply { remove(alarmToDelete) }
                                        alarms = updatedAlarms
                                        alarmStorage.saveAlarms(updatedAlarms)
                                    }
                                )
                                "stopwatch" -> StopwatchScreen()
                                "custom" -> CustomScreen(
                                    gridConfig = gridConfig,
                                    customGridItems = customGridItems,
                                    onUpdateItem = { updatedItem ->
                                        val updatedList = customGridItems.toMutableList()
                                        val index = updatedList.indexOfFirst { it.id == updatedItem.id }
                                        if (index != -1) {
                                            updatedList[index] = updatedItem
                                            customGridItems = updatedList
                                            customPageStorage.saveCustomGridItems(updatedList)
                                        }
                                    },
                                    onAddItem = { newItem ->
                                        val updatedList = customGridItems.toMutableList().apply { add(newItem) }
                                        customGridItems = updatedList
                                        customPageStorage.saveCustomGridItems(updatedList)
                                    },
                                    onDeleteItem = { itemToDelete ->
                                        val updatedList = customGridItems.toMutableList().apply { remove(itemToDelete) }
                                        customGridItems = updatedList
                                        customPageStorage.saveCustomGridItems(updatedList)
                                    }
                                )
                            }
                        }
                    }
                }

                if (showSettingsDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            gridConfigStorage.saveGridConfig(gridConfig)
                            pageConfigStorage.savePageConfig(pageConfig)
                            showSettingsDialog = false
                        },
                        title = { Text("Settings") },
                        text = {
                            SettingsDialogContent(
                                gridConfig = gridConfig,
                                onGridConfigChange = { newConfig -> gridConfig = newConfig },
                                onClearAlarms = { showClearAlarmsDialog = true },
                                onClearTimers = { showClearTimersDialog = true },
                                pageConfig = pageConfig,
                                onPageConfigChange = { newConfig -> pageConfig = newConfig }
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                gridConfigStorage.saveGridConfig(gridConfig)
                                pageConfigStorage.savePageConfig(pageConfig)
                                showSettingsDialog = false
                            }) {
                                Text("Close")
                            }
                        }
                    )
                }

                if (showClearAlarmsDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearAlarmsDialog = false },
                        title = { Text("Confirm Clear Alarms") },
                        text = { Text("Are you sure you want to clear all alarms?") },
                        confirmButton = {
                            TextButton(onClick = {
                                alarmStorage.saveAlarms(emptyList())
                                alarms = emptyList()
                                showClearAlarmsDialog = false
                            }) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearAlarmsDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showClearTimersDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearTimersDialog = false },
                        title = { Text("Confirm Clear Timers") },
                        text = { Text("Are you sure you want to clear all timers?") },
                        confirmButton = {
                            TextButton(onClick = {
                                timerStorage.saveTimers(emptyList())
                                timers = emptyList() 
                                showClearTimersDialog = false
                            }) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearTimersDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showAddSelectionDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddSelectionDialog = false },
                        title = { Text("Add New") },
                        text = { Text("What would you like to add?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showAddTimerDialog = true // Always show the dialog
                                showAddSelectionDialog = false
                            }) {
                                Text("Add Timer")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showAddAlarmDialog = true // Always show the dialog
                                showAddSelectionDialog = false
                            }) {
                                Text("Add Alarm")
                            }
                        }
                    )
                }

                if (showAddAlarmDialog) {
                    val onAlarmAddAction: (Alarm) -> Unit = { newAlarm ->
                        // Determine whether to add to customGridItems or global alarms
                        if (enabledPages.isNotEmpty() && enabledPages[pagerState.currentPage].id == "custom") {
                            onAddCustomGridItem(newAlarm)
                        } else {
                            val updatedAlarms = alarms.toMutableList().apply { add(newAlarm) }
                            alarms = updatedAlarms
                            alarmStorage.saveAlarms(updatedAlarms)
                            // If not on Custom page, automatically navigate to the Alarms page
                            val alarmsPageIndex = enabledPages.indexOfFirst { it.id == "alarms" }
                            if (alarmsPageIndex != -1) {
                                scope.launch { pagerState.animateScrollToPage(alarmsPageIndex) }
                            }
                        }
                        showAddAlarmDialog = false
                    }
                    AddAlarmDialog(
                        onDismissRequest = { showAddAlarmDialog = false },
                        onAddAlarm = onAlarmAddAction,
                        gridConfig = gridConfig,
                        alarms = alarms
                    )
                }

                if (showAddTimerDialog) {
                    val onTimerAddAction: (hours: Int, minutes: Int, seconds: Int) -> Unit = { hours, minutes, seconds ->
                        val totalSeconds = hours * 3600 + minutes * 60 + seconds
                        if (totalSeconds > 0) {
                            if (enabledPages.isNotEmpty() && enabledPages[pagerState.currentPage].id == "custom") {
                                var newX = 0
                                var newY = 0
                                var found = false

                                // Find first empty spot
                                for (y in 0 until gridConfig.rows) {
                                    for (x in 0 until gridConfig.columns) {
                                        if (!customGridItems.any { it.x == x && it.y == y }) {
                                            newX = x
                                            newY = y
                                            found = true
                                            break
                                        }
                                    }
                                    if (found) break
                                }

                                val newTimer = Timer(
                                    id = UUID.randomUUID().toString(),
                                    initialSeconds = totalSeconds,
                                    remainingSeconds = totalSeconds,
                                    isRunning = true,
                                    x = newX,
                                    y = newY
                                )
                                onAddCustomGridItem(newTimer)
                            } else {
                                var newX = 0
                                var newY = 0
                                var found = false

                                // Find first empty spot
                                for (y in 0 until gridConfig.rows) {
                                    for (x in 0 until gridConfig.columns) {
                                        if (!timers.any { it.x == x && it.y == y }) {
                                            newX = x
                                            newY = y
                                            found = true
                                            break
                                        }
                                    }
                                    if (found) break
                                }

                                val newTimer = Timer(
                                    id = java.util.UUID.randomUUID().toString(),
                                    initialSeconds = totalSeconds,
                                    remainingSeconds = totalSeconds,
                                    isRunning = true,
                                    x = newX,
                                    y = newY
                                )
                                val updatedTimers = timers.toMutableList().apply { add(newTimer) }
                                timers = updatedTimers
                                timerStorage.saveTimers(updatedTimers)
                                // If not on Custom page, automatically navigate to the Timers page
                                val timersPageIndex = enabledPages.indexOfFirst { it.id == "timers" }
                                if (timersPageIndex != -1) {
                                    scope.launch { pagerState.animateScrollToPage(timersPageIndex) }
                                }
                            }
                        }
                        showAddTimerDialog = false
                    }
                    AddTimerDialog(
                        onClose = { showAddTimerDialog = false },
                        onStart = onTimerAddAction
                    )
                }
            }
        }
    }
    }
}



