package com.taxiflash.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taxiflash.ui.theme.TaxiGreen
import com.taxiflash.ui.theme.TaxiRed
import com.taxiflash.ui.theme.TaxiYellow
import com.taxiflash.ui.theme.TaxiLightGray
import com.taxiflash.ui.theme.TaxiGray
import com.taxiflash.ui.viewmodel.EstadisticasViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasAvanzadasScreen(
    navController: NavController,
    viewModel: EstadisticasViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val estadisticasData by viewModel.estadisticasData.collectAsState()
    
    // Colores para el gráfico circular
    val colores = remember { listOf(TaxiGreen, TaxiRed, TaxiYellow, Color.Blue, Color.Magenta) }
    
    // Animación para los gráficos
    var animacionProgreso by remember { mutableStateOf(0f) }
    LaunchedEffect(key1 = true) {
        // Animar los gráficos gradualmente
        val duracion = 1000 // milisegundos
        val pasos = 100
        val incremento = 1f / pasos
        
        repeat(pasos) {
            animacionProgreso = min(1f, animacionProgreso + incremento)
            delay(duracion / pasos.toLong())
        }
    }
    
    // Efecto para cargar los datos
    LaunchedEffect(key1 = Unit) {
        viewModel.actualizarDatos()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Estadísticas Avanzadas",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Análisis de Rendimiento",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Visualiza tus datos de forma gráfica",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (estadisticasData.datosDiarios.isEmpty()) {
                // Mostrar indicador de carga si no hay datos
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                // Gráfico de barras - Ingresos vs Gastos por día
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Ingresos vs Gastos por Día",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Gráfico de barras
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val width = size.width
                                val height = size.height
                                val barWidth = width / (estadisticasData.datosDiarios.size * 3)
                                val maxValor = estadisticasData.datosDiarios.maxOfOrNull { maxOf(it.ingresos, it.gastos) } ?: 1f
                                
                                // Dibujar líneas de referencia horizontales
                                val numLineas = 5
                                for (i in 0..numLineas) {
                                    val y = height - (height * i / numLineas)
                                    drawLine(
                                        color = TaxiLightGray,
                                        start = Offset(0f, y),
                                        end = Offset(width, y),
                                        strokeWidth = 1f
                                    )
                                    
                                    // Valor en el eje Y
                                    val valor = (maxValor * i / numLineas)
                                    drawIntoCanvas { canvas ->
                                        canvas.nativeCanvas.drawText(
                                            numberFormat.format(valor).toString(),
                                            10f,
                                            y - 10,
                                            android.graphics.Paint().apply {
                                                color = android.graphics.Color.parseColor("#9E9E9E")
                                                textSize = 30f
                                            }
                                        )
                                    }
                                }
                                
                                // Dibujar barras para cada día
                                estadisticasData.datosDiarios.forEachIndexed { i, datos ->
                                    val x = (i * 3 + 1) * barWidth
                                    
                                    // Barra de ingresos
                                    val alturaIngresos = (height * datos.ingresos / maxValor) * animacionProgreso
                                    drawRect(
                                        color = TaxiGreen,
                                        topLeft = Offset(x, height - alturaIngresos),
                                        size = Size(barWidth, alturaIngresos)
                                    )
                                    
                                    // Barra de gastos
                                    val alturaGastos = (height * datos.gastos / maxValor) * animacionProgreso
                                    drawRect(
                                        color = TaxiRed,
                                        topLeft = Offset(x + barWidth, height - alturaGastos),
                                        size = Size(barWidth, alturaGastos)
                                    )
                                    
                                    // Etiqueta del día
                                    drawIntoCanvas { canvas ->
                                        canvas.nativeCanvas.drawText(
                                            datos.dia,
                                            x + barWidth / 2,
                                            height + 40,
                                            android.graphics.Paint().apply {
                                                color = android.graphics.Color.parseColor("#9E9E9E")
                                                textSize = 30f
                                                textAlign = android.graphics.Paint.Align.CENTER
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Leyenda
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            LeyendaItem(color = TaxiGreen, texto = "Ingresos")
                            Spacer(modifier = Modifier.width(24.dp))
                            LeyendaItem(color = TaxiRed, texto = "Gastos")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Gráfico circular - Distribución de gastos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Distribución de Gastos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Gráfico circular
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(
                                modifier = Modifier.size(180.dp)
                            ) {
                                val radio = size.minDimension / 2
                                val centro = Offset(size.width / 2, size.height / 2)
                                
                                var anguloInicial = 0f
                                
                                // Dibujar sectores
                                estadisticasData.datosGastosPorCategoria.take(5).forEachIndexed { index, datos ->
                                    val angulo = 360f * datos.porcentaje / 100f * animacionProgreso
                                    
                                    drawArc(
                                        color = colores[index % colores.size],
                                        startAngle = anguloInicial,
                                        sweepAngle = angulo,
                                        useCenter = true,
                                        topLeft = Offset(centro.x - radio, centro.y - radio),
                                        size = Size(radio * 2, radio * 2)
                                    )
                                    
                                    anguloInicial += angulo
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Leyenda
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            estadisticasData.datosGastosPorCategoria.take(5).forEachIndexed { index, datos ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(colores[index % colores.size])
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "${datos.categoria} (${datos.porcentaje.toInt()}%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Text(
                                        text = numberFormat.format(datos.monto),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Gráfico de línea - Tendencia de ingresos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Tendencia de Ingresos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Gráfico de línea
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val width = size.width
                                val height = size.height
                                val maxValor = estadisticasData.datosDiarios.maxOfOrNull { it.ingresos } ?: 1f
                                val puntosPorDia = width / (estadisticasData.datosDiarios.size - 1).coerceAtLeast(1)
                                
                                // Dibujar líneas de referencia horizontales
                                val numLineas = 5
                                for (i in 0..numLineas) {
                                    val y = height - (height * i / numLineas)
                                    drawLine(
                                        color = TaxiLightGray,
                                        start = Offset(0f, y),
                                        end = Offset(width, y),
                                        strokeWidth = 1f
                                    )
                                }
                                
                                // Dibujar línea de tendencia
                                val puntos = estadisticasData.datosDiarios.mapIndexed { i, datos ->
                                    Offset(
                                        x = i * puntosPorDia,
                                        y = height - (height * datos.ingresos / maxValor) * animacionProgreso
                                    )
                                }
                                
                                // Dibujar línea
                                for (i in 0 until puntos.size - 1) {
                                    drawLine(
                                        color = TaxiGreen,
                                        start = puntos[i],
                                        end = puntos[i + 1],
                                        strokeWidth = 5f,
                                        cap = StrokeCap.Round
                                    )
                                }
                                
                                // Dibujar puntos
                                puntos.forEach { punto ->
                                    drawCircle(
                                        color = TaxiGreen,
                                        radius = 8f,
                                        center = punto
                                    )
                                }
                                
                                // Etiquetas de días
                                estadisticasData.datosDiarios.forEachIndexed { i, datos ->
                                    drawIntoCanvas { canvas ->
                                        canvas.nativeCanvas.drawText(
                                            datos.dia,
                                            i * puntosPorDia,
                                            height + 40,
                                            android.graphics.Paint().apply {
                                                color = android.graphics.Color.parseColor("#9E9E9E")
                                                textSize = 30f
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Métricas clave
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Métricas Clave",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Métricas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricaCard(
                                titulo = "Ingreso Promedio",
                                valor = numberFormat.format(estadisticasData.ingresoPromedio),
                                color = TaxiGreen,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            MetricaCard(
                                titulo = "Gasto Promedio",
                                valor = numberFormat.format(estadisticasData.gastoPromedio),
                                color = TaxiRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricaCard(
                                titulo = "Mejor Día",
                                valor = estadisticasData.mejorDia,
                                color = TaxiGreen,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            MetricaCard(
                                titulo = "Margen",
                                valor = numberFormat.format(estadisticasData.margen),
                                color = TaxiYellow,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LeyendaItem(
    color: Color,
    texto: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MetricaCard(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
} 