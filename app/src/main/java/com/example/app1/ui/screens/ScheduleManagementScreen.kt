// Archivo: com.example.app1.ui.screens.ScheduleManagementScreen.kt
package com.example.app1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app1.di.AppDependencies
import com.example.app1.model.ScheduleConfig
import com.example.app1.viewmodel.ScheduleViewModel
import com.example.app1.viewmodel.ScheduleViewModelFactory
import com.example.app1.viewmodel.ScheduleUiState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleManagementScreen(
    navController: NavController,
    deviceId: String?
) {
    if (deviceId == null) {
        navController.popBackStack()
        return
    }

    val viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModelFactory(AppDependencies.pillboxRepository, deviceId)
    )
    val uiState by viewModel.uiState.collectAsState()

    var times by remember { mutableStateOf(emptyList<String>()) }
    var pillWeightText by rememberSaveable { mutableStateOf("") }
    var newTimeInput by rememberSaveable { mutableStateOf("") }
    var showSavedFeedback by remember { mutableStateOf(false) }

    // Sincronización de datos y feedback de guardado
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ScheduleUiState.Success -> {
                times = state.config.times
                pillWeightText = state.config.pill_weight_g.toString()
            }
            ScheduleUiState.Saved -> {
                showSavedFeedback = true
                delay(1500)
                showSavedFeedback = false
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Horarios: $deviceId") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                ScheduleUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                ScheduleUiState.Saving -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("Guardando configuración...")
                        }
                    }
                }
                is ScheduleUiState.Error -> {
                    Text("Error de BD: ${state.message}", color = Color.Red, modifier = Modifier.padding(16.dp))
                }

                is ScheduleUiState.Success, ScheduleUiState.Saved -> {
                    // 1. Obtén el rol del estado (si es ScheduleUiState.Success, si no, es null)
                    val currentRole = (uiState as? ScheduleUiState.Success)?.userRole
                    // 2. Definición del booleano de habilitación
                    // 🚨 CORRECCIÓN: isCaregiver es true SÓLO si el rol no es nulo Y es "Cuidador".
                    val isCaregiver = currentRole == "Cuidador"

                    if (showSavedFeedback) {
                        Text("¡Guardado con éxito!", color = Color.Green, modifier = Modifier.padding(8.dp))
                    }

                    // --- FORMULARIO DE PESO Y NUEVO HORARIO ---
                    Text("Configuración de la Dosis", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))

                    OutlinedTextField(
                        value = pillWeightText,
                        onValueChange = { pillWeightText = it },
                        label = { Text("Peso esperado por dosis (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTimeInput,
                            onValueChange = { newTimeInput = it },
                            label = { Text("Nuevo Horario (HH:mm)") },
                            placeholder = { Text("Ej: 14:30") },
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Button(onClick = {
                            if (newTimeInput.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"))) {
                                times = (times + newTimeInput).distinct().sorted()
                                newTimeInput = ""
                            }
                        }) {
                            Text("Añadir")
                        }
                    }

                    // --- LISTADO DE HORARIOS ---
                    Text("Horarios Programados", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        items(times) { time ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Schedule, contentDescription = "Hora", modifier = Modifier.size(24.dp))
                                Text(time, style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = {
                                    times = times.filter { it != time }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                }
                            }
                            Divider()
                        }
                    }

                    // --- BOTÓN DE GUARDAR ---
                    Button(
                        onClick = {
                            viewModel.saveSchedule(times, pillWeightText)
                        },
                        enabled = isCaregiver && times.isNotEmpty() && uiState !is ScheduleUiState.Saving,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        Text(
                        text =
                            if (isCaregiver)
                                "GUARDAR CAMBIOS"
                            else
                                "Solo el Cuidador puede modificar")
                    }
                }
            }
        }
    }
}