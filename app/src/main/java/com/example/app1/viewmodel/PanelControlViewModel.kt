// Archivo: com.example.app1.viewmodel.PanelControlViewModel.kt
package com.example.app1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app1.data.AuthRepository
import com.example.app1.data.PillboxRepository
import com.example.app1.model.Device
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class DeviceListUiState {
    object Loading : DeviceListUiState()
    data class Success(val devices: List<Device>) : DeviceListUiState()
    data class Error(val message: String) : DeviceListUiState()
    object Empty : DeviceListUiState()
}

class PanelControlViewModel(
    private val repository: PillboxRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeviceListUiState>(DeviceListUiState.Loading)
    val uiState: StateFlow<DeviceListUiState> = _uiState

    init {
        fetchDevices()
    }

    private fun fetchDevices() {
        viewModelScope.launch {
            val caregiverUid = AuthRepository.getCurrentUserUid()
            if (caregiverUid == null) {
                _uiState.value = DeviceListUiState.Error("Sesión de Cuidador no válida.")
                return@launch
            }

            repository.getDevicesByCaregiver(caregiverUid)
                .catch { e ->
                    _uiState.value = DeviceListUiState.Error("Error al cargar dispositivos: ${e.message}")
                }
                .collectLatest { devices ->
                    if (devices.isEmpty()) {
                        _uiState.value = DeviceListUiState.Empty
                    } else {
                        _uiState.value = DeviceListUiState.Success(devices)
                    }
                }
        }
    }
}