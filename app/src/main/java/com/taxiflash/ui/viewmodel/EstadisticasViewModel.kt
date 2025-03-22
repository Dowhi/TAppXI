package com.taxiflash.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.CarreraDao
import com.taxiflash.ui.data.GastoDao
import com.taxiflash.ui.data.TurnoDao
import com.taxiflash.ui.data.DatosDiarios
import com.taxiflash.ui.data.DatosGastosPorCategoria
import com.taxiflash.ui.data.EstadisticasData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    private val turnoDao: TurnoDao,
    private val carreraDao: CarreraDao,
    private val gastoDao: GastoDao
) : ViewModel() {
    
    private val _estadisticasData = MutableStateFlow(EstadisticasData())
    val estadisticasData: StateFlow<EstadisticasData> = _estadisticasData
    
    private val diasSemana = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    
    init {
        cargarEstadisticas()
    }
    
    private fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                // Intentar cargar datos reales de la base de datos
                val datosDiarios = obtenerDatosDiariosReales()
                val datosGastosPorCategoria = obtenerDatosGastosPorCategoriaReales()
                
                // Calcular métricas basadas en datos reales
                val ingresoPromedio = if (datosDiarios.isNotEmpty()) {
                    datosDiarios.map { it.ingresos }.average().toFloat()
                } else 0f
                
                val gastoPromedio = if (datosDiarios.isNotEmpty()) {
                    datosDiarios.map { it.gastos }.average().toFloat()
                } else 0f
                
                // Encontrar el mejor día (con mayor ingreso)
                val mejorDia = datosDiarios.maxByOrNull { it.ingresos }?.dia ?: ""
                
                // Calcular margen total
                val margen = datosDiarios.sumOf { (it.ingresos - it.gastos).toDouble() }.toFloat()
                
                _estadisticasData.value = EstadisticasData(
                    datosDiarios = datosDiarios,
                    datosGastosPorCategoria = datosGastosPorCategoria,
                    ingresoPromedio = ingresoPromedio,
                    gastoPromedio = gastoPromedio,
                    mejorDia = mejorDia,
                    margen = margen
                )
                
                Log.d("EstadisticasViewModel", "Datos cargados correctamente: ${datosDiarios.size} días")
            } catch (e: Exception) {
                // Si ocurre algún error, cargar datos de ejemplo como respaldo
                Log.e("EstadisticasViewModel", "Error al cargar datos reales: ${e.message}", e)
                cargarEstadisticasDummy()
            }
        }
    }
    
    private suspend fun obtenerDatosDiariosReales(): List<DatosDiarios> {
        val resultado = mutableListOf<DatosDiarios>()
        val calendar = Calendar.getInstance()
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoFechaID = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        
        // Obtenemos datos de los últimos 7 días
        for (i in 6 downTo 0) {
            val tempCalendar = Calendar.getInstance()
            tempCalendar.add(Calendar.DAY_OF_MONTH, -i)
            
            // Formato para consultas en la base de datos
            val fechaFormatoID = formatoFechaID.format(tempCalendar.time)
            val fechaFormatoDB = formatoFecha.format(tempCalendar.time)
            
            // Formato para mostrar el día de la semana
            // Calendar.DAY_OF_WEEK: 1=Domingo, 2=Lunes, ... 7=Sábado
            // Nuestro array diasSemana: 0=Lunes, 1=Martes, ... 6=Domingo
            // Convertimos el índice de Calendar a nuestro índice
            val dayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)
            val indiceDiaSemana = if (dayOfWeek == 1) 6 else dayOfWeek - 2 // Convertir Domingo(1) a 6, Lunes(2) a 0, etc.
            val diaSemana = diasSemana[indiceDiaSemana]
            
            // Obtenemos ingresos y gastos para esta fecha
            val ingresos = obtenerIngresosPorDia(fechaFormatoDB)
            val gastos = obtenerGastosPorDia(fechaFormatoDB)
            
            resultado.add(DatosDiarios(diaSemana, ingresos, gastos))
        }
        
        return resultado
    }
    
    private suspend fun obtenerIngresosPorDia(fecha: String): Float {
        try {
            // Obtener todas las carreras para la fecha dada
            val carreras = carreraDao.getCarrerasByFecha(fecha).first()
            
            // Sumar todos los importes de las carreras
            val totalIngresos = carreras.sumOf { 
                (it.importeReal + it.propina).toDouble() 
            }.toFloat()
            
            Log.d("EstadisticasViewModel", "Ingresos para $fecha: $totalIngresos (${carreras.size} carreras)")
            return totalIngresos
        } catch (e: Exception) {
            Log.e("EstadisticasViewModel", "Error al obtener ingresos por día: ${e.message}", e)
            return 0f
        }
    }
    
    private suspend fun obtenerGastosPorDia(fecha: String): Float {
        try {
            // Obtener todos los gastos para la fecha dada
            val gastos = gastoDao.getGastosByFecha(fecha).first()
            
            // Sumar todos los importes de los gastos
            val totalGastos = gastos.sumOf { it.importeTotal.toDouble() }.toFloat()
            
            Log.d("EstadisticasViewModel", "Gastos para $fecha: $totalGastos (${gastos.size} gastos)")
            return totalGastos
        } catch (e: Exception) {
            Log.e("EstadisticasViewModel", "Error al obtener gastos por día: ${e.message}", e)
            return 0f
        }
    }
    
    private suspend fun obtenerDatosGastosPorCategoriaReales(): List<DatosGastosPorCategoria> {
        try {
            // Obtener todos los gastos
            val gastos = gastoDao.getAllGastos().first()
            
            if (gastos.isEmpty()) {
                Log.d("EstadisticasViewModel", "No se encontraron gastos para categorizar")
                return emptyList()
            }
            
            // Agrupar gastos por categoría
            val gastosPorCategoria = gastos.groupBy { 
                it.tipoGasto.takeIf { it.isNotBlank() } ?: "Otros" 
            }
            
            // Calcular total de gastos
            val totalGastos = gastos.sumOf { it.importeTotal.toDouble() }
            
            // Preparar datos para el gráfico
            val resultado = gastosPorCategoria.map { (categoria, gastosCategoria) ->
                val montoCategoria = gastosCategoria.sumOf { it.importeTotal.toDouble() }.toFloat()
                val porcentaje = if (totalGastos > 0) (montoCategoria / totalGastos * 100).toFloat() else 0f
                DatosGastosPorCategoria(categoria, porcentaje, montoCategoria)
            }.sortedByDescending { it.porcentaje }
            
            Log.d("EstadisticasViewModel", "Categorías de gastos obtenidas: ${resultado.size}")
            return resultado
        } catch (e: Exception) {
            Log.e("EstadisticasViewModel", "Error al obtener datos de gastos por categoría: ${e.message}", e)
            return emptyList()
        }
    }
    
    // Método de respaldo con datos de ejemplo
    private fun cargarEstadisticasDummy() {
        viewModelScope.launch {
            // Datos de ejemplo para la demostración
            val datosDiarios = listOf(
                DatosDiarios("Lun", 1200f, 450f),
                DatosDiarios("Mar", 1450f, 380f),
                DatosDiarios("Mié", 1320f, 420f),
                DatosDiarios("Jue", 1500f, 400f),
                DatosDiarios("Vie", 1380f, 430f),
                DatosDiarios("Sáb", 1600f, 410f),
                DatosDiarios("Dom", 1700f, 460f)
            )
            
            val datosGastosPorCategoria = listOf(
                DatosGastosPorCategoria("Combustible", 45f, 1350f),
                DatosGastosPorCategoria("Mantenimiento", 25f, 750f),
                DatosGastosPorCategoria("Limpieza", 10f, 300f),
                DatosGastosPorCategoria("Parking", 15f, 450f),
                DatosGastosPorCategoria("Otros", 5f, 150f)
            )
            
            // Calcular métricas
            val ingresoPromedio = datosDiarios.map { it.ingresos }.average().toFloat()
            val gastoPromedio = datosDiarios.map { it.gastos }.average().toFloat()
            
            // Encontrar el mejor día (con mayor ingreso)
            val mejorDia = datosDiarios.maxByOrNull { it.ingresos }?.dia ?: ""
            
            // Calcular margen total
            val margen = datosDiarios.sumOf { (it.ingresos - it.gastos).toDouble() }.toFloat()
            
            _estadisticasData.value = EstadisticasData(
                datosDiarios = datosDiarios,
                datosGastosPorCategoria = datosGastosPorCategoria,
                ingresoPromedio = ingresoPromedio,
                gastoPromedio = gastoPromedio,
                mejorDia = mejorDia,
                margen = margen
            )
            
            Log.d("EstadisticasViewModel", "Se cargaron datos de ejemplo como respaldo")
        }
    }
    
    fun actualizarDatos() {
        cargarEstadisticas()
    }
} 