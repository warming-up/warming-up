package com.example.warming_up.ui.preparation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warming_up.data.routine.RoutineStep
import com.example.warming_up.model.PreparationStep
import com.example.warming_up.ui.theme.WarmBlue
import com.example.warming_up.ui.theme.WarmLine
import com.example.warming_up.ui.theme.WarmSubText
import com.example.warming_up.ui.theme.WarmText
import com.example.warming_up.ui.theme.WarmingupTheme

@Composable
fun PreparationStepSection(steps: List<RoutineStep> = emptyList()) {
    val preparationSteps = steps.mapIndexed { index, step ->
        PreparationStep(
            name = step.name,
            timeText = "${step.durationMinutes}분",
            isRunning = index == 0,
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "준비 순서",
                color = WarmText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "탭해서 완료 표시",
                color = WarmSubText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                if (preparationSteps.isEmpty()) {
                    Text(
                        text = "표시할 준비 단계가 없습니다.",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                        color = WarmSubText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                preparationSteps.forEach { step ->
                    PreparationStepItem(step = step)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreparationStepSectionPreview() {
    WarmingupTheme {
        PreparationStepSection()
    }
}

@Composable
private fun PreparationStepItem(step: PreparationStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .border(
                    width = 2.dp,
                    color = if (step.isRunning) WarmBlue else WarmLine,
                    shape = CircleShape,
                ),
        )

        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.name,
                color = WarmText,
                fontSize = 14.5.sp,
                fontWeight = if (step.isRunning) FontWeight.ExtraBold else FontWeight.SemiBold,
            )
            Text(
                text = step.timeText,
                color = WarmSubText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        if (step.isRunning) {
            Surface(
                color = WarmBlue.copy(alpha = 0.14f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = "진행 중",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    color = WarmBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}
