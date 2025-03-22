package com.taxiflash.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.taxiflash.ui.navigation.NavRoutes

/**
 * Elemento de navegación inferior
 */
data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val contentDescription: String
)

/**
 * Barra de navegación inferior para la aplicación
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    // Obtener la ruta actual para determinar el ítem seleccionado
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.INICIO
    
    // Lista de ítems de navegación
    val items = listOf(
        BottomNavItem(
            route = NavRoutes.INICIO,
            icon = Icons.Default.Home,
            contentDescription = "Inicio"
        )
    )
    
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp), // Altura reducida
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        items.forEach { item ->
            val isSelected = currentRoute.startsWith(NavRoutes.INICIO)
            
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.contentDescription
                    ) 
                },
                selected = isSelected,
                onClick = {
                    // Si ya estamos en la pantalla de inicio, no hacer nada
                    if (currentRoute != NavRoutes.INICIO) {
                        navController.navigate(NavRoutes.INICIO) {
                            // Limpiar el back stack
                            popUpTo(0) {
                                inclusive = true
                            }
                            // Evitar múltiples copias de la misma pantalla
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
} 