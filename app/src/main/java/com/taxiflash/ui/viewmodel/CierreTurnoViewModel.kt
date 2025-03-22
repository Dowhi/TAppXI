package com.taxiflash.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.TaxiFlashDatabase
import com.taxiflash.ui.data.Turno
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CierreTurnoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val turnoDao = database.turnoDao()

    private val _kmFin = MutableStateFlow("")
    val kmFin: StateFlow<String> = _kmFin
    
    private val _horaFin = MutableStateFlow(
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    )
    val horaFin: StateFlow<String> = _horaFin
    
    private val _kmFinError = MutableStateFlow(false)
    val kmFinError: StateFlow<Boolean> = _kmFinError
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val _turnoInfo = MutableStateFlow<Turno?>(null)
    val turnoInfo: StateFlow<Turno?> = _turnoInfo
    
    private val _cierreExitoso = MutableStateFlow(false)
    val cierreExitoso: StateFlow<Boolean> = _cierreExitoso
    
    fun cargarTurno(turnoId: String) {
        viewModelScope.launch {
            try {
                val turno = withContext(Dispatchers.IO) {
                    turnoDao.getTurnoById(turnoId)
                }
                _turnoInfo.value = turno
                
                // Inicializar km fin con los km inicio como sugerencia
                turno?.let {
                    _kmFin.value = it.kmInicio.toString()
                }
            } catch (e: Exception) {
                Log.e("CierreTurnoViewModel", "Error al cargar turno", e)
                _errorMessage.value = "Error al cargar información del turno: ${e.message}"
            }
        }
    }
    
    fun updateKmFin(km: String) {
        _kmFin.value = km
        validarKmFin()
    }
    
    private fun validarKmFin() {
        val kmFinValue = _kmFin.value.toIntOrNull()
        val kmInicio = _turnoInfo.value?.kmInicio ?: 0
        
        _kmFinError.value = when {
            _kmFin.value.isBlank() -> true
            kmFinValue == null -> true
            kmFinValue < kmInicio -> true
            else -> false
        }
        
        // Limpiar mensaje de error previo si se corrige el valor
        if (!_kmFinError.value) {
            _errorMessage.value = null
        }
    }

    fun cerrarTurno(turnoId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // Validar km fin
                validarKmFin()
                if (_kmFinError.value) {
                    _errorMessage.value = "Los kilómetros finales deben ser mayores que los iniciales"
                    return@launch
                }
                
                // Obtener valores
                val kmFin = _kmFin.value.toIntOrNull() ?: 0
                val horaFin = _horaFin.value
                
                // Actualizar turno en la base de datos
                withContext(Dispatchers.IO) {
                    // Llamar a la función actualizada que incluye kmFin
                    turnoDao.actualizarCierreTurno(turnoId, horaFin, kmFin)
                    
                    // Ya no es necesario llamar a desactivarTurno ya que actualizarCierreTurno ya lo hace
                    
                    Log.d("CierreTurnoViewModel", "Turno cerrado exitosamente: $turnoId, kmFin: $kmFin")
                }
                
                _cierreExitoso.value = true
                onComplete()
            } catch (e: Exception) {
                Log.e("CierreTurnoViewModel", "Error al cerrar turno", e)
                _errorMessage.value = "Error al cerrar turno: ${e.message}"
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
} 