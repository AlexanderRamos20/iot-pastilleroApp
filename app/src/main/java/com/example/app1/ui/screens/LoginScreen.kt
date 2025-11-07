package com.example.app1.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app1.Routes
import com.example.app1.data.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController){
    // 1. Estados para los campos de texto
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) } // Para deshabilitar el botón
    var errorMessage by remember { mutableStateOf<String?>(null) } // Para mostrar errores
    val scope = rememberCoroutineScope() // Scope para ejecutar la tarea asíncrona

    // Column organiza los elementos verticalmente
    Column (
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    )
    {
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // campo usuario
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it},
            label = { Text("Correo electrónico")},
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // campo contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it},
            label = { Text("Contraseña")},
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage !=null){
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // botón login
        Button(
            enabled = correo.isNotEmpty() && password.isNotEmpty() && !isLoading,
            onClick = {
                errorMessage = null
                isLoading = true

                scope.launch {
                    val result = AuthRepository.loginUser(
                        email = correo,
                        password = password
                    )

                    isLoading = false

                    if (result.isSuccess) {
                        navController.navigate(Routes.Home) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                        else {
                        errorMessage = result.exceptionOrNull()?.localizedMessage
                            ?: "Error de inicio de sesión. Verifica tus datos"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "Ingresando..." else "Ingresar")
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            modifier = Modifier.clickable(onClick = { navController.navigate(Routes.Register)}),
            text = "¿Olvidaste tu contraseña? ",
            color = Color.Blue,
            textDecoration = TextDecoration.Underline
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier.clickable(onClick = { navController.navigate(Routes.Register)}),
            text = "Crear cuenta",
            color = Color.Blue,
            textDecoration = TextDecoration.Underline
        )
    }


}