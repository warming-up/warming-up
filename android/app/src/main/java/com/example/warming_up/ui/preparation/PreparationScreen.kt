package com.example.warming_up.ui.preparation

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warming_up.data.appointment.Appointment
import com.example.warming_up.data.routine.Routine
import com.example.warming_up.navigation.BottomTab
import com.example.warming_up.ui.component.WarmingupBottomBar
import com.example.warming_up.ui.component.WarmingupHeader
import com.example.warming_up.ui.theme.WarmBackground
import com.example.warming_up.ui.theme.WarmSubText
import com.example.warming_up.ui.theme.WarmingupTheme
import com.example.warming_up.ui.route.RouteEtaUiState
import kotlinx.coroutines.delay

@Composable
fun PreparationScreen(
    modifier: Modifier = Modifier,
    routine: Routine? = null,
    appointment: Appointment? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    destinationName: String = "강남 오피스",
    routeEtaUiState: RouteEtaUiState = RouteEtaUiState(),
    onRequestRouteEta: () -> Unit = {},
    onDestinationClick: () -> Unit = {},
    onResetClick: () -> Unit = {},
    onTabClick: (BottomTab) -> Unit = {},
) {
    val sessionKey = appointment?.let { "appointment:${it.id}" }
        ?: routine?.let { "routine:${it.id}" }
        ?: "empty"
    val timerSteps = remember(sessionKey, appointment, routine) {
        when {
            appointment != null -> appointment.steps
                .sortedBy { it.itemOrder }
                .map {
                    PreparationTimerStep(
                        id = it.id,
                        name = it.name,
                        durationSeconds = it.durationMinutes.toDurationSeconds(),
                        isInitiallyCompleted = it.completed,
                    )
                }
            routine != null -> routine.steps
                .sortedBy { it.itemOrder }
                .map {
                    PreparationTimerStep(
                        id = it.id,
                        name = it.name,
                        durationSeconds = it.durationMinutes.toDurationSeconds(),
                        isInitiallyCompleted = false,
                    )
                }
            else -> emptyList()
        }
    }
    var completedStepIds by remember(sessionKey) {
        mutableStateOf(timerSteps.filter { it.isInitiallyCompleted }.map { it.id }.toSet())
    }
    var remainingSeconds by remember(sessionKey) { mutableStateOf(0) }
    val currentStep = timerSteps.firstOrNull { it.id !in completedStepIds }
    val nextStep = currentStep?.let { step ->
        timerSteps.dropWhile { it.id != step.id }.drop(1).firstOrNull { it.id !in completedStepIds }
    }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_ALARM, 100) }

    fun playStepAlarm() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 700)
    }

    fun completeStep(stepId: Long) {
        if (stepId !in completedStepIds) {
            playStepAlarm()
            completedStepIds = completedStepIds + stepId
        }
    }

    fun completeCurrentStep() {
        currentStep?.let { completeStep(it.id) }
    }

    DisposableEffect(toneGenerator) {
        onDispose { toneGenerator.release() }
    }

    LaunchedEffect(sessionKey, currentStep?.id) {
        val step = currentStep ?: run {
            remainingSeconds = 0
            return@LaunchedEffect
        }

        remainingSeconds = step.durationSeconds
        while (remainingSeconds > 0) {
            delay(1_000)
            remainingSeconds -= 1
        }
        completeStep(step.id)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmBackground,
        topBar = { WarmingupHeader(onResetClick = onResetClick) },
        bottomBar = {
            WarmingupBottomBar(
                selectedTab = BottomTab.Now,
                onTabClick = onTabClick,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 8.dp,
                bottom = 18.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                when {
                    appointment != null -> CurrentPreparationCard(
                        stepName = currentStep?.name ?: "준비 완료",
                        remainingSeconds = remainingSeconds,
                        progressFraction = currentStep?.progressFraction(remainingSeconds) ?: 0f,
                        nextStepName = nextStep?.name,
                        isCompleteEnabled = currentStep != null,
                        onCompleteClick = ::completeCurrentStep,
                    )
                    isLoading -> StatusText(text = "루틴을 불러오는 중입니다.")
                    errorMessage != null -> StatusText(text = errorMessage)
                    routine == null -> StatusText(text = "등록된 루틴이 없습니다.")
                    else -> CurrentPreparationCard(
                        stepName = currentStep?.name ?: "준비 완료",
                        remainingSeconds = remainingSeconds,
                        progressFraction = currentStep?.progressFraction(remainingSeconds) ?: 0f,
                        nextStepName = nextStep?.name,
                        isCompleteEnabled = currentStep != null,
                        onCompleteClick = ::completeCurrentStep,
                    )
                }
            }
            item {
                PreparationProgressSummary(
                    totalDurationMinutes = timerSteps.sumOf { it.durationSeconds } / 60,
                    completedStepCount = completedStepIds.size,
                    totalStepCount = timerSteps.size,
                )
            }
            item {
                DestinationSummaryCard(
                    appointment = appointment,
                    destinationName = destinationName,
                    routeEtaUiState = routeEtaUiState,
                    onRequestRouteEta = onRequestRouteEta,
                    onDestinationClick = onDestinationClick,
                )
            }
            item {
                PreparationStepSection(
                    routineSteps = routine?.steps.orEmpty(),
                    appointmentSteps = appointment?.steps.orEmpty(),
                    completedStepIds = completedStepIds,
                    currentStepId = currentStep?.id,
                    onCurrentStepClick = ::completeCurrentStep,
                )
            }
        }
    }
}

@Composable
private fun StatusText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 24.dp),
        color = WarmSubText,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

private data class PreparationTimerStep(
    val id: Long,
    val name: String,
    val durationSeconds: Int,
    val isInitiallyCompleted: Boolean,
)

private fun Int.toDurationSeconds(): Int = coerceAtLeast(0) * 60

private fun PreparationTimerStep.progressFraction(remainingSeconds: Int): Float {
    if (durationSeconds <= 0) return 0f

    return remainingSeconds.coerceIn(0, durationSeconds).toFloat() / durationSeconds
}

@Preview(showBackground = true)
@Composable
private fun PreparationScreenPreview() {
    WarmingupTheme {
        PreparationScreen()
    }
}
