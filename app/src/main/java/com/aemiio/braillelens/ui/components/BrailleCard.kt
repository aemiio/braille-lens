package com.aemiio.braillelens.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aemiio.braillelens.ui.BrailleLensColors
import com.aemiio.braillelens.utils.TTSManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream



data class BrailleCardData(
    val label: String,
    val braille: String,
    val category: String
)


fun loadBrailleCards(context: Context, grade: String): List<BrailleCardData> {
    val jsonString = readJsonFromAssets(context, "dictionary.json")
    val gson = Gson()
    val type = object : TypeToken<Map<String, Any>>() {}.type
    val dictionary: Map<String, Any> = gson.fromJson(jsonString, type)
    val brailleCards = mutableListOf<BrailleCardData>()

    val gradeData = dictionary[grade] as? Map<*, *> ?: return brailleCards
    gradeData.forEach { (key, value) ->
        if (value is Map<*, *>) {
            val category = (value["_category"] ?: "Unknown") as String
            if (grade == "grade1") {
                value.forEach { (label, braille) ->
                    if (label != "_category" && braille is String) {
                        brailleCards.add(BrailleCardData(label.toString(), braille, category))
                    }
                }
            } else if (grade == "grade2") {
                value.forEach { (subKey, subValue) ->
                    if (subValue is Map<*, *>) {
                        val subCategory = (subValue["_category"] ?: "Unknown") as String
                        subValue.forEach { (label, braille) ->
                            if (label != "_category" && braille is String) {
                                brailleCards.add(BrailleCardData(label.toString(), braille, subCategory))
                            }
                        }
                    }
                }
            }
        }
    }

    return brailleCards
}


fun readJsonFromAssets(context: Context, fileName: String): String {
    return try {
        val inputStream: InputStream = context.assets.open(fileName)
        inputStream.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}


@Composable
fun BrailleCard(card: BrailleCardData) {
    val context = LocalContext.current
    val ttsManager = remember { TTSManager.getInstance(context) }
    val isTTSReady by ttsManager.isTTSReady.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = card.braille,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Meaning: ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = card.label,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Category: ",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = card.category,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Braille", card.braille)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Braille copied to clipboard", Toast.LENGTH_SHORT)
                            .show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrailleLensColors.darkOlive,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Copy")
                }

                Button(
                    onClick = { ttsManager.speak(card.label) },
                    enabled = isTTSReady,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrailleLensColors.darkOlive,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Speak")
                }
            }
        }
    }
}

