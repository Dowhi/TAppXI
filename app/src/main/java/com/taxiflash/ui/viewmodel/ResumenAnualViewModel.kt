package com.taxiflash.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.TaxiFlashDatabase
import com.taxiflash.ui.data.Turno
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.FormaPago
import com.taxiflash.ui.utils.FechaUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat

data class ResumenMes(
    val nombre: String,
    val ingresos: Double,
    val gastos: Double,
    val total: Double
)

data class ResumenAnual(
    val año: String,
    val meses: List<ResumenMes>,
    val totalIngresos: Double,
    val totalGastos: Double,
    val totalNeto: Double
)

class ResumenAnualViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val turnoDao = database.turnoDao()
    private val carreraDao = database.carreraDao()
    private val gastoDao = database.gastoDao()

    private val _resumenAnual = MutableStateFlow<ResumenAnual?>(null)
    val resumenAnual: StateFlow<ResumenAnual?> = _resumenAnual.asStateFlow()

    private val mesesEspañol = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    init {
        cargarResumenAnual()
    }

    fun cargarResumenAnual(year: Int = Calendar.getInstance().get(Calendar.YEAR)) {
        viewModelScope.launch {
            try {
                Log.d("ResumenAnualViewModel", "Cargando resumen anual para el año: $year")
                
                // Obtener el primer y último día del año usando Calendar
                val calInicio = Calendar.getInstance()
                calInicio.set(year, Calendar.JANUARY, 1, 0, 0, 0)
                val inicioAño = calInicio.time
                
                val calFin = Calendar.getInstance()
                calFin.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
                val finAño = calFin.time

                // Formato para fechas en la base de datos (dd/MM/yyyy)
                val formatoFechaDB = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                
                // Formato para IDs de turnos (yyyyMMdd)
                val formatoFechaID = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val fechaInicioID = formatoFechaID.format(inicioAño)
                val fechaFinID = formatoFechaID.format(finAño)
                
                Log.d("ResumenAnualViewModel", "Consultando datos entre: $fechaInicioID y $fechaFinID")

                // Obtener todas las carreras del año por mes
                val todasLasCarreras = mutableListOf<Carrera>()
                val carrerasPorMes = mutableMapOf<Int, MutableList<Carrera>>()
                
                // Inicializar listas para cada mes
                for (mes in 0..11) {
                    carrerasPorMes[mes] = mutableListOf()
                }
                
                // Iterar por cada mes del año
                for (mes in 0..11) {
                    // Configurar el primer día del mes
                    val calMesInicio = Calendar.getInstance()
                    calMesInicio.set(year, mes, 1, 0, 0, 0)
                    
                    // Configurar el último día del mes
                    val calMesFin = Calendar.getInstance()
                    calMesFin.set(year, mes, calMesFin.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    
                    // Iterar por cada día del mes
                    val tempCalendar = Calendar.getInstance()
                    tempCalendar.time = calMesInicio.time
                    
                    while (tempCalendar.time <= calMesFin.time) {
                        val fechaDiaStr = formatoFechaDB.format(tempCalendar.time)
                        try {
                            val carrerasDia = carreraDao.getCarrerasByFecha(fechaDiaStr).first()
                            carrerasPorMes[mes]?.addAll(carrerasDia)
                            todasLasCarreras.addAll(carrerasDia)
                            Log.d("ResumenAnualViewModel", "Mes ${mes+1}, Fecha $fechaDiaStr: ${carrerasDia.size} carreras")
                        } catch (e: Exception) {
                            Log.e("ResumenAnualViewModel", "Error al obtener carreras para $fechaDiaStr", e)
                        }
                        tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
                
                Log.d("ResumenAnualViewModel", "Total carreras encontradas: ${todasLasCarreras.size}")
                
                // Obtener todos los gastos del año
                val gastos = gastoDao.getAllGastos().first().filter { gasto ->
                    try {
                        val fechaGasto = formatoFechaDB.parse(gasto.fecha)
                        fechaGasto != null && fechaGasto >= inicioAño && fechaGasto <= finAño
                    } catch (e: Exception) {
                        Log.e("ResumenAnualViewModel", "Error al filtrar gasto con fecha ${gasto.fecha}", e)
                        false
                    }
                    }

                    // Agrupar gastos por mes
                    val gastosPorMes = gastos.groupBy { gasto ->
                    try {
                        val fechaGasto = formatoFechaDB.parse(gasto.fecha)
                        if (fechaGasto != null) {
                            val cal = Calendar.getInstance()
                            cal.time = fechaGasto
                            cal.get(Calendar.MONTH)
                        } else {
                            // Si no se puede parsear, intentar extraer el mes directamente
                        val partesFecha = gasto.fecha.split("/")
                        if (partesFecha.size >= 2) {
                            partesFecha[1].toInt() - 1
                        } else {
                            0
                        }
                    }
                    } catch (e: Exception) {
                        Log.e("ResumenAnualViewModel", "Error al agrupar gasto por mes: ${gasto.fecha}", e)
                        0
                    }
                }
                
                Log.d("ResumenAnualViewModel", "Total gastos encontrados: ${gastos.size}")
                
                // Procesar los datos por mes
                val resumenMeses = mutableListOf<ResumenMes>()
                var totalIngresosAnual = 0.0
                var totalGastosAnual = 0.0
                
                for (mes in 0..11) {
                    val carrerasDelMes = carrerasPorMes[mes] ?: emptyList()
                    val gastosDelMes = gastosPorMes[mes] ?: emptyList()
                    
                    var ingresosMes = 0.0
                    var gastosMes = 0.0
                    
                    // Calcular ingresos del mes
                    if (carrerasDelMes.isNotEmpty()) {
                        // Sumar importes reales y propinas
                        ingresosMes += carrerasDelMes.sumOf { it.importeReal + it.propina }
                        
                        // Calcular comisiones (gastos)
                        val carrerasTarjeta = carrerasDelMes.count { it.formaPago == FormaPago.TARJETA }
                        val carrerasEmisora = carrerasDelMes.count { it.emisora }
                        val comisionTarjeta = carrerasTarjeta * 0.0 // 0.50€ por tarjeta
                        val comisionEmisora = carrerasEmisora * 0.0 // 1€ por emisora
                        gastosMes += comisionTarjeta + comisionEmisora
                    }
                    
                    // Sumar gastos del mes
                    if (gastosDelMes.isNotEmpty()) {
                        gastosMes += gastosDelMes.sumOf { it.importeTotal }
                    }
                    
                    // Obtener otros ingresos para el mes
                    val otrosIngresosDelMes = database.otrosIngresosDao().getOtrosIngresosByMes(mesesEspañol[mes], year.toString()).first()
                    val totalOtrosIngresosMes = otrosIngresosDelMes.sumOf { it.importe }
                    
                    // Calcular ingresos del mes: carreras + otros ingresos
                    val totalIngresosMes = ingresosMes + totalOtrosIngresosMes
                    
                    Log.d("ResumenAnualViewModel", "Mes: ${mesesEspañol[mes]}, Total Carreras: ${carrerasDelMes.size}, Total Otros Ingresos: $totalOtrosIngresosMes, Total Gastos: $gastosMes")

                    resumenMeses.add(
                        ResumenMes(
                            nombre = mesesEspañol[mes],
                            ingresos = totalIngresosMes,
                            gastos = gastosMes,
                            total = totalIngresosMes - gastosMes
                        )
                    )

                    totalIngresosAnual += totalIngresosMes
                    totalGastosAnual += gastosMes
                }

                Log.d("ResumenAnualViewModel", "Resumen anual calculado - Ingresos: $totalIngresosAnual, Gastos: $totalGastosAnual")
                
                // Actualizar el estado con el resumen anual
                _resumenAnual.value = ResumenAnual(
                        año = year.toString(),
                        meses = resumenMeses,
                        totalIngresos = totalIngresosAnual,
                        totalGastos = totalGastosAnual,
                        totalNeto = totalIngresosAnual - totalGastosAnual
                    )
                
                Log.d("ResumenAnualViewModel", "Resumen anual actualizado en la UI")
            } catch (e: Exception) {
                Log.e("ResumenAnualViewModel", "Error al cargar resumen anual: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
} 