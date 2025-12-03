package com.example.app1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app1.ui.screens.HomeScreen
import com.example.app1.ui.screens.LoginScreen
import com.example.app1.ui.screens.RegisterScreen
import com.example.app1.ui.screens.PanelControlScreen
import com.example.app1.ui.screens.DeviceRegisterScreen
import com.example.app1.ui.screens.ScheduleManagementScreen // Necesaria para la nueva ruta
import com.example.app1.ui.theme.App1Theme

object Routes{
    const val Login = "login"
    const val Register = "registro"
    const val Home = "home_route"
    const val PanelControl = "panel_control"
    const val DeviceRegister = "device_register"

    // 🚨 CONSTANTES Y FUNCIÓN PARA LA NUEVA RUTA PARAMÉTRICA
    const val ScheduleManagementBase = "schedule_management"
    const val ScheduleManagement = "schedule_management/{deviceId}"

    fun ScheduleManagementRoute(deviceId: String) = "schedule_management/$deviceId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App1Theme {
                val navController = rememberNavController()

                Scaffold { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Login,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable (Routes.Login){
                            LoginScreen(navController = navController)
                        }

                        composable (Routes.Register){
                            RegisterScreen(navController = navController)
                        }

                        composable(Routes.Home){
                            HomeScreen(navController = navController)
                        }
                        composable(Routes.PanelControl) {
                            PanelControlScreen(navController = navController)
                        }
                        composable(Routes.DeviceRegister) {
                            DeviceRegisterScreen(navController = navController)
                        }

                        // 🚨 CONFIGURACIÓN DE RUTA CON ARGUMENTO PARA GESTIÓN DE HORARIOS
                        composable(
                            route = Routes.ScheduleManagement,
                            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            // Extrae el ID del dispositivo y lo pasa a la pantalla de gestión
                            val deviceId = backStackEntry.arguments?.getString("deviceId")
                            ScheduleManagementScreen(navController = navController, deviceId = deviceId)
                        }
                    }
                }
            }
        }
    }
}