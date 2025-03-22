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
import com.taxiflash.ui.viewmodel.ResumenMensualItem
import com.taxiflash.ui.viewmodel.ResumenMensualViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenMensualScreen(
    onNavigateBack: () -> Unit,
    onDiaClick: (String) -> Unit,
    viewModel: ResumenMensualViewModel = viewModel()
) {
    val resumenes by viewModel.resumenesMensuales.collectAsState()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    
    // Variable para el mes seleccionado
    var mesSeleccionado by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time)
    }
    
    // Formato para mostrar mes y año
    val formatoMesAno = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    
    LaunchedEffect(mesSeleccionado) {
        // Obtener el mes y año de la fecha seleccionada
        val calendar = Calendar.getInstance().apply { time = mesSeleccionado }
        val mes = calendar.get(Calendar.MONTH) + 1 // +1 porque Calendar.MONTH va de 0 a 11
        val anio = calendar.get(Calendar.YEAR)
        
        // Usar el método público que acepta mes y año como parámetros
        viewModel.cargarResumenesMensualesPorMesAnio(mes, anio)
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
                    Text(
                        "Resumen Mensual", 
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver", tint = Color.White)
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
            // Selector de mes con botones de navegación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = formatoMesAno.format(mesSeleccionado).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Cabecera informativa
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Resumen Financiero",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Visualiza tus ingresos y gastos mensuales",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Encabezado de la tabla
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
                    Text(
                        text = "Fecha",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.3f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Ingr.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Gastos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Total",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp
                    )
                }
            }

            // Contenido de la tabla
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                if (resumenes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay datos disponibles",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 18.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(resumenes) { resumen ->
                            ResumenMensualRow(
                                resumen = resumen,
                                numberFormat = numberFormat,
                                onClick = { onDiaClick(resumen.fechaParaNavegacion) },
                                greenColor = MaterialTheme.colorScheme.primary,
                                redColor = MaterialTheme.colorScheme.error
                            )
                            
                            Divider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                color = MaterialTheme.colorScheme.outline,
                                thickness = 0.5.dp
                            )
                        }

                        // Totales
                        item {
                            val totalIngresos = resumenes.sumOf { it.ingresos }
                            val totalGastos = resumenes.sumOf { it.gastos }
                            val totalNeto = resumenes.sumOf { it.total }

                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                thickness = 1.dp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL:",
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = numberFormat.format(totalIngresos).replace("€", ""),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.1f),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = numberFormat.format(totalGastos).replace("€", ""),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.1f),
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = numberFormat.format(totalNeto).replace("€", ""),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.1f),
                                    color = if (totalNeto >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResumenMensualRow(
    resumen: ResumenMensualItem,
    numberFormat: NumberFormat,
    onClick: () -> Unit,
    greenColor: Color,
    redColor: Color
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp) // 🔹 Reduce la separación vertical entre filas
            .height(28.dp) // 🔹 Reduce la altura de la fila
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 1.dp), // 🔹 Reduce los márgenes laterales
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = resumen.fecha,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start, // 🔹 Alinea la fecha a la derecha
                modifier = Modifier.weight(1.1f), // 🔹 Reduce el espacio de la fecha
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = numberFormat.format(resumen.ingresos).replace("€", ""),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
                color = greenColor,
                fontSize = 13.sp // 🔹 Reduce el tamaño de la fuente
            )
            Text(
                text = numberFormat.format(resumen.gastos).replace("€", ""),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
                color = redColor,
                fontSize = 13.sp // 🔹 Reduce el tamaño de la fuente
            )
            Text(
                text = numberFormat.format(resumen.total).replace("€", ""),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
                color = if (resumen.total >= 0) greenColor else redColor,
                fontSize = 13.sp // 🔹 Reduce el tamaño de la fuente
            )
        }
    }
}
