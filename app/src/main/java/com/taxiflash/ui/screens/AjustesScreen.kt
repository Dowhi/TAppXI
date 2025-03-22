package com.taxiflash.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.taxiflash.ui.components.TaxiFlashScaffold
import com.taxiflash.ui.navigation.NavRoutes
import com.taxiflash.ui.theme.TaxiBlue
import com.taxiflash.ui.theme.TaxiYellow
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.viewmodel.AjustesViewModel
import com.taxiflash.ui.data.AjustesDataStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import com.taxiflash.ui.theme.TaxiGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(navController: NavController) {
    // Contexto para mostrar mensajes Toast
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // ViewModel para manejar los ajustes
    val viewModel: AjustesViewModel = viewModel()
    
    // Estados para los ajustes
    val temaOscuro by viewModel.temaOscuro.collectAsState(initial = false)
    val notificaciones by viewModel.notificaciones.collectAsState(initial = true)
    val formatoMoneda by viewModel.formatoMoneda.collectAsState(initial = "€")
    val formatoFecha by viewModel.formatoFecha.collectAsState(initial = "DD/MM/YYYY")
    
    // Estados para exportación/importación
    val exportandoDatos by viewModel.exportandoDatos.collectAsState()
    val importandoDatos by viewModel.importandoDatos.collectAsState()
    val exportacionExitosa by viewModel.exportacionExitosa.collectAsState()
    val importacionExitosa by viewModel.importacionExitosa.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Lanzador para actividad de selección de archivos
    val importarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.importarBaseDatos(uri)
            }
        }
    }
    
    // Estado para diálogos
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }
    var mostrarDialogoAcercaDe by remember { mutableStateOf(false) }
    var mostrarDialogoMoneda by remember { mutableStateOf(false) }
    var mostrarDialogoFecha by remember { mutableStateOf(false) }
    var mostrarDialogoExportar by remember { mutableStateOf(false) }
    var mostrarDialogoImportar by remember { mutableStateOf(false) }
    var mostrarDialogoSeleccionarCarpeta by remember { mutableStateOf(false) }
    var mostrarDialogoObjetivo by remember { mutableStateOf(false) }
    
    // Scroll state
    val scrollState = rememberScrollState()
    
    // Limpiar estado después de notificar al usuario
    LaunchedEffect(importacionExitosa, error) {
        if (importacionExitosa || error != null) {
            delay(3000) // Esperar 3 segundos para reiniciar estados
            viewModel.reiniciarEstado()
        }
    }
    
    // Limpiar estado de exportación solo después de haber usado el archivo
    LaunchedEffect(mostrarDialogoSeleccionarCarpeta) {
        if (!mostrarDialogoSeleccionarCarpeta && exportacionExitosa != null) {
            // Solo reiniciar cuando se cierre el diálogo
            viewModel.reiniciarEstado()
        }
    }
    
    // Mostrar mensajes según resultados
    LaunchedEffect(exportacionExitosa) {
        if (exportacionExitosa != null) {
            Log.d("AjustesScreen", "exportacionExitosa cambió a: $exportacionExitosa - Mostrando diálogo de selección")
            mostrarDialogoSeleccionarCarpeta = true
        }
    }
    
    LaunchedEffect(importacionExitosa) {
        if (importacionExitosa) {
            Toast.makeText(context, "Base de datos importada correctamente", Toast.LENGTH_SHORT).show()
        }
    }
    
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    
    TaxiFlashScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ajustes",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Sección de ajustes generales
            Text(
                text = "Ajustes generales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tarjeta de tema
            AjusteCardSwitch(
                title = "Tema oscuro",
                description = "Cambiar entre tema claro y oscuro",
                checked = temaOscuro,
                onCheckedChange = { 
                    viewModel.setTemaOscuro(it)
                    Toast.makeText(context, "Tema cambiado", Toast.LENGTH_SHORT).show()
                }
            )
            
            // Tarjeta de notificaciones
            AjusteCardSwitch(
                title = "Notificaciones",
                description = "Activar o desactivar notificaciones",
                checked = notificaciones,
                onCheckedChange = { 
                    viewModel.setNotificaciones(it)
                    Toast.makeText(
                        context, 
                        if (it) "Notificaciones activadas" else "Notificaciones desactivadas", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            
            // Tarjeta de moneda
            AjusteCardSeleccion(
                title = "Moneda",
                description = "Formato de moneda: $formatoMoneda",
                onClick = { mostrarDialogoMoneda = true }
            )
            
            // Tarjeta de fecha
            AjusteCardSeleccion(
                title = "Formato de fecha",
                description = formatoFecha,
                onClick = { mostrarDialogoFecha = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sección de datos
            Text(
                text = "Gestión de datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tarjeta de recordatorios
            AjusteCardAccion(
                title = "Recordatorios",
                description = "Gestionar recordatorios de ITV, cambio de aceite, IRPF, etc.",
                onClick = { navController.navigate(NavRoutes.RECORDATORIOS) }
            )
            
            // Sección de gestión de datos
            Text(
                text = "Gestión de datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tarjeta de exportar datos
            AjusteCardAccion(
                title = "Exportar datos",
                description = "Exporta todos los datos a un archivo Excel",
                onClick = { mostrarDialogoExportar = true }
            )
            
            // Tarjeta de importar datos
            AjusteCardAccion(
                title = "Importar datos",
                description = "Importa datos desde un archivo Excel",
                onClick = { mostrarDialogoImportar = true }
            )
            
            // Tarjeta de borrar datos
            AjusteCardAccion(
                title = "Borrar todos los datos",
                description = "Elimina todos los datos de la aplicación",
                onClick = { mostrarDialogoBorrar = true },
                colorTexto = Color.Red
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sección de configuración de valores
            Text(
                text = "Configuración de valores",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tarjeta para configurar el valor objetivo
            val objetivoValor by viewModel.objetivoImporte.collectAsState(initial = 100.0)
            
            AjusteCardAccion(
                title = "Valor objetivo diario",
                description = "Actualmente: ${String.format("%.2f", objetivoValor)}€",
                onClick = { mostrarDialogoObjetivo = true }
            )
            
            // Sección de información
            Text(
                text = "Información",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tarjeta de versión
            AjusteCard(
                title = "Versión",
                description = "1.1.0"
            )
            
            // Tarjeta de acerca de
            AjusteCardAccion(
                title = "Acerca de",
                description = "TaxiFlash - Gestión eficiente para taxistas",
                onClick = { mostrarDialogoAcercaDe = true }
            )
        }
        
        // Diálogos
        if (mostrarDialogoBorrar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoBorrar = false },
                title = { Text("Borrar todos los datos") },
                text = { Text("¿Estás seguro de que deseas borrar todos los datos de la aplicación? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            // Lógica para borrar datos
                            scope.launch {
                                viewModel.borrarTodosLosDatos()
                                Toast.makeText(context, "Todos los datos han sido eliminados", Toast.LENGTH_SHORT).show()
                            }
                            mostrarDialogoBorrar = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Borrar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { mostrarDialogoBorrar = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaxiGray
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        if (mostrarDialogoAcercaDe) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoAcercaDe = false },
                title = { Text("Acerca de TaxiFlash") },
                text = {
                    Column {
                        Text("TaxiFlash es una aplicación de gestión para taxistas que permite registrar turnos, carreras y gastos, así como visualizar estadísticas y resúmenes.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Versión: 1.1.0")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("© 2023 TaxiFlash")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { mostrarDialogoAcercaDe = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
        
        if (mostrarDialogoMoneda) {
            val opciones = listOf("€", "$", "£", "¥")
            var seleccionado by remember { mutableStateOf(formatoMoneda) }
            
            AlertDialog(
                onDismissRequest = { mostrarDialogoMoneda = false },
                title = { Text("Seleccionar moneda") },
                text = {
                    Column {
                        opciones.forEach { opcion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = CenterVertically
                            ) {
                                RadioButton(
                                    selected = seleccionado == opcion,
                                    onClick = { seleccionado = opcion }
                                )
                                Text(
                                    text = opcion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.setFormatoMoneda(seleccionado)
                            Toast.makeText(context, "Moneda cambiada a $seleccionado", Toast.LENGTH_SHORT).show()
                            mostrarDialogoMoneda = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { mostrarDialogoMoneda = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        if (mostrarDialogoFecha) {
            val opciones = listOf("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD")
            var seleccionado by remember { mutableStateOf(formatoFecha) }
            
            AlertDialog(
                onDismissRequest = { mostrarDialogoFecha = false },
                title = { Text("Seleccionar formato de fecha") },
                text = {
                    Column {
                        opciones.forEach { opcion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = CenterVertically
                            ) {
                                RadioButton(
                                    selected = seleccionado == opcion,
                                    onClick = { seleccionado = opcion }
                                )
                                Text(
                                    text = opcion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.setFormatoFecha(seleccionado)
                            Toast.makeText(context, "Formato de fecha cambiado a $seleccionado", Toast.LENGTH_SHORT).show()
                            mostrarDialogoFecha = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { mostrarDialogoFecha = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Diálogo para configurar el valor objetivo
        if (mostrarDialogoObjetivo) {
            val objetivoValor by viewModel.objetivoImporte.collectAsState(initial = 100.0)
            var valorObjetivo by remember { mutableStateOf(objetivoValor.toString()) }
            var errorTexto by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { mostrarDialogoObjetivo = false },
                title = { Text("Configurar valor objetivo") },
                text = {
                    Column {
                        Text("Establece el valor objetivo diario. Este valor se usará para calcular cuánto falta para alcanzar ese objetivo.")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextField(
                            value = valorObjetivo,
                            onValueChange = { nuevoValor -> 
                                valorObjetivo = nuevoValor
                                errorTexto = ""
                            },
                            label = { Text("Valor objetivo (€)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        if (errorTexto.isNotEmpty()) {
                            Text(
                                text = errorTexto,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                val valor = valorObjetivo.replace(',', '.').toDouble()
                                if (valor <= 0) {
                                    errorTexto = "El valor debe ser mayor que 0"
                                } else {
                                    viewModel.setObjetivoImporte(valor)
                                    Toast.makeText(context, "Valor objetivo actualizado", Toast.LENGTH_SHORT).show()
                                    mostrarDialogoObjetivo = false
                                }
                            } catch (e: NumberFormatException) {
                                errorTexto = "Introduce un valor numérico válido"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { mostrarDialogoObjetivo = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        if (mostrarDialogoExportar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoExportar = false },
                title = { Text("Exportar base de datos") },
                text = { 
                    Column {
                        Text("Se creará una copia de seguridad de todos los datos de la aplicación en formato Excel (.xlsx).")
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Al finalizar, podrás guardar el archivo en Google Drive o en otra aplicación compatible.")
                        
                        if (exportandoDatos) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                "Exportando datos...",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.exportarBaseDatos()
                            mostrarDialogoExportar = false
                            // Agregar log para depuración
                            scope.launch {
                                delay(1000) // Esperar un poco para dar tiempo a que cambie el valor
                                Log.d("AjustesScreen", "Valor de exportacionExitosa después de exportar: $exportacionExitosa")
                            }
                        },
                        enabled = !exportandoDatos,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Exportar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { mostrarDialogoExportar = false },
                        enabled = !exportandoDatos,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        if (mostrarDialogoImportar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoImportar = false },
                title = { Text("Importar base de datos") },
                text = { 
                    Column {
                        Text("⚠️ ATENCIÓN: Esta acción sobrescribirá todos los datos actuales con los del archivo seleccionado. Los datos actuales se perderán.")
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Selecciona un archivo Excel (.xlsx) previamente exportado desde la aplicación.")
                        
                        if (importandoDatos) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                "Importando datos...",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            importarLauncher.launch(viewModel.obtenerIntentImportacion())
                            mostrarDialogoImportar = false
                        },
                        enabled = !importandoDatos,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Seleccionar archivo")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { mostrarDialogoImportar = false },
                        enabled = !importandoDatos,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        if (mostrarDialogoSeleccionarCarpeta && exportacionExitosa != null) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoSeleccionarCarpeta = false },
                title = { Text("Exportación completada") },
                text = { 
                    Column {
                        Text("La base de datos se ha exportado correctamente.")
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Pulsa 'Compartir' para enviar o guardar el archivo. Se abrirá un selector con las aplicaciones disponibles (Google Drive, Gmail, OneDrive, etc).")
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Si el selector no se abre, intenta de nuevo o reinicia la aplicación.", 
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            exportacionExitosa?.let { uri ->
                                Log.d("AjustesScreen", "Intentando compartir archivo con URI: $uri")
                                viewModel.exportarAGoogleDrive(uri)
                            }
                            mostrarDialogoSeleccionarCarpeta = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Compartir")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { mostrarDialogoSeleccionarCarpeta = false },
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

// Componentes para los ajustes
@Composable
fun AjusteCard(
    title: String,
    description: String,
    colorTexto: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AjusteCardSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun AjusteCardSeleccion(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AjusteCardAccion(
    title: String,
    description: String,
    onClick: () -> Unit,
    colorTexto: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto.copy(alpha = 0.7f)
                )
            }
        }
    }
} 