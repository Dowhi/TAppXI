package com.taxiflash.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.viewmodel.ResumenAnualViewModel
import java.text.NumberFormat
import java.util.*
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.taxiflash.ui.utils.PdfGenerator
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.clickable

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenAnualScreen(
    onNavigateBack: () -> Unit,
    viewModel: ResumenAnualViewModel = viewModel()
) {
    val resumen by viewModel.resumenAnual.collectAsState()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Variable para el mes seleccionado para el PDF
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var showMonthSelector by remember { mutableStateOf(false) }
    
    // Variable para el año seleccionado
    var yearSelected by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    
    // Estado para controlar las animaciones
    var isLoading by remember { mutableStateOf(true) }
    
    // Efecto para actualizar el estado de carga cuando cambian los datos
    LaunchedEffect(resumen) {
        isLoading = resumen == null
    }
    
    // Animación para el contenido principal
    val contentAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(durationMillis = 500)
    )
    
    LaunchedEffect(yearSelected) {
        isLoading = true
        viewModel.cargarResumenAnual(yearSelected)
    }

    // Usar colores del tema Material3
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        "Resumen Financiero Anual",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .alpha(contentAlpha)
        ) {
            // Selector de año con flechas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        yearSelected--
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        "Año anterior",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = yearSelected.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        yearSelected++
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowForward, 
                        "Año siguiente",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Encabezado de la tabla
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(primaryGradient)
                    .padding(horizontal = 12.dp, vertical = 1.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mes",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Ingresos",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1.4f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Gastos",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1.4f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Total",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp
                    )
                }
            }

            // Contenido de la tabla
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                    }
                } else {
                    AnimatedVisibility(
                        visible = !isLoading && resumen != null,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                            animationSpec = tween(500),
                            initialOffsetY = { it / 2 }
                        ),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                            animationSpec = tween(300),
                            targetOffsetY = { it / 2 }
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Filas de datos mensuales
                            resumen?.meses?.let { meses ->
                                items(meses) { mes ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 1.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            mes.nombre.take(3).lowercase().replaceFirstChar { it.uppercase() },
                                            modifier = Modifier.weight(0.6f),
                                            textAlign = TextAlign.Start,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            numberFormat.format(mes.ingresos).replace("€", ""),
                                            modifier = Modifier.weight(1.5f),
                                            textAlign = TextAlign.End,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            numberFormat.format(mes.gastos).replace("€", ""),
                                            modifier = Modifier.weight(1.5f),
                                            textAlign = TextAlign.End,
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            numberFormat.format(mes.total).replace("€", ""),
                                            modifier = Modifier.weight(1.5f),
                                            textAlign = TextAlign.End,
                                            fontWeight = FontWeight.Bold,
                                            color = if (mes.total >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                            fontSize = 13.sp
                                        )
                                    }
                                    // Agregar un divisor entre filas
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                            
                            // Agregar un total anual
                            item {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    thickness = 1.dp
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 1.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "💶",
                                        modifier = Modifier.weight(0.6f),
                                        textAlign = TextAlign.Start,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        numberFormat.format(resumen?.totalIngresos ?: 0.0).replace("€", ""),
                                        modifier = Modifier.weight(1.5f),
                                        textAlign = TextAlign.End,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        numberFormat.format(resumen?.totalGastos ?: 0.0).replace("€", ""),
                                        modifier = Modifier.weight(1.5f),
                                        textAlign = TextAlign.End,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp
                                    )
                                    val totalAnual = (resumen?.totalIngresos ?: 0.0) - (resumen?.totalGastos ?: 0.0)
                                    Text(
                                        numberFormat.format(totalAnual).replace("€", ""),
                                        modifier = Modifier.weight(1.5f),
                                        textAlign = TextAlign.End,
                                        fontWeight = FontWeight.Bold,
                                        color = if (totalAnual >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            
                            // Botón para generar PDF
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = { 
                                            if (resumen?.meses?.isNotEmpty() == true) {
                                                showMonthSelector = true
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "No hay datos para generar un informe",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 4.dp,
                                            pressedElevation = 8.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            "Generar Informe PDF",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para seleccionar el mes
    if (showMonthSelector) {
        AlertDialog(
            onDismissRequest = { showMonthSelector = false },
            title = { Text("Seleccionar Mes") },
            text = {
                Column {
                    Text("Selecciona el mes para generar el informe PDF:", 
                        modifier = Modifier.padding(bottom = 16.dp))
                    
                    LazyColumn {
                        resumen?.meses?.forEachIndexed { index, mes ->
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedMonth = index
                                            showMonthSelector = false
                                            
                                            // Generar el PDF
                                            coroutineScope.launch {
                                                val pdfUri = viewModel.generarInformePdf(context, index)
                                                if (pdfUri != null) {
                                                    // Abrir el PDF
                                                    PdfGenerator.abrirPdf(context, pdfUri)
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Error al generar el PDF",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = mes.nombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    if (mes.total >= 0) {
                                        Text(
                                            text = numberFormat.format(mes.total),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = numberFormat.format(mes.total),
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                if (index < resumen?.meses?.size?.minus(1) ?: 0) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { 
                TextButton(onClick = { showMonthSelector = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}