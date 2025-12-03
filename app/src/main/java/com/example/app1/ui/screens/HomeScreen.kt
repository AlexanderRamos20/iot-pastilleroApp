package com.example.app1.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app1.viewmodel.UserRoleViewModel // Importar el VM
import com.example.app1.viewmodel.UserRole
import com.example.app1.viewmodel.UserRoleViewModelFactory // Importar el Factory
import com.example.app1.di.AppDependencies // Fuente de dependencias

@Composable
fun HomeScreen(navController: NavController) {

    // 1. Obtener el ViewModel y observarlo
    val viewModel: UserRoleViewModel = viewModel(
        factory = UserRoleViewModelFactory(AppDependencies.pillboxRepository)
    )
    val userRoleState by viewModel.userRoleState.collectAsState()

    // 2. Definir estados de conveniencia
    val isCaregiver = userRoleState is UserRole.Found && (userRoleState as UserRole.Found).role == "Cuidador"
    val isLoading = userRoleState is UserRole.Loading
    val userRoleDisplay = if (isCaregiver) "Cuidador" else if (userRoleState is UserRole.Found) "Paciente" else "Usuario"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Logo y Título
        Image(
            painter = painterResource(id = R.drawable.logo_pastillero), // Asumimos este recurso
            contentDescription = "Logo del pastillero",
            modifier = Modifier
                .size(140.dp)
                .padding(top = 16.dp, bottom = 24.dp)
        )

        Text(
            text = buildAnnotatedString {
                append("Bienvenido, ${userRoleDisplay}\n")
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue
                    )
                ) {
                    append("a tu pastillero inteligente")
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

        // 3. Lógica Condicional para el Botón de Registro
        if (isLoading) {
            // Mostrar indicador mientras se carga el rol
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else if (isCaregiver) {
            // 🚨 SOLO MUESTRA ESTE BOTÓN PARA EL CUIDADOR
            Button(
                onClick = { navController.navigate(Routes.DeviceRegister) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar dispositivo")
            }
        }
        // Si es Paciente o el rol no se encuentra (NotFound), el botón no se muestra.

        Spacer(modifier = Modifier.height(16.dp))

        // Botón: Panel de control
        Button(
            onClick = { navController.navigate(Routes.PanelControl) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir al panel de control")
        }

        // ... (Cerrar sesión)
        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = {
                AuthRepository.logout()
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