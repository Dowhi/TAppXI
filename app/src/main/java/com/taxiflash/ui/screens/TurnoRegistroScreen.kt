package com.taxiflash.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.components.TaxiFlashScaffold
import com.taxiflash.ui.navigation.NavRoutes
import com.taxiflash.ui.theme.TaxiYellow
import com.taxiflash.ui.viewmodel.TurnoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnoRegistroScreen(
    navController: NavController,
    viewModel: TurnoViewModel = viewModel()
) {
    // Determinar si estamos en modo edición
    val editMode = false // Esto debería determinarse según el estado del ViewModel
    
    val kmInicio by viewModel.kmInicio.collectAsState()
    val horaInicio by viewModel.horaInicio.collectAsState()
    val numeroTurno by viewModel.numeroTurno.collectAsState()
    val turnoGuardado by viewModel.turnoGuardado.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val kmInicioError by viewModel.kmInicioError.collectAsState()
    val fechaSeleccionadaStr by viewModel.fechaSeleccionada.collectAsState()
    val puedeCrearTurno by viewModel.puedeCrearTurno.collectAsState()
    val turnoActivoInfo by viewModel.turnoActivoInfo.collectAsState()
    
    // Estado para la fecha seleccionada en milisegundos
    // Inicializar con el timestamp actual
    var fechaSeleccionadaMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    
    // Efecto para navegar de vuelta cuando se guarda el turno
    LaunchedEffect(turnoGuardado) {
        if (turnoGuardado != null) {
            navController.navigate(NavRoutes.INICIO) {
                popUpTo(NavRoutes.INICIO)
                launchSingleTop = true
            }
        }
    }
    
    // Verificar si hay un turno activo al iniciar la pantalla
    LaunchedEffect(Unit) {
        viewModel.verificarTurnoActivo()
    }
    
    // Si hay un turno activo, mostrar un mensaje y redirigir
    LaunchedEffect(puedeCrearTurno) {
        if (!puedeCrearTurno) {
            // Hay un turno activo, navegar a la pantalla de vista de carreras
            turnoActivoInfo?.let { turno ->
                navController.navigate("vista_carreras/${turno.idTurno}") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        }
    }
    
    // Mostrar DatePicker si se solicita
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fechaSeleccionadaMs)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { 
                        fechaSeleccionadaMs = it 
                        // Actualizar el ViewModel con la nueva fecha
                        val formatoFechaVM = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        viewModel.updateFechaSeleccionada(formatoFechaVM.format(Date(it)))
                    }
                    mostrarDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    TaxiFlashScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Comenzar Turno",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TaxiYellow,
                    titleContentColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!puedeCrearTurno) {
                // Mostrar mensaje de que ya hay un turno activo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ya hay un turno activo",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Debes cerrar el turno actual antes de iniciar uno nuevo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                turnoActivoInfo?.let { turno ->
                                    navController.navigate("vista_carreras/${turno.idTurno}")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Ir al turno activo")
                        }
                    }
                }
            } else {
                // Título del turno
                Text(
                    text = "Turno $numeroTurno",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // Campo de fecha con selector mejorado
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Fecha del turno",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatoFecha.format(Date(fechaSeleccionadaMs)),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Button(
                                onClick = { 
                                    mostrarDatePicker = true
                                    println("DEBUG: Mostrando DatePicker: $mostrarDatePicker")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Cambiar fecha"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cambiar fecha")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo para kilómetros iniciales
                OutlinedTextField(
                    value = kmInicio,
                    onValueChange = { viewModel.updateKmInicio(it) },
                    label = { Text("Kilómetros iniciales") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = kmInicioError,
                    supportingText = if (kmInicioError) {
                        { Text("Introduce un valor válido") }
                    } else null
                )
                
                // Mostrar hora de inicio
                Text(
                    text = "Hora de inicio: $horaInicio",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
                
                // Mostrar mensaje de error si existe
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Botón para guardar turno
                Button(
                    onClick = {
                        // Convertir la fecha seleccionada en el formato requerido por el ViewModel
                        val formatoViewModel = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
                        val fechaFormateada = formatoViewModel.format(Date(fechaSeleccionadaMs))
                        
                        // Guardar turno con la fecha seleccionada
                        viewModel.guardarTurnoConFecha(kmInicio, fechaFormateada)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !kmInicioError && kmInicio.isNotBlank()
                ) {
                    Text("Comenzar Turno")
                }
            }
        }
    }
} 