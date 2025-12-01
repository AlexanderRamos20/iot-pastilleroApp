package com.example.app1.di

import com.example.app1.data.AuthRepository
import com.example.app1.data.PillboxRepository
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Singleton para centralizar la creación de Repositorios e inyectar sus dependencias.
 */
object AppDependencies {
    // ⚠️ Asumimos que Firebase ya está inicializado en la aplicación.
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // AuthRepository es un objeto Singleton (ya está definido en tu código).
    val authRepository: AuthRepository by lazy { AuthRepository }

    // Instancia de PillboxRepository, solo requiere Firestore.
    val pillboxRepository: PillboxRepository by lazy {
        PillboxRepository(firestore)
    }
}