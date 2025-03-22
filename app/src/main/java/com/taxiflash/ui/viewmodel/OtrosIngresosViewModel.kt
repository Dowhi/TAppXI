package com.taxiflash.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.OtrosIngresos
import com.taxiflash.ui.data.TaxiFlashDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * ViewModel para la pantalla de Otros Ingresos
 */
class OtrosIngresosViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val otrosIngresosDao = database.otrosIngresosDao()
    
    private val _concepto = MutableStateFlow("")
    val concepto: StateFlow<String> = _concepto
    
    private val _fecha = MutableStateFlow(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
    )
    val fecha: StateFlow<String> = _fecha
    
    private val _importe = MutableStateFlow("")
    val importe: StateFlow<String> = _importe
    
    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion
    
    private val _notas = MutableStateFlow("")
    val notas: StateFlow<String> = _notas
    
    private val _guardadoExitoso = MutableStateFlow(false)
    val guardadoExitoso: StateFlow<Boolean> = _guardadoExitoso
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val _otrosIngresos = MutableStateFlow<List<OtrosIngresos>>(emptyList())
    val otrosIngresos: StateFlow<List<OtrosIngresos>> = _otrosIngresos
    
    init {
        cargarOtrosIngresos()
    }
    
    private fun cargarOtrosIngresos() {
        viewModelScope.launch {
            try {
                otrosIngresosDao.getAllOtrosIngresos().collect { ingresos ->
                    _otrosIngresos.value = ingresos
                }
            } catch (e: Exception) {
                Log.e("OtrosIngresosViewModel", "Error al cargar otros ingresos", e)
                _errorMessage.value = "Error al cargar los ingresos: ${e.message}"
            }
        }
    }
    
    fun updateConcepto(value: String) {
        _concepto.value = value
        _errorMessage.value = null
    }
    
    fun updateFecha(value: String) {
        _fecha.value = value
        _errorMessage.value = null
    }
    
    fun updateImporte(value: String) {
        _importe.value = value
        _errorMessage.value = null
    }
    
    fun updateDescripcion(value: String) {
        _descripcion.value = value
    }
    
    fun updateNotas(value: String) {
        _notas.value = value
    }
    
    fun guardarOtrosIngresos() {
        viewModelScope.launch {
            try {
                if (concepto.value.isBlank()) {
                    _errorMessage.value = "El concepto no puede estar vacío"
                    return@launch
                }
                
                val importeDouble = _importe.value.toDoubleOrNull()
                if (importeDouble == null || importeDouble <= 0) {
                    _errorMessage.value = "El importe debe ser un número mayor que cero"
                    return@launch
                }
                
                val nuevoIngreso = OtrosIngresos(
                    concepto = _concepto.value,
                    fecha = _fecha.value,
                    importe = importeDouble,
                    descripcion = if (_descripcion.value.isBlank()) null else _descripcion.value,
                    notas = if (_notas.value.isBlank()) null else _notas.value
                )
                
                withContext(Dispatchers.IO) {
                    otrosIngresosDao.insertOtrosIngresos(nuevoIngreso)
                }
                
                // Resetear campos
                _concepto.value = ""
                _fecha.value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
                _importe.value = ""
                _descripcion.value = ""
                _notas.value = ""
                
                _guardadoExitoso.value = true
                Log.d("OtrosIngresosViewModel", "Ingreso guardado exitosamente")
            } catch (e: Exception) {
                Log.e("OtrosIngresosViewModel", "Error al guardar ingreso", e)
                _errorMessage.value = "Error al guardar el ingreso: ${e.message}"
            }
        }
    }
    
    fun resetGuardadoExitoso() {
        _guardadoExitoso.value = false
    }
    
    fun resetErrorMessage() {
        _errorMessage.value = null
    }
    
    fun obtenerIngresoById(id: Long) {
        viewModelScope.launch {
            try {
                val ingreso = withContext(Dispatchers.IO) {
                    otrosIngresosDao.getOtrosIngresosById(id)
                }
                
                if (ingreso != null) {
                    _concepto.value = ingreso.concepto
                    _fecha.value = ingreso.fecha
                    _importe.value = ingreso.importe.toString()
                    _descripcion.value = ingreso.descripcion ?: ""
                    _notas.value = ingreso.notas ?: ""
                } else {
                    _errorMessage.value = "No se encontró el ingreso"
                }
            } catch (e: Exception) {
                Log.e("OtrosIngresosViewModel", "Error al obtener ingreso", e)
                _errorMessage.value = "Error al obtener el ingreso: ${e.message}"
            }
        }
    }
    
    fun eliminarIngreso(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    otrosIngresosDao.deleteOtrosIngresosById(id)
                }
                Log.d("OtrosIngresosViewModel", "Ingreso eliminado exitosamente")
            } catch (e: Exception) {
                Log.e("OtrosIngresosViewModel", "Error al eliminar ingreso", e)
                _errorMessage.value = "Error al eliminar el ingreso: ${e.message}"
            }
        }
    }
} 