package com.taxiflash.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.viewmodel.UsuarioViewModel
import java.time.LocalDate

/**
 * Pantalla para recuperar el PIN
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarPinScreen(
    onNavigateBack: () -> Unit,
    viewModel: UsuarioViewModel = viewModel()
) {
    val dni by viewModel.dni.collectAsState()
    val fechaNacimientoTexto by viewModel.fechaNacimientoTexto.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    
    // Estado para mostrar el selector de fecha
    var mostrarDatePicker by remember { mutableStateOf(false) }
    
    // Estado para mostrar el PIN recuperado
    var pinRecuperado by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary
    
    // Date Picker
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val fecha = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            viewModel.updateFechaNacimiento(fecha)
                        }
                        mostrarDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDatePicker = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recuperar PIN",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Formulario de recuperación
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Recuperar PIN",
                        style = MaterialTheme.typography.titleLarge,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "Ingresa tu DNI y fecha de nacimiento para recuperar tu PIN",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Campo DNI
                    OutlinedTextField(
                        value = dni,
                        onValueChange = { viewModel.updateDni(it) },
                        label = { Text("DNI") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = "DNI")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor
                        )
                    )
                    
                    // Campo Fecha de Nacimiento
                    OutlinedTextField(
                        value = fechaNacimientoTexto,
                        onValueChange = { /* No se cambia manualmente */ },
                        label = { Text("Fecha de Nacimiento") },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = "Fecha")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor
                        ),
                        trailingIcon = {
                            IconButton(onClick = { mostrarDatePicker = true }) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = "Seleccionar Fecha"
                                )
                            }
                        }
                    )
                    
                    // Mostrar PIN recuperado
                    pinRecuperado?.let { pin ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Tu PIN es:",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = pin,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Text(
                                    text = "Guárdalo en un lugar seguro",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    
                    // Mensaje de error
                    mensajeError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Botón de recuperación
                    Button(
                        onClick = { 
                            viewModel.recuperarPin { pin ->
                                pinRecuperado = pin
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        enabled = pinRecuperado == null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = "Recuperar"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recuperar PIN")
                    }
                    
                    TextButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver al Login")
                    }
                }
            }
        }
    }
    
    // Limpiar mensajes de error al cambiar cualquier campo
    LaunchedEffect(dni, fechaNacimientoTexto) {
        viewModel.resetError()
    }
} 