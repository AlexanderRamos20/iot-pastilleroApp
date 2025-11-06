package com.example.app1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app1.ui.screens.LoginScreen
import com.example.app1.ui.screens.RegisterScreen
import com.example.app1.ui.theme.App1Theme

object Routes{
    const val Login = "login"
    const val Register = "registro"
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
                    }
                }

            }
        }
    }
}
