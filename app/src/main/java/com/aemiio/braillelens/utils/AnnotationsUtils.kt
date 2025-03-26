package com.aemiio.braillelens.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aemiio.braillelens.ui.screens.DetectedBox
import com.aemiio.braillelens.ui.screens.ResizeHandle
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

// Utility functions for box manipulation and interaction
fun isPointInBox(point: Offset, box: DetectedBox): Boolean {
    return point.x in box.x..(box.x + box.width) &&
            point.y in box.y..(box.y + box.height)
}

fun getResizeHandle(point: Offset, box: DetectedBox, threshold: Float): ResizeHandle {
    val corners = listOf(
        Pair(Offset(box.x, box.y), ResizeHandle.TOP_LEFT),
        Pair(Offset(box.x + box.width, box.y), ResizeHandle.TOP_RIGHT),
        Pair(Offset(box.x, box.y + box.height), ResizeHandle.BOTTOM_LEFT),
        Pair(Offset(box.x + box.width, box.y + box.height), ResizeHandle.BOTTOM_RIGHT)
    )

    val sides = listOf(
        Pair(Offset(box.x + box.width / 2, box.y), ResizeHandle.TOP),
        Pair(Offset(box.x + box.width, box.y + box.height / 2), ResizeHandle.RIGHT),
        Pair(Offset(box.x + box.width / 2, box.y + box.height), ResizeHandle.BOTTOM),
        Pair(Offset(box.x, box.y + box.height / 2), ResizeHandle.LEFT)
    )

    // Check corners
    for ((handlePoint, handleType) in corners) {
        val distance = sqrt(
            (point.x - handlePoint.x).pow(2) + (point.y - handlePoint.y).pow(2)
        )
        if (distance < threshold) return handleType
    }

    // Check sides
    for ((handlePoint, handleType) in sides) {
        val distance = sqrt(
            (point.x - handlePoint.x).pow(2) + (point.y - handlePoint.y).pow(2)
        )
        if (distance < threshold) return handleType
    }

    return ResizeHandle.NONE
}

fun validateAndNormalizeBox(box: DetectedBox): DetectedBox {
    return box.copy(
        x = if (box.width >= 0) box.x else box.x + box.width,
        y = if (box.height >= 0) box.y else box.y + box.height,
        width = abs(box.width),
        height = abs(box.height)
    )
}

@Composable
fun BoxEditControls(
    box: DetectedBox,
    onBoxUpdate: (DetectedBox) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Box Editor", fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { onBoxUpdate(box.copy(y = box.y - 5f)) }) {
                Text("↑")
            }
            Button(onClick = { onBoxUpdate(box.copy(y = box.y + 5f)) }) {
                Text("↓")
            }
            Button(onClick = { onBoxUpdate(box.copy(x = box.x - 5f)) }) {
                Text("←")
            }
            Button(onClick = { onBoxUpdate(box.copy(x = box.x + 5f)) }) {
                Text("→")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { onBoxUpdate(box.copy(width = box.width + 5f)) }) {
                Text("Width +")
            }
            Button(
                onClick = {
                    if (box.width > 10f) {
                        onBoxUpdate(box.copy(width = box.width - 5f))
                    }
                }
            ) {
                Text("Width -")
            }
            Button(onClick = { onBoxUpdate(box.copy(height = box.height + 5f)) }) {
                Text("Height +")
            }
            Button(
                onClick = {
                    if (box.height > 10f) {
                        onBoxUpdate(box.copy(height = box.height - 5f))
                    }
                }
            ) {
                Text("Height -")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeAndClassSelector(
    gradeSelected: String,
    onGradeChange: (String) -> Unit,
    classOptions: List<String>,
    selectedBox: Int?,
    boxes: List<DetectedBox>,
    currentClass: String,
    onClassChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var gradeExpanded by remember { mutableStateOf(false) }
        var classExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = gradeExpanded,
            onExpandedChange = { gradeExpanded = !gradeExpanded }
        ) {
            TextField(
                value = gradeSelected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = gradeExpanded,
                onDismissRequest = { gradeExpanded = false }
            ) {
                listOf("Grade 1", "Grade 2").forEach { grade ->
                    DropdownMenuItem(
                        text = { Text(grade) },
                        onClick = {
                            onGradeChange(grade)
                            gradeExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = classExpanded,
            onExpandedChange = { classExpanded = !classExpanded }
        ) {
            TextField(
                value = if (selectedBox != null) boxes[selectedBox].classId else currentClass,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = classExpanded,
                onDismissRequest = { classExpanded = false }
            ) {
                classOptions.forEach { classOption ->
                    DropdownMenuItem(
                        text = { Text(classOption) },
                        onClick = {
                            onClassChange(classOption)
                            classExpanded = false
                        }
                    )
                }
            }
        }
    }
}