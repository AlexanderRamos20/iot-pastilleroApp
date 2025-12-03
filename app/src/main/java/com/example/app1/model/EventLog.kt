package com.example.app1.model

import com.google.firebase.Timestamp

/**
 * Representa un evento histórico (toma, relleno, alerta) registrado por el Arduino.
 * @param type Tipo de evento (ALERTA_AMBIENTAL, TOMA_DETECTADA, etc.)
 * @param description Descripción detallada del evento.
 * @param timestamp Marca de tiempo del evento.
 */
data class EventLog(
    val type: String = "",
    val description: String = "",
    val timestamp: Timestamp? = null
)