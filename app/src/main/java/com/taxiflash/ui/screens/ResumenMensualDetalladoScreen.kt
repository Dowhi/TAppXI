package com.taxiflash.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.viewmodel.ResumenMensualDetalladoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenMensualDetalladoScreen(
    onNavigateBack: () -> Unit,
    viewModel: ResumenMensualDetalladoViewModel = viewModel()
) {
    val resumen by viewModel.resumenMensual.collectAsState()
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
    
    // Cargar datos cuando cambia el mes seleccionado
    LaunchedEffect(mesSeleccionado) {
        viewModel.cargarResumenMensual(mesSeleccionado)
    }
    
    // Función para obtener el mes y año en español
    fun getFormattedDate(mes: Int, año: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, mes - 1) // Los meses en Calendar son base-0 (enero = 0)
        calendar.set(Calendar.YEAR, año)

        val sdf = SimpleDateFormat("MMMM yyyy", Locale("es", "ES")) // Formato: "Mes Año"
        return sdf.format(calendar.time).uppercase(Locale("es", "ES")) // Convertir a mayúsculas
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen Mensual Detallado", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Selector de mes con botones de navegación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
                
                // ✅ FILA: Mes y Año + TOTAL INGRESOS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mes y Año - Removido ya que ahora está en el selector
                    /*
                    Text(
                        text = "${resumen.mes.uppercase()} ${resumen.año}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                    */
                    // Añadir un texto descriptivo en lugar del mes/año duplicado
                    Text(
                        text = "RESUMEN COMPLETO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )

                    // Tarjeta TOTAL INGRESOS
                    Surface(
                        modifier = Modifier
                            .width(150.dp)
                            .height(65.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.tertiary,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.2f)),
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "TOTAL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = numberFormat.format(resumen.totalIngresos),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                // ✅ Sección de datos secundarios
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    InfoCard("Días", resumen.dias.toString(), Color(0xFF42A5F5))
                    InfoCard("Carreras", resumen.carreras.toString(), Color(0xFF66BB6A))
                    InfoCard("Turno 1", resumen.turno1.toString(), Color(0xFFFFA726))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    InfoCard("S.Tarjeta", numberFormat.format(resumen.sTarjeta).toString(), Color(0xFF42A5F5))
                    InfoCard("S.Emisora", numberFormat.format(resumen.sEmisora).toString(), Color(0xFF66BB6A))
                    InfoCard("S.Vales", numberFormat.format(resumen.svales).toString(), Color(0xFFFFA726))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    InfoCard("C.Tarjeta", resumen.cTarjeta.toString(), Color(0xFF42A5F5))
                    InfoCard("C.Emisora", resumen.cEmisora.toString(), Color(0xFF66BB6A))
                    InfoCard("C.Vales", resumen.cVales.toString().toString(), Color(0xFFFFA726))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    InfoCard("Propinas", numberFormat.format(resumen.propinas).toString(), Color(0xFF42A5F5))
                    InfoCard("Aerop.", numberFormat.format(resumen.aeropuerto).toString(), Color(0xFF66BB6A))
                    InfoCard("Horas", resumen.horas.toString(), Color(0xFFFFA726))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    InfoCard("Kilome.", resumen.kilometros.toString(), Color(0xFF42A5F5))
                    InfoCard("In. Varios", numberFormat.format(resumen.ingresosVariados).toString(), Color(0xFF66BB6A))
                    InfoCard("Combust.", resumen.combustible.toString(),Color(0xFFFFA726))
                }
                Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                InfoCard("NETO", numberFormat.format(resumen.totalIngresos).toString(), Color.Blue)
                InfoCard("GASTOS", numberFormat.format(resumen.totalGastos).toString(), Color.Blue)
                InfoCard("TOTAL", numberFormat.format(resumen.totalNeto).toString(), Color.Blue)
            }
            }
        }
    }
}

// ✅ Implementación de InfoCard
@Composable
fun InfoCard(title: String, value: String, color: Color) {
    Surface(
        modifier = Modifier
            .width(106.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp) // 🔹 Baja solo el título
                )

                Text(
                    text = if (value == "0" || value == "00:00" || value == "0,00 €") "" else value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

