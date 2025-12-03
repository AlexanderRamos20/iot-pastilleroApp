package com.example.app1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app1.Routes // Usamos el Routes.object de MainActivity
import com.example.app1.di.AppDependencies
import com.example.app1.model.Device
import com.example.app1.viewmodel.DeviceListUiState
import com.example.app1.viewmodel.PanelControlViewModel
import com.example.app1.viewmodel.PanelControlViewModelFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.app1.data.AuthRepository // Asegurar importación para compilación
import com.example.app1.model.DeviceMonitorData

// ===========================================
// COMPONENTE DE TARJETA DE DISPOSITIVO
// ===========================================
@Composable
fun DeviceCard(monitorData: DeviceMonitorData, navController: NavController) {
    val device = monitorData.device
    val reading = monitorData.currentReading
    val logs = monitorData.recentLogs

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título y Paciente
            Text(text = device.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "Paciente: ${device.patientName}", color = MaterialTheme.colorScheme.primary)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Monitoreo en Tiempo Real (Reading) ---
            Text("Condición Ambiental Actual:", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Temp: ${reading.temp}°C")
                Text("Humedad: ${reading.humidity}%")
                Text("Peso: ${reading.weight}g")
            }
            Text("Última lectura: ${reading.last_updated?.toDate() ?: "N/A"}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            // --- Historial de Eventos (Log Deslizable) ---
            Text("Historial (Últimos ${logs.size})", style = MaterialTheme.typography.titleSmall)

            // Usamos un Column en lugar de LazyColumn para un historial pequeño y rápido
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp) // Limita la altura para hacerlo deslizable visualmente
                    .padding(top = 4.dp)
            ) {
                if (logs.isEmpty()) {
                    Text("No hay eventos registrados.", color = Color.Gray)
                } else {
                    logs.forEach { log ->
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(log.type, style = MaterialTheme.typography.bodySmall, color = if (log.type.contains("ALERTA")) Color.Red else Color.Blue)
                            Text(log.description.substringBefore("Peso"), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                    }
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Botón de Acción
            Button(
                onClick = { navController.navigate(Routes.ScheduleManagementRoute(device.deviceId)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestionar Horarios / Configuración")
            }
        }
    }
}

// ===========================================
// PANTALLA PRINCIPAL: PANEL DE CONTROL
// ===========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelControlScreen(navController: NavController) {

    // 1. Obtener el ViewModel y observar su estado
    val viewModel: PanelControlViewModel = viewModel(
        factory = PanelControlViewModelFactory(AppDependencies.pillboxRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Control IoT") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (val state = uiState) {
                DeviceListUiState.Loading -> {
                    // 2. Muestra indicador de carga
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DeviceListUiState.Success -> {
                    // 3. Muestra la lista de dispositivos asignados
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp)
                    ) {
                        items(state.monitoringData) { data -> // ⬅️ Usamos el modelo enriquecido
                            DeviceCard(monitorData = data, navController = navController)
                        }
                    }
                }
                DeviceListUiState.Empty -> {
                    // 4. Muestra mensaje si no hay dispositivos
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tienes dispositivos vinculados.", style = MaterialTheme.typography.titleMedium)
                    }
                }
                is DeviceListUiState.Error -> {
                    // 5. Muestra errores de carga
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = Color.Red, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}