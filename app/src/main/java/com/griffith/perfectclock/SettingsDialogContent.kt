package com.griffith.perfectclock

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsDialogContent(
    gridConfig: GridLayoutConfig,
    onGridConfigChange: (GridLayoutConfig) -> Unit,
    onClearAlarms: () -> Unit,
    onClearTimers: () -> Unit
) {
    var columnsText by remember { mutableStateOf(gridConfig.columns.toString()) }
    var rowsText by remember { mutableStateOf(gridConfig.rows.toString()) }

    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Grid Layout", fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = columnsText,
                onValueChange = { newValue ->
                    columnsText = newValue
                    val newColumns = newValue.toIntOrNull() ?: gridConfig.columns
                    onGridConfigChange(gridConfig.copy(columns = newColumns))
                },
                label = { Text("Columns") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Text("x", fontSize = 20.sp)
            OutlinedTextField(
                value = rowsText,
                onValueChange = { newValue ->
                    rowsText = newValue
                    val newRows = newValue.toIntOrNull() ?: gridConfig.rows
                    onGridConfigChange(gridConfig.copy(rows = newRows))
                },
                label = { Text("Rows") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show Grid Edges Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .selectable(
                    selected = gridConfig.showEdges,
                    onClick = { onGridConfigChange(gridConfig.copy(showEdges = !gridConfig.showEdges)) },
                    role = Role.Checkbox
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = gridConfig.showEdges,
                onCheckedChange = null // null recommended for accessibility with screenreaders
            )
            Text(
                text = "Display Grid Edges",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onClearAlarms, modifier = Modifier.fillMaxWidth()) {
            Text("Clear All Alarms")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onClearTimers, modifier = Modifier.fillMaxWidth()) {
            Text("Clear All Timers")
        }
    }
}
