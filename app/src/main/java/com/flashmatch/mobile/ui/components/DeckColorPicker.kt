package com.flashmatch.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val deckSwatches = listOf(
    "#5C6BC0", // Indigo
    "#E57373", // Rose
    "#26A69A", // Teal
    "#FFA726", // Amber
    "#AB47BC", // Purple
    "#66BB6A", // Green
    "#42A5F5", // Blue
    "#EC407A", // Pink
)

@Composable
fun DeckColorPicker(selected: String, onSelect: (String) -> Unit) {
    Column {
        Text(
            text = "Icon Color",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            deckSwatches.forEach { hex ->
                val color = Color(android.graphics.Color.parseColor(hex))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onSelect(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (hex == selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
