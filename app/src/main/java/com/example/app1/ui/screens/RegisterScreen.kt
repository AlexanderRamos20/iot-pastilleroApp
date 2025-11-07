package com.example.app1.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app1.data.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController){
    var username by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password1 by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }
    val opcionesUsuario = listOf("Paciente", "Cuidador")

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Scope para ejecutar la tarea asíncrona


    Scaffold (
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(  "Crear cuenta")},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al login"
                        )
                    }
                }
            )
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                // 2. Aplica el padding exterior para margen
                .padding(24.dp),

            // 3. Usa el Arrangement para centrar el contenido (o Align.Top para empezar arriba)
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        )
        {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it},
                label = { Text("Nombre completo")},
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it},
                label = { Text("Correo electrónico")},
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    // muestra el teclado de email
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // campo contraseña
            OutlinedTextField(
                value = password1,
                onValueChange = { password1 = it},
                label = { Text("Contraseña")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password2,
                onValueChange = { password2 = it},
                label = { Text("Confirmar contraseña")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            var selectedOption by remember { mutableStateOf(opcionesUsuario[0]) }
            Text("Selecciona una opción:", style = MaterialTheme.typography.titleMedium)
            Row (
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                opcionesUsuario.forEach { text ->
                    Row (
                        Modifier
                            .clickable{selectedOption = text},
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = { selectedOption = text}
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔑 MOSTRAR ERROR si existe
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error, // Usar el color de error del tema
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                // Deshabilitar si los campos están vacíos O si está cargando
                enabled = username.isNotEmpty() && password1.isNotEmpty() && password2.isNotEmpty() && !isLoading,
                onClick = {
                    errorMessage = null // Limpiar errores anteriores

                    // --- 🔑 VALIDACIÓN DE CONTRASEÑAS ---
                    if (password1 != password2) {
                        errorMessage = "Las contraseñas no coinciden."
                        return@Button
                    }

                    // --- 🔑 INICIO DE LÓGICA ASÍNCRONA ---
                    isLoading = true // Mostrar estado de carga
                    scope.launch {
                        val result = AuthRepository.registerUser(
                            email = correo,
                            password = password1,
                            fullName = username,
                            role = selectedOption
                        )

                        isLoading = false // Finalizar carga

                        if (result.isSuccess) {
                            // 🚀 ÉXITO: Navegar al Login o a Home
                            navController.popBackStack() // Asumiendo que quieres volver al Login
                        } else {
                            // 🛑 ERROR: Mostrar error de Firebase (ej. correo ya en uso)
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido. Inténtalo de nuevo."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Registrando..." else "Registrar")
            }
        }
    }
}