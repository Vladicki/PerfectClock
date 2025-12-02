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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider 
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsDialogContent(
    gridConfig: GridLayoutConfig,
    onGridConfigChange: (GridLayoutConfig) -> Unit,
    onClearAlarms: () -> Unit,
    onClearTimers: () -> Unit,
    pageConfig: MutableList<PageConfig>,
    onPageConfigChange: (MutableList<PageConfig>) -> Unit
) {
    var columnsSliderValue by remember { mutableStateOf(gridConfig.columns.toFloat()) }
    var rowsSliderValue by remember { mutableStateOf(gridConfig.rows.toFloat()) }
    
    var showPages by remember { mutableStateOf(false) }
    var showAddPageDialog by remember { mutableStateOf(false) }
    var newPageName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Grid Layout", fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Columns:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Text(
                text = columnsSliderValue.toInt().toString(),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
        
        Slider(
            value = columnsSliderValue,
            onValueChange = { newValue ->
                // Update the local state for immediate visual feedback
                columnsSliderValue = newValue
                
                // For continuous update, use onValueChange
                val newColumns = newValue.toInt()
                onGridConfigChange(gridConfig.copy(columns = newColumns))
            },
            valueRange = 1f..6f,
            // 5 steps between 1 and 6 (for 1, 2, 3, 4, 5, 6)
            steps = 4,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Rows Slider
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rows:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Text(
                text = rowsSliderValue.toInt().toString(),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
        
        Slider(
            value = rowsSliderValue,
            onValueChange = { newValue ->
                rowsSliderValue = newValue
                val newRows = newValue.toInt()
                onGridConfigChange(gridConfig.copy(rows = newRows))
            },
            valueRange = 1f..6f,
            steps = 4,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
        
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
                onCheckedChange = null 
            )
            Text(
                text = "Display Grid Edges",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showPages = !showPages }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showPages) "Hide Pages" else "Pages")
        }

        if (showPages) {
            pageConfig.forEachIndexed { index, page ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (page.isCustom) {
                        IconButton(onClick = {
                            val newConfig = pageConfig.toMutableList().apply { removeAt(index) }
                            onPageConfigChange(newConfig)
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Page")
                        }
                    }
                    Checkbox(
                        checked = page.isEnabled,
                        onCheckedChange = { isChecked ->
                            val newConfig = pageConfig.toMutableList()
                            newConfig[index] = newConfig[index].copy(isEnabled = isChecked)
                            onPageConfigChange(newConfig)
                        }
                    )
                    Text(text = page.title)
                }
            }
            Button(onClick = { showAddPageDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Custom pages")
            }
        }


        if (showAddPageDialog) {
            AlertDialog(
                onDismissRequest = { showAddPageDialog = false },
                title = { Text("Add New Page") },
                text = {
                    OutlinedTextField(
                        value = newPageName,
                        onValueChange = { newPageName = it },
                        label = { Text("Page Name") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newPageName.isNotBlank()) {
                            val newConfig = pageConfig.toMutableList().apply {
                                add(PageConfig(id = UUID.randomUUID().toString(), title = newPageName, isEnabled = true, isCustom = true))
                            }
                            onPageConfigChange(newConfig)
                            newPageName = ""
                            showAddPageDialog = false
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPageDialog = false }) {
                        Text("Cancel")
                    }
                }
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
