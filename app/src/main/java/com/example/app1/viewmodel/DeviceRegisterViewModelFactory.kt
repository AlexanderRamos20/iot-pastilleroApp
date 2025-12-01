package com.example.app1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app1.data.PillboxRepository

/**
 * Factory que resuelve el error de Inyección de Dependencias
 * al indicarle a Compose cómo crear el ViewModel.
 */
class DeviceRegisterViewModelFactory(
    private val repository: PillboxRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceRegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceRegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}