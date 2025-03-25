package com.taxiflash.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.taxiflash.ui.theme.TaxiYellow

/**
 * Elementos de la barra de navegación inferior
 */
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Inicio : BottomNavItem(NavRoutes.INICIO, Icons.Outlined.Home, "Inicio")
    object Ingresos : BottomNavItem(NavRoutes.INGRESOS, Icons.Outlined.AttachMoney, "Ingresos")
    object Gastos : BottomNavItem(NavRoutes.GASTOS, Icons.Outlined.Receipt, "Gastos")
    object Estadisticas : BottomNavItem(NavRoutes.ESTADISTICAS, Icons.Outlined.BarChart, "Estadísticas")
    
    companion object {
        // Lista de elementos de navegación
        val items = listOf(
            Inicio,
            Ingresos,
            Gastos,
            Estadisticas
        )
    }
}

/**
 * Determina si se debe mostrar la barra de navegación inferior
 */
@Composable
fun shouldShowBottomBar(navController: NavController): Boolean {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    
    // Lista de rutas principales donde se debe mostrar la barra
    val mainRoutes = listOf(
        NavRoutes.INICIO,
        NavRoutes.INGRESOS,
        NavRoutes.GASTOS,
        NavRoutes.ESTADISTICAS
    )
    
    // Mostrar la barra solo en las rutas principales
    return mainRoutes.contains(currentRoute)
}

/**
 * Barra de navegación inferior
 */
@Composable
fun TaxiFlashBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    
    // Solo mostrar la barra en las rutas principales
    val mainRoutes = listOf(
        NavRoutes.INICIO,
        NavRoutes.INGRESOS,
        NavRoutes.GASTOS,
        NavRoutes.ESTADISTICAS
    )
    
    if (mainRoutes.contains(currentRoute)) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = modifier
        ) {
            // Elemento Inicio
            NavigationBarItem(
                icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
                selected = currentRoute == NavRoutes.INICIO,
                onClick = {
                    navController.navigate(NavRoutes.INICIO) {
                       popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            
            // Elemento Ingresos
            NavigationBarItem(
                icon = { Icon(Icons.Outlined.AttachMoney, contentDescription = "Ingresos") },
                selected = currentRoute == NavRoutes.INGRESOS,
                onClick = {
                    navController.navigate(NavRoutes.INGRESOS) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            
            // Elemento Gastos
            NavigationBarItem(
                icon = { Icon(Icons.Outlined.Receipt, contentDescription = "Gastos") },
                selected = currentRoute == NavRoutes.GASTOS,
                onClick = {
                    navController.navigate(NavRoutes.GASTOS) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            
            // Elemento Estadísticas
            NavigationBarItem(
                icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Estadísticas") },
                selected = currentRoute == NavRoutes.ESTADISTICAS,
                onClick = {
                    navController.navigate(NavRoutes.ESTADISTICAS) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Botón flotante de acción
 */
@Composable
fun TaxiFlashFloatingActionButton(
    navController: NavController,
    currentRoute: String?
) {
    if (currentRoute == BottomNavItem.Ingresos.route) {
        FloatingActionButton(
            onClick = { navController.navigate(NavRoutes.TURNO_REGISTRO) },
            containerColor = TaxiYellow,
            contentColor = Color.Black
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nuevo Turno")
        }
    }
} 