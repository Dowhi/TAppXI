package com.taxiflash.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taxiflash.ui.R
import com.taxiflash.ui.components.LoadingIndicator
import com.taxiflash.ui.components.StatCard
import com.taxiflash.ui.navigation.NavRoutes
import com.taxiflash.ui.theme.TaxiBlue
import com.taxiflash.ui.theme.TaxiGreen
import com.taxiflash.ui.theme.TaxiRed
import com.taxiflash.ui.theme.TaxiYellow
import com.taxiflash.ui.theme.TaxiDarkGray
import com.taxiflash.ui.theme.TaxiGray
import com.taxiflash.ui.viewmodel.InicioViewModel
import com.taxiflash.ui.viewmodel.CarreraViewModel
import com.taxiflash.ui.viewmodel.TurnoViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.components.TaxiFlashScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    navController: NavController,
    turnoViewModel: TurnoViewModel = viewModel(),
    carreraViewModel: CarreraViewModel = viewModel(),
    inicioViewModel: InicioViewModel = viewModel()
) {
    val resumenMensual by inicioViewModel.resumenMensual.collectAsState()
    val turnoActivo by inicioViewModel.turnoActivo.collectAsState()
    val turnoActivoCompleto by inicioViewModel.turnoActivoCompleto.collectAsState()
    val isLoading by inicioViewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val ingresos = resumenMensual?.ingresos ?: 0.0
    val gastos = resumenMensual?.gastos ?: 0.0
    val balance = ingresos - gastos

    // Animación de entrada
    var startAnimation by remember { mutableStateOf(false) }
    
    // Forzar recarga de datos cada vez que se muestra la pantalla
    LaunchedEffect(key1 = true) {
        println("TaxiFlash - InicioScreen - Forzando recarga de datos")
        inicioViewModel.recargarDatos()
        startAnimation = true
    }
    
    // Log para depuración
    println("TaxiFlash - InicioScreen - Ingresos: $ingresos, Gastos: $gastos, Balance: $balance")
    
    // Estados para la información del turno activo
    val turnoActivoId by turnoViewModel.turnoActivoId.collectAsState()
    val turnoActivoInfo by turnoViewModel.turnoActivoInfo.collectAsState()
    val puedeCrearTurno by turnoViewModel.puedeCrearTurno.collectAsState()
    
    // Estado para la suma de importes del turno activo
    var sumaImportes by remember { mutableStateOf(0.0) }
    
    // Efecto para verificar el turno activo y obtener la suma de importes
    LaunchedEffect(Unit) {
        turnoViewModel.verificarTurnoActivo()
        
        // Si hay un turno activo, obtener la suma de importes
        turnoActivoId?.let { id ->
            carreraViewModel.obtenerSumaImportesPorTurno(id)
        }
    }
    
    // Efecto para actualizar la suma de importes cuando cambia el turno activo
    LaunchedEffect(turnoActivoId) {
        turnoActivoId?.let { id ->
            val suma = carreraViewModel.obtenerSumaImportesPorTurno(id)
            sumaImportes = suma
        }
    }
    
    // Efecto para actualizar periódicamente la suma de importes
    LaunchedEffect(turnoActivoId) {
        while (turnoActivoId != null) {
            delay(30000) // Actualizar cada 30 segundos
            turnoActivoId?.let { id ->
                val suma = carreraViewModel.obtenerSumaImportesPorTurno(id)
                sumaImportes = suma
            }
        }
    }
    
    // Estado para controlar el menú desplegable
    var menuExpandido by remember { mutableStateOf(false) }
    
    TaxiFlashScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalTaxi,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TaxiFlash",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Gestión eficiente para taxistas",
                                fontSize = 12.sp,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TaxiYellow,
                    titleContentColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = { menuExpandido = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menú",
                            tint = Color.Black
                        )
                    }
                    
                    // Menú desplegable
                    DropdownMenu(
                        expanded = menuExpandido,
                        onDismissRequest = { menuExpandido = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Notificaciones") 
                                }
                            },
                            onClick = { 
                                menuExpandido = false
                                navController.navigate(NavRoutes.RECORDATORIOS) 
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Otros Ingresos") 
                                }
                            },
                            onClick = { 
                                menuExpandido = false
                                navController.navigate(NavRoutes.OTROS_INGRESOS) 
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ajustes") 
                                }
                            },
                            onClick = { 
                                menuExpandido = false
                                navController.navigate(NavRoutes.AJUSTES) 
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = TaxiRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cerrar Sesión", color = TaxiRed) 
                                }
                            },
                            onClick = { 
                                menuExpandido = false
                                // Añadir la lógica para cerrar sesión aquí
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(NavRoutes.INICIO) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo con gradiente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                TaxiBlue.copy(alpha = 0.05f),
                                Color.White
                            )
                        )
                    )
            )
            
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .verticalScroll(scrollState)
            ) {
                // Cabecera con logo y título
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { -200 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    // El header ya no es necesario porque se ha movido a la TopAppBar
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Tarjeta de turno activo
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { 180 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    TurnoActivoCard(
                        turnoActivo = turnoActivo,
                        turnoCompleto = turnoActivoCompleto,
                        onVerTurnoClick = { turnoId ->
                            navController.navigate(NavRoutes.VISTA_CARRERAS.replace("{turnoId}", turnoId))
                        },
                        onNuevoTurnoClick = {
                            navController.navigate(NavRoutes.TURNO_REGISTRO)
                        },
                        sumaImportes = sumaImportes
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Estadísticas rápidas
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { 230 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Text(
                        text = "Resumen del mes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TaxiBlue
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Tarjetas de estadísticas
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { 330 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ingresos
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.EuroSymbol,
                                        contentDescription = "Ingresos",
                                        tint = TaxiGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Ingresos",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                Text(
                                    text = numberFormat.format(resumenMensual?.ingresos ?: 0.0),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TaxiGreen
                                )
                            }
                            
                            // Separador vertical
                            Divider(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            
                            // Gastos
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Receipt,
                                        contentDescription = "Gastos",
                                        tint = TaxiRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Gastos",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                Text(
                                    text = numberFormat.format(resumenMensual?.gastos ?: 0.0),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TaxiRed
                                )
                            }
                            
                            // Separador vertical
                            Divider(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            
                            // Balance
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                val balance = (resumenMensual?.ingresos ?: 0.0) - (resumenMensual?.gastos ?: 0.0)
                                val balanceColor = if (balance >= 0) TaxiGreen else TaxiRed
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBalance,
                                        contentDescription = "Balance",
                                        tint = balanceColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Balance",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                Text(
                                    text = numberFormat.format(balance),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = balanceColor
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Menú de acceso rápido - Solo botones ocupando todo el ancho
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { 500 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Opción 1: Ingresos
                        MenuCard(
                            title = "Ingresos",
                            icon = Icons.Outlined.DirectionsCar,
                            onClick = { 
                                if (turnoActivoId != null) {
                                    navController.navigate(NavRoutes.VISTA_CARRERAS.replace("{turnoId}", turnoActivoId!!))
                                } else {
                                    navController.navigate(NavRoutes.TURNO_REGISTRO)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Opción 2: Gastos
                        MenuCard(
                            title = "Gastos",
                            icon = Icons.Outlined.Receipt,
                            onClick = { navController.navigate(NavRoutes.GASTOS) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Opción 3: Histórico
                        MenuCard(
                            title = "Histórico",
                            icon = Icons.Outlined.History,
                            onClick = { navController.navigate(NavRoutes.HISTORICO) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Opción 4: Estadísticas
                        MenuCard(
                            title = "Estadísticas",
                            icon = Icons.Outlined.BarChart,
                            onClick = { navController.navigate(NavRoutes.ESTADISTICAS_AVANZADAS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Indicador de carga
            LoadingIndicator(isLoading = isLoading)
        }
    }
}

@Composable
private fun TurnoActivoCard(
    turnoActivo: String?,
    turnoCompleto: com.taxiflash.ui.data.Turno?,
    onVerTurnoClick: (String) -> Unit,
    onNuevoTurnoClick: () -> Unit,
    sumaImportes: Double = 0.0
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Turno Activo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (turnoActivo == null) {
                // No hay turno activo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.errorContainer,
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "No hay turno activo",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "No hay turno activo",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = onNuevoTurnoClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Comenzar Turno")
                        }
                    }
                }
            } else {
                // Hay un turno activo
                turnoCompleto?.let { turno ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Turno ${turno.numeroTurno}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Primera fila: Total, H. Inicio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Columna 1: Total
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Total",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${String.format("%.2f", sumaImportes)}€",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                // Columna 2: H. Inicio
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "H. Inicio",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${turno.horaInicio}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Segunda fila: Fecha, Kms. Inicio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Columna 3: Fecha
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Fecha",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${turno.fecha}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                // Columna 4: Kms. Inicio
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Kms. Inicio",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${turno.kmInicio}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Button(
                                onClick = { onVerTurnoClick(turnoActivo) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver Detalles")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp), // Reducido de 120.dp a 80.dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp) // Reducido de 16.dp a 8.dp
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TaxiBlue,
                modifier = Modifier.size(24.dp) // Reducido de 32.dp a 24.dp
            )
            
            Spacer(modifier = Modifier.height(4.dp)) // Reducido de 8.dp a 4.dp
            
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                color = TaxiDarkGray,
                fontSize = 12.sp // Añadido tamaño más pequeño
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .height(100.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TaxiGray
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
} 