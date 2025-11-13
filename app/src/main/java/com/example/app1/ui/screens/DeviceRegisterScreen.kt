package com.example.app1.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app1.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceRegisterScreen(navController: NavController) {

    var deviceCode by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar dispositivo") },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // Icono QR centrado
            Image(
                painter = painterResource(id = R.drawable.scan_logo),
                contentDescription = "Escanear código QR",
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 24.dp)
            )

            // Campo: código del dispositivo
            OutlinedTextField(
                value = deviceCode,
                onValueChange = { deviceCode = it },
                label = { Text("Código del dispositivo") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            // Campo: nombre del dispositivo
            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("Nombre del dispositivo") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón "Vincular"
            Button(
                onClick = {
                    // aquí iría la lógica para vincular el dispositivo
                    // por ejemplo, llamar a un repositorio y luego navegar atrás
                },
                enabled = deviceCode.isNotBlank() && deviceName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Vincular")
            }
        }
    }
}
