package com.griffith.perfectclock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimerDialog(
    onStart: (hours: Int, minutes: Int, seconds: Int) -> Unit,
    onClose: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    fun parseTime(): Triple<Int, Int, Int> {
        val padded = inputText.padStart(6, '0').takeLast(6)
        val hours = padded.substring(0, 2).toIntOrNull() ?: 0
        val minutes = padded.substring(2, 4).toIntOrNull() ?: 0
        val seconds = padded.substring(4, 6).toIntOrNull() ?: 0
        return Triple(hours, minutes, seconds)
    }

    val (hours, minutes, seconds) = parseTime()

    AlertDialog(
        onDismissRequest = { onClose() },
        confirmButton = {},
        title = { Text("Set Timer", style = MaterialTheme.typography.titleLarge) },
        modifier = Modifier
            .width(380.dp)
            .height(600.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // TIME DISPLAY WITH RIGHT-HIGHLIGHTED INPUT
                val paddedInput = inputText.padStart(6, '0').takeLast(6)
                val totalLength = paddedInput.length

                val annotatedTime = buildAnnotatedString {
                    paddedInput.forEachIndexed { i, char ->
                        val isHighlighted = i >= 6 - totalLength
                        withStyle(
                            style = SpanStyle(
                                color = if (isHighlighted) Color.White else Color.Gray,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(char)
                        }
                        // Append h/m/s after every 2 digits
                        if (i == 1) withStyle(SpanStyle(color = Color.Gray, fontSize = 20.sp)) { append("h ") }
                        if (i == 3) withStyle(SpanStyle(color = Color.Gray, fontSize = 20.sp)) { append("m ") }
                        if (i == 5) withStyle(SpanStyle(color = Color.Gray, fontSize = 20.sp)) { append("s") }
                    }
                }

                Text(
                    text = annotatedTime,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // NUMPAD
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("00", "0", "⌫")
                )

                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        for (key in row) {
                            Button(
                                onClick = {
                                    when (key) {
                                        "⌫" -> if (inputText.isNotEmpty()) inputText =
                                            inputText.dropLast(1)
                                        "00" -> if (inputText.isNotEmpty() && inputText.length < 6) inputText += "00"
                                        else -> if (inputText.length < 6 && !(inputText.isEmpty() && key == "0")) inputText += key
                                    }
                                },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(60.dp)
                            ) {
                                Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ACTION BUTTONS
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onClose() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .height(70.dp)
                    ) {
                        // Text("X", color = Color.White, fontSize = 18.sp)
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "",
                            tint = Color.White,
                        )

                    }

                    val startEnabled = inputText.isNotEmpty()
                    Button(
                        onClick = {
                            val (h, m, s) = parseTime()
                            if (h + m + s > 0) onStart(h, m, s)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (startEnabled) Color(0xFF81C784) else Color.Gray
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .height(70.dp),
                        enabled = startEnabled
                    ) {
                        Text("▶", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}
