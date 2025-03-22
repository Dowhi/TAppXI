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
import com.taxiflash.ui.data.Turno
import com.taxiflash.ui.utils.FechaUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.*
import java.text.SimpleDateFormat
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers


fun formatearFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = Date(timestamp) // Convertir a Date
    return sdf.format(date)
}

fun formatearTurno(turno: String): String {
    val turnoNumero = turno.substringAfter("-") // Extraer el número después del "-"
    return "Turno $turnoNumero"
}


data class ResumenMensual(
    val mes: String = "",
    val año: Int = 0,
    val dias: Int = 0,
    val carreras: Int = 0,
    val turno1: Int = 0,
    val turno2: Int = 0,
    val propinas: Double = 0.0,
    val sTarjeta: Double = 0.0,
    val sEmisora: Double = 0.0,
    val cTarjeta: Int = 0,
    val cEmisora: Int = 0,
    val cVales: Int = 0,
    val svales: Double = 0.0,
    val ingresosVariados: Double = 0.0,
    val kilometros: Int = 0,
    val totalIngresos: Double = 0.0,
    val totalGastos: Double = 0.0,
    val totalNeto: Double = 0.0,
    val combustible: Double = 0.0,
    val media: Double = 0.0,
    val horas: String = "00:00",
    val aeropuerto: Double = 0.0
)

@RequiresApi(Build.VERSION_CODES.O)
class ResumenMensualDetalladoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val turnoDao = database.turnoDao()
    private val carreraDao = database.carreraDao()
    private val gastoDao = database.gastoDao()

    private val _resumenMensual = MutableStateFlow(ResumenMensual())
    val resumenMensual: StateFlow<ResumenMensual> = _resumenMensual.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarResumenMensual(Date())
    }

    fun cargarResumenMensual(fecha: Date) {
        viewModelScope.launch {
            try {
                _cargando.value = true
                _error.value = null
                
                // Usar Calendar para obtener el primer día del mes
                val calInicio = Calendar.getInstance()
                calInicio.time = fecha
                calInicio.set(Calendar.DAY_OF_MONTH, 1)
                calInicio.set(Calendar.HOUR_OF_DAY, 0)
                calInicio.set(Calendar.MINUTE, 0)
                calInicio.set(Calendar.SECOND, 0)
                val inicioMes = calInicio.time
                
                // Usar Calendar para obtener el último día del mes
                val calFin = Calendar.getInstance()
                calFin.time = fecha
                calFin.set(Calendar.DAY_OF_MONTH, calFin.getActualMaximum(Calendar.DAY_OF_MONTH))
                calFin.set(Calendar.HOUR_OF_DAY, 23)
                calFin.set(Calendar.MINUTE, 59)
                calFin.set(Calendar.SECOND, 59)
                val finMes = calFin.time

                // Formato para fechas en la base de datos (dd/MM/yyyy)
                val formatoFechaDB = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaInicioStr = formatoFechaDB.format(inicioMes)
                val fechaFinStr = formatoFechaDB.format(finMes)
                
                // Formato para IDs de turnos (yyyyMMdd)
                val formatoFechaID = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val fechaInicioID = formatoFechaID.format(inicioMes)
                val fechaFinID = formatoFechaID.format(finMes)
                
                // Obtener nombre del mes y año para el resumen
                val formatoMes = SimpleDateFormat("MMMM", Locale("es", "ES"))
                val formatoAnio = SimpleDateFormat("yyyy", Locale.getDefault())
                val nombreMes = formatoMes.format(fecha).capitalize(Locale.getDefault())
                val anio = formatoAnio.format(fecha).toInt()

                Log.d("ResumenMensualDetalladoViewModel", "Cargando resumen para $nombreMes $anio")
                Log.d("ResumenMensualDetalladoViewModel", "Rango de fechas: $fechaInicioStr a $fechaFinStr")

                // Obtener todas las carreras del mes directamente por fecha
                val todasLasCarreras = mutableListOf<Carrera>()
                
                // Iterar por cada día del mes para obtener las carreras
                val tempCalendar = Calendar.getInstance()
                tempCalendar.time = inicioMes
                
                while (tempCalendar.time <= finMes) {
                    val fechaDiaStr = formatoFechaDB.format(tempCalendar.time)
                    try {
                        val carrerasDia = carreraDao.getCarrerasByFecha(fechaDiaStr).first()
                        todasLasCarreras.addAll(carrerasDia)
                        Log.d("ResumenMensualDetalladoViewModel", "Fecha $fechaDiaStr: ${carrerasDia.size} carreras")
                    } catch (e: Exception) {
                        Log.e("ResumenMensualDetalladoViewModel", "Error al obtener carreras para $fechaDiaStr", e)
                    }
                    tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                Log.d("ResumenMensualDetalladoViewModel", "Total carreras encontradas: ${todasLasCarreras.size}")

                // Obtener todos los gastos del mes
                val gastos = gastoDao.getAllGastos().first().filter { gasto ->
                    try {
                        val fechaGasto = formatoFechaDB.parse(gasto.fecha)
                        fechaGasto != null && fechaGasto >= inicioMes && fechaGasto <= finMes
                    } catch (e: Exception) {
                        Log.e("ResumenMensualDetalladoViewModel", "Error al filtrar gasto con fecha ${gasto.fecha}", e)
                        false
                    }
                }
                
                Log.d("ResumenMensualDetalladoViewModel", "Total gastos encontrados: ${gastos.size}")

                // Procesar todas las carreras
            var totalCarreras = 0
            var totalPropinas = 0.0
            var totalNeto = 0.0
            var totalServicioTarjeta = 0.0
            var totalGastos = 0.0
            var totalServicioEmisora = 0.0
            var carrerasTarjeta = 0
            var carrerasEmisora = 0
            var totalKilometros = 0
            var totalAeropuerto = 0.0
            var totalHorasMinutos = 0L
            var totalVales = 0.0
            var turno1Count = 0
            var turno2Count = 0
            var carrerasVales = 0
            var totalCombustible = 0.0
                var diasConCarreras = 0

                // Agrupar carreras por día para contar días trabajados
                val carrerasPorDia = todasLasCarreras.groupBy { it.fecha }
                diasConCarreras = carrerasPorDia.size

                // Procesar todas las carreras
                todasLasCarreras.forEach { carrera ->
                    totalCarreras++
                    totalPropinas += carrera.propina
                    totalNeto += carrera.importeReal
                    
                    if (carrera.formaPago == FormaPago.TARJETA) {
                        totalServicioTarjeta += carrera.importeReal
                        carrerasTarjeta++
                    }
                    
                    if (carrera.emisora) {
                        totalServicioEmisora += carrera.importeReal
                        carrerasEmisora++
                    }
                    
                    if (carrera.formaPago == FormaPago.VALES) {
                        totalVales += carrera.importeReal
                        carrerasVales++
                    }
                    
                    if (carrera.aeropuerto) {
                        totalAeropuerto += carrera.importeReal
                    }

                // Contar turnos
                    if (carrera.turno.endsWith("1") || carrera.turno.contains("Turno 1")) {
                        turno1Count++
                    } else if (carrera.turno.endsWith("2") || carrera.turno.contains("Turno 2")) {
                        turno2Count++
                    }
                }

                // Obtener turnos para calcular kilómetros y horas
                val turnos = obtenerTurnosEntreFechas(fechaInicioID, fechaFinID)
                
                // También buscar turnos con formato "Turno X"
                val turnosSimples = mutableListOf<Turno>()
                for (dia in 1..calFin.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    tempCalendar.time = inicioMes
                    tempCalendar.set(Calendar.DAY_OF_MONTH, dia)
                    val fechaDiaStr = formatoFechaDB.format(tempCalendar.time)
                    
                    try {
                        // Intentar obtener turnos por fecha
                        val turnosDia = withContext(Dispatchers.IO) {
                            turnoDao.getTurnosByFechaExacta(fechaDiaStr)
                        }
                        turnosSimples.addAll(turnosDia)
                        Log.d("ResumenMensualDetalladoViewModel", "Fecha $fechaDiaStr: ${turnosDia.size} turnos simples")
                    } catch (e: Exception) {
                        Log.e("ResumenMensualDetalladoViewModel", "Error al obtener turnos para $fechaDiaStr: ${e.message}")
                    }
                }
                
                // Combinar ambos tipos de turnos
                val todosTurnos = ArrayList<Turno>()
                todosTurnos.addAll(turnos)
                todosTurnos.addAll(turnosSimples)
                val turnosUnicos = todosTurnos.distinctBy { turno -> turno.idTurno }
                
                Log.d("ResumenMensualDetalladoViewModel", "Total turnos encontrados: ${turnosUnicos.size}")

                // Procesar turnos para kilómetros y horas
                turnosUnicos.forEach { turno ->
                    // Calcular kilómetros
                    totalKilometros += turno.kmFin - turno.kmInicio

                // Calcular horas trabajadas
                val horaInicio = turno.horaInicio.split(":").map { it.toIntOrNull() ?: 0 }
                    val horaFin = (turno.horaFin ?: "00:00").split(":").map { it.toIntOrNull() ?: 0 }
                val minutosHoy = if (horaInicio.size >= 2 && horaFin.size >= 2) {
                    (horaFin[0] * 60 + horaFin[1]) - (horaInicio[0] * 60 + horaInicio[1])
                } else {
                    0
                }
                totalHorasMinutos += if (minutosHoy > 0) minutosHoy.toLong() else 0L
            }

            // Procesar gastos
            gastos.forEach { gasto ->
                totalGastos += gasto.importeTotal
                if (gasto.tipoGasto == "Combustible") {
                    totalCombustible += gasto.importeTotal
                }
            }

            // Sumar gastos de servicios
            totalGastos += (carrerasTarjeta * 0.0) + (carrerasEmisora * 0.0)

            val media = if (totalCarreras > 0) totalNeto / totalCarreras else 0.0
            val horas = totalHorasMinutos / 60
            val minutos = totalHorasMinutos % 60
            val horasFormateadas = String.format("%02d:%02d", horas, minutos)

            _resumenMensual.value = ResumenMensual(
                    mes = nombreMes,
                    año = anio,
                    dias = diasConCarreras,
                carreras = totalCarreras,
                turno1 = turno1Count,
                turno2 = turno2Count,
                propinas = totalPropinas,
                sTarjeta = totalServicioTarjeta,
                sEmisora = totalServicioEmisora,
                cTarjeta = carrerasTarjeta,
                cEmisora = carrerasEmisora,
                    cVales = carrerasVales,
                svales = totalVales,
                ingresosVariados = 0.0,
                kilometros = totalKilometros,
                    totalIngresos = totalNeto + totalPropinas,
                totalGastos = totalGastos,
                    totalNeto = (totalNeto + totalPropinas) - totalGastos,
                combustible = totalCombustible,
                media = media,
                horas = horasFormateadas,
                aeropuerto = totalAeropuerto
            )
                
                Log.d("ResumenMensualDetalladoViewModel", "Resumen mensual cargado: ${totalCarreras} carreras, ${diasConCarreras} días")
                _cargando.value = false
            } catch (e: Exception) {
                _error.value = "Error al cargar el resumen mensual: ${e.message}"
                _cargando.value = false
                Log.e("ResumenMensualDetalladoViewModel", "Error al cargar el resumen mensual", e)
                e.printStackTrace()
            }
        }
    }

    private suspend fun obtenerTurnosEntreFechas(fechaInicio: String, fechaFin: String): List<Turno> {
        return try {
            turnoDao.getTurnosEntreFechas(fechaInicio, fechaFin)
        } catch (e: Exception) {
            Log.e("ResumenMensualDetalladoViewModel", "Error al obtener turnos entre fechas", e)
            emptyList()
        }
    }
}