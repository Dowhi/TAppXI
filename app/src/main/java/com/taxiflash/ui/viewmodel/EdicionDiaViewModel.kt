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
    private val fechaStr: String // Formato: "dd/MM/yyyy"
) : ViewModel() {
    private val turnoRepository = TurnoRepository(application)
    private val carreraRepository = CarreraRepository(application)
    
    private val _turnos = MutableStateFlow<List<Turno>>(emptyList())
    val turnos: StateFlow<List<Turno>> = _turnos.asStateFlow()
    
    private val _carreras = MutableStateFlow<List<Carrera>>(emptyList())
    val carreras: StateFlow<List<Carrera>> = _carreras.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _fechasDisponibles = MutableStateFlow<List<String>>(emptyList())
    val fechasDisponibles: StateFlow<List<String>> = _fechasDisponibles.asStateFlow()
    
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
                Log.d("EdicionDiaViewModel", "Iniciando carga de datos para fecha: $fechaStr")
                
                withContext(Dispatchers.IO) {
                    // Primero, normalizar la fecha al formato dd/MM/yyyy
                    val fechaNormalizada = normalizarFecha(fechaStr)
                    Log.d("EdicionDiaViewModel", "Fecha normalizada: $fechaNormalizada")
                    
                    // Obtener todos los turnos para debugging
                    val todosTurnos = turnoRepository.getAllTurnos()
                    val fechas = todosTurnos.map { it.fecha }.distinct()
                    _fechasDisponibles.value = fechas
                    Log.d("EdicionDiaViewModel", "Todas las fechas disponibles: $fechas")
                    
                    // Buscar turnos con la fecha normalizada
                    var turnosDelDia = turnoRepository.getTurnosByFecha(fechaNormalizada)
                    
                    // Si no se encuentran turnos, intentar con otras variantes
                    if (turnosDelDia.isEmpty()) {
                        val variantes = generarVariantesFecha(fechaNormalizada)
                        Log.d("EdicionDiaViewModel", "Intentando con variantes: $variantes")
                        
                        for (variante in variantes) {
                            turnosDelDia = turnoRepository.getTurnosByFecha(variante)
                            if (turnosDelDia.isNotEmpty()) {
                                Log.d("EdicionDiaViewModel", "Encontrados turnos con variante: $variante")
                                break
                            }
                        }
                    }
                    
                    // Si aún no hay turnos, buscar por coincidencia parcial
                    if (turnosDelDia.isEmpty()) {
                        val partesFecha = fechaNormalizada.split("/")
                        if (partesFecha.size == 3) {
                            val dia = partesFecha[0]
                            val mes = partesFecha[1]
                            val anio = partesFecha[2]
                            
                            turnosDelDia = todosTurnos.filter { turno ->
                                turno.fecha.contains(dia) && 
                                turno.fecha.contains(mes) && 
                                turno.fecha.contains(anio)
                            }
                        }
                    }
                    
                    // Actualizar los turnos encontrados
                    _turnos.value = turnosDelDia
                    
                    // Cargar las carreras asociadas
                    val todasLasCarreras = mutableListOf<Carrera>()
                    turnosDelDia.forEach { turno ->
                        val carrerasDeTurno = carreraRepository.getCarrerasForTurno(turno.idTurno)
                        Log.d("EdicionDiaViewModel", "Carreras para turno ${turno.idTurno}: ${carrerasDeTurno.size}")
                        todasLasCarreras.addAll(carrerasDeTurno)
                    }
                    _carreras.value = todasLasCarreras
                }
            } catch (e: Exception) {
                Log.e("EdicionDiaViewModel", "Error al cargar datos: ${e.message}", e)
                _turnos.value = emptyList()
                _carreras.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun normalizarFecha(fecha: String): String {
        try {
            Log.d("EdicionDiaViewModel", "Normalizando fecha: $fecha")
            
            // Si la fecha ya está en formato dd/MM/yyyy
            if (fecha.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                return fecha
            }
            
            // Si la fecha está en formato yyyyMMdd
            if (fecha.matches(Regex("\\d{8}"))) {
                val year = fecha.substring(0, 4)
                val month = fecha.substring(4, 6)
                val day = fecha.substring(6, 8)
                val fechaNormalizada = "$day/$month/$year"
                Log.d("EdicionDiaViewModel", "Fecha normalizada desde yyyyMMdd: $fechaNormalizada")
                return fechaNormalizada
            }
            
            // Si la fecha tiene guiones, convertir a formato con barras
            if (fecha.contains("-")) {
                val partes = fecha.split("-")
                if (partes.size == 3) {
                    val fechaNormalizada = "${partes[0]}/${partes[1]}/${partes[2]}"
                    Log.d("EdicionDiaViewModel", "Fecha normalizada desde guiones: $fechaNormalizada")
                    return fechaNormalizada
                }
            }
            
            // Intentar parsear con SimpleDateFormat como último recurso
            val posiblesFormatos = listOf(
                "yyyyMMdd",
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd"
            )
            
            for (formato in posiblesFormatos) {
                try {
                    val parser = SimpleDateFormat(formato, Locale.getDefault())
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaDate = parser.parse(fecha)
                    if (fechaDate != null) {
                        val fechaNormalizada = formatter.format(fechaDate)
                        Log.d("EdicionDiaViewModel", "Fecha normalizada con formato $formato: $fechaNormalizada")
                        return fechaNormalizada
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            
            Log.w("EdicionDiaViewModel", "No se pudo normalizar la fecha, devolviendo original: $fecha")
            return fecha
            
        } catch (e: Exception) {
            Log.e("EdicionDiaViewModel", "Error normalizando fecha: ${e.message}")
            return fecha
        }
    }
    
    private fun generarVariantesFecha(fechaStr: String): List<String> {
        val variantes = mutableListOf<String>()
        
        // Añadir la fecha original
        variantes.add(fechaStr)
        
        try {
            // Si la fecha contiene '/', intentar varias formas con días/meses con/sin ceros
            if (fechaStr.contains("/")) {
                val partes = fechaStr.split("/")
                if (partes.size == 3) {
                    val dia = partes[0].toIntOrNull() ?: return variantes
                    val mes = partes[1].toIntOrNull() ?: return variantes
                    val anio = partes[2]
                    
                    // Variantes con formato dd/MM/yyyy y d/M/yyyy
                    variantes.add(String.format("%02d/%02d/%s", dia, mes, anio)) // 01/01/2025
                    variantes.add(String.format("%d/%d/%s", dia, mes, anio))      // 1/1/2025
                    variantes.add(String.format("%02d/%d/%s", dia, mes, anio))    // 01/1/2025
                    variantes.add(String.format("%d/%02d/%s", dia, mes, anio))    // 1/01/2025
                    
                    // También probar con guiones
                    variantes.add(String.format("%02d-%02d-%s", dia, mes, anio)) // 01-01-2025
                    variantes.add(String.format("%d-%d-%s", dia, mes, anio))      // 1-1-2025
                    variantes.add(String.format("%02d-%d-%s", dia, mes, anio))    // 01-1-2025
                    variantes.add(String.format("%d-%02d-%s", dia, mes, anio))    // 1-01-2025
                }
            } 
            // Si la fecha contiene '-', intentar varias formas
            else if (fechaStr.contains("-")) {
                val partes = fechaStr.split("-")
                if (partes.size == 3) {
                    // Si es formato yyyy-MM-dd
                    if (partes[0].length == 4) {
                        val anio = partes[0]
                        val mes = partes[1].toIntOrNull() ?: return variantes
                        val dia = partes[2].toIntOrNull() ?: return variantes
                        
                        // Variantes con formato dd/MM/yyyy
                        variantes.add(String.format("%02d/%02d/%s", dia, mes, anio)) // 01/01/2025
                        variantes.add(String.format("%d/%d/%s", dia, mes, anio))      // 1/1/2025
                        variantes.add(String.format("%02d/%d/%s", dia, mes, anio))    // 01/1/2025
                        variantes.add(String.format("%d/%02d/%s", dia, mes, anio))    // 1/01/2025
                    }
                    // Si es formato dd-MM-yyyy
                    else {
                        val dia = partes[0].toIntOrNull() ?: return variantes
                        val mes = partes[1].toIntOrNull() ?: return variantes
                        val anio = partes[2]
                        
                        // Variantes con formato dd/MM/yyyy
                        variantes.add(String.format("%02d/%02d/%s", dia, mes, anio)) // 01/01/2025
                        variantes.add(String.format("%d/%d/%s", dia, mes, anio))      // 1/1/2025
                        variantes.add(String.format("%02d/%d/%s", dia, mes, anio))    // 01/1/2025
                        variantes.add(String.format("%d/%02d/%s", dia, mes, anio))    // 1/01/2025
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("EdicionDiaViewModel", "Error al generar variantes: ${e.message}")
        }
        
        return variantes.distinct()
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
            // Si ya está en formato dd/MM/yyyy, añadirlo directamente
            if (fechaStr.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                variantes.add(fechaStr)
            }

            // Si está en formato yyyy-MM-dd, convertirlo
            if (fechaStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(fechaStr)
                date?.let {
                    variantes.add(outputFormat.format(it))
                }
            }

            Log.d("EdicionDiaViewModel", "Variantes de fecha generadas: $variantes")
        } catch (e: Exception) {
            Log.e("EdicionDiaViewModel", "Error al obtener variantes de fecha: ${e.message}", e)
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