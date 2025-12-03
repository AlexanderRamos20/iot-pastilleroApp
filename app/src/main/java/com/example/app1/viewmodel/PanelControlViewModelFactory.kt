// Archivo: com.example.app1.viewmodel.PanelControlViewModelFactory.kt
package com.example.app1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app1.data.PillboxRepository

class PanelControlViewModelFactory(
    private val repository: PillboxRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PanelControlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PanelControlViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}