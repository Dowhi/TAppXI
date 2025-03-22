package com.taxiflash.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.taxiflash.ui.data.Gasto
import com.taxiflash.ui.viewmodel.GastoViewModel
import org.tensorflow.lite.support.label.Category
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastoScreen(
    viewModel: GastoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var categoria by remember { mutableStateOf("") }
    var tipoGasto by remember { mutableStateOf("") }
    var fecha by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time))
    }
    var factura by remember { mutableStateOf("") }
    var proveedor by remember { mutableStateOf("") }
    var importe by remember { mutableStateOf("") }
    var incluyeIVA by remember { mutableStateOf("Sí") }
    var kilometros by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var mostrarDialogoProveedores by remember { mutableStateOf(false) }
    var nuevoProveedor by remember { mutableStateOf("") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    // Obtener la lista de proveedores
    val listaProveedores by viewModel.proveedores.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Registrar Gasto",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                            "Información del Gasto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Categoría
                        ModernDropdownMenuComponent(
                            label = "Categoría",
                            selectedOption = categoria,
                            options = listOf("Vehículo", "Actividad"),
                            icon = Icons.Default.Category,
                            onOptionSelected = {
                                categoria = it
                                tipoGasto = "" // Reinicia el tipo de gasto al cambiar de categoría
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tipo de Gasto según categoría
                        if (categoria == "Vehículo") {
                            ModernDropdownMenuComponent(
                                label = "Tipo de Gasto",
                                selectedOption = tipoGasto,
                                options = listOf("Neumáticos", "Avería", "Cambio de Aceite", "Freno"),
                                icon = Icons.Default.Description,
                                onOptionSelected = { tipoGasto = it }
                            )
                        } else if (categoria == "Actividad") {
                            ModernDropdownMenuComponent(
                                label = "Tipo de Gasto",
                                selectedOption = tipoGasto,
                                options = listOf("Combustible", "Gestoría", "Autónomo", "Telefonía"),
                                icon = Icons.Default.Description,
                                onOptionSelected = { tipoGasto = it }
                            )
                        }
                    }
                }

                // Sección de detalles de la factura
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
                            "Detalles de Facturación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Fecha
                        ModernTextField(
                            value = fecha,
                            onValueChange = { fecha = it },
                            label = "Fecha",
                            icon = Icons.Default.CalendarToday,
                            keyboardType = KeyboardType.Text
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Número de Factura
                        ModernTextField(
                            value = factura,
                            onValueChange = { factura = it },
                            label = "Número de Factura",
                            icon = Icons.Default.Receipt,
                            keyboardType = KeyboardType.Text
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo Proveedor
                        OutlinedTextField(
                            value = proveedor,
                            onValueChange = { proveedor = it },
                            label = { Text("Proveedor") },
                            placeholder = { Text("Nombre del proveedor") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = primaryColor
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { mostrarDialogoProveedores = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Gestionar proveedores",
                                        tint = primaryColor
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { mostrarDialogoProveedores = true },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                                focusedLabelColor = primaryColor,
                                unfocusedLabelColor = primaryColor.copy(alpha = 0.7f),
                                cursorColor = primaryColor
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
                        ModernTextField(
                            value = importe,
                            onValueChange = { importe = it },
                            label = "Importe (€)",
                            icon = Icons.Default.Euro,
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Incluye IVA
                        ModernDropdownMenuComponent(
                            label = "¿Incluye IVA?",
                            selectedOption = incluyeIVA,
                            options = listOf("Sí", "No"),
                            icon = Icons.Default.Euro,
                            onOptionSelected = { incluyeIVA = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Kilómetros
                        ModernTextField(
                            value = kilometros,
                            onValueChange = { kilometros = it },
                            label = "Kilómetros",
                            icon = Icons.Default.DirectionsCar,
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                // Sección de notas
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
                            "Notas Adicionales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Notas
                        OutlinedTextField(
                            value = notas,
                            onValueChange = { notas = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Notas") },
                            leadingIcon = { Icon(Icons.Default.Note, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Botón Guardar
                Button(
                    onClick = {
                        val importeDouble = importe.toDoubleOrNull() ?: 0.0
                        val ivaDouble = if (incluyeIVA == "Sí") importeDouble * 0.21 else 0.0
                        viewModel.guardarGasto(
                            factura = factura,
                            proveedor = proveedor,
                            importeTotal = importeDouble,
                            iva = ivaDouble,
                            kilometros = kilometros.toIntOrNull() ?: 0,
                            tipoGasto = categoria,
                            tipoGastoEspecifico = tipoGasto,
                            descripcion = notas
                        )
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Guardar Datos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Diálogo para gestionar proveedores
        if (mostrarDialogoProveedores) {
            ProveedoresDialog(
                proveedores = listaProveedores,
                onDismiss = { mostrarDialogoProveedores = false },
                onProveedorSeleccionado = { 
                    proveedor = it
                    mostrarDialogoProveedores = false
                },
                onGuardarNuevoProveedor = { nuevoProveedor ->
                    viewModel.guardarProveedor(nuevoProveedor)
                },
                onEliminarProveedor = { proveedorAEliminar ->
                    viewModel.eliminarProveedor(proveedorAEliminar)
                }
            )
        }
    }
}

@Composable
fun ModernGastoItem(
    gasto: Gasto,
    onDelete: (Gasto) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${gasto.tipoGasto} - ${gasto.tipoGastoEspecifico}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = gasto.fecha,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                FilledIconButton(
                    onClick = { onDelete(gasto) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        "Importe: ${gasto.importeTotal}€",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        "IVA: ${gasto.iva}€",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (gasto.factura.isNotEmpty() || gasto.proveedor.isNotEmpty() || gasto.descripcion?.isNotEmpty() == true) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (gasto.factura.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Factura: ${gasto.factura}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (gasto.proveedor.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = if (gasto.factura.isNotEmpty()) 8.dp else 0.dp)
                            ) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Proveedor: ${gasto.proveedor}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (gasto.descripcion?.isNotEmpty() == true) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(
                                    top = if (gasto.factura.isNotEmpty() || gasto.proveedor.isNotEmpty()) 8.dp else 0.dp
                                )
                            ) {
                                Icon(
                                    Icons.Default.Note,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Notas: ${gasto.descripcion}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Componente moderno reutilizable para TextFields
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

// Componente moderno reutilizable para Dropdowns
@Composable
fun ModernDropdownMenuComponent(
    label: String,
    selectedOption: String,
    options: List<String>,
    icon: ImageVector,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { expanded = true }
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = selectedOption.ifEmpty { "Seleccione..." },
                    color = if (selectedOption.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProveedoresDialog(
    proveedores: List<String>,
    onDismiss: () -> Unit,
    onProveedorSeleccionado: (String) -> Unit,
    onGuardarNuevoProveedor: (String) -> Unit,
    onEliminarProveedor: (String) -> Unit
) {
    var nuevoProveedor by remember { mutableStateOf("") }
    var mostrarFormularioNuevo by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Seleccionar Proveedor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Lista de proveedores existentes
                if (proveedores.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(proveedores) { proveedor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onProveedorSeleccionado(proveedor) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = proveedor,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                IconButton(
                                    onClick = { onEliminarProveedor(proveedor) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar proveedor",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay proveedores guardados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Formulario para añadir nuevo proveedor
                if (mostrarFormularioNuevo) {
                    OutlinedTextField(
                        value = nuevoProveedor,
                        onValueChange = { nuevoProveedor = it },
                        label = { Text("Nombre del proveedor") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { 
                                mostrarFormularioNuevo = false
                                nuevoProveedor = ""
                            }
                        ) {
                            Text("Cancelar")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { 
                                if (nuevoProveedor.isNotBlank()) {
                                    onGuardarNuevoProveedor(nuevoProveedor)
                                    nuevoProveedor = ""
                                    mostrarFormularioNuevo = false
                                }
                            },
                            enabled = nuevoProveedor.isNotBlank()
                        ) {
                            Text("Guardar")
                        }
                    }
                } else {
                    Button(
                        onClick = { mostrarFormularioNuevo = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir Nuevo Proveedor")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}