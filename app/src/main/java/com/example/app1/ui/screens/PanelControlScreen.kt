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

// ===========================================
// COMPONENTE DE TARJETA DE DISPOSITIVO
// ===========================================
@Composable
fun DeviceCard(device: Device, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título del dispositivo
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${device.deviceId} (Paciente: ${device.patientUid})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // MOCK: Aquí iría la lógica dinámica de T/H/Peso (Fase de Monitoreo)
            Text("Estado ambiental: OK (T/H/Peso)")

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Acción para navegar a la gestión de horarios
            Button(
                onClick = {
                    // Navegación a la pantalla de gestión, pasando el deviceId como argumento.
                    navController.navigate(Routes.ScheduleManagementRoute(device.deviceId))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestionar Horarios / Monitoreo")
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
                        items(state.devices) { device ->
                            DeviceCard(device = device, navController = navController)
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