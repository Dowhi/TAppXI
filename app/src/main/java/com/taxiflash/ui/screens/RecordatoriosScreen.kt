package com.taxiflash.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.taxiflash.ui.components.TaxiFlashScaffold
import com.taxiflash.ui.theme.TaxiBlue
import com.taxiflash.ui.theme.TaxiGreen
import com.taxiflash.ui.theme.TaxiRed
import com.taxiflash.ui.theme.TaxiYellow
import com.taxiflash.ui.viewmodel.RecordatoriosViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.animation.AnimatedVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordatoriosScreen(
    navController: NavController,
    viewModel: RecordatoriosViewModel = viewModel()
) {
    // Estado para mostrar diálogos
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var recordatorioAEliminar by remember { mutableStateOf<Recordatorio?>(null) }
    
    // Contexto para mostrar mensajes Toast
    val context = LocalContext.current
    
    // Obtener recordatorios
    val recordatorios by viewModel.recordatorios.collectAsState(initial = emptyList())
    
    // Cargar recordatorios
    LaunchedEffect(Unit) {
        viewModel.cargarRecordatorios()
    }
    
    TaxiFlashScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recordatorios",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Fondo con gradiente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
            
            // Contenido
            if (recordatorios.isEmpty()) {
                // Mensaje cuando no hay recordatorios
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(80.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No tienes recordatorios",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Añade recordatorios para fechas importantes como ITV, cambio de aceite, IRPF, etc.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { mostrarDialogoNuevo = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir recordatorio")
                    }
                }
            } else {
                // Lista de recordatorios
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = "Próximos recordatorios",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(recordatorios.sortedBy { it.fecha }) { recordatorio ->
                        RecordatorioItem(
                            recordatorio = recordatorio,
                            onEliminar = {
                                recordatorioAEliminar = recordatorio
                                mostrarDialogoEliminar = true
                            },
                            onEditar = {
                                viewModel.seleccionarRecordatorio(recordatorio)
                                mostrarDialogoNuevo = true
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            
            // Botón flotante para añadir recordatorio
            FloatingActionButton(
                onClick = { 
                    viewModel.limpiarSeleccion()
                    mostrarDialogoNuevo = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir recordatorio"
                )
            }
        }
        
        // Diálogo para añadir/editar recordatorio
        if (mostrarDialogoNuevo) {
            NuevoRecordatorioDialog(
                onDismiss = { mostrarDialogoNuevo = false },
                onGuardar = { tipo, titulo, fecha, descripcion, horaAviso, horaAviso2 ->
                    viewModel.guardarRecordatorio(tipo, titulo, fecha, descripcion, horaAviso, horaAviso2)
                    Toast.makeText(
                        context,
                        "Recordatorio guardado",
                        Toast.LENGTH_SHORT
                    ).show()
                    mostrarDialogoNuevo = false
                },
                recordatorio = viewModel.recordatorioSeleccionado.value
            )
        }
        
        // Diálogo para eliminar recordatorio
        if (mostrarDialogoEliminar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoEliminar = false },
                title = { Text("Eliminar Recordatorio") },
                text = { Text("¿Estás seguro de que deseas eliminar este recordatorio?") },
                confirmButton = {
                    Button(
                        onClick = {
                            recordatorioAEliminar?.let { viewModel.eliminarRecordatorio(it.id) }
                            Toast.makeText(
                                context,
                                "Recordatorio eliminado",
                                Toast.LENGTH_SHORT
                            ).show()
                            mostrarDialogoEliminar = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { mostrarDialogoEliminar = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun RecordatorioItem(
    recordatorio: Recordatorio,
    onEliminar: () -> Unit,
    onEditar: () -> Unit
) {
    // Calcular días restantes
    val diasRestantes = calcularDiasRestantes(recordatorio.fecha)
    
    // Determinar color según proximidad
    val colorFondo = when {
        diasRestantes < 0 -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        diasRestantes < 7 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        diasRestantes < 30 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    // Determinar icono según tipo
    val icono = when (recordatorio.tipo) {
        TipoRecordatorio.ITV -> Icons.Default.DirectionsCar
        TipoRecordatorio.CAMBIO_ACEITE -> Icons.Default.LocalGasStation
        TipoRecordatorio.IRPF -> Icons.Default.Receipt
        TipoRecordatorio.OTRO -> Icons.Default.CalendarMonth
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorFondo
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recordatorio.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = formatearFecha(recordatorio.fecha),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (recordatorio.descripcion.isNotEmpty()) {
                    Text(
                        text = recordatorio.descripcion,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Días restantes
                val textoRestante = when {
                    diasRestantes < 0 -> "Vencido hace ${-diasRestantes} días"
                    diasRestantes == 0 -> "¡Hoy!"
                    diasRestantes == 1 -> "Mañana"
                    else -> "En $diasRestantes días"
                }
                
                val colorTexto = when {
                    diasRestantes < 0 -> MaterialTheme.colorScheme.error
                    diasRestantes < 7 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                }
                
                Text(
                    text = textoRestante,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
            }
            
            // Botones
            IconButton(onClick = onEditar) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoRecordatorioDialog(
    onDismiss: () -> Unit,
    onGuardar: (TipoRecordatorio, String, Long, String, String, String) -> Unit,
    recordatorio: Recordatorio? = null
) {
    // Estados para los campos
    var titulo by remember { mutableStateOf(recordatorio?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(recordatorio?.descripcion ?: "") }
    var tipoSeleccionado by remember { mutableStateOf(recordatorio?.tipo ?: TipoRecordatorio.ITV) }
    var horaAviso by remember { mutableStateOf(recordatorio?.horaAviso ?: "09:00") }
    var horaAviso2 by remember { mutableStateOf(recordatorio?.horaAviso2 ?: "") }
    var mostrarMenuTipo by remember { mutableStateOf(false) }
    
    // Estado para el segundo aviso
    var segundoAvisoActivo by remember { mutableStateOf(recordatorio?.horaAviso2?.isNotEmpty() ?: false) }
    
    // Estado para el selector de fecha
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = recordatorio?.fecha ?: System.currentTimeMillis()
    )
    var mostrarDatePicker by remember { mutableStateOf(false) }
    
    // Estado para el selector de hora
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker2 by remember { mutableStateOf(false) }
    
    // Validación
    val tituloValido = titulo.isNotBlank()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = if (recordatorio == null) "Nuevo Recordatorio" else "Editar Recordatorio",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Selector de tipo
                OutlinedTextField(
                    value = tipoSeleccionado.name.replace("_", " "),
                    onValueChange = { },
                    label = { Text("Tipo") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarMenuTipo = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Seleccionar tipo",
                                modifier = Modifier.rotate(270f)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = mostrarMenuTipo,
                            onDismissRequest = { mostrarMenuTipo = false }
                        ) {
                            TipoRecordatorio.values().forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo.name.replace("_", " ")) },
                                    onClick = {
                                        tipoSeleccionado = tipo
                                        mostrarMenuTipo = false
                                    }
                                )
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Campo de título
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !tituloValido
                )
                
                if (!tituloValido) {
                    Text(
                        text = "El título es obligatorio",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selector de fecha
                OutlinedTextField(
                    value = formatearFecha(datePickerState.selectedDateMillis ?: System.currentTimeMillis()),
                    onValueChange = { },
                    label = { Text("Fecha") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Seleccionar fecha"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selector de hora de aviso
                OutlinedTextField(
                    value = horaAviso,
                    onValueChange = { },
                    label = { Text("Hora del primer aviso") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Seleccionar hora"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Switch para activar/desactivar segundo aviso
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Añadir segundo aviso",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = segundoAvisoActivo,
                        onCheckedChange = { 
                            segundoAvisoActivo = it
                            if (!it) {
                                horaAviso2 = ""
                            } else if (horaAviso2.isEmpty()) {
                                horaAviso2 = "18:00" // Valor por defecto
                            }
                        }
                    )
                }
                
                // Selector de segunda hora de aviso (solo si está activo)
                AnimatedVisibility(visible = segundoAvisoActivo) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = horaAviso2,
                            onValueChange = { },
                            label = { Text("Hora del segundo aviso") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { mostrarTimePicker2 = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Seleccionar segunda hora"
                                    )
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Campo de descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tituloValido) {
                        onGuardar(
                            tipoSeleccionado,
                            titulo,
                            datePickerState.selectedDateMillis ?: System.currentTimeMillis(),
                            descripcion,
                            horaAviso,
                            if (segundoAvisoActivo) horaAviso2 else ""
                        )
                        onDismiss()
                    }
                },
                enabled = tituloValido
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
    
    // Selector de fecha
    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                Button(onClick = { mostrarDatePicker = false }) {
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
    
    // Selector de hora (primer aviso)
    if (mostrarTimePicker) {
        TimePickerDialog(
            onDismissRequest = { mostrarTimePicker = false },
            horaActual = horaAviso,
            onConfirm = { hora, minuto ->
                horaAviso = String.format("%02d:%02d", hora, minuto)
                mostrarTimePicker = false
            }
        )
    }
    
    // Selector de hora (segundo aviso)
    if (mostrarTimePicker2) {
        TimePickerDialog(
            onDismissRequest = { mostrarTimePicker2 = false },
            horaActual = horaAviso2.takeIf { it.isNotEmpty() } ?: "18:00",
            onConfirm = { hora, minuto ->
                horaAviso2 = String.format("%02d:%02d", hora, minuto)
                mostrarTimePicker2 = false
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    horaActual: String,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(horaActual.split(":")[0].toInt()) }
    var selectedMinute by remember { mutableStateOf(horaActual.split(":")[1].toInt()) }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Seleccionar hora") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selector de hora
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Horas
                    IconButton(
                        onClick = {
                            selectedHour = if (selectedHour > 0) selectedHour - 1 else 23
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Hora anterior",
                            modifier = Modifier.rotate(90f)
                        )
                    }
                    
                    Text(
                        text = String.format("%02d", selectedHour),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    IconButton(
                        onClick = {
                            selectedHour = if (selectedHour < 23) selectedHour + 1 else 0
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Hora siguiente",
                            modifier = Modifier.rotate(270f)
                        )
                    }
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Minutos
                    IconButton(
                        onClick = {
                            selectedMinute = if (selectedMinute > 0) selectedMinute - 1 else 59
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Minuto anterior",
                            modifier = Modifier.rotate(90f)
                        )
                    }
                    
                    Text(
                        text = String.format("%02d", selectedMinute),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    IconButton(
                        onClick = {
                            selectedMinute = if (selectedMinute < 59) selectedMinute + 1 else 0
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Minuto siguiente",
                            modifier = Modifier.rotate(270f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedHour, selectedMinute) }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancelar")
            }
        }
    )
}

// Función para calcular días restantes
fun calcularDiasRestantes(fechaMillis: Long): Int {
    val hoy = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val diferencia = fechaMillis - hoy
    return (TimeUnit.MILLISECONDS.toDays(diferencia)).toInt()
}

// Función para formatear fecha
fun formatearFecha(fechaMillis: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    return formato.format(Date(fechaMillis))
}

// Añadir la clase Recordatorio con el campo horaAviso
data class Recordatorio(
    val id: String = "",
    val tipo: TipoRecordatorio = TipoRecordatorio.OTRO,
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: Long = 0,
    val horaAviso: String = "09:00", // Hora por defecto: 9:00 AM
    val horaAviso2: String = ""      // Segunda hora de aviso (opcional)
)

enum class TipoRecordatorio {
    ITV, CAMBIO_ACEITE, IRPF, OTRO
} 