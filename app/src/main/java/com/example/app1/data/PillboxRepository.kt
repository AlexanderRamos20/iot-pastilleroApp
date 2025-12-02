package com.example.app1.data

import com.example.app1.model.Device
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.Timestamp
import com.example.app1.model.Patient // Importamos el modelo correcto
import com.example.app1.model.ScheduleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class PillboxRepository(
    // 💡 Simplificado: Solo requiere Firestore.
    private val firestore: FirebaseFirestore
) {

    fun getPatients(): Flow<List<Patient>> = flow {
        val snapshot = firestore.collection("users")
            .whereEqualTo("rol", "Paciente")
            .get()
            .await()

        val patients = snapshot.documents.map { doc ->
            Patient(doc.id, doc.getString("nombre_completo") ?: "Paciente Desconocido")
        }
        emit(patients)
    }

    suspend fun linkDevice(deviceId: String, patientUid: String, deviceName: String) {

        // Acceso directo al AuthRepository Singleton
        val caregiverUid = AuthRepository.getCurrentUserUid()
            ?: throw Exception("Usuario no autenticado para vincular dispositivo.")

        val now = Timestamp.now()

        val deviceData = hashMapOf(
            "caregiver_uid" to caregiverUid,
            "patient_uid" to patientUid,
            "name" to deviceName
        )

        val readingsData = hashMapOf(
            "temp" to 25.0,
            "humidity" to 50.0,
            "weight" to 0.0,
            "last_updated" to now
        )

        val schedulesData = hashMapOf(
            "times" to listOf("08:00", "20:00"),
            "pill_weight_g" to 0.5
        )

        val eventsData = hashMapOf(
            "initializedAt" to now
        )

        firestore.collection("devices").document(deviceId)
            .set(deviceData as Map<String, Any>)
            .await()

        firestore.collection("readings").document(deviceId)
            .set(readingsData)
            .await()

        firestore.collection("schedules").document(deviceId)
            .set(schedulesData)
            .await()

        firestore.collection("events").document(deviceId)
            .set(eventsData)
            .await()
    }

    // --- LÓGICA DEL PANEL DE CONTROL ---
    fun getDevicesByCaregiver(caregiverUid: String): Flow<List<Device>> = flow {
        val snapshot = firestore.collection("devices")
            .whereEqualTo("caregiver_uid", caregiverUid)
            .get()
            .await()
        val devices = snapshot.documents.map { doc ->
            Device(
                deviceId = doc.id,
                name = doc.getString("name") ?: "Dispositivo sin nombre",
                patientUid = doc.getString("patient_uid") ?: "N/A"
            )
        }
        emit(devices)
    }

    // --- LÓGICA DE HORARIOS ---
    fun getScheduleConfig(deviceId: String): Flow<ScheduleConfig> = flow {
        // Escucha en tiempo real
        firestore.collection("schedules").document(deviceId)
            .snapshots()
            .collect { snapshot ->
                val config = ScheduleConfig(
                    pill_weight_g = snapshot.getDouble("pill_weight_g") ?: 0.5,
                    times = snapshot.get("times") as? List<String> ?: emptyList()
                )
                emit(config)
            }
    }

    suspend fun saveScheduleConfig(deviceId: String, config: ScheduleConfig) {
        val now = Timestamp.now()
        firestore.collection("schedules").document(deviceId)
            .update(
                mapOf(
                    "pill_weight_g" to config.pill_weight_g,
                    "times" to config.times,
                    "last_updated_app" to now
                )
            )
            .await()
    }
    suspend fun getUserRole(uid: String): String? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.getString("rol") // ⬅️ Usa el campo "rol" correcto
        } catch (e: Exception) {
            null
        }
    }
}