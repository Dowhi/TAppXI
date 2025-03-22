package com.taxiflash.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenAnualScreen(
    onNavigateBack: () -> Unit,
    viewModel: ResumenAnualViewModel = viewModel()
) {
    val resumen by viewModel.resumenAnual.collectAsState()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    
    // Variable para el año seleccionado
    var yearSelected by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    
    LaunchedEffect(yearSelected) {
        viewModel.cargarResumenAnual()
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = yearSelected.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = {
                        yearSelected++
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowForward, 
                        "Año siguiente",
                        tint = MaterialTheme.colorScheme.primary
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
                if (resumen == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Filas de datos mensuales
                        resumen?.meses?.let { meses ->
                            items(meses) { mes ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        mes.nombre.take(3).lowercase().replaceFirstChar { it.uppercase() },
                                        modifier = Modifier.weight(0.6f),
                                        textAlign = TextAlign.Start,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        numberFormat.format(mes.ingresos).replace("€", ""),
                                        modifier = Modifier.weight(1.5f),
                                        textAlign = TextAlign.End,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        numberFormat.format(mes.gastos).replace("€", ""),
                                        modifier = Modifier.weight(1.5f),
                                        textAlign = TextAlign.End,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        numberFormat.format(mes.total).replace("€", ""),
                                        modifier = Modifier.weight(1.5f),
                                        textAlign = TextAlign.End,
                                        fontWeight = FontWeight.Bold,
                                        color = if (mes.total >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp
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
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "TOTAL",
                                    modifier = Modifier.weight(0.6f),
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
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
                    }
                }
            }
        }
    }
}