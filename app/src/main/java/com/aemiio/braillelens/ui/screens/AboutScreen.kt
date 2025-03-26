package com.aemiio.braillelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aemiio.braillelens.R
import com.aemiio.braillelens.ui.components.about.BrailleGradesSection
import com.aemiio.braillelens.ui.components.about.BrailleSection
import com.aemiio.braillelens.ui.components.about.FilipinoBrailleSection
import com.aemiio.braillelens.ui.components.about.PurposeSection
import com.aemiio.braillelens.ui.components.about.ResourcesSection


data class MenuItem(
    val title: String,
    val icon: Int,
    val color: Color,
    val description: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
    var modalVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                HeaderSection()
            }

            item {
                GridMenu(onItemClick = { item ->
                    selectedItem = item
                    modalVisible = true
                })
            }
        }
    }

    if (modalVisible && selectedItem != null) {
        ModalBottomSheetContent(
            menuItem = selectedItem!!,
            onDismiss = { modalVisible = false }
        )
    }
}

@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.braille_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(70.dp),
            contentScale = ContentScale.Fit
        )

        Text("Braille-Lens", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("Making Braille accessible for everyone", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun GridMenu(onItemClick: (MenuItem) -> Unit) {
    val menuItems = listOf(
        MenuItem("Braille", R.drawable.ic_braille, Color(0xFFFFDEAB)),
        MenuItem("Filipino Braille", R.drawable.ic_filipino, Color(0xFFD3DB9B)),
        MenuItem("Braille Grades", R.drawable.ic_grades, Color(0xFFf7e1d7)),
        MenuItem("Purpose", R.drawable.ic_purpose, Color(0xFFd8e2dc)),
        MenuItem("Team", R.drawable.ic_team, Color(0xFFBE9DAF)),
        MenuItem("Resources", R.drawable.ic_resources, Color(0xFFBBAAA0))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        menuItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onItemClick(item) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = item.color)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = item.title,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = null,
                                tint = Color(0xB4131010),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .size(60.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetContent(menuItem: MenuItem, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 0.dp)
        ) {
            item {
                Text(
                    text = menuItem.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                when (menuItem.title) {
                    "Team" -> TeamSection()
                    "Braille" -> BrailleSection()
                    "Filipino Braille" -> FilipinoBrailleSection()
                    "Braille Grades" -> BrailleGradesSection()
                    "Purpose" -> PurposeSection()
                    "Resources" -> ResourcesSection()
                    else -> {
                        Text(
                            text = "Content for ${menuItem.title} section",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun TeamSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Meet Our Team",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )


        TeamMemberCard(
            name = "Jyra Mae Celajes",
            role = "Developer",
            imageRes = R.drawable.celajes,
            backgroundColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(12.dp))

        TeamMemberCard(
            name = "Crestalyn Luardo",
            role = "Designer",
            imageRes = R.drawable.luardo,
            backgroundColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(12.dp))

        TeamMemberCard(
            name = "Louie Jenn Jaspe",
            role = "Researcher",
            imageRes = R.drawable.jaspe,
            backgroundColor = MaterialTheme.colorScheme.onSecondaryContainer
        )


    }
}

@Composable
fun TeamMemberCard(
    name: String,
    role: String,
    imageRes: Int,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Profile image of $name",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))


            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (role) {
                        "Developer" -> Color(0xFFFFDEAB)
                        "Researcher" -> Color(0xFFD3DB9B)
                        "Designer" -> Color(0xFFf7e1d7)
                        else -> Color(0xFFD3DB9B)
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = role,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
    }
}
