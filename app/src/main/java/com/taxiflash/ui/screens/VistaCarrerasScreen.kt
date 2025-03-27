package com.taxiflash.ui.screens

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.R
import com.taxiflash.ui.viewmodel.VistaCarrerasViewModel
import com.taxiflash.ui.viewmodel.VistaCarrerasViewModelFactory
import com.taxiflash.ui.data.FormaPago
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import com.taxiflash.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaCarrerasScreen(
    turnoId: String,
    onNuevaCarrera: () -> Unit,
    onNavigateBack: () -> Unit,
    onEditarCarrera: (Long) -> Unit,
    onResumenDiarioClick: () -> Unit,
    onResumenMensualClick: () -> Unit,
    onResumenMensualDetalladoClick: () -> Unit,
    onCerrarTurno: () -> Unit,
    application: Application = LocalContext.current.applicationContext as Application
) {
    val viewModel: VistaCarrerasViewModel = viewModel(
        factory = VistaCarrerasViewModelFactory(application, turnoId)
    )
    val carreras by viewModel.carreras.collectAsState()
    val resumen by viewModel.resumenDia.collectAsState()
    val turnoActivo by viewModel.turnoActivo.collectAsState()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Usar colores del MaterialTheme
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val errorColor = MaterialTheme.colorScheme.error
    val successColor = MaterialTheme.colorScheme.tertiary
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    // Gradiente adaptado para tema claro/oscuro
    val cardBackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            primaryColor,
            primaryColor.copy(alpha = 0.8f)
        )
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    "Eliminar Turno", 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = { 
                Text(
                    "¿Estás seguro de que quieres eliminar este turno y todas sus carreras? Esta acción no se puede deshacer.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarTurnoCompleto {
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = errorColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = surfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Vista Carreras", 
                        color = onPrimaryColor,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Filled.Menu, 
                            "Menú",
                            tint = onPrimaryColor
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.DateRange,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Resumen Diario")
                                }
                            },
                            onClick = {
                                showMenu = false
                                onResumenDiarioClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.CalendarMonth,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Resumen Mensual")
                                }
                            },
                            onClick = {
                                showMenu = false
                                onResumenMensualClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Assessment,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Resumen Mensual Detallado")
                                }
                            },
                            onClick = {
                                showMenu = false
                                onResumenMensualDetalladoClick()
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = null,
                                        tint = errorColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Eliminar Turno", color = errorColor)
                                }
                            },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack, 
                            "Volver",
                            tint = onPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                )
            )
        },
        floatingActionButton = {
            // Alineación simétrica para los botones flotantes con posición fija en la parte inferior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp), // Aumentando el padding horizontal para alejarlos de los bordes
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón cerrar turno (izquierda)
                    FloatingActionButton(
                        onClick = {
                            if (turnoActivo) {
                                onCerrarTurno()
                            }
                        },
                        containerColor = secondaryColor,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Cerrar Turno",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    // Botón nueva carrera (derecha)
                    FloatingActionButton(
                        onClick = {
                            if (turnoActivo) {
                                onNuevaCarrera()
                            }
                        },
                        containerColor = primaryColor,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Nueva carrera",
                            tint = onPrimaryColor
                        )
                    }
                }
            }
        }
        ,
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mensaje si el turno está cerrado
            if (!turnoActivo) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Información",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Este turno ha sido cerrado. No se pueden añadir más carreras.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Resumen del día - mantener esto para información esencial
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBackgroundGradient)
                        .padding(8.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Primera fila: Faltan y Total con texto más grande
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InfoBox(
                                label = if (resumen.faltaPara100 > 0) {
                                    "Pendiente" } else {
                                    "Excede"
                                },
                                value = "${String.format("%.2f", resumen.faltaPara100.let { if (it < 0) -it else it })}€",
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(), // Para que se adapte al espacio disponible
                                valueColor = if (resumen.faltaPara100 > 0) errorColor else successColor,
                                backgroundColor = onPrimaryColor.copy(alpha = 0.1f),
                                valueFontSize = 24.sp,
                                labelFontSize= 18.sp,
                                isLargeText = true
                            )

                            Spacer(modifier = Modifier.width(12.dp))
                            InfoBox(
                                label = "TOTAL",
                                value = String.format("%.2f", resumen.totalImporte),
                                valueColor = onPrimaryColor,
                                backgroundColor = onPrimaryColor.copy(alpha = 0.1f),
                                modifier = Modifier.weight(1f),
                                valueFontSize = 24.sp,
                                isLargeText = true
                            )
                        }

                        // Segunda fila: Carreras, Tarjeta, H.Inicio, H.Trab
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoBox(
                                label = "Carr.",
                                value = resumen.totalCarreras.toString(),
                                modifier = Modifier.weight(1f),
                                backgroundColor = onPrimaryColor.copy(alpha = 0.1f),
                                valueColor = onPrimaryColor,
                                valueFontSize = 22.sp,
                                isLargeText = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            InfoBox(
                                label = "Tarjeta",
                                value = resumen.carrerasTarjeta.toString(),
                                modifier = Modifier.weight(1f),
                                backgroundColor = onPrimaryColor.copy(alpha = 0.1f),
                                valueColor = onPrimaryColor,
                                valueFontSize = 22.sp,
                                isLargeText = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            InfoBox(
                                label = "H.Inic.",
                                value = resumen.horaInicio,
                                modifier = Modifier.weight(1f),
                                backgroundColor = onPrimaryColor.copy(alpha = 0.1f),
                                valueColor = onPrimaryColor,
                                valueFontSize = 18.sp,
                                isLargeText = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            InfoBox(
                                label = "H.Trab",
                                value = calcularTiempoTrabajado(resumen.horaInicio),
                                modifier = Modifier.weight(1f),
                                backgroundColor = onPrimaryColor.copy(alpha = 0.1f),
                                valueColor = onPrimaryColor,
                                valueFontSize = 18.sp,
                                isLargeText = true
                            )
                        }

                        // Tercera fila: Kms. Ini, Propina, Emisora
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoBox(
                                label = "Kms. Ini",
                                value = resumen.kmInicio.toString(),
                                modifier = Modifier.weight(1f),
                                backgroundColor = onPrimaryColor.copy(alpha = 0.1f),
                                valueColor = onPrimaryColor,
                                valueFontSize = 18.sp,
                                isLargeText = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            InfoBox(
                                label = "Propina",
                                value = numberFormat.format(resumen.totalPropinas).replace("€", ""),
                                modifier = Modifier.weight(1f),
                                backgroundColor = onPrimaryColor.copy(alpha = 0.1f),
                                valueColor = onPrimaryColor,
                                valueFontSize = 18.sp,
                                isLargeText = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            InfoBox(
                                label = "Tarjeta",
                                value = numberFormat.format(resumen.sumaTarjetas).replace("€", ""),
                                modifier = Modifier.weight(1f),
                                backgroundColor = onPrimaryColor.copy(alpha = 0.1f),
                                valueColor = onPrimaryColor,
                                valueFontSize = 20.sp,
                                isLargeText = true
                            )
                        }
                    }
                }
            }
            
            // Eliminar las pestañas y mostrar siempre la lista de carreras
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // Encabezado de la tabla
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor)
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "€", 
                            modifier = Modifier.weight(1f), 
                            fontWeight = FontWeight.Bold, 
                            textAlign = TextAlign.Center,
                            color = onPrimaryColor
                        )
                        
                        // Reemplazar texto por icono en el encabezado
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.monedasbuenas),
                                contentDescription = "Forma de Pago",
                                modifier = Modifier.size(28.dp)  // Aumentado tamaño
                            )
                        }
                        
                        Text(
                            "Prop.", 
                            modifier = Modifier.weight(1f), 
                            fontWeight = FontWeight.Bold, 
                            textAlign = TextAlign.Center,
                            color = onPrimaryColor
                        )
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.width(30.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.svg_radio_emisora_blanco),
                                contentDescription = "Emisora",
                                modifier = Modifier.size(32.dp)  // Aumentado tamaño
                            )
                        }
                        
                        Box(
                            contentAlignment = Alignment.CenterEnd,
                            modifier = Modifier.width(30.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.svg_aeropuerto_blanco),
                                contentDescription = "Aeropuerto",
                                modifier = Modifier.size(32.dp)  // Aumentado tamaño
                            )
                        }
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_reloj),
                                contentDescription = "Hora",
                                modifier = Modifier
                                    .size(28.dp)  // Aumentado tamaño
                                    .alpha(0.8f)
                            )
                        }
                    }

                    // Reducir aún más el espacio entre filas
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp) // Eliminado el espacio entre filas
                    ) {
                        items(carreras.sortedByDescending { it.hora }) { carrera ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditarCarrera(carrera.id) },
                                colors = CardDefaults.cardColors(
                                    containerColor = surfaceColor
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 0.5.dp, horizontal = 4.dp), // Reducido aún más el padding vertical
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Importe
                                    Text(
                                        "${String.format("%.2f", carrera.importeReal.toFloat())}", 
                                        modifier = Modifier.weight(1f), 
                                        textAlign = TextAlign.Center,
                                        color = onSurfaceColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    // Forma de pago con iconos más grandes y visibles
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        when (carrera.formaPago) {
                                            FormaPago.EFECTIVO -> {
                                                Image(
                                                    painter = painterResource(id = R.drawable.monedasbuenas),
                                                    contentDescription = "Efectivo",
                                                    modifier = Modifier.size(26.dp)  // Aumentado tamaño
                                                )
                                            }
                                            FormaPago.TARJETA -> {
                                                Image(
                                                    painter = painterResource(R.drawable.tarjeta),
                                                    contentDescription = "Tarjeta",
                                                    modifier = Modifier.size(26.dp)  // Aumentado tamaño
                                                )
                                            }
                                            FormaPago.VALES -> {
                                                Image(
                                                    painter = painterResource(R.drawable.vales),
                                                    contentDescription = "Vales",
                                                    modifier = Modifier.size(26.dp)  // Aumentado tamaño
                                                )
                                            }
                                            else -> {
                                    Text(
                                                    "BIZUM", 
                                        textAlign = TextAlign.Center,
                                                    color = primaryColor
                                    )
                                            }
                                        }
                                    }
                                    
                                    // Propina
                                    Text(
                                        "${String.format("%.2f", carrera.propina.toFloat())}", 
                                        modifier = Modifier.weight(1f), 
                                        textAlign = TextAlign.Center,
                                        color = onSurfaceColor
                                    )
                                    
                                    // Emisora con iconos según el tema - forzar los iconos correctos
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.width(30.dp)
                                    ) {
                                        if (carrera.emisora) {
                                            // Determinar si estamos en modo oscuro de manera más confiable
                                            val isDarkTheme = isSystemInDarkTheme()
                                            // Garantizar el uso del icono correcto según el tema
                                            Image(
                                                painter = painterResource(
                                                    id = if (isDarkTheme) 
                                                            R.drawable.svg_radio_emisora_blanco
                                                         else 
                                                            R.drawable.svg_radio_emisora
                                                ),
                                                contentDescription = "Emisora",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    
                                    // Aeropuerto con iconos según el tema - forzar los iconos correctos
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.width(30.dp)
                                    ) {
                                        if (carrera.aeropuerto) {
                                            // Determinar si estamos en modo oscuro de manera más confiable
                                            val isDarkTheme = isSystemInDarkTheme()
                                            // Garantizar el uso del icono correcto según el tema
                                            Image(
                                                painter = painterResource(
                                                    id = if (isDarkTheme) 
                                                            R.drawable.svg_aeropuerto_blanco
                                                         else 
                                                            R.drawable.svg_aeropuerto_b
                                                ),
                                                contentDescription = "Aeropuerto",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    
                                    // Hora
                                    Text(
                                        carrera.hora, 
                                        modifier = Modifier.weight(1f), 
                                        textAlign = TextAlign.End,
                                        color = onSurfaceColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Al final del composable, justo antes de cerrar el Scaffold
        // añadir el indicador de carga
        LoadingIndicator(isLoading = viewModel.isLoading.collectAsState().value)
    }
}

// Componente de información con fondo y borde redondeado
@Composable
fun InfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    valueColor: Color = Color.White,
    labelFontSize: TextUnit = 12.sp,
    valueFontSize: TextUnit = 18.sp,
    isLargeText: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                style = TextStyle(
                    fontSize = labelFontSize,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = value,
                color = valueColor,
                style = TextStyle(
                    fontSize = if (isLargeText) valueFontSize else 14.sp,
                    fontWeight = if (isLargeText) FontWeight.Bold else FontWeight.SemiBold
                )
            )
        }
    }
}

// Calcular el tiempo trabajado desde la hora de inicio
fun calcularTiempoTrabajado(horaInicio: String): String {
    try {
        val formato = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaInicioDate = formato.parse(horaInicio) ?: return "00:00"
        
        val ahora = Calendar.getInstance()
        val horaActual = formato.parse(formato.format(ahora.time)) ?: return "00:00"
        
        val diferencia = horaActual.time - horaInicioDate.time
        val horas = diferencia / (60 * 60 * 1000)
        val minutos = (diferencia % (60 * 60 * 1000)) / (60 * 1000)
        
        return String.format("%02d:%02d", horas, minutos)
    } catch (e: Exception) {
        return "00:00"
    }
}