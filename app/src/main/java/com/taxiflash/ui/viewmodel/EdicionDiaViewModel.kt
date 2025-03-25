package com.taxiflash.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.Turno
import com.taxiflash.ui.data.repository.CarreraRepository
import com.taxiflash.ui.data.repository.TurnoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EdicionDiaViewModel(
    application: Application,
    private val fechaStr: String // Formato: "yyyy-MM-dd"
) : ViewModel() {
    private val turnoRepository = TurnoRepository(application)
    private val carreraRepository = CarreraRepository(application)
    
    private val _turnos = MutableStateFlow<List<Turno>>(emptyList())
    val turnos: StateFlow<List<Turno>> = _turnos.asStateFlow()
    
    private val _carreras = MutableStateFlow<List<Carrera>>(emptyList())
    val carreras: StateFlow<List<Carrera>> = _carreras.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Cache para cambios en edición
    private val cambiosKilometros = mutableMapOf<String, Pair<Int, Int>>() // turnoId -> (kmInicio, kmFin)
    private val cambiosHorarios = mutableMapOf<String, Pair<String, String>>() // turnoId -> (horaInicio, horaFin)
    
    init {
        Log.d("EdicionDiaViewModel", "Inicializando ViewModel con fecha: $fechaStr")
        cargarDatosDeFecha()
    }
    
    private fun cargarDatosDeFecha() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("EdicionDiaViewModel", "Formato de fecha original: $fechaStr")
                
                // Intentar diferentes formatos de fecha - La fecha en la DB es exactamente "24/03/2025"
                var fechaFormateada = fechaStr
                
                // Intentar convertir de yyyy-MM-dd a dd/MM/yyyy
                try {
                    val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fecha = formatoEntrada.parse(fechaStr)
                    if (fecha != null) {
                        fechaFormateada = formatoSalida.format(fecha)
                        Log.d("EdicionDiaViewModel", "Fecha convertida a formato dd/MM/yyyy: $fechaFormateada")
                    }
                } catch (e: Exception) {
                    Log.e("EdicionDiaViewModel", "Error al convertir formato yyyy-MM-dd: ${e.message}")
                    
                    // Si la conversión falla, intentar extraer directamente de la fecha
                    try {
                        // Para fechas como 2025-03-24 convertir a 24/03/2025
                        val partes = fechaStr.split("-")
                        if (partes.size == 3) {
                            fechaFormateada = "${partes[2]}/${partes[1]}/${partes[0]}"
                            Log.d("EdicionDiaViewModel", "Fecha convertida manualmente: $fechaFormateada")
                        }
                    } catch (e: Exception) {
                        Log.e("EdicionDiaViewModel", "Error al convertir fecha manualmente: ${e.message}")
                    }
                }
                
                // Probar directamente con el formato de la base de datos
                val fechaBaseDatos = obtenerFechaExacta(fechaStr)
                if (fechaBaseDatos != null) {
                    fechaFormateada = fechaBaseDatos
                    Log.d("EdicionDiaViewModel", "Usando formato exacto de base de datos: $fechaFormateada")
                }
                
                Log.d("EdicionDiaViewModel", "Consultando turnos para fecha formateada final: $fechaFormateada")
                
                // Cargar los turnos de la fecha específica
                withContext(Dispatchers.IO) {
                    // Intentar con la fecha formateada primero
                    var turnosDelDia = turnoRepository.getTurnosByFecha(fechaFormateada)
                    Log.d("EdicionDiaViewModel", "Turnos encontrados con fecha formateada: ${turnosDelDia.size}")
                    
                    // Si no hay resultados, intentar con búsqueda por formato exacto en la DB
                    if (turnosDelDia.isEmpty()) {
                        // Obtener todos los turnos y buscar por fecha
                        val todosTurnos = turnoRepository.getAllTurnos()
                        Log.d("EdicionDiaViewModel", "Total de turnos en DB: ${todosTurnos.size}")
                        
                        // Imprimir todas las fechas disponibles para debugging
                        val fechasDisponibles = todosTurnos.map { it.fecha }.distinct()
                        Log.d("EdicionDiaViewModel", "Fechas disponibles en DB: $fechasDisponibles")
                        
                        // Intentar diferentes variantes de formato
                        val fechasAlternativas = obtenerVariantesDeFecha(fechaStr)
                        Log.d("EdicionDiaViewModel", "Intentando con variantes de fecha: $fechasAlternativas")
                        
                        for (fechaAlt in fechasAlternativas) {
                            val turnosAlt = turnoRepository.getTurnosByFecha(fechaAlt)
                            if (turnosAlt.isNotEmpty()) {
                                Log.d("EdicionDiaViewModel", "Encontrados ${turnosAlt.size} turnos con formato alternativo: $fechaAlt")
                                turnosDelDia = turnosAlt
                                break
                            }
                        }
                    }
                    
                    if (turnosDelDia.isNotEmpty()) {
                        Log.d("EdicionDiaViewModel", "Turnos encontrados finalmente: ${turnosDelDia.size}")
                        Log.d("EdicionDiaViewModel", "Primer turno: ${turnosDelDia[0].idTurno}, fecha: ${turnosDelDia[0].fecha}")
                        
                        _turnos.value = turnosDelDia
                        
                        // Obtener todas las carreras asociadas a los turnos
                        val todasLasCarreras = mutableListOf<Carrera>()
                        for (turno in turnosDelDia) {
                            Log.d("EdicionDiaViewModel", "Obteniendo carreras para turno: ${turno.idTurno}")
                            val carrerasDeTurno = carreraRepository.getCarrerasForTurno(turno.idTurno)
                            Log.d("EdicionDiaViewModel", "Carreras encontradas para turno ${turno.idTurno}: ${carrerasDeTurno.size}")
                            todasLasCarreras.addAll(carrerasDeTurno)
                        }
                        _carreras.value = todasLasCarreras
                        Log.d("EdicionDiaViewModel", "Total de carreras cargadas: ${todasLasCarreras.size}")
                    } else {
                        Log.d("EdicionDiaViewModel", "No se encontraron turnos para ningún formato de fecha")
                        _turnos.value = emptyList()
                        _carreras.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("EdicionDiaViewModel", "Error al cargar datos: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Obtener la fecha exacta como aparece en la base de datos
    private fun obtenerFechaExacta(fechaStr: String): String? {
        // Si la fecha ya está en formato dd/MM/yyyy, la devolvemos tal cual
        if (fechaStr.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
            return fechaStr
        }

        // Si la fecha está en formato yyyy-MM-dd, la convertimos a dd/MM/yyyy
        try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fecha = formatoEntrada.parse(fechaStr)
            
            if (fecha != null) {
                val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                return formatoSalida.format(fecha)
            }
        } catch (e: Exception) {
            Log.e("EdicionDiaViewModel", "Error al obtener fecha exacta: ${e.message}")
        }
        
        return null
    }

    // Obtener varias variantes de la fecha para intentar
    private fun obtenerVariantesDeFecha(fechaStr: String): List<String> {
        val variantes = mutableListOf<String>()
        
        try {
            // Si la fecha ya está en formato dd/MM/yyyy, agregarla
            if (fechaStr.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                variantes.add(fechaStr)
                return variantes
            }
            
            // Si la fecha está en formato yyyy-MM-dd
            if (fechaStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                val partes = fechaStr.split("-")
                if (partes.size == 3) {
                    // Convertir a dd/MM/yyyy
                    variantes.add("${partes[2]}/${partes[1]}/${partes[0]}")
                    return variantes
                }
            }
            
            // Intentar convertir usando SimpleDateFormat como último recurso
            try {
                val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fecha = formatoEntrada.parse(fechaStr)
                if (fecha != null) {
                    variantes.add(formatoSalida.format(fecha))
                }
            } catch (e: Exception) {
                Log.e("EdicionDiaViewModel", "Error al convertir fecha con SimpleDateFormat: ${e.message}")
            }
            
            Log.d("EdicionDiaViewModel", "Variantes de fecha generadas: $variantes")
        } catch (e: Exception) {
            Log.e("EdicionDiaViewModel", "Error al generar variantes de fecha: ${e.message}")
        }
        
        return variantes.distinct()
    }
    
    // Métodos para edición
    fun actualizarKilometros(turnoId: String, kmInicio: Int, kmFin: Int) {
        Log.d("EdicionDiaViewModel", "Actualizando kilómetros para turno $turnoId: $kmInicio-$kmFin")
        cambiosKilometros[turnoId] = Pair(kmInicio, kmFin)
        
        // Actualizar la interfaz de usuario inmediatamente
        _turnos.value = _turnos.value.map { turno ->
            if (turno.idTurno == turnoId) {
                turno.copy(kmInicio = kmInicio, kmFin = kmFin)
            } else {
                turno
            }
        }
    }
    
    fun actualizarHorarios(turnoId: String, horaInicio: String, horaFin: String) {
        Log.d("EdicionDiaViewModel", "Actualizando horarios para turno $turnoId: $horaInicio-$horaFin")
        cambiosHorarios[turnoId] = Pair(horaInicio, horaFin)
        
        // Actualizar la interfaz de usuario inmediatamente
        _turnos.value = _turnos.value.map { turno ->
            if (turno.idTurno == turnoId) {
                turno.copy(horaInicio = horaInicio, horaFin = horaFin)
            } else {
                turno
            }
        }
    }
    
    fun guardarCambios() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("EdicionDiaViewModel", "Guardando cambios...")
                withContext(Dispatchers.IO) {
                    // Aplicar cambios de kilómetros
                    for ((turnoId, kms) in cambiosKilometros) {
                        val turnoActual = turnoRepository.getTurnoById(turnoId)
                        if (turnoActual != null) {
                            val turnoActualizado = turnoActual.copy(
                                kmInicio = kms.first,
                                kmFin = kms.second
                            )
                            turnoRepository.updateTurno(turnoActualizado)
                            Log.d("EdicionDiaViewModel", "Kilómetros actualizados para turno $turnoId: ${kms.first}-${kms.second}")
                        } else {
                            Log.e("EdicionDiaViewModel", "No se encontró el turno con ID $turnoId")
                        }
                    }
                    
                    // Aplicar cambios de horarios
                    for ((turnoId, horas) in cambiosHorarios) {
                        val turnoActual = turnoRepository.getTurnoById(turnoId)
                        if (turnoActual != null) {
                            val turnoActualizado = turnoActual.copy(
                                horaInicio = horas.first,
                                horaFin = horas.second
                            )
                            turnoRepository.updateTurno(turnoActualizado)
                            Log.d("EdicionDiaViewModel", "Horarios actualizados para turno $turnoId: ${horas.first}-${horas.second}")
                        } else {
                            Log.e("EdicionDiaViewModel", "No se encontró el turno con ID $turnoId")
                        }
                    }
                }
                
                // Limpiar los cambios en caché
                cambiosKilometros.clear()
                cambiosHorarios.clear()
                
                // Recargar datos actualizados
                cargarDatosDeFecha()
                Log.d("EdicionDiaViewModel", "Guardado de cambios completado")
            } catch (e: Exception) {
                Log.e("EdicionDiaViewModel", "Error al guardar cambios: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// Factory para crear el ViewModel con parámetros
class EdicionDiaViewModelFactory(
    private val application: Application,
    private val fechaStr: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EdicionDiaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EdicionDiaViewModel(application, fechaStr) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 