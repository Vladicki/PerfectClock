package com.griffith.perfectclock

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsDialogContent(
    gridConfig: GridLayoutConfig,
    onGridConfigChange: (GridLayoutConfig) -> Unit
) {
    val gridOptions = listOf(
        "4x5" to (4 to 5),
        "4x6" to (4 to 6),
        "5x6" to (5 to 6)
    )

    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Grid Layout", fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))
        Column(Modifier.selectableGroup()) {
            gridOptions.forEach { (text, dimensions) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (dimensions.first == gridConfig.columns && dimensions.second == gridConfig.rows),
                            onClick = {
                                onGridConfigChange(gridConfig.copy(columns = dimensions.first, rows = dimensions.second))
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (dimensions.first == gridConfig.columns && dimensions.second == gridConfig.rows),
                        onClick = null // null recommended for accessibility with screenreaders
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

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
    }
}
