package com.example.app1.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.snapshots
import com.example.app1.model.Patient
import com.example.app1.model.Device
import com.example.app1.model.EventLog
import com.example.app1.model.Reading
import com.example.app1.model.ScheduleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception

/**
 * Repositorio para la gestión de datos relacionados con el dispensador de pastillas (Pillbox).
 * Es el puente entre los ViewModels y Firebase Firestore.
 */
class PillboxRepository(
    private val firestore: FirebaseFirestore
) {

    // --- LÓGICA DE REGISTRO DE DISPOSITIVOS ---

    /**
     * Obtiene la lista de usuarios con rol 'Paciente' para el dropdown de vinculación.
     */
    fun getPatients(): Flow<List<Patient>> = flow {
        try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("rol", "Paciente") // Filtra por campo "rol" en español
                .get()
                .await()

            val patients = snapshot.documents.map { doc ->
                // Usa el campo "nombre_completo" de la BD
                Patient(doc.id, doc.getString("nombre_completo") ?: "Paciente Desconocido")
            }
            emit(patients)
        } catch (e: Exception) {
            println("Error al obtener la lista de pacientes: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * Vincula un dispositivo (crea o actualiza) y asigna cuidador/paciente,
     * e inicializa las colecciones secundarias necesarias.
     */
    suspend fun linkDevice(deviceId: String, patientUid: String, deviceName: String) {

        // Verifica que el usuario actual (cuidador) esté autenticado
        val caregiverUid = AuthRepository.getCurrentUserUid()
            ?: throw Exception("Error de Autenticación: Sesión inválida o expirada. No se puede vincular el dispositivo.")

        val now = Timestamp.now()

        // 1. Datos de Vinculación para la colección 'devices'
        val deviceData = hashMapOf(
            "caregiver_uid" to caregiverUid,
            "patient_uid" to patientUid,
            "name" to deviceName
        )

        // 2. Datos de Inicialización para las sub-colecciones (alineados con el firmware)
        val readingsData = hashMapOf(
            "temp" to 25.0, "humidity" to 50.0, "weight" to 0.0, "last_updated" to now
        )
        val schedulesData = hashMapOf(
            "times" to listOf("08:00", "20:00"), "pill_weight_g" to 0.5
        )
        val eventsData = hashMapOf(
            "initializedAt" to now
        )

        // 3. Ejecutar Escrituras Atómicas
        try {
            firestore.collection("devices").document(deviceId).set(deviceData).await()
            firestore.collection("readings").document(deviceId).set(readingsData).await()
            firestore.collection("schedules").document(deviceId).set(schedulesData).await()
            firestore.collection("events").document(deviceId).set(eventsData).await()
        } catch (e: Exception) {
            throw Exception("Error al escribir los datos de inicialización del dispositivo en Firestore: ${e.message}")
        }
    }

    // --- LÓGICA DEL PANEL DE CONTROL (Listado de Dispositivos) ---

    /**
     * Obtiene dispositivos donde el UID del usuario es el Cuidador O el Paciente,
     * realizando dos consultas separadas y combinando los resultados.
     */
    fun getDevicesByUser(userUid: String): Flow<List<Device>> = flow {
        coroutineScope {

            // 1. Consulta A: Dispositivos donde el usuario es el Cuidador
            val caregiverQuery = async {
                firestore.collection("devices")
                    .whereEqualTo("caregiver_uid", userUid)
                    .get()
                    .await()
            }

            // 2. Consulta B: Dispositivos donde el usuario es el Paciente
            val patientQuery = async {
                firestore.collection("devices")
                    .whereEqualTo("patient_uid", userUid)
                    .get()
                    .await()
            }

            // Esperar ambas consultas y combinar los documentos, eliminando duplicados por ID
            val combinedDocs = (caregiverQuery.await().documents + patientQuery.await().documents).distinctBy { it.id }

            // 3. Mapear y enriquecer el modelo con el nombre del paciente
            val allDevices = combinedDocs.map { doc ->
                val patientUid = doc.getString("patient_uid") ?: "N/A"
                val patientName = getUserFullName(patientUid) // Búsqueda adicional de nombre

                Device(
                    deviceId = doc.id,
                    name = doc.getString("name") ?: "Dispositivo sin nombre",
                    patientUid = patientUid,
                    patientName = patientName // Asignación del nombre del paciente
                )
            }

            emit(allDevices)
        }
    }

    /**
     * Función auxiliar para obtener el nombre completo de un usuario dado su UID desde la colección 'users'.
     */
    suspend fun getUserFullName(uid: String): String {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            // Se asume que el campo en la BD es 'nombre_completo'
            doc.getString("nombre_completo") ?: "Paciente Desconocido"
        } catch (e: Exception) {
            // Devuelve un mensaje de error si la carga falla
            "Error al cargar nombre"
        }
    }

    // --- LÓGICA DE GESTIÓN DE HORARIOS Y CONFIGURACIÓN ---

    /**
     * Obtiene la configuración de horarios en tiempo real para un dispositivo.
     */
    fun getScheduleConfig(deviceId: String): Flow<ScheduleConfig> = flow {
        try {
            // Usa snapshots para escuchar cambios en tiempo real (Firestore Flow)
            firestore.collection("schedules").document(deviceId)
                .snapshots()
                .collect { snapshot ->
                    val config = ScheduleConfig(
                        pill_weight_g = snapshot.getDouble("pill_weight_g") ?: 0.5,
                        times = snapshot.get("times") as? List<String> ?: emptyList()
                    )
                    emit(config)
                }
        } catch (e: Exception) {
            println("Error al escuchar la configuración de horarios: ${e.message}")
            // Emitir una configuración por defecto en caso de error
            emit(ScheduleConfig())
        }
    }

    /**
     * Guarda la configuración de horarios y peso
     */
    suspend fun saveScheduleConfig(deviceId: String, config: ScheduleConfig) {
        AuthRepository.getCurrentUserUid() // Verifica que el usuario esté logueado
            ?: throw Exception("Usuario no autenticado. Falló el permiso de escritura.")

        val now = Timestamp.now()

        firestore.collection("schedules").document(deviceId)
            .update(
                mapOf(
                    "pill_weight_g" to config.pill_weight_g,
                    "times" to config.times, // Firestore maneja la actualización del array
                    "last_updated_app" to now
                )
            )
            .await()
    }

    /**
     * Obtiene el rol del usuario autenticado actual (usado en HomeScreen).
     */
    suspend fun getUserRole(uid: String): String? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.getString("rol")
        } catch (e: Exception) { null }
    }
    suspend fun getLatestReading(deviceId: String): Reading {
        return try {
            val snapshot = firestore.collection("readings").document(deviceId).get().await()
            Reading(
                temp = snapshot.getDouble("temp") ?: 0.0,
                humidity = snapshot.getDouble("humidity") ?: 0.0,
                weight = snapshot.getDouble("weight") ?: 0.0,
                last_updated = snapshot.getTimestamp("last_updated")
            )
        } catch (e: Exception) {
            Reading(temp = -1.0) // Devuelve error o valores por defecto en caso de falla.
        }
    }

    suspend fun getRecentLogs(deviceId: String): List<EventLog> {
        return try {
            val snapshot = firestore.collection("events").document(deviceId)
                .collection("logs")
                // Filtro para mostrar los 5 logs más recientes
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()

            snapshot.documents.map { doc ->
                EventLog(
                    type = doc.getString("type") ?: "DESCONOCIDO",
                    description = doc.getString("description") ?: "N/A",
                    timestamp = doc.getTimestamp("timestamp")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}