package com.taxiflash.ui.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.TaxiFlashDatabase
import com.taxiflash.ui.data.FormaPago
import com.taxiflash.ui.data.OtrosIngresos
import com.taxiflash.ui.utils.FechaUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.ZoneId
import java.util.*

data class ResumenMensualItem(
    val fecha: String = "",
    val fechaDate: Date = Date(),
    val fechaParaNavegacion: String = "",
    val ingresos: Double = 0.0,
    val gastos: Double = 0.0,
    val total: Double = 0.0
)

@RequiresApi(Build.VERSION_CODES.O)
class ResumenMensualViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val turnoDao = database.turnoDao()
    private val carreraDao = database.carreraDao()
    private val gastoDao = database.gastoDao()

    private val _resumenesMensuales = MutableStateFlow<List<ResumenMensualItem>>(emptyList())
    val resumenesMensuales: StateFlow<List<ResumenMensualItem>> = _resumenesMensuales.asStateFlow()
    
    // Estado para mostrar si hay una recarga en progreso
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()
    
    // Estado para mostrar errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        Log.d("ResumenMensualViewModel", "Inicializando ResumenMensualViewModel")
        cargarResumenesMensuales()
    }

    private fun formatearFecha(fecha: Date): String {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
        return formatoFecha.format(fecha)
    }

    private fun formatearFechaParaNavegacion(fecha: Date): String {
        val formatoFecha = SimpleDateFormat("ddMMyyyy", Locale("es", "ES"))
        return formatoFecha.format(fecha)
    }

    private fun parsearFecha(fechaStr: String): Date {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).parse(fechaStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    private fun cargarResumenesMensuales() {
        viewModelScope.launch {
            try {
                _cargando.value = true
                _error.value = null
                
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val fechaInicio = calendar.time

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val fechaFin = calendar.time

                cargarResumenesMensualesEnRango(fechaInicio, fechaFin)
            } catch (e: Exception) {
                _error.value = "Error al cargar el resumen mensual: ${e.message}"
                _cargando.value = false
                Log.e("ResumenMensualViewModel", "Error al cargar resumen mensual", e)
            }
        }
    }
    
    // Método público que acepta mes y año como parámetros
    fun cargarResumenesMensualesPorMesAnio(mes: Int, anio: Int) {
        viewModelScope.launch {
            try {
                _cargando.value = true
                _error.value = null
                
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, anio)
                calendar.set(Calendar.MONTH, mes - 1) // Restamos 1 porque Calendar.MONTH va de 0 a 11
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val fechaInicio = calendar.time

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val fechaFin = calendar.time

                cargarResumenesMensualesEnRango(fechaInicio, fechaFin)
            } catch (e: Exception) {
                _error.value = "Error al cargar el resumen mensual: ${e.message}"
                _cargando.value = false
                Log.e("ResumenMensualViewModel", "Error al cargar resumen mensual", e)
            }
        }
    }
    
    // Método privado que procesa el rango de fechas
    private suspend fun cargarResumenesMensualesEnRango(fechaInicio: Date, fechaFin: Date) {
        try {
            // Formato dd/MM/yyyy para buscar en la base de datos
            val formatoAlmacenamiento = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaInicioAlm = formatoAlmacenamiento.format(fechaInicio)
            val fechaFinAlm = formatoAlmacenamiento.format(fechaFin)
            
            Log.d("ResumenMensualViewModel", "Consultando datos entre fechas: $fechaInicioAlm y $fechaFinAlm")

            // Obtener todas las carreras del mes directamente por fecha
            val todasLasCarreras = mutableListOf<Carrera>()
            
            // Iterar por cada día del mes para obtener las carreras
            val tempCalendar = Calendar.getInstance()
            tempCalendar.time = fechaInicio
            
            while (tempCalendar.time <= fechaFin) {
                val fechaDiaStr = formatoAlmacenamiento.format(tempCalendar.time)
                try {
                    val carrerasDia = carreraDao.getCarrerasByFecha(fechaDiaStr).first()
                    todasLasCarreras.addAll(carrerasDia)
                    Log.d("ResumenMensualViewModel", "Fecha $fechaDiaStr: ${carrerasDia.size} carreras")
                } catch (e: Exception) {
                    Log.e("ResumenMensualViewModel", "Error al obtener carreras para $fechaDiaStr", e)
                }
                tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            Log.d("ResumenMensualViewModel", "Total carreras encontradas: ${todasLasCarreras.size}")
            
            // Obtener todos los gastos del mes
            val gastos = gastoDao.getAllGastos().first().filter { gasto ->
                try {
                    val fechaGasto = formatoAlmacenamiento.parse(gasto.fecha)
                    fechaGasto != null && fechaGasto >= fechaInicio && fechaGasto <= fechaFin
                } catch (e: Exception) {
                    Log.e("ResumenMensualViewModel", "Error al filtrar gasto con fecha ${gasto.fecha}", e)
                    false
                }
            }
            
            Log.d("ResumenMensualViewModel", "Total gastos encontrados: ${gastos.size}")
            
            // Obtener otros ingresos para el mes
            val todosLosOtrosIngresos = mutableListOf<OtrosIngresos>()
            val calendar = Calendar.getInstance()
            for (dia in 1..31) {
                calendar.time = fechaInicio
                calendar.set(Calendar.DAY_OF_MONTH, dia)
                
                if (calendar.time > fechaFin) break
                
                val fechaDiaStr = formatoAlmacenamiento.format(calendar.time)
                try {
                    val otrosIngresosDia = database.otrosIngresosDao().getOtrosIngresosByFecha(fechaDiaStr).first()
                    todosLosOtrosIngresos.addAll(otrosIngresosDia)
                } catch (e: Exception) {
                    Log.e("ResumenMensualViewModel", "Error al obtener otros ingresos para $fechaDiaStr", e)
                }
            }
            
            Log.d("ResumenMensualViewModel", "Carreras encontradas: ${todasLasCarreras.size}, Gastos encontrados: ${gastos.size}, Otros ingresos encontrados: ${todosLosOtrosIngresos.size}")
            
            // Agrupar las carreras por día
            val carrerasPorDia = todasLasCarreras.groupBy { carrera ->
                carrera.fecha
            }
            
            // Agrupar los gastos por día
            val gastosPorDia = gastos.groupBy { gasto ->
                gasto.fecha
            }
            
            // Obtener otros ingresos por día
            val otrosIngresosPorDia = todosLosOtrosIngresos.groupBy { ingreso ->
                try {
                    val fechaIngreso = formatoAlmacenamiento.parse(ingreso.fecha)
                    formatoAlmacenamiento.format(fechaIngreso ?: Date())
                } catch (e: Exception) {
                    ingreso.fecha // Si hay error, usar la fecha original
                }
            }
            
            // Generar la lista de todas las fechas del mes
            val todasLasFechasDelMes = mutableListOf<String>()
            tempCalendar.time = fechaInicio
            while (tempCalendar.time <= fechaFin) {
                todasLasFechasDelMes.add(formatoAlmacenamiento.format(tempCalendar.time))
                tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            // Combinar todas las fechas (del mes, con carreras y con gastos)
            val todasLasFechas = (todasLasFechasDelMes + carrerasPorDia.keys + gastosPorDia.keys + otrosIngresosPorDia.keys).distinct().sorted()

            val resumenes = mutableListOf<ResumenMensualItem>()

            // Procesar cada día
            for (fechaStr in todasLasFechas) {
                var totalIngresos = 0.0
                var totalGastos = 0.0
                
                // Calcular ingresos de las carreras para este día
                val carrerasDelDia = carrerasPorDia[fechaStr] ?: emptyList()
                if (carrerasDelDia.isNotEmpty()) {
                    // Sumar importes reales y propinas
                    totalIngresos += carrerasDelDia.sumOf { it.importeReal + it.propina }
                    
                    // Calcular comisiones (que son gastos)
                    val carrerasTarjeta = carrerasDelDia.count { it.formaPago == FormaPago.TARJETA }
                    val carrerasEmisora = carrerasDelDia.count { it.emisora }
                    val comisionTarjeta = carrerasTarjeta * 0.0 // 0.50€ por carrera con tarjeta
                    val comisionEmisora = carrerasEmisora * 0.0 // 1€ por carrera de emisora
                    
                    // Añadir comisiones a los gastos
                    totalGastos += comisionTarjeta + comisionEmisora
                    
                    Log.d("ResumenMensualViewModel", "Fecha $fechaStr - Carreras: ${carrerasDelDia.size}, Ingresos: $totalIngresos")
                }
                
                // Añadir gastos para este día
                val gastosDelDia = gastosPorDia[fechaStr] ?: emptyList()
                if (gastosDelDia.isNotEmpty()) {
                    totalGastos += gastosDelDia.sumOf { it.importeTotal }
                    Log.d("ResumenMensualViewModel", "Fecha $fechaStr - Gastos: ${gastosDelDia.size}, Total Gastos: $totalGastos")
                }
                
                // Obtener otros ingresos para este día
                val otrosIngresosDelDia = otrosIngresosPorDia[fechaStr] ?: emptyList()
                if (otrosIngresosDelDia.isNotEmpty()) {
                    totalIngresos += otrosIngresosDelDia.sumOf { it.importe }
                    Log.d("ResumenMensualViewModel", "Otros ingresos para $fechaStr: ${otrosIngresosDelDia.sumOf { it.importe }}")
                }
                
                // Solo incluir días con ingresos o gastos
                if (totalIngresos > 0 || totalGastos > 0) {
                    // Convertir la fecha para navegación y display
                    val fechaDate = formatoAlmacenamiento.parse(fechaStr) ?: Date()
                    val fechaParaNavegacion = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(fechaDate)
                    
                resumenes.add(
                    ResumenMensualItem(
                            fecha = fechaStr,
                        fechaDate = fechaDate,
                            fechaParaNavegacion = fechaParaNavegacion,
                        ingresos = totalIngresos,
                        gastos = totalGastos,
                        total = totalIngresos - totalGastos
                    )
                )
                    
                    Log.d("ResumenMensualViewModel", "Resumen añadido para $fechaStr: Ingresos=$totalIngresos, Gastos=$totalGastos")
                }
            }

            // Ordenar por fecha descendente y actualizar el estado
            _resumenesMensuales.value = resumenes.sortedByDescending { it.fechaDate }
            
            // Al finalizar
            _cargando.value = false
            
            Log.d("ResumenMensualViewModel", "Resúmenes cargados: ${resumenes.size}")
        } catch (e: Exception) {
            _error.value = "Error al cargar los resúmenes: ${e.message}"
            _cargando.value = false
            Log.e("ResumenMensualViewModel", "Error al cargar resúmenes", e)
            e.printStackTrace()
        }
    }

    fun cargarResumenMensual(fecha: Date) {
        viewModelScope.launch {
            try {
                _cargando.value = true
                _error.value = null
                
                // Configurar el rango de fechas del mes
            val calendar = Calendar.getInstance()
            calendar.time = fecha
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val fechaInicio = calendar.time

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val fechaFin = calendar.time

                // Formato dd/MM/yyyy para buscar en la base de datos
                val formatoAlmacenamiento = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaInicioAlm = formatoAlmacenamiento.format(fechaInicio)
                val fechaFinAlm = formatoAlmacenamiento.format(fechaFin)
                
                // Formato yyyyMMdd para búsquedas en formato ID
                val fechaInicioStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(fechaInicio)
                val fechaFinStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(fechaFin)
                
                Log.d("ResumenMensualViewModel", "Cargando resumen para mes específico: $fechaInicioAlm a $fechaFinAlm")
                
                // 1. Consultar todos los gastos del mes
                val gastos = gastoDao.getAllGastos().first().filter { gasto ->
                    try {
                        val fechaGasto = formatoAlmacenamiento.parse(gasto.fecha)
                        fechaGasto != null && fechaGasto >= fechaInicio && fechaGasto <= fechaFin
                    } catch (e: Exception) {
                        false
                    }
                }
                
                // 2. Obtener todas las carreras del mes
                val todasLasCarreras = mutableListOf<Carrera>()
                val todosLosOtrosIngresos = mutableListOf<OtrosIngresos>()
                
                for (dia in 1..31) {
                    calendar.time = fechaInicio
                    calendar.set(Calendar.DAY_OF_MONTH, dia)
                    
                    if (calendar.time > fechaFin) break
                    
                    val fechaDiaStr = formatoAlmacenamiento.format(calendar.time)
                    try {
                        val carrerasDia = carreraDao.getCarrerasByFecha(fechaDiaStr).first()
                        todasLasCarreras.addAll(carrerasDia)
                    } catch (e: Exception) {
                        Log.e("ResumenMensualViewModel", "Error al obtener carreras para $fechaDiaStr", e)
                    }
                }
                
                // Obtener otros ingresos para el mes
                for (dia in 1..31) {
                    calendar.time = fechaInicio
                    calendar.set(Calendar.DAY_OF_MONTH, dia)
                    
                    if (calendar.time > fechaFin) break
                    
                    val fechaDiaStr = formatoAlmacenamiento.format(calendar.time)
                    try {
                        val otrosIngresosDia = database.otrosIngresosDao().getOtrosIngresosByFecha(fechaDiaStr).first()
                        todosLosOtrosIngresos.addAll(otrosIngresosDia)
                    } catch (e: Exception) {
                        Log.e("ResumenMensualViewModel", "Error al obtener otros ingresos para $fechaDiaStr", e)
                    }
                }
                
                Log.d("ResumenMensualViewModel", "Carreras encontradas: ${todasLasCarreras.size}, Gastos encontrados: ${gastos.size}, Otros ingresos encontrados: ${todosLosOtrosIngresos.size}")
                
                // 3. Agrupar las carreras por día
                val carrerasPorDia = todasLasCarreras.groupBy { carrera ->
                    try {
                        val fechaCarrera = formatoAlmacenamiento.parse(carrera.fecha)
                        formatoAlmacenamiento.format(fechaCarrera ?: Date())
                    } catch (e: Exception) {
                        carrera.fecha // Si hay error, usar la fecha original
                    }
                }
                
                // 4. Agrupar los gastos por día
                val gastosPorDia = gastos.groupBy { gasto -> gasto.fecha }
                
                // Obtener otros ingresos por día
                val otrosIngresosPorDia = todosLosOtrosIngresos.groupBy { ingreso ->
                    try {
                        val fechaIngreso = formatoAlmacenamiento.parse(ingreso.fecha)
                        formatoAlmacenamiento.format(fechaIngreso ?: Date())
                    } catch (e: Exception) {
                        ingreso.fecha // Si hay error, usar la fecha original
                    }
                }
                
                // 5. Generar todas las fechas del mes
                val todasLasFechasDelMes = mutableListOf<String>()
                calendar.time = fechaInicio
                while (calendar.time <= fechaFin) {
                    todasLasFechasDelMes.add(formatoAlmacenamiento.format(calendar.time))
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                // 6. Añadir fechas de carreras, gastos y otros ingresos
                val todasLasFechas = (todasLasFechasDelMes + carrerasPorDia.keys + gastosPorDia.keys + otrosIngresosPorDia.keys).distinct()

            val resumenes = mutableListOf<ResumenMensualItem>()

                // 7. Procesar cada día
                for (fechaStr in todasLasFechas) {
                var totalIngresos = 0.0
                var totalGastos = 0.0
                    
                    // 7.1 Calcular ingresos de las carreras para este día
                    val carrerasDelDia = carrerasPorDia[fechaStr] ?: emptyList()
                    if (carrerasDelDia.isNotEmpty()) {
                        totalIngresos += carrerasDelDia.sumOf { it.importeReal }
                        
                        // Calcular comisiones (gastos)
                        val carrerasTarjeta = carrerasDelDia.count { it.formaPago == FormaPago.TARJETA }
                        val carrerasEmisora = carrerasDelDia.count { it.emisora }
                        val comisionTarjeta = carrerasTarjeta * 0.0 // 0.50€ por tarjeta
                        val comisionEmisora = carrerasEmisora * 0.0 // 1€ por emisora
                        totalGastos += comisionTarjeta + comisionEmisora
                    }
                    
                    // 7.2 Añadir gastos del día
                    val gastosDelDia = gastosPorDia[fechaStr] ?: emptyList()
                    if (gastosDelDia.isNotEmpty()) {
                    totalGastos += gastosDelDia.sumOf { it.importeTotal }
                }

                    // 7.3 Obtener otros ingresos para este día
                    val otrosIngresosDelDia = otrosIngresosPorDia[fechaStr] ?: emptyList()
                    if (otrosIngresosDelDia.isNotEmpty()) {
                        totalIngresos += otrosIngresosDelDia.sumOf { it.importe }
                        Log.d("ResumenMensualViewModel", "Otros ingresos para $fechaStr: ${otrosIngresosDelDia.sumOf { it.importe }}")
                    }

                    // 7.4 Crear el resumen del día si hay ingresos o gastos
                    if (totalIngresos > 0 || totalGastos > 0) {
                        // Convertir fecha para mostrar y ordenar
                        val fechaDate = formatoAlmacenamiento.parse(fechaStr) ?: Date()
                        val fechaParaNavegacion = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(fechaDate)
                        
                resumenes.add(
                    ResumenMensualItem(
                                fecha = fechaStr,
                        fechaDate = fechaDate,
                                fechaParaNavegacion = fechaParaNavegacion,
                        ingresos = totalIngresos,
                        gastos = totalGastos,
                        total = totalIngresos - totalGastos
                    )
                )
                    }
            }

                // 8. Ordenar y actualizar estado
            _resumenesMensuales.value = resumenes.sortedByDescending { it.fechaDate }
                _cargando.value = false
                
                Log.d("ResumenMensualViewModel", "Resumen mensual cargado con ${resumenes.size} días")
            } catch (e: Exception) {
                _error.value = "Error al cargar el resumen mensual: ${e.message}"
                _cargando.value = false
                Log.e("ResumenMensualViewModel", "Error al cargar resumen mensual", e)
            }
        }
    }
} 