package com.example.app1.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app1.model.Patient
import com.example.app1.viewmodel.DeviceRegisterViewModel
import com.example.app1.viewmodel.DeviceRegisterUiState
import com.example.app1.viewmodel.DeviceRegisterViewModelFactory
import com.example.app1.di.AppDependencies // ⬅️ Fuente de las dependencias
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHostState // Necesario para mostrar Snackbar


// Componente para el selector de pacientes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDropdown(
    patients: List<Patient>,
    selectedPatient: Patient?,
    onPatientSelected: (Patient) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedPatient?.name ?: "Seleccione un paciente",
            onValueChange = {},
            readOnly = true,
            label = { Text("Paciente a vincular") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            patients.forEach { patient ->
                DropdownMenuItem(
                    text = { Text(patient.name) },
                    onClick = {
                        onPatientSelected(patient)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Pantalla principal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceRegisterScreen(
    navController: NavController,
    // 🎯 SOLUCIÓN AL CRASH: Usamos la Fábrica
    viewModel: DeviceRegisterViewModel = viewModel(
        factory = DeviceRegisterViewModelFactory(AppDependencies.pillboxRepository)
    )
) {
    // Observar estados
    val uiState by viewModel.uiState.collectAsState()
    val isLinking by viewModel.linkStatus.collectAsState()

    // 🚨 MODIFICACIÓN: deviceCode ahora es mutable y se inicializa vacío/default.
    var deviceCode by rememberSaveable { mutableStateOf("prototipo_001") }
    var deviceName by rememberSaveable { mutableStateOf("") }
    var selectedPatient: Patient? by rememberSaveable { mutableStateOf(null) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }


    // Manejo de la navegación y errores
    LaunchedEffect(uiState) {
        // Navegará si el estado es Idle (indica éxito)
        if (uiState == DeviceRegisterUiState.Idle && !isLinking) {
            // Mostrar mensaje de éxito antes de navegar (opcional)
            // snackbarHostState.showSnackbar("Dispositivo vinculado con éxito.")
            navController.popBackStack()
        }
    }

    // Muestra errores si ocurren
    if (uiState is DeviceRegisterUiState.Error) {
        val errorMessage = (uiState as DeviceRegisterUiState.Error).message
        LaunchedEffect(errorMessage) {
            // Muestra el error en el Snackbar
            snackbarHostState.showSnackbar(
                message = "Error: $errorMessage",
                duration = SnackbarDuration.Short
            )
            // Limpia el estado de error después de que el Snackbar se dispara
            viewModel.clearError()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vincular Dispositivo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) } // Host para mostrar errores
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // ... (Contenido de UI, imagen, etc.) ...

            // 🚨 MODIFICACIÓN: Campo de Código del dispositivo (Editable)
            OutlinedTextField(
                value = deviceCode,
                onValueChange = { deviceCode = it }, // Permite al usuario ingresar un nuevo ID
                label = { Text("Código del dispositivo (ID)") },
                singleLine = true,
                // Ahora es EDITABLE: quitamos readOnly = true
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            // --- Selector de Paciente REAL (Manejo de estados) ---
            Spacer(modifier = Modifier.height(8.dp))
            when (val state = uiState) {
                DeviceRegisterUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(30.dp))
                    }
                }
                is DeviceRegisterUiState.Success -> {
                    PatientDropdown(
                        patients = state.patients,
                        selectedPatient = selectedPatient,
                        onPatientSelected = { patient -> selectedPatient = patient }
                    )
                }
                // Manejamos Idle y Error para asegurar que el dropdown se muestre si hay datos cacheados
                else -> {
                    val currentPatients = (viewModel.uiState.value as? DeviceRegisterUiState.Success)?.patients ?: emptyList()
                    PatientDropdown(
                        patients = currentPatients,
                        selectedPatient = selectedPatient,
                        onPatientSelected = { patient -> selectedPatient = patient }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo: nombre descriptivo del dispositivo
            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("Nombre del dispositivo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón "Vincular"
            Button(
                onClick = {
                    // La validación ahora requiere que deviceCode NO esté vacío
                    if (deviceCode.isNotBlank()) {
                        viewModel.linkDevice(deviceCode, selectedPatient, deviceName)
                    }
                },
                // Ahora también verificamos que deviceCode NO esté en blanco
                enabled = selectedPatient != null && deviceName.isNotBlank() && deviceCode.isNotBlank() && !isLinking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isLinking) {
                    Text("Vinculando...")
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Vincular Dispositivo")
                }
            }
        }
    }
}