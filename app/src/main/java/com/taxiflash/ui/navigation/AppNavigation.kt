package com.taxiflash.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.taxiflash.ui.screens.*
import com.taxiflash.ui.viewmodel.InicioViewModel
import com.taxiflash.ui.viewmodel.TurnoViewModel
import com.taxiflash.ui.viewmodel.UsuarioViewModel
import com.taxiflash.ui.data.UsuarioRepository
import java.text.SimpleDateFormat
import java.util.*

/**
 * Configuración principal de navegación de la aplicación
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    turnoViewModel: TurnoViewModel,
    inicioViewModel: InicioViewModel,
    usuarioRepository: UsuarioRepository,
    modifier: Modifier = Modifier
) {
    val turnoActivoId by turnoViewModel.turnoActivoId.collectAsState()
    
    // Crear el ViewModel para usuarios con su Factory
    val usuarioViewModel = UsuarioViewModel.Factory(usuarioRepository).create(UsuarioViewModel::class.java)
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    
    // Efecto para navegar automáticamente a la pantalla de inicio cuando hay un usuario activo
    LaunchedEffect(usuarioActual) {
        if (usuarioActual != null) {
            // Si hay un usuario activo, ir al inicio o al turno activo si existe
            if (turnoActivoId != null) {
                navController.navigate("${NavRoutes.VISTA_CARRERAS.replace("{turnoId}", turnoActivoId!!)}") {
                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.navigate(NavRoutes.INICIO) {
                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
    
    // Efecto para navegar automáticamente a la vista de carreras cuando hay un turno activo
    LaunchedEffect(turnoActivoId) {
        if (usuarioActual != null && turnoActivoId != null) {
            navController.navigate("${NavRoutes.VISTA_CARRERAS.replace("{turnoId}", turnoActivoId!!)}") {
                popUpTo(NavRoutes.INICIO)
                launchSingleTop = true
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        modifier = modifier
    ) {
        // Pantalla de Splash
        composable(
            route = NavRoutes.SPLASH,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            }
        ) {
            SplashScreen(navController = navController)
        }
        
        // Pantallas de autenticación
        composable(
            route = NavRoutes.LOGIN,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            }
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoutes.INICIO) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    navController.navigate(NavRoutes.REGISTRO_USUARIO)
                },
                onRecuperarClick = {
                    navController.navigate(NavRoutes.RECUPERAR_PIN)
                },
                viewModel = usuarioViewModel
            )
        }
        
        composable(
            route = NavRoutes.REGISTRO_USUARIO,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            }
        ) {
            RegistroUsuarioScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegistroExitoso = {
                    navController.navigate(NavRoutes.INICIO) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRecuperarClick = {
                    navController.navigate(NavRoutes.RECUPERAR_PIN) {
                        popUpTo(NavRoutes.REGISTRO_USUARIO) { inclusive = true }
                    }
                },
                viewModel = usuarioViewModel
            )
        }
        
        composable(
            route = NavRoutes.RECUPERAR_PIN,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            }
        ) {
            RecuperarPinScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = usuarioViewModel
            )
        }
        
        // Pantallas principales
        mainScreens(navController, inicioViewModel)
        
        // Pantallas de gestión de turnos y carreras
        turnoCarreraScreens(navController)
        
        // Pantallas de resúmenes y estadísticas
        resumenScreens(navController)
        
        // Ajustes
        composable(NavRoutes.AJUSTES) {
            AjustesScreen(navController = navController)
        }
        
        // Recordatorios
        composable(
            route = NavRoutes.RECORDATORIOS,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            RecordatoriosScreen(navController = navController)
        }

        composable(NavRoutes.HISTORICO) {
            HistoricoScreen(
                navController = navController
            )
        }

        composable(NavRoutes.ESTADISTICAS_AVANZADAS) {
            EstadisticasAvanzadasScreen(
                navController = navController
            )
        }
    }
}

/**
 * Pantallas principales de la aplicación
 */
private fun NavGraphBuilder.mainScreens(
    navController: NavController,
    inicioViewModel: InicioViewModel
) {
    // Pantalla de inicio
    composable(
        route = NavRoutes.INICIO,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) {
        InicioScreen(
            navController = navController,
            inicioViewModel = inicioViewModel
        )
    }
    
    // Pantalla de ingresos
    composable(
        route = NavRoutes.INGRESOS,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        ResumenDiarioScreen(
            fecha = SimpleDateFormat("ddMMyyyy", Locale("es", "ES")).format(Date()),
            onNavigateBack = { navController.navigateUp() }
        )
    }
    
    // Pantalla de gastos
    composable(
        route = NavRoutes.GASTOS,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        GastoScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }
    
    // Pantalla de Otros Ingresos
    composable(
        route = NavRoutes.OTROS_INGRESOS,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        OtrosIngresosScreen(
            navController = navController
        )
    }
    
    // Pantalla de estadísticas
    composable(
        route = NavRoutes.ESTADISTICAS,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        HistoricoEstadisticasScreen(
            onNavigateBack = { navController.navigateUp() },
            onResumenDiarioClick = { 
                val fechaActual = SimpleDateFormat("ddMMyyyy", Locale("es", "ES")).format(Date())
                navController.navigate(NavRoutes.RESUMEN_DIARIO_FECHA.replace("{fecha}", fechaActual)) 
            },
            onResumenMensualClick = { navController.navigate(NavRoutes.RESUMEN_MENSUAL) },
            onResumenAnualClick = { navController.navigate(NavRoutes.RESUMEN_ANUAL) },
            onResumenGastosMensualClick = { navController.navigate(NavRoutes.RESUMEN_GASTOS_MENSUAL) }
        )
    }
    
    // Pantalla de registro de gasto
    composable(
        route = NavRoutes.GASTO,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) {
        GastoScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }
}

/**
 * Pantallas relacionadas con turnos y carreras
 */
private fun NavGraphBuilder.turnoCarreraScreens(navController: NavHostController) {
    // Pantalla de registro de turno
    composable(
        route = NavRoutes.TURNO_REGISTRO,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) {
        TurnoRegistroScreen(
            navController = navController
        )
    }
    
    // Pantalla de vista de carreras
    composable(
        route = NavRoutes.VISTA_CARRERAS,
        arguments = listOf(navArgument("turnoId") { type = NavType.StringType }),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) { backStackEntry ->
        val turnoId = backStackEntry.arguments?.getString("turnoId") ?: return@composable
        VistaCarrerasScreen(
            turnoId = turnoId,
            onNuevaCarrera = {
                navController.navigate(NavRoutes.REGISTRO_CARRERA.replace("{turnoId}", turnoId))
            },
            onNavigateBack = { 
                navController.navigate(NavRoutes.INGRESOS) {
                    popUpTo(NavRoutes.INGRESOS) { inclusive = true }
                }
            },
            onEditarCarrera = { carreraId ->
                val route = NavRoutes.REGISTRO_CARRERA_EDITAR
                    .replace("{turnoId}", turnoId)
                    .replace("{carreraId}", carreraId.toString())
                navController.navigate(route)
            },
            onResumenDiarioClick = {
                val fechaActual = SimpleDateFormat("ddMMyyyy", Locale("es", "ES")).format(Date())
                navController.navigate(NavRoutes.RESUMEN_DIARIO_FECHA.replace("{fecha}", fechaActual))
            },
            onCerrarTurno = {
                navController.navigate(NavRoutes.CIERRE_TURNO.replace("{turnoId}", turnoId))
            },
            onResumenMensualClick = {
                navController.navigate(NavRoutes.RESUMEN_MENSUAL)
            },
            onResumenMensualDetalladoClick = {
                navController.navigate(NavRoutes.RESUMEN_MENSUAL_DETALLADO)
            }
        )
    }
    
    // Pantalla de registro de carrera
    composable(
        route = NavRoutes.REGISTRO_CARRERA,
        arguments = listOf(navArgument("turnoId") { type = NavType.StringType }),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) { backStackEntry ->
        val turnoId = backStackEntry.arguments?.getString("turnoId") ?: return@composable
        RegistroCarreraScreen(
            turnoActual = turnoId,
            onNavigateBack = { navController.navigateUp() }
        )
    }
    
    // Pantalla de edición de carrera
    composable(
        route = NavRoutes.REGISTRO_CARRERA_EDITAR,
        arguments = listOf(
            navArgument("turnoId") { type = NavType.StringType },
            navArgument("carreraId") { 
                type = NavType.LongType
                defaultValue = -1L
            }
        ),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) { backStackEntry ->
        val turnoId = backStackEntry.arguments?.getString("turnoId") ?: return@composable
        val carreraId = backStackEntry.arguments?.getLong("carreraId") ?: -1L
        RegistroCarreraScreen(
            turnoActual = turnoId,
            carreraId = carreraId,
            onNavigateBack = { navController.navigateUp() }
        )
    }
    
    // Pantalla de cierre de turno
    composable(
        route = NavRoutes.CIERRE_TURNO,
        arguments = listOf(navArgument("turnoId") { type = NavType.StringType }),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) { backStackEntry ->
        val turnoId = backStackEntry.arguments?.getString("turnoId") ?: return@composable
        CierreTurnoScreen(
            navController = navController,
            turnoId = turnoId
        )
    }
    
    // Pantalla de detalle de carrera
    composable(
        route = NavRoutes.DETALLE_CARRERA,
        arguments = listOf(navArgument("carreraId") { type = NavType.LongType }),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) { backStackEntry ->
        val carreraId = backStackEntry.arguments?.getLong("carreraId") ?: return@composable
        DetalleCarreraScreen(
            navController = navController,
            carreraId = carreraId
        )
    }
}

/**
 * Pantallas de resúmenes y estadísticas
 */
private fun NavGraphBuilder.resumenScreens(navController: NavHostController) {
    // Pantalla de resumen diario por fecha
    composable(
        route = NavRoutes.RESUMEN_DIARIO_FECHA,
        arguments = listOf(navArgument("fecha") { type = NavType.StringType }),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) { backStackEntry ->
        val fecha = backStackEntry.arguments?.getString("fecha") ?: 
            SimpleDateFormat("ddMMyyyy", Locale("es", "ES")).format(Date())
        ResumenDiarioScreen(
            fecha = fecha,
            onNavigateBack = { navController.popBackStack() }
        )
    }
    
    // Pantalla de resumen mensual
    composable(
        route = NavRoutes.RESUMEN_MENSUAL,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        ResumenMensualScreen(
            onNavigateBack = { navController.popBackStack() },
            onDiaClick = { fecha ->
                navController.navigate(NavRoutes.RESUMEN_DIARIO_FECHA.replace("{fecha}", fecha))
            }
        )
    }
    
    // Pantalla de resumen mensual detallado
    composable(
        route = NavRoutes.RESUMEN_MENSUAL_DETALLADO,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        ResumenMensualDetalladoScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
    
    // Pantalla de resumen de gastos mensual
    composable(
        route = NavRoutes.RESUMEN_GASTOS_MENSUAL,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        ResumenGastosMensualScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }
    
    // Pantalla de resumen anual
    composable(
        route = NavRoutes.RESUMEN_ANUAL,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        ResumenAnualScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }
    
    // Pantalla de edición de día
    composable(
        route = NavRoutes.EDITAR_DIA,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(300)
            )
        }
    ) {
        EditarDiaScreen(
            fecha = SimpleDateFormat("ddMMyyyy", Locale("es", "ES")).format(Date()),
            onNavigateBack = { navController.navigateUp() }
        )
    }
} 