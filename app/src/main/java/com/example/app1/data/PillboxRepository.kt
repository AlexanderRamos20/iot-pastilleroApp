package com.example.app1.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.example.app1.model.Patient // Importamos el modelo correcto
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
}