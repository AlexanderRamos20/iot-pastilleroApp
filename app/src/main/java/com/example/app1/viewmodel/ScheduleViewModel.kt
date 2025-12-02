// Archivo: com.example.app1.viewmodel.ScheduleViewModel.kt
package com.example.app1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app1.data.PillboxRepository
import com.example.app1.model.ScheduleConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class ScheduleUiState {
    object Loading : ScheduleUiState()
    data class Success(val config: ScheduleConfig) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
    object Saving : ScheduleUiState()
    object Saved : ScheduleUiState()
}

class ScheduleViewModel(
    private val repository: PillboxRepository,
    private val deviceId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            repository.getScheduleConfig(deviceId)
                .catch { e ->
                    _uiState.value = ScheduleUiState.Error("Error al cargar horarios: ${e.message}")
                }
                .collect { config ->
                    _uiState.value = ScheduleUiState.Success(config)
                }
        }
    }

    fun saveSchedule(times: List<String>, weightText: String) {
        if (_uiState.value is ScheduleUiState.Saving) return

        _uiState.value = ScheduleUiState.Saving

        viewModelScope.launch {
            try {
                val currentConfig = (_uiState.value as? ScheduleUiState.Success)?.config
                val weight = weightText.toDoubleOrNull() ?: currentConfig?.pill_weight_g ?: 0.5

                val configToSave = ScheduleConfig(
                    pill_weight_g = weight,
                    times = times.sorted()
                )

                repository.saveScheduleConfig(deviceId, configToSave)

                _uiState.value = ScheduleUiState.Saved

            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error("Fallo al guardar: ${e.message}")
            }
        }
    }
}