// Archivo: com.example.app1.viewmodel.ScheduleViewModelFactory.kt
package com.example.app1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app1.data.PillboxRepository

class ScheduleViewModelFactory(
    private val repository: PillboxRepository,
    private val deviceId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(repository, deviceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}