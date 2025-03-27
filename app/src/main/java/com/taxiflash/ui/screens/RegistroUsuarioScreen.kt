package com.taxiflash.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.components.TaxiFlashScaffold
import com.taxiflash.ui.viewmodel.UsuarioViewModel
import java.time.LocalDate

/**
 * Pantalla de registro de usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroUsuarioScreen(
    onNavigateBack: () -> Unit,
    onRegistroExitoso: () -> Unit,
    onRecuperarClick: () -> Unit,
    viewModel: UsuarioViewModel = viewModel(),
    navController: androidx.navigation.NavController
) {
    val nombre by viewModel.nombre.collectAsState()
    val pin by viewModel.pin.collectAsState()
    val dni by viewModel.dni.collectAsState()
    val fechaNacimientoTexto by viewModel.fechaNacimientoTexto.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    
    // Estado para mostrar el selector de fecha
    var mostrarDatePicker by remember { mutableStateOf(false) }
    
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
    
    TaxiFlashScaffold(
        navController = navController,
        showBottomBar = false,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registro de Usuario",
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
            // Formulario de registro
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
                        text = "Datos del Taxista",
                        style = MaterialTheme.typography.titleLarge,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Campo Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { viewModel.updateNombre(it) },
                        label = { Text("Nombre completo") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Nombre")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor
                        )
                    )
                    
                    // Campo PIN
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { viewModel.updatePin(it) },
                        label = { Text("PIN (4 dígitos)") },
                        leadingIcon = {
                            Icon(Icons.Default.Password, contentDescription = "PIN")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor
                        )
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
                    
                    // Mensaje de error
                    mensajeError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Botón de registro
                    Button(
                        onClick = { viewModel.registrarUsuario(onSuccess = onRegistroExitoso) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Registrar"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Usuario")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Opciones adicionales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { onNavigateBack() }
                ) {
                    Text("Ya tengo una cuenta")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                TextButton(
                    onClick = onRecuperarClick
                ) {
                    Text("Olvidé mi PIN")
                }
            }
        }
    }
    
    // Limpiar mensajes de error al cambiar cualquier campo
    LaunchedEffect(nombre, pin, dni, fechaNacimientoTexto) {
        viewModel.resetError()
    }
} 