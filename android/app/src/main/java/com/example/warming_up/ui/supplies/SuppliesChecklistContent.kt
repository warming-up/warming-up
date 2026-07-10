package com.example.warming_up.ui.supplies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warming_up.ui.theme.SuppliesMuted
import com.example.warming_up.ui.theme.WarmBlue
import com.example.warming_up.ui.theme.WarmLine
import com.example.warming_up.ui.theme.WarmText
import com.example.warming_up.ui.theme.WarmingupTheme

@Composable
fun SuppliesChecklistContent(modifier: Modifier = Modifier) {
    val supplies = listOf("지갑", "신용카드", "보조배터리", "지훈이 선물", "접이식 우산 · 비 예보")
    val checked = remember { mutableStateListOf(false, false, false, false, false) }
    val checkedCount = checked.count { it }

    Column(
        modifier = modifier.padding(start = 18.dp, end = 18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 9.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "준비물 체크리스트",
                color = WarmText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "$checkedCount / ${supplies.size}",
                color = SuppliesMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(vertical = 4.dp),
        ) {
            supplies.forEachIndexed { index, item ->
                SupplyChecklistItem(
                    text = item,
                    checked = checked[index],
                    onClick = { checked[index] = !checked[index] },
                )
            }
        }

        Text(
            text = "'약속'을 새로 입력하면 준비물도 함께 바뀔 수 있어요.",
            modifier = Modifier.padding(top = 10.dp, start = 4.dp),
            color = SuppliesMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuppliesChecklistContentPreview() {
    WarmingupTheme {
        SuppliesChecklistContent()
    }
}

@Composable
private fun SupplyChecklistItem(
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (checked) WarmBlue else Color.White)
                .border(
                    width = 2.dp,
                    color = if (checked) WarmBlue else WarmLine,
                    shape = RoundedCornerShape(7.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = text,
            color = WarmText,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
