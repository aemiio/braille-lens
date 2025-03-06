package com.example.braillelens.ui.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.braillelens.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionCard(
    navController: NavController? = null,
    context: Context? = null
) {
    val actualContext = context ?: androidx.compose.ui.platform.LocalContext.current
    val sharedPreferences: SharedPreferences =
        actualContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val detectionModes =
        mapOf("GRADE1" to "Grade 1 Braille", "GRADE2" to "Grade 2 Braille", "BOTH" to "Both Grades")

    var selectedMode by remember {
        mutableStateOf(
            sharedPreferences.getString(
                "detectionMode",
                "Grade 1 Braille"
            ) ?: "Grade 1 Braille"
        )
    }
    var showSampleDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    fun saveMode(mode: String) {
        selectedMode = mode
        sharedPreferences.edit().putString("detectionMode", mode).apply()
        expanded = false
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.dark_olive)),
        modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recognize Braille", fontSize = 20.sp, color = Color.White)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.White
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedMode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Detection Mode", color = Color.White) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.clickable { expanded = true }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    detectionModes.forEach { (_, value) ->
                        DropdownMenuItem(
                            text = { Text(value) },
                            onClick = { saveMode(value) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    navController?.navigate("capture/$selectedMode") ?: run {
                        // Fallback behavior when navController is null
                    }
                }) {
                    Text("Capture")
                }
                Button(onClick = {
                    navController?.navigate("import/$selectedMode") ?: run {
                        // Fallback behavior when navController is null
                    }
                }) {
                    Text("Import")
                }
                Button(onClick = { showSampleDialog = true }) {
                    Text("Sample")
                }
            }
        }
    }

    if (showSampleDialog) {
        AlertDialog(
            onDismissRequest = { showSampleDialog = false },
            title = { Text("Choose a Sample") },
            text = {
                Column {
                    listOf(R.drawable.braille_logo, R.drawable.braille_logo).forEach { sample ->
                        Image(
                            painter = painterResource(id = sample),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    showSampleDialog = false
                                    navController?.navigate("result/$selectedMode/$sample")
                                }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSampleDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}