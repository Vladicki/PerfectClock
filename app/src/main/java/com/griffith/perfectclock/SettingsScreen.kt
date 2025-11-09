package com.griffith.perfectclock

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val gridConfigStorage = remember { GridConfigStorage(context) }
    var gridConfig by remember { mutableStateOf(gridConfigStorage.loadGridConfig()) }

    val gridOptions = listOf(
        "4x5" to (4 to 5),
        "4x6" to (4 to 6),
        "5x6" to (5 to 6)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Settings", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        SettingsDialogContent(
            gridConfig = gridConfig,
            onGridConfigChange = { newConfig ->
                gridConfig = newConfig
                gridConfigStorage.saveGridConfig(newConfig)
            },
            onClearAlarms = { /* Handled in MainActivity */ },
            onClearTimers = { /* Handled in MainActivity */ }
        )
    }
}
