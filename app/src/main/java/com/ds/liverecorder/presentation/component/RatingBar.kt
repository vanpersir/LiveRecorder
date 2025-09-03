package com.ds.liverecorder.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    isEditable: Boolean = false
) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i <= rating) Color.Yellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = if (isEditable) {
                    Modifier
                        .size(32.dp)
                        .clickable { onRatingChanged(i) }
                        .padding(2.dp)
                } else {
                    Modifier
                        .size(32.dp)
                        .padding(2.dp)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RatingBarPreview() {
    RatingBar(rating = 3)
}

@Preview(showBackground = true)
@Composable
fun EditableRatingBarPreview() {
    RatingBar(
        rating = 3,
        onRatingChanged = {},
        isEditable = true
    )
}