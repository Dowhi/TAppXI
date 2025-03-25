package com.taxiflash.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.TaxiFlashDatabase
import com.taxiflash.ui.data.Turno
import com.taxiflash.ui.utils.FechaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.withContext

class TurnoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val turnoDao = database.turnoDao()

    private val _kmInicio = MutableStateFlow("")
    val kmInicio: StateFlow<String> = _kmInicio

    private val _horaInicio = MutableStateFlow(
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    )
    val horaInicio: StateFlow<String> = _horaInicio

    private val _numeroTurno = MutableStateFlow(1)
    val numeroTurno: StateFlow<Int> = _numeroTurno

    private val _turnoGuardado = MutableStateFlow<String?>(null)
    val turnoGuardado: StateFlow<String?> = _turnoGuardado

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _puedeCrearTurno = MutableStateFlow(true)
    val puedeCrearTurno: StateFlow<Boolean> = _puedeCrearTurno

    private val _turnoActivoId = MutableStateFlow<String?>(null)
    val turnoActivoId: StateFlow<String?> = _turnoActivoId

    private val _kmInicioError = MutableStateFlow(false)
    val kmInicioError: StateFlow<Boolean> = _kmInicioError

    private val _turnoActivoInfo = MutableStateFlow<Turno?>(null)
    val turnoActivoInfo: StateFlow<Turno?> = _turnoActivoInfo

    private val _fechaSeleccionada = MutableStateFlow(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    )
    val fechaSeleccionada: StateFlow<String> = _fechaSeleccionada

    init {
        viewModelScope.launch {
            // Verificar si hay un turno activo
            val turnoActivo = withContext(Dispatchers.IO) {
                turnoDao.getTurnoActivo()
            }
            
            if (turnoActivo != null) {
                _turnoActivoId.value = turnoActivo.idTurno
                _turnoActivoInfo.value = turnoActivo
                _puedeCrearTurno.value = false
                Log.d("TurnoViewModel", "Turno activo encontrado: ${turnoActivo.idTurno}")
            } else {
                _puedeCrearTurno.value = true
                // Obtener el siguiente número de turno solo si no hay turno activo
                _numeroTurno.value = obtenerSiguienteNumeroTurno()
                Log.d("TurnoViewModel", "No hay turno activo, puede crear uno nuevo")
            }
        }
    }

    fun updateKmInicio(km: String) {
        _kmInicio.value = km
        _kmInicioError.value = km.isBlank() || km.toIntOrNull() == null
        _errorMessage.value = null // Limpiar mensaje de error previo
    }

    fun updateFechaSeleccionada(fecha: String) {
        _fechaSeleccionada.value = fecha
        viewModelScope.launch {
            _numeroTurno.value = obtenerSiguienteNumeroTurnoPorFecha(fecha)
        }
    }

    private suspend fun obtenerSiguienteNumeroTurno(): Int {
        return withContext(Dispatchers.IO) {
            try {
                // Obtener fecha actual en formato dd/MM/yyyy
                val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                
                Log.d("TurnoViewModel", "Obteniendo siguiente número de turno para fecha: $fechaHoy")
                
                // Consultar directamente el último número de turno para hoy
                val ultimoNumeroTurno = turnoDao.getUltimoNumeroTurnoDia(fechaHoy)
                
                // Si hay un último número, devolver el siguiente, si no, devolver 1
                val siguienteNumero = if (ultimoNumeroTurno != null && ultimoNumeroTurno > 0) {
                    ultimoNumeroTurno + 1
                } else {
                    1
                }
                
                Log.d("TurnoViewModel", "Último número de turno para $fechaHoy: $ultimoNumeroTurno, siguiente: $siguienteNumero")
                
                return@withContext siguienteNumero
            } catch (e: Exception) {
                Log.e("TurnoViewModel", "Error al obtener siguiente número de turno", e)
                e.printStackTrace()
                return@withContext 1
            }
        }
    }

    private suspend fun obtenerSiguienteNumeroTurnoPorFecha(fechaStr: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("TurnoViewModel", "Obteniendo siguiente número de turno para fecha específica: $fechaStr")
                
                // Verificar formato de fecha
                if (!fechaStr.matches(Regex("^\\d{2}/\\d{2}/\\d{4}$"))) {
                    Log.e("TurnoViewModel", "Formato de fecha incorrecto: $fechaStr. Se esperaba dd/MM/yyyy")
                    return@withContext 1
                }
                
                // Usar directamente el formato dd/MM/yyyy para la consulta
                // Consultar directamente el último número de turno para la fecha seleccionada
                val ultimoNumeroTurno = turnoDao.getUltimoNumeroTurnoDia(fechaStr)
                
                // Si hay un último número, devolver el siguiente, si no, devolver 1
                val siguienteNumero = if (ultimoNumeroTurno != null && ultimoNumeroTurno > 0) {
                    ultimoNumeroTurno + 1
                } else {
                    1
                }
                
                Log.d("TurnoViewModel", "Último número de turno para $fechaStr: $ultimoNumeroTurno, siguiente: $siguienteNumero")
                
                return@withContext siguienteNumero
            } catch (e: Exception) {
                Log.e("TurnoViewModel", "Error al obtener siguiente número de turno por fecha", e)
                e.printStackTrace()
                return@withContext 1
            }
        }
    }

    fun guardarTurno() {
        viewModelScope.launch {
            try {
                Log.d("TurnoViewModel", "Iniciando guardarTurno - kmInicio: ${kmInicio.value}")
                
                // Validar el campo de KM
                if (kmInicio.value.isBlank() || kmInicio.value.toIntOrNull() == null) {
                    _errorMessage.value = "Por favor, introduce un valor válido para los kilómetros iniciales"
                    _kmInicioError.value = true
                    Log.e("TurnoViewModel", "Error: kilómetros iniciales inválidos - '${kmInicio.value}'")
                    return@launch
                }
                
                // Verificar que no haya otro turno activo
                val turnoActivo = withContext(Dispatchers.IO) { 
                    turnoDao.getTurnoActivo() 
                }
                if (turnoActivo != null) {
                    _errorMessage.value = "Ya hay un turno activo. Cérralo antes de iniciar uno nuevo."
                    _turnoActivoId.value = turnoActivo.idTurno
                    _turnoActivoInfo.value = turnoActivo
                    _puedeCrearTurno.value = false
                    Log.e("TurnoViewModel", "Error: Ya existe un turno activo con ID: ${turnoActivo.idTurno}")
                    return@launch
                }
                
                // Generar fecha actual en formato dd/MM/yyyy
                val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaActual = formatoFecha.format(Date())
                
                // Generar ID único para el turno: yyyyMMdd-numeroTurno
                val formatoFechaId = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val fechaId = formatoFechaId.format(Date())
                val turnoId = "$fechaId-${numeroTurno.value}"
                
                // Crear objeto turno
                val turno = Turno(
                    fecha = fechaActual,
                    horaInicio = horaInicio.value,
                    horaFin = "",
                    kmInicio = kmInicio.value.toInt(),
                    kmFin = 0,
                    numeroTurno = numeroTurno.value,
                    idTurno = turnoId,
                    activo = true
                )
                
                Log.d("TurnoViewModel", "Guardando turno: fecha=$fechaActual, idTurno=$turnoId, numeroTurno=${numeroTurno.value}")
                
                // Guardar en la base de datos
                try {
                    val idInsertado = withContext(Dispatchers.IO) {
                        turnoDao.insertTurno(turno)
                    }
                    
                    Log.d("TurnoViewModel", "Turno guardado en base de datos con ID: $idInsertado")
                    
                    // Verificar que el turno se haya guardado correctamente
                    val turnoGuardado = withContext(Dispatchers.IO) {
                        turnoDao.getTurnoById(turnoId)
                    }
                    
                    if (turnoGuardado == null) {
                        throw Exception("El turno no se guardó correctamente en la base de datos")
                    }
                    
                    Log.d("TurnoViewModel", "Verificación de turno guardado exitosa: ${turnoGuardado.idTurno}")
                    
                    // Actualizar estados
                    _turnoGuardado.value = turnoId
                    _turnoActivoId.value = turnoId
                    _turnoActivoInfo.value = turno
                    _puedeCrearTurno.value = false
                    _errorMessage.value = null
                } catch (e: Exception) {
                    Log.e("TurnoViewModel", "Error al guardar turno en base de datos", e)
                    _errorMessage.value = "Error al guardar el turno en la base de datos: ${e.message}"
                    return@launch
                }
                
                Log.d("TurnoViewModel", "Turno guardado exitosamente: $turnoId")
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar el turno: ${e.message}"
                Log.e("TurnoViewModel", "Error en el proceso de guardar turno", e)
                e.printStackTrace()
            }
        }
    }

    fun verificarTurnoActivo() {
        viewModelScope.launch {
            try {
                Log.d("TurnoViewModel", "Iniciando verificación de turno activo...")
                
                val turnoActivo = withContext(Dispatchers.IO) {
                    // Primero verificamos la cantidad de turnos activos que hay
                    val cantidadTurnosActivos = turnoDao.hayTurnoActivo()
                    Log.d("TurnoViewModel", "Cantidad de turnos activos: $cantidadTurnosActivos")
                    
                    // Si hay más de un turno activo, esto es un error de consistencia
                    if (cantidadTurnosActivos > 1) {
                        Log.w("TurnoViewModel", "¡Atención! Hay $cantidadTurnosActivos turnos activos, cuando debería haber máximo 1")
                    }
                    
                    // Obtener el turno activo
                    turnoDao.getTurnoActivo()
                }
                
                if (turnoActivo != null) {
                    Log.d("TurnoViewModel", "Turno activo encontrado: ID=${turnoActivo.idTurno}, Fecha=${turnoActivo.fecha}, KmInicio=${turnoActivo.kmInicio}")
                    _turnoActivoId.value = turnoActivo.idTurno
                    _turnoActivoInfo.value = turnoActivo
                    _puedeCrearTurno.value = false
                } else {
                    Log.d("TurnoViewModel", "No hay turno activo")
                    _turnoActivoId.value = null
                    _turnoActivoInfo.value = null
                    _puedeCrearTurno.value = true
                    
                    // Actualizar el número de turno para el próximo turno
                    _numeroTurno.value = obtenerSiguienteNumeroTurno()
                    Log.d("TurnoViewModel", "Número para próximo turno: ${_numeroTurno.value}")
                }
            } catch (e: Exception) {
                Log.e("TurnoViewModel", "Error al verificar turno activo", e)
                e.printStackTrace()
                
                // En caso de error, asumimos que no hay turno activo para evitar bloquear al usuario
                _turnoActivoId.value = null
                _turnoActivoInfo.value = null
                _puedeCrearTurno.value = true
                _errorMessage.value = "Error al verificar el turno activo: ${e.message}"
            }
        }
    }

    fun cerrarTurno(turnoId: String, kmFin: Int) {
        viewModelScope.launch {
            try {
                // Obtener la hora actual
                val horaFin = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                
                // Actualizar el turno
                withContext(Dispatchers.IO) {
                    val turno = turnoDao.getTurnoById(turnoId)
                    if (turno != null) {
                        val turnoActualizado = turno.copy(
                            horaFin = horaFin,
                            kmFin = kmFin,
                            activo = false
                        )
                        turnoDao.updateTurno(turnoActualizado)
                        Log.d("TurnoViewModel", "Turno cerrado exitosamente: $turnoId")
                    } else {
                        throw Exception("No se encontró el turno con ID: $turnoId")
                    }
                }
                
                // Actualizar estados
                _turnoActivoId.value = null
                _turnoActivoInfo.value = null
                _puedeCrearTurno.value = true
                
                // Obtener el siguiente número de turno para el próximo turno
                _numeroTurno.value = obtenerSiguienteNumeroTurno()
                
            } catch (e: Exception) {
                Log.e("TurnoViewModel", "Error al cerrar turno", e)
            }
        }
    }

    fun resetTurnoGuardado() {
        _turnoGuardado.value = null
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun guardarTurnoConFecha(kmInicioStr: String, fechaFormateada: String) {
        viewModelScope.launch {
            try {
                // Actualizar el valor de kmInicio para que los validadores funcionen
                _kmInicio.value = kmInicioStr
                
                Log.d("TurnoViewModel", "Iniciando guardarTurnoConFecha - kmInicio: $kmInicioStr, fecha: $fechaFormateada")
                
                if (kmInicioStr.isBlank() || kmInicioStr.toIntOrNull() == null) {
                    _errorMessage.value = "Por favor, introduce un valor válido para los kilómetros iniciales"
                    _kmInicioError.value = true
                    Log.e("TurnoViewModel", "Error: kilómetros iniciales inválidos - '$kmInicioStr'")
                    return@launch
                }

                // Verificar que no haya otro turno activo
                val turnoActivo = turnoDao.getTurnoActivo()
                if (turnoActivo != null) {
                    _errorMessage.value = "Ya hay un turno activo. Cérralo antes de iniciar uno nuevo."
                    Log.e("TurnoViewModel", "Error: Ya existe un turno activo con ID: ${turnoActivo.idTurno}")
                    return@launch
                }

                // Convertir la fecha formateada (ddMMyyyy) al formato de visualización (dd/MM/yyyy)
                val fechaDate = SimpleDateFormat("ddMMyyyy", Locale.getDefault()).parse(fechaFormateada) ?: Date()
                val fechaFormateadaConBarras = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaDate)
                
                // Generar ID único para el turno: yyyyMMdd-numeroTurno
                val fechaId = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(fechaDate)
                val turnoId = "$fechaId-${numeroTurno.value}"
                
                val km = kmInicioStr.toIntOrNull() ?: 0
                val hora = horaInicio.value

                Log.d("TurnoViewModel", "Guardando turno con fecha personalizada: $fechaFormateadaConBarras, formato ID: $fechaId, número: ${numeroTurno.value}, km: $km")

                // Crear objeto turno
                val turno = Turno(
                    fecha = fechaFormateadaConBarras,
                    horaInicio = hora,
                    horaFin = "",
                    kmInicio = km,
                    kmFin = 0,
                    numeroTurno = numeroTurno.value,
                    idTurno = turnoId,
                    activo = true
                )

                // Guardar en base de datos
                try {
                    val idInsertado = withContext(Dispatchers.IO) {
                        turnoDao.insertTurno(turno)
                    }
                    
                    Log.d("TurnoViewModel", "Turno guardado en base de datos con ID: $idInsertado")
                    
                    // Verificar que el turno se haya guardado correctamente
                    val turnoGuardado = withContext(Dispatchers.IO) {
                        turnoDao.getTurnoById(turnoId)
                    }
                    
                    if (turnoGuardado == null) {
                        throw Exception("El turno no se guardó correctamente en la base de datos")
                    }
                    
                    Log.d("TurnoViewModel", "Verificación de turno guardado exitosa: ${turnoGuardado.idTurno}")
                } catch (e: Exception) {
                    Log.e("TurnoViewModel", "Error al guardar turno en base de datos", e)
                    _errorMessage.value = "Error al guardar el turno en la base de datos: ${e.message}"
                    return@launch
                }
                
                // Actualizar información del turno activo
                _turnoActivoId.value = turnoId
                _turnoActivoInfo.value = turno
                _puedeCrearTurno.value = false

                // Notificar éxito
                _turnoGuardado.value = turnoId
                
                Log.d("TurnoViewModel", "Turno con fecha personalizada guardado exitosamente: $turnoId")
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar el turno: ${e.message}"
                Log.e("TurnoViewModel", "Error en el proceso de guardar turno con fecha personalizada", e)
                e.printStackTrace()
            }
        }
    }
} 