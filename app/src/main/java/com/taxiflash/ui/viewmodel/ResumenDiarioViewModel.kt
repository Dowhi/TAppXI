package com.taxiflash.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.TaxiFlashDatabase
import com.taxiflash.ui.data.FormaPago
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.Turno
import com.taxiflash.ui.data.TurnoRepository
import com.taxiflash.ui.utils.FechaUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import android.annotation.SuppressLint

data class ResumenTurno(
    val numeroTurno: Int = 0,
    val totalCarreras: Int = 0,
    val propinas: Double = 0.0,
    val carrerasTarjeta: Int = 0,
    val carrerasEmisora: Int = 0,
    val sumaEmisora: Double = 0.0,
    val sumaTarjetas: Double = 0.0,
    val kmInicio: Int = 0,
    val kmFin: Int = 0,
    val horaInicio: String = "",
    val horaFin: String = "",
    val totalImporte: Double = 0.0
)

data class ResumenDiarioData(
    val fecha: String = "",
    val turnos: List<ResumenTurno> = emptyList()
)

class ResumenDiarioViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val carreraDao = database.carreraDao()
    private val turnoDao = database.turnoDao()
    private val turnoRepository = TurnoRepository(turnoDao, carreraDao)

    private val _resumenDiario = MutableStateFlow(ResumenDiarioData())
    val resumenDiario: StateFlow<ResumenDiarioData> = _resumenDiario

    private val _turnos = MutableStateFlow<List<Turno>>(emptyList())
    val turnos: StateFlow<List<Turno>> = _turnos

    init {
        cargarResumenDiario(Date())
    }

    fun cargarResumenDiario(fecha: Date) {
        viewModelScope.launch {
            try {
                // Convertir la fecha al formato correspondiente
                val fechaFormateadaParaUI = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("es")).format(fecha)
                val fechaParaConsulta = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha)
                
                Log.d("ResumenDiarioViewModel", "Cargando resumen para fecha: $fechaParaConsulta")
    
                // Obtener las carreras del día usando el formato correcto
                val carreras = carreraDao.getCarrerasByFecha(fechaParaConsulta).first()
                Log.d("ResumenDiarioViewModel", "Carreras encontradas para $fechaParaConsulta: ${carreras.size}")
                
                // Obtener los turnos para esta fecha 
                val fechaConsulta = fechaParaConsulta ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                val turnos = withContext(Dispatchers.IO) {
                    turnoRepository.getTurnosByFechaExacta(fechaConsulta)
                }
                Log.d("ResumenDiarioViewModel", "Turnos encontrados para $fechaConsulta: ${turnos.size}")
                
                // Mapear turnos por su ID y número
                val turnosPorId = turnos.associateBy { it.idTurno }
                val turnosPorNumero = turnos.associateBy { it.numeroTurno }
                
                // Agrupar carreras por turno
                val carrerasPorTurno = mutableMapOf<String, List<Carrera>>()
                
                // Procesar carreras con formato "Turno X"
                val carrerasConTurnoSimple = carreras.filter { it.turno.startsWith("Turno ") }
                for (carrera in carrerasConTurnoSimple) {
                    val numeroTurnoStr = carrera.turno.substringAfter("Turno ").trim()
                    val numeroTurno = numeroTurnoStr.toIntOrNull() ?: 0
                    
                    // Buscar si existe un turno con ese número
                    val turno = turnosPorNumero[numeroTurno]
                    if (turno != null) {
                        // Agregar al grupo usando el ID del turno
                        val turnoId = turno.idTurno
                        val carrerasActuales = carrerasPorTurno[turnoId] ?: emptyList()
                        carrerasPorTurno[turnoId] = carrerasActuales + carrera
                    } else {
                        // Si no hay turno, agrupar por el texto del turno
                        val carrerasActuales = carrerasPorTurno[carrera.turno] ?: emptyList()
                        carrerasPorTurno[carrera.turno] = carrerasActuales + carrera
                    }
                }
                
                // Procesar carreras con formato yyyyMMdd-numeroTurno
                val carrerasConTurnoComplejo = carreras.filter { !it.turno.startsWith("Turno ") }
                for (carrera in carrerasConTurnoComplejo) {
                    val turnoId = carrera.turno
                    val carrerasActuales = carrerasPorTurno[turnoId] ?: emptyList()
                    carrerasPorTurno[turnoId] = carrerasActuales + carrera
                }
                
                Log.d("ResumenDiarioViewModel", "Grupos de carreras por turno: ${carrerasPorTurno.keys}")
                
                // Crear resúmenes para cada turno
                val resumenTurnos = mutableListOf<ResumenTurno>()
                
                // Primero procesar los turnos que existen en la base de datos
                for (turno in turnos) {
                    // Verificar si hay carreras para este turno
                    val carrerasDelTurno = carrerasPorTurno[turno.idTurno] ?: emptyList()
                    
                    Log.d("ResumenDiarioViewModel", "Turno ${turno.idTurno} (número ${turno.numeroTurno}) tiene ${carrerasDelTurno.size} carreras")
                    
                    // Crear el resumen para este turno
                    resumenTurnos.add(
                        ResumenTurno(
                            numeroTurno = turno.numeroTurno,
                            totalCarreras = carrerasDelTurno.size,
                            propinas = carrerasDelTurno.sumOf { it.propina },
                            carrerasTarjeta = carrerasDelTurno.count { it.formaPago == FormaPago.TARJETA },
                            carrerasEmisora = carrerasDelTurno.count { it.emisora },
                            sumaEmisora = carrerasDelTurno.filter { it.emisora }
                                .sumOf { it.importeReal },
                            sumaTarjetas = carrerasDelTurno.filter { it.formaPago == FormaPago.TARJETA }
                                .sumOf { it.importeReal },
                            kmInicio = turno.kmInicio,
                            kmFin = turno.kmFin,
                            horaInicio = turno.horaInicio,
                            horaFin = turno.horaFin ?: "",
                            totalImporte = carrerasDelTurno.sumOf { it.importeReal }
                        )
                    )
                }
                
                // Luego procesar carreras que están en turnos que no existen en la base de datos
                for ((turnoId, carrerasDelTurno) in carrerasPorTurno.entries) {
                    // Si ya procesamos este turno, omitirlo
                    if (turnosPorId.containsKey(turnoId)) continue
                    
                    Log.d("ResumenDiarioViewModel", "Procesando turno adicional: $turnoId con ${carrerasDelTurno.size} carreras")
                    
                    // Determinar el número de turno
                    val numeroTurno = if (turnoId.startsWith("Turno ")) {
                        turnoId.substringAfter("Turno ").trim().toIntOrNull() ?: 0
                    } else {
                        turnoId.split("-").lastOrNull()?.toIntOrNull() ?: 0
                    }
                    
                    // Crear resumen para este turno
                    resumenTurnos.add(
                    ResumenTurno(
                        numeroTurno = numeroTurno,
                        totalCarreras = carrerasDelTurno.size,
                        propinas = carrerasDelTurno.sumOf { it.propina },
                        carrerasTarjeta = carrerasDelTurno.count { it.formaPago == FormaPago.TARJETA },
                        carrerasEmisora = carrerasDelTurno.count { it.emisora },
                        sumaEmisora = carrerasDelTurno.filter { it.emisora }
                            .sumOf { it.importeReal },
                        sumaTarjetas = carrerasDelTurno.filter { it.formaPago == FormaPago.TARJETA }
                            .sumOf { it.importeReal },
                        totalImporte = carrerasDelTurno.sumOf { it.importeReal }
                        )
                    )
                }
                
                Log.d("ResumenDiarioViewModel", "Resumen actualizado con ${resumenTurnos.size} turnos para $fechaFormateadaParaUI")

                _resumenDiario.value = ResumenDiarioData(
                    fecha = fechaFormateadaParaUI,
                    turnos = resumenTurnos
                )
            } catch (e: Exception) {
                Log.e("ResumenDiarioViewModel", "Error al cargar resumen diario: ${e.message}", e)
            }
        }
    }

    fun cargarTurnosDia(fecha: Date) {
        viewModelScope.launch {
            try {
            val fechaStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(fecha)
                val turnos = withContext(Dispatchers.IO) {
                    turnoRepository.getTurnosByFechaExacta(fechaStr)
                }
                _turnos.value = turnos
            } catch(e: Exception) {
                Log.e("ResumenDiarioViewModel", "Error al cargar turnos del día", e)
            }
        }
    }

    fun eliminarTurno(turnoId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                turnoRepository.eliminarTurnoYCarreras(turnoId)
                cargarResumenDiario(Date())
                onComplete()
            } catch (e: Exception) {
                // Manejar el error
                Log.e("ResumenDiarioViewModel", "Error al eliminar turno", e)
            }
        }
    }

    private suspend fun procesarDatos(fechaConsulta: String) {
        try {
            withContext(Dispatchers.IO) {
                // Obtener turnos para la fecha
                val turnos = turnoDao.getTurnosByFechaExacta(fechaConsulta)
                Log.d("ResumenDiarioViewModel", "Turnos encontrados para $fechaConsulta: ${turnos.size}")
                
                // Mapear turnos por su ID y número
                val turnosPorId = turnos.associateBy { it.idTurno }
                val turnosPorNumero = turnos.associateBy { it.numeroTurno }
                
                // ... resto del código existente ...
            }
        } catch (e: Exception) {
            // ... manejo de errores existente ...
        }
    }

    @SuppressLint("Range")
    private suspend fun procesarCarreras(fechaConsulta: String, turnos: List<Turno>) {
        // ... código existente ...
    }
} 