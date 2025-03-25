package com.taxiflash.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.FormaPago
import com.taxiflash.ui.viewmodel.EdicionDiaViewModel
import com.taxiflash.ui.viewmodel.EdicionDiaViewModelFactory
import com.taxiflash.ui.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdicionDiaScreen(
    fechaStr: String, // Formato: "yyyy-MM-dd"
    onNavigateBack: () -> Unit,
    onEditarCarrera: (Long) -> Unit
) {
    val context = LocalContext.current
    val viewModel: EdicionDiaViewModel = viewModel(
        factory = EdicionDiaViewModelFactory(context.applicationContext as android.app.Application, fechaStr)
    )
    
    // Log para depuración
    Log.d("EdicionDiaScreen", "Pantalla iniciada con fecha: $fechaStr")
    
    val turnos by viewModel.turnos.collectAsState()
    val carreras by viewModel.carreras.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Log para depuración
    LaunchedEffect(turnos) {
        Log.d("EdicionDiaScreen", "Turnos recibidos: ${turnos.size}")
        turnos.forEach { turno ->
            Log.d("EdicionDiaScreen", "Turno recibido: ${turno.idTurno}, horaInicio: ${turno.horaInicio}, kmInicio: ${turno.kmInicio}")
        }
    }
    
    LaunchedEffect(carreras) {
        Log.d("EdicionDiaScreen", "Carreras recibidas: ${carreras.size}")
    }
    
    // Estados para edición de campos
    var editandoKilometros by remember { mutableStateOf(false) }
    var kmInicio by remember { mutableStateOf("") }
    var kmFin by remember { mutableStateOf("") }
    
    var editandoHorario by remember { mutableStateOf(false) }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    
    var turnoSeleccionadoId by remember { mutableStateOf<String?>(null) }
    
    // Actualizar valores cuando los turnos cambian
    LaunchedEffect(turnos) {
        if (turnos.isNotEmpty()) {
            val primerTurno = turnos.firstOrNull()
            turnoSeleccionadoId = primerTurno?.idTurno
            kmInicio = primerTurno?.kmInicio?.toString() ?: ""
            kmFin = primerTurno?.kmFin?.toString() ?: ""
            horaInicio = primerTurno?.horaInicio ?: ""
            horaFin = primerTurno?.horaFin ?: ""
            
            Log.d("EdicionDiaScreen", "Valores actualizados desde turnos: kmInicio=$kmInicio, kmFin=$kmFin, horaInicio=$horaInicio, horaFin=$horaFin")
        } else {
            Log.d("EdicionDiaScreen", "No hay turnos para mostrar")
        }
    }
    
    // Formatear la fecha para mostrarla en el título
    val fechaFormateada = remember(fechaStr) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(fechaStr)
            date?.let { outputFormat.format(it) } ?: fechaStr
        } catch (e: Exception) {
            fechaStr
        }
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Editar Día: $fechaFormateada", 
                        color = onPrimaryColor,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack, 
                            "Volver",
                            tint = onPrimaryColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.guardarCambios() }) {
                        Icon(
                            Icons.Filled.Save, 
                            "Guardar Cambios",
                            tint = onPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Sección de selección de turno (si hay múltiples)
            if (turnos.size > 1) {
                Text(
                    "Seleccionar Turno",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Selector de turno
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    turnos.forEach { turno ->
                        OutlinedButton(
                            onClick = { 
                                turnoSeleccionadoId = turno.idTurno
                                kmInicio = turno.kmInicio.toString()
                                kmFin = turno.kmFin.toString()
                                horaInicio = turno.horaInicio
                                horaFin = turno.horaFin
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (turnoSeleccionadoId == turno.idTurno) 
                                    MaterialTheme.colorScheme.primaryContainer
                                else 
                                    MaterialTheme.colorScheme.surface
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp,
                                brush = SolidColor(
                                    if (turnoSeleccionadoId == turno.idTurno) 
                                        MaterialTheme.colorScheme.primary
                                    else 
                                        MaterialTheme.colorScheme.outline
                                )
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.DateRange,
                                    contentDescription = null,
                                    tint = if (turnoSeleccionadoId == turno.idTurno) 
                                        MaterialTheme.colorScheme.primary
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Turno ${turno.numeroTurno}",
                                    color = if (turnoSeleccionadoId == turno.idTurno) 
                                        MaterialTheme.colorScheme.primary
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            
            // Secciones de edición
            if (turnos.isEmpty()) {
                // Mostrar mensaje cuando no hay turnos
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
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
                            "No se encontraron turnos para esta fecha",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Fecha consultada: $fechaStr",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Volver")
                        }
                    }
                }
            } else {
                // Si hay turnos, mostrar la sección de edición
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = surfaceColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Sección de kilómetros
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Kilómetros",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            
                            IconButton(onClick = { editandoKilometros = !editandoKilometros }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Editar kilómetros",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        if (editandoKilometros) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = kmInicio,
                                    onValueChange = { kmInicio = it },
                                    label = { Text("Km. Inicio") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                                
                                OutlinedTextField(
                                    value = kmFin,
                                    onValueChange = { kmFin = it },
                                    label = { Text("Km. Fin") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                            }
                            
                            Button(
                                onClick = { 
                                    turnoSeleccionadoId?.let { id ->
                                        viewModel.actualizarKilometros(
                                            id,
                                            kmInicio.toIntOrNull() ?: 0,
                                            kmFin.toIntOrNull() ?: 0
                                        )
                                        editandoKilometros = false
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Guardar Kilómetros")
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Inicio", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        kmInicio,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Fin", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        kmFin,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "${kmFin.toIntOrNull()?.minus(kmInicio.toIntOrNull() ?: 0) ?: 0}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                        
                        Divider()
                        
                        // Sección de horarios
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Horarios",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            
                            IconButton(onClick = { editandoHorario = !editandoHorario }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Editar horarios",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        if (editandoHorario) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = horaInicio,
                                    onValueChange = { horaInicio = it },
                                    label = { Text("Hora Inicio") },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("HH:MM") }
                                )
                                
                                OutlinedTextField(
                                    value = horaFin,
                                    onValueChange = { horaFin = it },
                                    label = { Text("Hora Fin") },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("HH:MM") }
                                )
                            }
                            
                            Button(
                                onClick = { 
                                    turnoSeleccionadoId?.let { id ->
                                        viewModel.actualizarHorarios(
                                            id,
                                            horaInicio,
                                            horaFin
                                        )
                                        editandoHorario = false
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Guardar Horarios")
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Inicio", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        horaInicio,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Fin", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        horaFin,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Lista de carreras
            Text(
                "Carreras del día",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (carreras.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        "No hay carreras registradas en esta fecha",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Encabezado de la tabla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor)
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Hora", 
                        modifier = Modifier.weight(1f), 
                        fontWeight = FontWeight.Bold,
                        color = onPrimaryColor
                    )
                    Text(
                        "Importe", 
                        modifier = Modifier.weight(1f), 
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = onPrimaryColor
                    )
                    Text(
                        "Forma Pago", 
                        modifier = Modifier.weight(1f), 
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = onPrimaryColor
                    )
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(carreras.sortedBy { it.hora }) { carrera ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditarCarrera(carrera.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = surfaceColor
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    carrera.hora,
                                    modifier = Modifier.weight(1f),
                                    color = onSurfaceColor
                                )
                                
                                Text(
                                    "${String.format("%.2f", carrera.importeReal.toFloat())}€",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                
                                Text(
                                    when(carrera.formaPago) {
                                        FormaPago.EFECTIVO -> "Efectivo"
                                        FormaPago.TARJETA -> "Tarjeta"
                                        FormaPago.VALES -> "Vales"
                                        FormaPago.BIZUM -> "Bizum"
                                        else -> "Efectivo"
                                    },
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
        
        // Indicador de carga
        LoadingIndicator(isLoading = isLoading)
    }
} 