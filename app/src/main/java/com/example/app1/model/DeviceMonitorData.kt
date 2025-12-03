package com.example.app1.model

// Modelo usado para la tarjeta del Panel de Control
data class DeviceMonitorData(
    val device: Device, // Configuración básica (nombre, UID)
    val currentReading: Reading, // Lecturas en tiempo real
    val recentLogs: List<EventLog> // Historial de eventos (para la lista deslizable)
)