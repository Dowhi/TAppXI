package com.taxiflash.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.taxiflash.ui.viewmodel.GastoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenGastosMensualScreen(
    viewModel: GastoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val gastos by viewModel.gastos.collectAsState()
    var mesSeleccionado by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time)
    }

    val formatoMesAno = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

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
                    Text("Resumen de Gastos", color = Color.White, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver", tint = Color(0xFFF8F9FA))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.shadow(4.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // 🎯 Selector de mes con botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { time = mesSeleccionado }
                        cal.add(Calendar.MONTH, -1)
                        mesSeleccionado = cal.time
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        "Mes anterior",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = formatoMesAno.format(mesSeleccionado).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { time = mesSeleccionado }
                        cal.add(Calendar.MONTH, 1)
                        mesSeleccionado = cal.time
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowForward, 
                        "Mes siguiente",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 🔹 Encabezado de la tabla
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(primaryGradient)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Día", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), fontSize = 14.sp)
                    Text("€", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.1f), fontSize = 14.sp)
                    Text("Concepto", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.5f), fontSize = 14.sp)
                    Text("Proveedor", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start, modifier = Modifier.weight(1.5f), fontSize = 14.sp)
                }
            }

            // 🔹 Contenedor con la lista de gastos
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val gastosFiltrados = gastos.filter { gasto ->
                        val fechaGasto = try { formatoFecha.parse(gasto.fecha) } catch (e: Exception) { null }
                        if (fechaGasto != null) {
                            val calGasto = Calendar.getInstance().apply { time = fechaGasto }
                            val calSeleccionado = Calendar.getInstance().apply { time = mesSeleccionado }
                            calGasto.get(Calendar.YEAR) == calSeleccionado.get(Calendar.YEAR) &&
                                    calGasto.get(Calendar.MONTH) == calSeleccionado.get(Calendar.MONTH)
                        } else {
                            false
                        }
                    }.sortedBy { it.fecha }

                    items(gastosFiltrados) { gasto ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(gasto.fecha.substring(0, 5), modifier = Modifier.weight(0.8f),color= MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, textAlign = TextAlign.Start)
                            Text("%.2f".format(gasto.importeTotal), modifier = Modifier.weight(1.1f), fontSize = 14.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                            Text(gasto.tipoGastoEspecifico, modifier = Modifier.weight(1.5f), color =  MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Start, fontSize = 12.sp)
                            Text(gasto.proveedor, modifier = Modifier.weight(1.5f),color= MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Start, fontSize = 12.sp)
                        }
                        Divider(color =  MaterialTheme.colorScheme.onSurface, thickness = 0.5.dp)
                    }
                }
            }

            // 🔹 Total del mes
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total del Mes:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    Text(
                        text = "%.2f€".format(
                            gastos.filter { gasto ->
                                if (gasto.fecha.isNullOrEmpty()) {
                                    false
                                } else {
                                    try {
                                        formatoFecha.parse(gasto.fecha)?.let {
                                            val calGasto = Calendar.getInstance().apply { time = it }
                                            val calSeleccionado = Calendar.getInstance().apply { time = mesSeleccionado }
                                            calGasto.get(Calendar.MONTH) == calSeleccionado.get(Calendar.MONTH)
                                        } ?: false
                                    } catch (e: Exception) {
                                        Log.e("ResumenGastosMensual", "Error al analizar fecha: ${gasto.fecha}", e)
                                        false
                                    }
                                }
                            }.sumOf { it.importeTotal }
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
