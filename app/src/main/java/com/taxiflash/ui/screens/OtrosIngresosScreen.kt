package com.taxiflash.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.taxiflash.ui.components.LoadingIndicator
import com.taxiflash.ui.components.TaxiFlashScaffold
import com.taxiflash.ui.data.OtrosIngresos
import com.taxiflash.ui.theme.TaxiYellow
import com.taxiflash.ui.viewmodel.OtrosIngresosViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtrosIngresosScreen(
    navController: NavController,
    viewModel: OtrosIngresosViewModel = viewModel()
) {
    // Collectores para los estados del ViewModel
    val concepto by viewModel.concepto.collectAsState()
    val fecha by viewModel.fecha.collectAsState()
    val importe by viewModel.importe.collectAsState()
    val descripcion by viewModel.descripcion.collectAsState()
    val notas by viewModel.notas.collectAsState()
    val guardadoExitoso by viewModel.guardadoExitoso.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val listaIngresos by viewModel.listaIngresos.collectAsState()
    val totalIngresosMes by viewModel.totalIngresosMes.collectAsState()
    val mesSeleccionado by viewModel.mesSeleccionado.collectAsState()
    val anioSeleccionado by viewModel.anioSeleccionado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val ingresoSeleccionadoId by viewModel.ingresoSeleccionadoId.collectAsState()
    
    // Formateo de números y fechas
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    
    // Mostrar snackbar si hay un éxito o error
    val context = LocalContext.current
    
    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) {
            Toast.makeText(
                context,
                "Ingreso guardado exitosamente",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetGuardadoExitoso()
        }
    }
    
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Toast.makeText(
                context,
                errorMessage ?: "Error desconocido",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetErrorMessage()
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDeleteDialog(false) },
            title = { 
                Text(
                    "Eliminar Ingreso", 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = { 
                Text(
                    "¿Estás seguro de que quieres eliminar este ingreso? Esta acción no se puede deshacer.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarIngreso()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.toggleDeleteDialog(false) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    TaxiFlashScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Otros Ingresos",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TaxiYellow,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Pestañas
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 4.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("Formulario") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("Resumen") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
            }

            // Contenido de la pestaña seleccionada
            when (selectedTab) {
                0 -> FormularioTab(
                    concepto = concepto,
                    fecha = fecha,
                    importe = importe,
                    descripcion = descripcion,
                    notas = notas,
                    onConceptoChange = viewModel::updateConcepto,
                    onFechaChange = viewModel::updateFecha,
                    onImporteChange = viewModel::updateImporte,
                    onDescripcionChange = viewModel::updateDescripcion,
                    onNotasChange = viewModel::updateNotas,
                    onGuardarClick = viewModel::guardarIngreso,
                    isEditing = ingresoSeleccionadoId != null,
                    onEliminarClick = { viewModel.toggleDeleteDialog(true) }
                )
                1 -> ResumenTab(
                    listaIngresos = listaIngresos,
                    totalIngresosMes = totalIngresosMes,
                    mesSeleccionado = mesSeleccionado,
                    anioSeleccionado = anioSeleccionado,
                    onMesChange = viewModel::setMesSeleccionado,
                    onAnioChange = viewModel::setAnioSeleccionado,
                    onIngresoClick = viewModel::setIngresoSeleccionado,
                    numberFormat = numberFormat
                )
            }
        }
        
        // Indicador de carga
        LoadingIndicator(isLoading = isLoading)
    }
}

@Composable
fun FormularioTab(
    concepto: String,
    fecha: String,
    importe: String,
    descripcion: String,
    notas: String,
    onConceptoChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onImporteChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    isEditing: Boolean,
    onEliminarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Sección de información básica
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Información del Ingreso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Concepto
                OutlinedTextField(
                    value = concepto,
                    onValueChange = onConceptoChange,
                    label = { Text("Concepto") },
                    placeholder = { Text("Ej: Servicio extra, Propina especial...") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Description, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fecha
                OutlinedTextField(
                    value = fecha,
                    onValueChange = onFechaChange,
                    label = { Text("Fecha") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.CalendarToday, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Sección de importes
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Información Económica",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Importe
                OutlinedTextField(
                    value = importe,
                    onValueChange = onImporteChange,
                    label = { Text("Importe (€)") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Euro, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Sección de descripción
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Descripción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = onDescripcionChange,
                    label = { Text("Descripción detallada") },
                    placeholder = { Text("Detalles del ingreso...") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Info, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notas adicionales
                OutlinedTextField(
                    value = notas,
                    onValueChange = onNotasChange,
                    label = { Text("Notas adicionales") },
                    placeholder = { Text("Cualquier otra información relevante...") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Note, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Botones de acción
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEditing) {
                Button(
                    onClick = onEliminarClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar")
                }
            }
            
            Button(
                onClick = onGuardarClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditing) "Actualizar" else "Guardar")
            }
        }
    }
}

@Composable
fun ResumenTab(
    listaIngresos: List<OtrosIngresos>,
    totalIngresosMes: Double,
    mesSeleccionado: Int,
    anioSeleccionado: Int,
    onMesChange: (Int) -> Unit,
    onAnioChange: (Int) -> Unit,
    onIngresoClick: (Long) -> Unit,
    numberFormat: NumberFormat
) {
    val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Selector de mes y año
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selector de mes
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        if (mesSeleccionado > 1) {
                            onMesChange(mesSeleccionado - 1)
                        } else {
                            onMesChange(12)
                            onAnioChange(anioSeleccionado - 1)
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Mes anterior"
                    )
                }
                
                Text(
                    text = meses[mesSeleccionado - 1],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { 
                        if (mesSeleccionado < 12) {
                            onMesChange(mesSeleccionado + 1)
                        } else {
                            onMesChange(1)
                            onAnioChange(anioSeleccionado + 1)
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Mes siguiente"
                    )
                }
            }
            
            // Selector de año
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAnioChange(anioSeleccionado - 1) }) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Año anterior"
                    )
                }
                
                Text(
                    text = anioSeleccionado.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { onAnioChange(anioSeleccionado + 1) }) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Año siguiente"
                    )
                }
            }
        }
        
        // Tarjeta con el total
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total de Otros Ingresos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = numberFormat.format(totalIngresosMes),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Lista de ingresos
        Text(
            text = "Listado de Ingresos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        if (listaIngresos.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay ingresos registrados para este mes",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listaIngresos) { ingreso ->
                    IngresoItem(
                        ingreso = ingreso,
                        onClick = { onIngresoClick(ingreso.id) },
                        numberFormat = numberFormat
                    )
                }
            }
        }
    }
}

@Composable
fun IngresoItem(
    ingreso: OtrosIngresos,
    onClick: () -> Unit,
    numberFormat: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información del ingreso
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ingreso.concepto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = ingreso.fecha,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (ingreso.descripcion?.isNotBlank() == true) {
                    Text(
                        text = ingreso.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            // Importe
            Text(
                text = numberFormat.format(ingreso.importe),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 