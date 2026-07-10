package com.example.warming_up.ui.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.warming_up.data.routine.RoutineRepository
import com.example.warming_up.data.routine.RoutineStepInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutineCreateViewModel(
    private val routineRepository: RoutineRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoutineCreateUiState())
    val uiState: StateFlow<RoutineCreateUiState> = _uiState.asStateFlow()

    fun createRoutine(
        name: String,
        steps: List<RoutineStepInput>,
        checklist: List<String>,
        onSuccess: () -> Unit,
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

        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            routineRepository.createRoutine(trimmedName, validSteps, validChecklist)
                .onSuccess {
                    _uiState.update { state -> state.copy(isLoading = false, errorMessage = null) }
                    onSuccess()
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
        fun factory(routineRepository: RoutineRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(RoutineCreateViewModel::class.java)) {
                        return RoutineCreateViewModel(routineRepository) as T
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
