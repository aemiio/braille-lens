package com.aemiio.braillelens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aemiio.braillelens.R
import com.aemiio.braillelens.ui.BrailleLensColors

@Composable
fun InfoPopover(
    title: String,
    infoItems: List<Pair<String, String>>
) {
    var showPopover by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(
            onClick = { showPopover = true },
            modifier = Modifier
                .background(
                    color = BrailleLensColors.backgroundGrey.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .size(36.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.info_24px),
                contentDescription = "Info",
                tint = BrailleLensColors.darkOrange
            )
        }

        DropdownMenu(
            expanded = showPopover,
            onDismissRequest = { showPopover = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
                .width(240.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    infoItems.forEach { (itemTitle, description) ->
                        InfoItem(
                            title = itemTitle,
                            description = description
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp
        )
    }
}