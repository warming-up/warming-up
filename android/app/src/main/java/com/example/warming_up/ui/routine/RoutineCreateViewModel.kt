package com.example.warming_up.ui.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.warming_up.data.appointment.Appointment
import com.example.warming_up.data.appointment.AppointmentRepository
import com.example.warming_up.data.routine.RoutineRepository
import com.example.warming_up.data.routine.RoutineStepInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RoutineCreateViewModel(
    private val routineRepository: RoutineRepository,
    private val appointmentRepository: AppointmentRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoutineCreateUiState())
    val uiState: StateFlow<RoutineCreateUiState> = _uiState.asStateFlow()

    fun createRoutine(
        name: String,
        steps: List<RoutineStepInput>,
        checklist: List<String>,
        arrivalTime: String,
        travelMinutes: Int,
        bufferMinutes: Int,
        onSuccess: (Appointment) -> Unit,
    ) {
        val trimmedName = name.trim()
        val validSteps = steps
            .map { it.copy(name = it.name.trim()) }
            .filter { it.name.isNotBlank() }
        val validChecklist = checklist
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "루틴 이름을 입력해주세요.") }
            return
        }

        if (validSteps.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "준비 단계를 하나 이상 입력해주세요.") }
            return
        }

        val todayArrivalTime = buildTodayArrivalDateTime(arrivalTime)
        if (todayArrivalTime.isBlank()) {
            _uiState.update { it.copy(errorMessage = "도착 시간을 HH:mm 형식으로 입력해주세요.") }
            return
        }

        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            routineRepository.createRoutine(trimmedName, validSteps, validChecklist)
                .onSuccess { routine ->
                    appointmentRepository.createAppointment(
                        routineId = routine.id,
                        name = routine.name,
                        arrivalTime = todayArrivalTime,
                        travelMinutes = travelMinutes,
                        bufferMinutes = bufferMinutes,
                    )
                        .onSuccess { appointment ->
                            appointmentRepository.getAppointment(appointment.id)
                                .onSuccess { appointmentDetail ->
                                    _uiState.update { state ->
                                        state.copy(isLoading = false, errorMessage = null)
                                    }
                                    onSuccess(appointmentDetail)
                                }
                                .onFailure {
                                    _uiState.update { state ->
                                        state.copy(
                                            isLoading = false,
                                            errorMessage = "약속은 생성됐지만 상세 조회에 실패했습니다. 잠시 후 다시 시도해주세요.",
                                        )
                                    }
                                }
                        }
                        .onFailure {
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    errorMessage = "루틴은 생성됐지만 약속 생성에 실패했습니다. 약속 시간을 확인해주세요.",
                                )
                            }
                        }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "루틴 생성에 실패했습니다. 입력값을 확인해주세요.",
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(
            routineRepository: RoutineRepository,
            appointmentRepository: AppointmentRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(RoutineCreateViewModel::class.java)) {
                        return RoutineCreateViewModel(
                            routineRepository = routineRepository,
                            appointmentRepository = appointmentRepository,
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}

data class RoutineCreateUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

internal fun buildTodayArrivalDateTime(time: String, now: Calendar = Calendar.getInstance()): String {
    val clockFormatter = SimpleDateFormat("HH:mm", Locale.US).apply {
        isLenient = false
    }
    val trimmedTime = time.trim()
    val position = ParsePosition(0)
    val parsedClock = clockFormatter.parse(trimmedTime, position)
        ?.takeIf { position.index == trimmedTime.length }
        ?: return ""

    val clock = Calendar.getInstance().apply {
        this.time = parsedClock
    }
    val arrival = now.clone() as Calendar
    arrival.set(Calendar.HOUR_OF_DAY, clock.get(Calendar.HOUR_OF_DAY))
    arrival.set(Calendar.MINUTE, clock.get(Calendar.MINUTE))
    arrival.set(Calendar.SECOND, 0)
    arrival.set(Calendar.MILLISECOND, 0)

    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(arrival.time)
}
