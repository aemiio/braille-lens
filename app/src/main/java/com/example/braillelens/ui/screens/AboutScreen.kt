package com.example.braillelens.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.braillelens.R


@Composable
fun AboutScreen() {
    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
    var modalVisible by remember { mutableStateOf(false) }
    val modalAlpha by animateFloatAsState(
        targetValue = if (modalVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        HeaderSection()
        GridMenu(onItemClick = { item ->
            selectedItem = item
            modalVisible = true
        })
    }

    if (modalVisible && selectedItem != null) {
        ModalDialog(selectedItem!!, onClose = { modalVisible = false })
    }
}

@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.braille_logo), contentDescription = "Logo")
        Spacer(modifier = Modifier.height(10.dp))
        Text("Braille-Lens", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text("Making Braille accessible for everyone", fontSize = 16.sp, color = Color.Gray)
    }
}

@Composable
fun GridMenu(onItemClick: (MenuItem) -> Unit) {
    val menuItems = listOf(
        MenuItem("Braille", R.drawable.braille_logo, Color.Blue),
        MenuItem("Filipino Braille", R.drawable.braille_logo, Color.Green),
        MenuItem("Grades", R.drawable.braille_logo, Color.Cyan),
        MenuItem("Team", R.drawable.braille_logo, Color.Magenta)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        menuItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .size(150.dp)
                            .clickable { onItemClick(item) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = item.icon),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = item.title, color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ModalDialog(item: MenuItem, onClose: () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.8f).padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = item.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = getDescription(item.title))
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onClose) {
                    Text("Close")
                }
            }
        }
    }
}

data class MenuItem(val title: String, val icon: Int, val color: Color)

fun getDescription(title: String): String {
    return when (title) {
        "Braille" -> "Braille is a tactile writing system used by visually impaired people."
        "Filipino Braille" -> "Filipino Braille follows Unified English Braille with specific contractions."
        "Grades" -> "Braille has different grades, from Grade 1 (letter-for-letter) to Grade 2 (contractions)."
        "Team" -> "Meet the developers behind Braille-Lens."
        else -> "Information not available."
    }
}
