package com.taxiflash.ui.screens

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.primaryContainerColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.data.Turno
import com.taxiflash.ui.viewmodel.ResumenDiarioViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenDiarioScreen(
    fecha: String,
    onNavigateBack: () -> Unit,
    viewModel: ResumenDiarioViewModel = viewModel(),
    onEditarDia: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val primaryColor = Color(0xFF1E3F8B) // Color principal azul oscuro
    var showDatePicker by remember { mutableStateOf(false) }
    
    var selectedDate by remember { mutableStateOf(parseFecha(fecha)) }
    
    // Formatear la fecha para mostrarla en el título
    val fechaFormateada = remember(selectedDate) {
        try {
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
            outputFormat.format(selectedDate)
        } catch (e: Exception) {
            fecha
        }
    }
    
    // Formatear la fecha para edición (yyyy-MM-dd)
    val fechaParaEdicion = remember(selectedDate) {
        try {
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES"))
            val resultado = outputFormat.format(selectedDate)
            Log.d("ResumenDiarioScreen", "Fecha formateada para edición: $fecha -> $resultado")
            resultado
        } catch (e: Exception) {
            Log.e("ResumenDiarioScreen", "Error al formatear fecha: ${e.message}", e)
            fecha
        }
    }

    LaunchedEffect(selectedDate) {
        viewModel.cargarResumenDiario(selectedDate)
        viewModel.cargarTurnosDia(selectedDate)
    }

    val resumen by viewModel.resumenDiario.collectAsState()
    val turnos by viewModel.turnos.collectAsState()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    showDeleteDialog?.let { turnoId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Turno", fontWeight = FontWeight.Bold) },
            text = { Text("¿Quieres eliminar este turno? Esta acción no se puede deshacer.") },
            icon = { Icon(Icons.Outlined.Info, null, tint = Color.Red) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarTurno(turnoId) { showDeleteDialog = null }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Resumen Diario",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = fechaFormateada,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = "Seleccionar Fecha",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { 
                        try {
                            Log.d("ResumenDiarioScreen", "Enviando fecha para edición: $fechaFormateada")
                            onEditarDia(fechaFormateada)
                        } catch (e: Exception) {
                            Log.e("ResumenDiarioScreen", "Error al formatear fecha para edición: ${e.message}")
                        }
                    }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Editar día",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Selector de fecha con flechas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { time = selectedDate }
                            cal.add(Calendar.DAY_OF_MONTH, -1)
                            selectedDate = cal.time
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            "Día anterior",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = fechaFormateada,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { time = selectedDate }
                            cal.add(Calendar.DAY_OF_MONTH, 1)
                            selectedDate = cal.time
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowForward, 
                            "Día siguiente",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))

                        // Tarjeta de resumen
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            elevation = CardDefaults.elevatedCardElevation(
                                defaultElevation = 4.dp
                            ),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color.Blue
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp)
                            ) {

                                // Encabezado de la tabla
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "",
                                            modifier = Modifier.weight(1.5f),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        if (resumen.turnos.isNotEmpty()) {
                                            resumen.turnos.forEach { turno ->
                                                Text(
                                                    "Turno ${turno.numeroTurno}",
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.End,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Filas de datos
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    if (resumen.turnos.isNotEmpty()) {
                                        FilaResumenModernizada("Carreras", resumen.turnos.map { it.totalCarreras.toString() })
                                        FilaResumenModernizada("C. Tarjeta", resumen.turnos.map { it.carrerasTarjeta.toString() })
                                        FilaResumenModernizada("C. Emisora", resumen.turnos.map { it.carrerasEmisora.toString() })
                                        FilaResumenModernizada("Suma Tarjetas", resumen.turnos.map {
                                            numberFormat.format(it.sumaTarjetas).replace("€", "")
                                        })
                                        FilaResumenModernizada("Suma Emisora", resumen.turnos.map {
                                            numberFormat.format(it.sumaEmisora).replace("€", "")
                                        })
                                        FilaResumenModernizada("Km inicial", resumen.turnos.map { it.kmInicio.toString() })
                                        FilaResumenModernizada("Km final", resumen.turnos.map { it.kmFin.toString() })
                                        FilaResumenModernizada("Hora Inicio", resumen.turnos.map { it.horaInicio })
                                        FilaResumenModernizada("Hora Fin", resumen.turnos.map { it.horaFin })

                                        Divider(modifier = Modifier.padding(vertical = 1.dp))

                                        FilaResumenModernizada(
                                            "TOTAL",
                                            resumen.turnos.map { numberFormat.format(it.totalImporte).replace("€", "") },
                                            destacado = true,
                                            backgroundColor = Color.Blue
                                        )
                                    } else {
                                        Text(
                                            "No hay datos disponibles para esta fecha",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            textAlign = TextAlign.Center,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Lista de turnos del día
                    items(turnos) { turno ->
                        TurnoCardModernizada(turno, primaryColor) { showDeleteDialog = turno.idTurno }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
            
            // DatePicker Dialog
            if (showDatePicker) {
                DatePickerDialogModernizado(
                    onDismissRequest = { showDatePicker = false },
                    onDateSelected = { selectedDate = it; showDatePicker = false }
                )
            }
        }
    }
}

// ⬇️ ⬇️ Aquí vienen las funciones complementarias ⬇️ ⬇️

@Composable
fun FilaResumenModernizada(
    label: String,
    valores: List<String>,
    destacado: Boolean = false,
    backgroundColor: Color = Color.Transparent
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)),
        color = if (destacado) backgroundColor else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
                style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            valores.forEach { valor ->
                Text(
                    text = valor,
                    fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
                    style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DatePickerDialogModernizado(
    onDismissRequest: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        onDateSelected(date)
                    }
                    onDismissRequest()
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TurnoCardModernizada(
    turno: Turno,
    primaryColor: Color,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Turno ${turno.numeroTurno}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )

                FilledTonalIconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.Red.copy(alpha = 0.1f),
                        contentColor = Color.Red
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar turno")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(label = "Horario", value = "${turno.horaInicio} - ${turno.horaFin}", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                InfoChip(label = "Kilómetros", value = "${turno.kmInicio} - ${turno.kmFin}", modifier = Modifier.weight(1f))
            }
        }
    }
}

fun parseFecha(fecha: String): Date = SimpleDateFormat("ddMMyyyy", Locale("es", "ES")).parse(fecha) ?: Date()

@Composable
fun InfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}