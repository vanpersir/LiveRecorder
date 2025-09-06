package com.ds.liverecorder.presentation.common.component

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// 定义棕色颜色
val BrownColor = Color(0xFF8B4513) // 棕色

@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit = {},
    @SuppressLint("ModifierParameter")
    modifier: Modifier = Modifier,
    isEditable: Boolean = false
) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            if (isEditable) {
                // 编辑模式下显示所有星级
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = if (i <= rating) BrownColor else BrownColor.copy(alpha = 0.4f), // 选中为棕色，未选中为半透明棕色
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { 
                            // 如果点击已选中的星级，则清空评分，否则设置为对应星级
                            if (i == rating) {
                                onRatingChanged(0)
                            } else {
                                onRatingChanged(i)
                            }
                        }
                        .padding(2.dp)
                )
            } else {
                // 展示模式下只显示选中的星级
                if (i <= rating) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = BrownColor,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(2.dp)
                    )
                }
            }
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