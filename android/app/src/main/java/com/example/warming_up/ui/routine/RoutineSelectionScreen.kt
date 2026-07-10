package com.example.warming_up.ui.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warming_up.data.appointment.Appointment
import com.example.warming_up.data.routine.RoutineStepInput
import com.example.warming_up.ui.component.WarmingupHeader
import com.example.warming_up.ui.theme.WarmBackground
import com.example.warming_up.ui.theme.WarmBlue
import com.example.warming_up.ui.theme.WarmLine
import com.example.warming_up.ui.theme.WarmSubText
import com.example.warming_up.ui.theme.WarmText
import com.example.warming_up.ui.theme.WarmingupTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val FieldBackground = Color.White
private val MutedFieldBackground = Color(0xFFF8F4EF)
private val DeleteText = Color(0xFFBDB4A8)

private data class EditableStep(
    val id: Int,
    val name: String,
    val durationMinutes: Int,
)

private data class EditableChecklistItem(
    val id: Int,
    val name: String,
)

@Composable
fun RoutineSelectionScreen(
    modifier: Modifier = Modifier,
    viewModel: RoutineCreateViewModel? = null,
    onRoutineCreated: (Appointment?) -> Unit = {},
) {
    val uiState by viewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(RoutineCreateUiState()) }
    var routineName by rememberSaveable { mutableStateOf("저녁 약속 · 지훈") }
    var steps by remember {
        mutableStateOf(
            listOf(
                EditableStep(id = 1, name = "샤워", durationMinutes = 15),
                EditableStep(id = 2, name = "헤어 · 스킨케어", durationMinutes = 12),
                EditableStep(id = 3, name = "옷 입기", durationMinutes = 8),
                EditableStep(id = 4, name = "준비물 챙기기", durationMinutes = 5),
            ),
        )
    }
    var checklist by remember {
        mutableStateOf(
            listOf(
                EditableChecklistItem(id = 1, name = "지갑"),
                EditableChecklistItem(id = 2, name = "이어폰"),
            ),
        )
    }
    var nextStepId by remember { mutableIntStateOf(5) }
    var nextChecklistId by remember { mutableIntStateOf(3) }
    val defaultArrival = remember { Calendar.getInstance().apply { add(Calendar.MINUTE, 90) } }
    var arrivalTime by rememberSaveable { mutableStateOf(defaultArrival.toTimeInput()) }
    var travelMinutes by rememberSaveable { mutableIntStateOf(30) }
    var bufferMinutes by rememberSaveable { mutableIntStateOf(10) }
    val totalMinutes = steps.sumOf { it.durationMinutes }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmBackground,
        topBar = {
            WarmingupHeader(showReset = false)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 8.dp,
                bottom = 18.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                RoutineIntro()
            }

            item {
                AppointmentInfoSection(
                    routineName = routineName,
                    onRoutineNameChange = { routineName = it },
                    arrivalTime = arrivalTime,
                    onArrivalTimeChange = { arrivalTime = it },
                )
            }

            item {
                RoutineStepSection(
                    totalMinutes = totalMinutes,
                    steps = steps,
                    onStepNameChange = { id, name ->
                        steps = steps.map { step ->
                            if (step.id == id) step.copy(name = name) else step
                        }
                    },
                    onStepDurationChange = { id, duration ->
                        steps = steps.map { step ->
                            if (step.id == id) step.copy(durationMinutes = duration.coerceAtLeast(1)) else step
                        }
                    },
                    onStepDelete = { id ->
                        if (steps.size > 1) {
                            steps = steps.filterNot { it.id == id }
                        }
                    },
                    onStepAdd = {
                        steps = steps + EditableStep(
                            id = nextStepId,
                            name = "",
                            durationMinutes = 5,
                        )
                        nextStepId += 1
                    },
                )
            }

            item {
                ChecklistSection(
                    checklist = checklist,
                    onChecklistNameChange = { id, name ->
                        checklist = checklist.map { item ->
                            if (item.id == id) item.copy(name = name) else item
                        }
                    },
                    onChecklistDelete = { id ->
                        checklist = checklist.filterNot { it.id == id }
                    },
                    onChecklistAdd = {
                        checklist = checklist + EditableChecklistItem(id = nextChecklistId, name = "")
                        nextChecklistId += 1
                    },
                )
            }

            item {
                AppointmentScheduleSection(
                    travelMinutes = travelMinutes,
                    bufferMinutes = bufferMinutes,
                    onTravelMinutesChange = { travelMinutes = it.coerceAtLeast(0) },
                    onBufferMinutesChange = { bufferMinutes = it.coerceAtLeast(0) },
                )
            }

            item {
                SubmitSection(
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage,
                    onSubmit = {
                        viewModel?.createRoutine(
                            name = routineName,
                            steps = steps.map {
                                RoutineStepInput(
                                    name = it.name,
                                    durationMinutes = it.durationMinutes,
                                )
                            },
                            checklist = checklist.map { it.name },
                            arrivalTime = arrivalTime,
                            travelMinutes = travelMinutes,
                            bufferMinutes = bufferMinutes,
                            onSuccess = onRoutineCreated,
                        ) ?: onRoutineCreated(null)
                    },
                )
            }
        }
    }
}

@Composable
private fun AppointmentScheduleSection(
    travelMinutes: Int,
    bufferMinutes: Int,
    onTravelMinutesChange: (Int) -> Unit,
    onBufferMinutesChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(text = "이동 시간 / 여유 시간")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ScheduleTimeRow(
                    title = "이동 시간",
                    durationMinutes = travelMinutes,
                    onDurationChange = onTravelMinutesChange,
                )
                ScheduleTimeRow(
                    title = "여유 시간",
                    durationMinutes = bufferMinutes,
                    onDurationChange = onBufferMinutesChange,
                )
            }
        }
    }
}

@Composable
private fun ScheduleTimeRow(
    title: String,
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp),
            color = WarmText,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        OutlinedTextField(
            value = durationMinutes.toString(),
            onValueChange = { text ->
                text.filter { it.isDigit() }
                    .take(3)
                    .toIntOrNull()
                    ?.let(onDurationChange)
            },
            modifier = Modifier
                .width(86.dp)
                .height(58.dp),
            suffix = {
                Text(
                    text = "분",
                    color = WarmSubText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = WarmText,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = routineTextFieldColors(containerColor = MutedFieldBackground),
        )
    }
}

@Composable
private fun RoutineIntro() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "오늘, 어떤 약속을\n준비할까요?",
            color = WarmText,
            fontSize = 24.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "약속을 선택하면 준비 시작 시간을 계산해드려요.",
            color = Color(0xFF92887D),
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AppointmentInfoSection(
    routineName: String,
    onRoutineNameChange: (String) -> Unit,
    arrivalTime: String,
    onArrivalTimeChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(text = "일정")
        RoundedInput(
            value = routineName,
            onValueChange = onRoutineNameChange,
            placeholder = "일정 이름",
        )
        RoundedInput(
            value = arrivalTime,
            onValueChange = onArrivalTimeChange,
            placeholder = "도착 시간 (HH:mm)",
        )
        Text(
            text = "오늘 일정의 도착 시간을 입력하면 준비 시작 시간이 계산됩니다.",
            color = Color(0xFF92887D),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RoutineStepSection(
    totalMinutes: Int,
    steps: List<EditableStep>,
    onStepNameChange: (Int, String) -> Unit,
    onStepDurationChange: (Int, Int) -> Unit,
    onStepDelete: (Int) -> Unit,
    onStepAdd: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(text = "준비 단계")
            Text(
                text = "총 ${totalMinutes}분",
                color = WarmSubText,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                steps.forEach { step ->
                    StepRow(
                        step = step,
                        onNameChange = { onStepNameChange(step.id, it) },
                        onDurationChange = { duration ->
                            onStepDurationChange(step.id, duration)
                        },
                        onDelete = { onStepDelete(step.id) },
                    )
                }

                AddRowButton(
                    text = "준비 단계 추가",
                    onClick = onStepAdd,
                )
            }
        }
    }
}

@Composable
private fun StepRow(
    step: EditableStep,
    onNameChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = step.name,
            onValueChange = onNameChange,
            modifier = Modifier
                .weight(1f)
                .height(58.dp),
            placeholder = {
                Text(
                    text = "단계명",
                    color = WarmSubText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = WarmText,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
            ),
            colors = routineTextFieldColors(containerColor = MutedFieldBackground),
        )
        OutlinedTextField(
            value = step.durationMinutes.toString(),
            onValueChange = { text ->
                text.filter { it.isDigit() }
                    .take(3)
                    .toIntOrNull()
                    ?.let(onDurationChange)
            },
            modifier = Modifier
                .width(86.dp)
                .height(58.dp),
            suffix = {
                Text(
                    text = "분",
                    color = WarmSubText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = WarmText,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = routineTextFieldColors(containerColor = MutedFieldBackground),
        )
        DeleteRowButton(onClick = onDelete)
    }
}

@Composable
private fun ChecklistSection(
    checklist: List<EditableChecklistItem>,
    onChecklistNameChange: (Int, String) -> Unit,
    onChecklistDelete: (Int) -> Unit,
    onChecklistAdd: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(text = "준비물")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                checklist.forEach { item ->
                    ChecklistRow(
                        item = item,
                        onNameChange = { onChecklistNameChange(item.id, it) },
                        onDelete = { onChecklistDelete(item.id) },
                    )
                }

                AddRowButton(
                    text = "준비물 추가",
                    onClick = onChecklistAdd,
                )
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    item: EditableChecklistItem,
    onNameChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundedInput(
            value = item.name,
            onValueChange = onNameChange,
            placeholder = "준비물 이름",
            modifier = Modifier.weight(1f),
            containerColor = MutedFieldBackground,
            height = 58.dp,
        )
        DeleteRowButton(onClick = onDelete)
    }
}

@Composable
private fun DeleteRowButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "×",
            color = DeleteText,
            fontSize = 24.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Light,
        )
    }
}

@Composable
private fun SubmitSection(
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = WarmBlue),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        ) {
            Text(
                text = if (isLoading) "약속 생성 중..." else "루틴과 약속 생성하기",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color(0xFFD32F2F),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFF92887D),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.ExtraBold,
    )
}

@Composable
private fun RoundedInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    containerColor: Color = FieldBackground,
    keyboardType: KeyboardType = KeyboardType.Text,
    height: androidx.compose.ui.unit.Dp = 72.dp,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        placeholder = {
            Text(
                text = placeholder,
                color = WarmSubText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = WarmText,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = routineTextFieldColors(containerColor),
    )
}

@Composable
private fun AddRowButton(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        color = Color(0xFFF4F0EB),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "+ $text",
                color = WarmBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun routineTextFieldColors(containerColor: Color) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = containerColor,
    unfocusedContainerColor = containerColor,
    focusedBorderColor = WarmBlue,
    unfocusedBorderColor = WarmLine,
    cursorColor = WarmBlue,
    focusedTextColor = WarmText,
    unfocusedTextColor = WarmText,
)

private fun Calendar.toTimeInput(): String {
    val roundedMinute = (get(Calendar.MINUTE) / 10) * 10
    set(Calendar.MINUTE, roundedMinute)
    return SimpleDateFormat("HH:mm", Locale.US).format(time)
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun RoutineSelectionScreenPreview() {
    WarmingupTheme {
        RoutineSelectionScreen()
    }
}
