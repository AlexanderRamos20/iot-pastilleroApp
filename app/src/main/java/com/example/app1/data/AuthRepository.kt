package com.example.app1.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await // Para manejar las Tasks de Firebase con Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio encargado de todas las operaciones de Autenticación (Auth) y Perfil (Firestore).
 * Utiliza Coroutines para operaciones asíncronas.
 */
object AuthRepository {

    // Instancias de Firebase (usando la sintaxis KTX: Firebase.auth)
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    /**
     * Registra un nuevo usuario en Firebase Authentication y guarda su perfil en Firestore.
     * @return Result.success(Unit) si es exitoso, o Result.failure(Exception) si falla.
     */
    suspend fun registerUser(
        email: String,
        password: String,
        fullName: String,
        role: String
    ): Result<Unit> = withContext(Dispatchers.IO) { // Ejecuta en un hilo optimizado para I/O

        // --- 1. Validaciones de Negocio ---
        if (password.length < 6) {
            return@withContext Result.failure(Exception("La contraseña debe tener al menos 6 caracteres."))
        }

        try {
            // --- 2. REGISTRO EN AUTHENTICATION ---
            // Llama a la API de Firebase Auth de forma asíncrona usando .await()
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Error al obtener el ID de usuario (UID).")

            // --- 3. GUARDAR PERFIL EN FIRESTORE ---
            // Creamos un mapa (similar a un objeto JSON) con la información del usuario
            val userProfile = hashMapOf(
                "nombre_completo" to fullName,
                "rol" to role,
                "correo" to email,
                "fecha_registro" to System.currentTimeMillis()
            )

            // Crea un nuevo documento en la colección "usuarios" con el UID como clave
            db.collection("usuarios").document(userId).set(userProfile).await()

            // Si ambas operaciones son exitosas
            return@withContext Result.success(Unit)

        } catch (e: Exception) {
            // --- 4. MANEJO DE ERRORES ---
            // Captura errores específicos de red, contraseña débil, correo duplicado, etc.
            return@withContext Result.failure(e)
        }
    }

    /**
     * Inicia sesión de un usuario existente en Firebase Authentication.
     * @return Result.success(Unit) si es exitoso.
     */
    suspend fun loginUser(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Llama al método de inicio de sesión de Firebase Auth
            auth.signInWithEmailAndPassword(email, password).await()

            // Si la llamada es exitosa, el usuario queda logueado automáticamente
            return@withContext Result.success(Unit)

        } catch (e: Exception) {
            // Maneja errores de login (ej. credenciales inválidas, usuario no encontrado)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Verifica si hay un usuario actualmente autenticado.
     */
    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun logout() {
        auth.signOut()
    }

}