package com.example.warming_up.ui.preparation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warming_up.data.routine.Routine
import com.example.warming_up.ui.component.StatusChip
import com.example.warming_up.ui.theme.WarmBlue
import com.example.warming_up.ui.theme.WarmBlueDark
import com.example.warming_up.ui.theme.WarmingupTheme

@Composable
fun CurrentPreparationCard(routine: Routine? = null) {
    val currentStep = routine?.steps?.firstOrNull()
    val nextStep = routine?.steps?.drop(1)?.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(WarmBlue, WarmBlueDark),
                ),
            ),
    ) {
        Column(
            modifier = Modifier.padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "지금 할 일",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                StatusChip(text = "진행 중")
            }

            Spacer(modifier = Modifier.height(10.dp))
            CircularStepTimer(
                stepName = currentStep?.name ?: routine?.name ?: "루틴 없음",
                remainingTimeText = currentStep?.durationMinutes?.toTimerText() ?: "0:00",
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            ) {
                Text(
                    text = "이 단계 완료",
                    color = WarmBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = nextStep?.let { "다음 · ${it.name}" } ?: "다음 단계 없음",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrentPreparationCardPreview() {
    WarmingupTheme {
        CurrentPreparationCard()
    }
}

@Composable
private fun CircularStepTimer(
    stepName: String,
    remainingTimeText: String,
) {
    Box(
        modifier = Modifier
            .size(174.dp)
            .border(
                width = 11.dp,
                color = Color.White.copy(alpha = 0.22f),
                shape = CircleShape,
            )
            .border(
                width = 11.dp,
                brush = Brush.sweepGradient(
                    0.0f to Color.White,
                    0.72f to Color.White,
                    0.73f to Color.Transparent,
                    1.0f to Color.Transparent,
                ),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stepName,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = remainingTimeText,
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "이 단계 남은 시간",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun Int.toTimerText(): String = "$this:00"
