package com.example.app1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app1.data.AuthRepository
import com.example.app1.data.PillboxRepository // Necesario para la función de obtener el perfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estados posibles del rol
sealed class UserRole {
    object Loading : UserRole()
    data class Found(val role: String) : UserRole() // "Cuidador" o "Paciente"
    object NotFound : UserRole() // Usuario logueado pero perfil no encontrado
}

class UserRoleViewModel(
    private val repository: PillboxRepository
) : ViewModel() {

    private val _userRoleState = MutableStateFlow<UserRole>(UserRole.Loading)
    val userRoleState: StateFlow<UserRole> = _userRoleState

    init {
        fetchUserRole()
    }

    private fun fetchUserRole() {
        viewModelScope.launch {
            val currentUid = AuthRepository.getCurrentUserUid()
            if (currentUid == null) {
                _userRoleState.value = UserRole.NotFound
                return@launch
            }

            // 💡 Asumimos un nuevo método en PillboxRepository para obtener el rol
            val role = repository.getUserRole(currentUid)

            if (role != null) {
                _userRoleState.value = UserRole.Found(role)
            } else {
                _userRoleState.value = UserRole.NotFound
            }
        }
    }
}