package com.example.braillelens.ui.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.braillelens.R
import com.example.braillelens.ui.BrailleLensColors
import com.example.braillelens.utils.WindowType
import com.example.braillelens.utils.rememberWindowSize

@Composable
fun RecognitionCard(
    navController: NavController? = null,
    context: Context? = null
) {
    val windowSize = rememberWindowSize()
    when (windowSize.height) {
        WindowType.Compact -> {
            MediumRecognitionCard(navController = navController, context = context)
        }

        else -> {
            MediumRecognitionCard(navController = navController, context = context)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediumRecognitionCard(
    navController: NavController? = null,
    context: Context? = null
) {
    val actualContext = context ?: LocalContext.current
    val sharedPreferences: SharedPreferences =
        actualContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val detectionModes = listOf("Grade 1 Braille", "Grade 2 Braille", "Both Grades")

    var selectedMode by remember {
        mutableStateOf(
            sharedPreferences.getString("detectionMode", detectionModes[0]) ?: detectionModes[0]
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
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.pastel_green)),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Recognize Braille",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = Color.Black.copy(alpha = 0.7f)
                )

                // Detection Mode and Tagalog text in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dropdown Menu
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(0.7f)
                    ) {
                        OutlinedTextField(
                            value = selectedMode,
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text(
                                    "Detection Mode",
                                    color = Color.Black.copy(alpha = 0.9f)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Toggle Dropdown",
                                    tint = Color.Black
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.7f),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(BrailleLensColors.lightCream)
                                .width(IntrinsicSize.Max)
                        ) {
                            detectionModes.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode) },
                                    onClick = { saveMode(mode) }
                                )
                            }
                        }
                    }

                    // Icon and Tagalog text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = "Arrow Right",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Tagalog",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val buttonModifier = Modifier
                        .weight(1f)
                        .height(90.dp)

                    // Capture Button
                    Button(
                        onClick = { navController?.navigate("capture/$selectedMode") },
                                shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(
                            horizontal = 4.dp,
                            vertical = 0.dp
                        ),  // Removed vertical padding
                        colors = ButtonDefaults.buttonColors(containerColor = BrailleLensColors.darkOlive),
                        modifier = buttonModifier
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,  // This centers the content vertically
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.camera_24px),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = colorResource(id = R.color.font_white)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Capture",
                                fontSize = 12.sp,
                                color = BrailleLensColors.fontWhite,
                                softWrap = false
                            )
                        }
                    }

                    // Import Button
                    Button(
                        onClick = { navController?.navigate("import/$selectedMode") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(
                            horizontal = 4.dp,
                            vertical = 0.dp
                        ),  // Removed vertical padding
                        colors = ButtonDefaults.buttonColors(containerColor = BrailleLensColors.darkOlive),
                        modifier = buttonModifier
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,  // This centers the content vertically
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.image_24px),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = colorResource(id = R.color.font_white)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Import",
                                fontSize = 12.sp,
                                color = BrailleLensColors.fontWhite,
                                softWrap = false
                            )
                        }
                    }

                    // Sample Button
                    Button(
                        onClick = { showSampleDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(
                            horizontal = 4.dp,
                            vertical = 0.dp
                        ),  // Removed vertical padding
                        colors = ButtonDefaults.buttonColors(containerColor = BrailleLensColors.darkOlive),
                        modifier = buttonModifier
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,  // This centers the content vertically
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.collections_24px),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = colorResource(id = R.color.font_white)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sample",
                                fontSize = 12.sp,
                                color = BrailleLensColors.fontWhite,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Add InfoPopover at the top right corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                InfoPopover(
                    title = "Detection Modes",
                    infoItems = listOf(
                        "Grade 1 Braille" to "Includes alphabets, composition signs, and digits.",
                        "Grade 2 Braille" to "Includes one-cell and two-cell contractions.",
                        "Both Grades" to "Detects and translates both Grade 1 and Grade 2 Braille."
                    )
                )
            }
        }
    }

    if (showSampleDialog) {
        AlertDialog(
            onDismissRequest = { showSampleDialog = false },
            title = { Text("Choose a Sample", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            R.drawable.sample1,
                            R.drawable.sample2,
                            R.drawable.sample3
                        ).forEachIndexed { index, sample ->
                            SampleItem(sample, index + 1) {
                                showSampleDialog = false
                                navController?.navigate("sample/$selectedMode/$sample")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            R.drawable.sample4,
                            R.drawable.sample5
                        ).forEachIndexed { index, sample ->
                            SampleItem(sample, index + 4) {
                                showSampleDialog = false
                                navController?.navigate("sample/$selectedMode/$sample")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSampleDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrailleLensColors.darkOlive)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SampleItem(
    sampleRes: Int,
    index: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = sampleRes),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(6.dp)
        )
        Text("Sample $index", fontSize = 14.sp)
    }
}
