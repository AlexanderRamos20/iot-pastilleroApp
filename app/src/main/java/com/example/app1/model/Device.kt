// Archivo: com.example.app1.model.Device.kt
package com.example.app1.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Device(
    val deviceId: String,
    val name: String,
    val patientUid: String,
    val patientName: String = "Cargando..."
) : Parcelable