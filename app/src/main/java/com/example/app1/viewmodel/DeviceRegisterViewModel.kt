package com.example.app1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app1.data.PillboxRepository
import com.example.app1.model.Patient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class DeviceRegisterUiState {
    object Loading : DeviceRegisterUiState()
    data class Success(val patients: List<Patient>) : DeviceRegisterUiState()
    data class Error(val message: String) : DeviceRegisterUiState()
    object Idle : DeviceRegisterUiState()
}

class DeviceRegisterViewModel(
    private val repository: PillboxRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeviceRegisterUiState>(DeviceRegisterUiState.Loading)
    val uiState: StateFlow<DeviceRegisterUiState> = _uiState

    private val _linkStatus = MutableStateFlow<Boolean>(false)
    val linkStatus: StateFlow<Boolean> = _linkStatus

    init {
        fetchPatients()
    }

    private fun fetchPatients() {
        viewModelScope.launch {
            repository.getPatients()
                .catch { e ->
                    _uiState.value = DeviceRegisterUiState.Error("Error al cargar pacientes: ${e.message}")
                }
                .collectLatest { patients ->
                    _uiState.value = DeviceRegisterUiState.Success(patients)
                }
        }
    }

    fun linkDevice(deviceId: String, selectedPatient: Patient?, deviceName: String) {
        if (selectedPatient == null) {
            _uiState.value = DeviceRegisterUiState.Error("Debe seleccionar un paciente.")
            return
        }

        _linkStatus.value = true

        viewModelScope.launch {
            try {
                repository.linkDevice(deviceId, selectedPatient.uid, deviceName)
                _uiState.value = DeviceRegisterUiState.Idle
            } catch (e: Exception) {
                _uiState.value = DeviceRegisterUiState.Error("Fallo al vincular: ${e.message}")
            } finally {
                _linkStatus.value = false
            }
        }
    }

    fun clearError() {
        _uiState.value = DeviceRegisterUiState.Idle
    }
}