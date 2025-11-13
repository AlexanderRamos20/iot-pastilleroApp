package com.example.app1.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app1.R
import com.example.app1.Routes
import com.example.app1.data.AuthRepository
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.withStyle

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Logo arriba, centrado
        Image(
            painter = painterResource(id = R.drawable.logo_pastillero),
            contentDescription = "Logo del pastillero",
            modifier = Modifier
                .size(140.dp)
                .padding(top = 16.dp, bottom = 24.dp)
        )

        Text(
            text = buildAnnotatedString {
                append("Bienvenido a\n")

                withStyle(
                    style = SpanStyle(
                        color = Color.Blue
                    )
                ) {
                    append("tu pastillero inteligente")
                }
            },
            style = MaterialTheme.typography.headlineLarge.copy(
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón: Registro de dispositivo
        Button(
            onClick = { navController.navigate(Routes.DeviceRegister) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar dispositivo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón: Panel de control
        Button(
            onClick = { navController.navigate(Routes.PanelControl) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir al panel de control")
        }

        // deja el boton abajo deltodo
        Spacer(modifier = Modifier.weight(1f))

        // Botón: Cerrar sesión
        OutlinedButton(
            onClick = {
                // 1. Cerrar sesión en Firebase
                AuthRepository.logout()

                // 2. Volver a la pantalla de login limpiando el back stack (no vuelve al home)
                navController.navigate(Routes.Login) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}
