package com.taxiflash.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.taxiflash.ui.components.BottomNavigationBar
import com.taxiflash.ui.data.AjustesDataStore
import com.taxiflash.ui.data.AjustesDataStore.dataStore
import com.taxiflash.ui.data.TaxiFlashDatabase
import com.taxiflash.ui.data.UsuarioRepository
import com.taxiflash.ui.navigation.AppNavHost
import com.taxiflash.ui.navigation.NavRoutes
import com.taxiflash.ui.theme.TaxiFlashTheme
import com.taxiflash.ui.viewmodel.InicioViewModel
import com.taxiflash.ui.viewmodel.TurnoViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Log para depuración
        Log.d("TaxiFlash", "MainActivity onCreate")
        
        setContent {
            // Obtener la preferencia de tema oscuro
            val darkTheme by dataStore.data
                .map { preferences -> 
                    preferences[AjustesDataStore.THEME_KEY] ?: false 
                }
                .collectAsState(initial = false)
            
            TaxiFlashTheme(darkTheme = darkTheme) {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // Controlador de navegación
    val navController = rememberNavController()
    
    // ViewModels
    val inicioViewModel: InicioViewModel = viewModel()
    val turnoViewModel: TurnoViewModel = viewModel()
    
    // Crear el repositorio de usuarios
    val database = TaxiFlashDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current)
    val usuarioRepository = UsuarioRepository(database.usuarioDao())
    
    // Forzar recarga de datos al iniciar la aplicación
    LaunchedEffect(key1 = Unit) {
        Log.d("TaxiFlash", "Forzando recarga de datos")
        inicioViewModel.recargarDatos()
    }
    
    // Determinar si se debe mostrar la barra inferior
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: ""
    val showBottomBar = shouldShowBottomBar(currentRoute)
    
    // Scaffold principal con barra de navegación inferior
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        // Configuración de navegación
        AppNavHost(
            navController = navController,
            turnoViewModel = turnoViewModel,
            inicioViewModel = inicioViewModel,
            usuarioRepository = usuarioRepository,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

/**
 * Determina si se debe mostrar la barra inferior basado en la ruta actual
 */
private fun shouldShowBottomBar(currentRoute: String): Boolean {
    val routesWithoutBottomBar = listOf(
        "registro_carrera",
        "cierre_turno",
        "splash",
        NavRoutes.LOGIN,
        NavRoutes.REGISTRO_USUARIO,
        NavRoutes.RECUPERAR_PIN
    )
    
    return routesWithoutBottomBar.none { route ->
        currentRoute.contains(route)
    }
}
