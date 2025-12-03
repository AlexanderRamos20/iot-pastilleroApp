package com.example.app1.model

import com.google.firebase.Timestamp

/**
 * Representa las lecturas de sensores en tiempo real desde readings/{deviceId}.
 * Alineado con las claves del firmware del Arduino.
 */
data class Reading(
    val temp: Double = 0.0,
    val humidity: Double = 0.0,
    val weight: Double = 0.0,
    val last_updated: Timestamp? = null
)