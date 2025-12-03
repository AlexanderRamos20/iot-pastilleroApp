// Archivo: com.example.app1.model.ScheduleConfig.kt
package com.example.app1.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScheduleConfig(
    val pill_weight_g: Double = 0.5,
    val times: List<String> = emptyList()
) : Parcelable