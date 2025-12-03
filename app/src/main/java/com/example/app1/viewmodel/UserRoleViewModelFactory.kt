package com.example.app1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app1.data.PillboxRepository

/**
 * Factory para crear instancias de UserRoleViewModel, inyectando PillboxRepository.
 * Esto evita el error "Cannot create an instance of class..."
 */
class UserRoleViewModelFactory(
    private val repository: PillboxRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserRoleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserRoleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}