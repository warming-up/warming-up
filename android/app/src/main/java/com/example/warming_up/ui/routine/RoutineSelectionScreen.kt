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
import androidx.compose.foundation.shape.CircleShape
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
import com.example.warming_up.data.routine.RoutineStepInput
import com.example.warming_up.ui.component.WarmingupHeader
import com.example.warming_up.ui.theme.WarmBlue
import com.example.warming_up.ui.theme.WarmLine
import com.example.warming_up.ui.theme.WarmSubText
import com.example.warming_up.ui.theme.WarmText
import com.example.warming_up.ui.theme.WarmingupTheme

private val RoutineBackground = Color(0xFFF0EDE8)
private val FieldBackground = Color.White
private val MutedFieldBackground = Color(0xFFF8F4EF)
private val ControlBackground = Color(0xFFE1EAFF)
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
    onRoutineCreated: () -> Unit = {},
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
    val totalMinutes = steps.sumOf { it.durationMinutes }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = RoutineBackground,
        topBar = {
            WarmingupHeader(showReset = false)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 34.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                RoutineIntro()
            }

            item {
                RoutineNameSection(
                    routineName = routineName,
                    onRoutineNameChange = { routineName = it },
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
                            onSuccess = onRoutineCreated,
                        ) ?: onRoutineCreated()
                    },
                )
            }
        }
    }
}

@Composable
private fun RoutineIntro() {
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Text(
            text = "오늘, 어떤 약속을\n준비할까요?",
            color = WarmText,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "약속을 선택하면 준비 시작 시간을 계산해드려요.",
            color = Color(0xFF92887D),
            fontSize = 16.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RoutineNameSection(
    routineName: String,
    onRoutineNameChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(text = "약속")
        RoundedInput(
            value = routineName,
            onValueChange = onRoutineNameChange,
            placeholder = "루틴 이름",
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
                        onDurationMinus = {
                            onStepDurationChange(step.id, step.durationMinutes - 1)
                        },
                        onDurationPlus = {
                            onStepDurationChange(step.id, step.durationMinutes + 1)
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
    onDurationMinus: () -> Unit,
    onDurationPlus: () -> Unit,
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
        RoundControl(text = "-", onClick = onDurationMinus)
        Text(
            text = "${step.durationMinutes}분",
            modifier = Modifier.width(42.dp),
            color = WarmText,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        RoundControl(text = "+", onClick = onDurationPlus)
        Text(
            text = "×",
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onDelete),
            color = DeleteText,
            fontSize = 32.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Light,
        )
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
        )
        Text(
            text = "×",
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onDelete),
            color = DeleteText,
            fontSize = 32.sp,
            lineHeight = 32.sp,
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
                text = if (isLoading) "루틴 생성 중..." else "루틴 생성하기",
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
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = routineTextFieldColors(containerColor),
    )
}

@Composable
private fun RoundControl(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        color = ControlBackground,
        shape = CircleShape,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = WarmBlue,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
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

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun RoutineSelectionScreenPreview() {
    WarmingupTheme {
        RoutineSelectionScreen()
    }
}
