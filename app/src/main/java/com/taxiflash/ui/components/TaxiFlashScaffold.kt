package com.taxiflash.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.BarChart
import com.taxiflash.ui.navigation.NavRoutes

/**
 * Scaffold común para todas las pantallas de la aplicación
 * 
 * @param topBar Barra superior opcional
 * @param showBottomBar Mostrar barra de navegación
 * @param content Contenido de la pantalla
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiFlashScaffold(
    navController: NavController,
    topBar: @Composable () -> Unit = {},
    showBottomBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio") },
                        selected = navController.currentDestination?.route == NavRoutes.INICIO,
                        onClick = { navController.navigate(NavRoutes.INICIO) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Turnos") },
                        label = { Text("Turnos") },
                        selected = navController.currentDestination?.route == NavRoutes.TURNO_REGISTRO,
                        onClick = { navController.navigate(NavRoutes.TURNO_REGISTRO) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Receipt, contentDescription = "Gastos") },
                        label = { Text("Gastos") },
                        selected = navController.currentDestination?.route == NavRoutes.GASTOS,
                        onClick = { navController.navigate(NavRoutes.GASTOS) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Estadísticas") },
                        label = { Text("Stats") },
                        selected = navController.currentDestination?.route == NavRoutes.ESTADISTICAS_AVANZADAS,
                        onClick = { navController.navigate(NavRoutes.ESTADISTICAS_AVANZADAS) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content(paddingValues)
        }
    }
} 