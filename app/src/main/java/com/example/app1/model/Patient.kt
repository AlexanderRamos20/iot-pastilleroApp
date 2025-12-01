
package com.example.app1.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize // Necesitas agregar la dependencia del plugin en Gradle

// 🚨 Implementamos Parcelable y usamos @Parcelize
@Parcelize
data class Patient(
    val uid: String,
    val name: String
) : Parcelable