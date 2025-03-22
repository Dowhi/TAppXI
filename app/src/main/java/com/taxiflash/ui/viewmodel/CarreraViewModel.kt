package com.taxiflash.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.FormaPago
import com.taxiflash.ui.data.TaxiFlashDatabase
import com.taxiflash.ui.utils.FechaUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Estados posibles para la operación de guardar una carrera
 */
sealed class SaveCarreraState {
    object Idle : SaveCarreraState()
    object Loading : SaveCarreraState()
    object Success : SaveCarreraState()
    data class Error(val message: String) : SaveCarreraState()
}

/**
 * ViewModel para la gestión de carreras (creación, edición, eliminación)
 * 
 * Este ViewModel maneja toda la lógica de negocio relacionada con las carreras,
 * incluyendo la validación de datos, cálculos automáticos y persistencia.
 */
@HiltViewModel
class CarreraViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    
    private val TAG = "CarreraViewModel"
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val carreraDao = database.carreraDao()

    // Estados de la UI
    private val _taximetro = MutableStateFlow("")
    val taximetro: StateFlow<String> = _taximetro.asStateFlow()

    private val _importeReal = MutableStateFlow("")
    val importeReal: StateFlow<String> = _importeReal.asStateFlow()

    private val _propina = MutableStateFlow("")
    val propina: StateFlow<String> = _propina.asStateFlow()

    private val _formaPago = MutableStateFlow(FormaPago.EFECTIVO)
    val formaPago: StateFlow<FormaPago> = _formaPago.asStateFlow()

    private val _emisora = MutableStateFlow(false)
    val emisora: StateFlow<Boolean> = _emisora.asStateFlow()

    private val _aeropuerto = MutableStateFlow(false)
    val aeropuerto: StateFlow<Boolean> = _aeropuerto.asStateFlow()
    
    private val _saveState = MutableStateFlow<SaveCarreraState>(SaveCarreraState.Idle)
    val saveState: StateFlow<SaveCarreraState> = _saveState.asStateFlow()

    // Datos de la carrera que se está editando
    private var carreraId: Long = -1L
    private var fechaOriginal: String = ""
    private var horaOriginal: String = ""

    /**
     * Actualiza el valor del taxímetro y recalcula la propina
     * 
     * @param value El nuevo valor del taxímetro
     */
    fun updateTaximetro(value: String) {
        _taximetro.value = value
        
        // Convertir el valor a Double para validar
        val taximetroDouble = value.toDoubleOrNull() ?: 0.0
        
        // Si el importe real está vacío o es cero, lo igualamos al taxímetro
        if (_importeReal.value.isEmpty() || _importeReal.value.toDoubleOrNull() == 0.0) {
            _importeReal.value = value
        }
        
        calcularPropina()
    }

    /**
     * Actualiza el importe real y recalcula la propina
     * 
     * @param value El nuevo valor del importe real
     */
    fun updateImporteReal(value: String) {
        _importeReal.value = value
        calcularPropina()
    }

    /**
     * Calcula la propina como la diferencia entre el importe real y el taxímetro
     */
    private fun calcularPropina() {
        val importeReal = _importeReal.value.toDoubleOrNull() ?: 0.0
        val taximetro = _taximetro.value.toDoubleOrNull() ?: 0.0
        if (importeReal > 0 && taximetro > 0) {
            val propina = importeReal - taximetro
            _propina.value = if (propina > 0) propina.toString() else "0.0"
        } else {
            _propina.value = "0.0"
        }
    }

    /**
     * Actualiza la forma de pago seleccionada
     * 
     * @param formaPago La nueva forma de pago
     */
    fun updateFormaPago(formaPago: FormaPago) {
        _formaPago.value = formaPago
    }

    /**
     * Actualiza si la carrera es de emisora
     * 
     * @param value Verdadero si es de emisora, falso en caso contrario
     */
    fun updateEmisora(value: Boolean) {
        _emisora.value = value
    }

    /**
     * Actualiza si la carrera es de aeropuerto
     * 
     * @param value Verdadero si es de aeropuerto, falso en caso contrario
     */
    fun updateAeropuerto(value: Boolean) {
        _aeropuerto.value = value
    }

    /**
     * Guarda la carrera en la base de datos (crea una nueva o actualiza una existente)
     * 
     * @param turnoActual El turno al que se asociará la carrera
     */
    fun guardarCarrera(turnoActual: String) {
        viewModelScope.launch {
            try {
                _saveState.value = SaveCarreraState.Loading
                
                // Validar datos
                val taximetroValue = _taximetro.value.toDoubleOrNull()
                if (taximetroValue == null || taximetroValue <= 0) {
                    _saveState.value = SaveCarreraState.Error("El taxímetro debe ser un número mayor que cero")
                    return@launch
                }
                
                val importeRealValue = _importeReal.value.toDoubleOrNull()
                if (importeRealValue == null || importeRealValue <= 0) {
                    _saveState.value = SaveCarreraState.Error("El importe debe ser un número mayor que cero")
                    return@launch
                }
                
                // Verificar si el turno está activo o cerrado
                val turnoInfo = database.turnoDao().getTurnoById(turnoActual)
                if (turnoInfo == null) {
                    _saveState.value = SaveCarreraState.Error("No se encontró el turno especificado")
                    return@launch
                }
                
                // Si es una edición, permitir guardar sin importar si el turno está activo
                // Si es una carrera nueva, solo permitir guardar si el turno está activo
                if (carreraId == -1L && !turnoInfo.activo) {
                    _saveState.value = SaveCarreraState.Error("No se pueden añadir carreras a un turno cerrado")
                    return@launch
                }
                
                // Si es una carrera nueva, obtener la fecha del turno para usarla
                var fechaCarrera = if (carreraId != -1L) fechaOriginal else FechaUtils.obtenerFechaActual()
                var horaCarrera = if (carreraId != -1L) horaOriginal else FechaUtils.obtenerHoraActual()
                
                // Buscar la información del turno para usar su fecha si es una carrera nueva
                if (carreraId == -1L) {
                    if (turnoInfo != null) {
                        Log.d(TAG, "Usando fecha del turno: ${turnoInfo.fecha}")
                        fechaCarrera = turnoInfo.fecha // Usar la fecha del turno
                    } else {
                        Log.d(TAG, "No se encontró información del turno, usando fecha actual")
                    }
                }
                
                // Crear objeto Carrera
                val carrera = Carrera(
                    id = if (carreraId != -1L) carreraId else 0L,
                    fecha = fechaCarrera,
                    hora = horaCarrera,
                    taximetro = taximetroValue,
                    importeReal = importeRealValue,
                    propina = _propina.value.toDoubleOrNull() ?: 0.0,
                    formaPago = _formaPago.value,
                    emisora = _emisora.value,
                    aeropuerto = _aeropuerto.value,
                    turno = turnoActual
                )

                // Guardar en la base de datos
                if (carreraId != -1L) {
                    Log.d(TAG, "Actualizando carrera con ID: $carreraId")
                    carreraDao.updateCarrera(carrera)
                } else {
                    Log.d(TAG, "Insertando nueva carrera con fecha: $fechaCarrera")
                    carreraDao.insertCarrera(carrera)
                }
                
                _saveState.value = SaveCarreraState.Success
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar carrera", e)
                _saveState.value = SaveCarreraState.Error("Error al guardar: ${e.message}")
            }
        }
    }

    /**
     * Carga los datos de una carrera existente para su edición
     * 
     * @param carreraId El ID de la carrera a cargar
     */
    fun cargarCarrera(carreraId: Long) {
        if (carreraId != -1L) {
            this.carreraId = carreraId
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Cargando carrera con ID: $carreraId")
                    carreraDao.getCarreraById(carreraId)?.let { carrera ->
                        fechaOriginal = carrera.fecha
                        horaOriginal = carrera.hora
                        _taximetro.value = carrera.taximetro.toString()
                        _importeReal.value = carrera.importeReal.toString()
                        _propina.value = carrera.propina.toString()
                        _formaPago.value = carrera.formaPago
                        _emisora.value = carrera.emisora
                        _aeropuerto.value = carrera.aeropuerto
                        Log.d(TAG, "Carrera cargada correctamente")
                    } ?: run {
                        Log.e(TAG, "No se encontró la carrera con ID: $carreraId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar carrera", e)
                }
            }
        }
    }

    /**
     * Elimina una carrera de la base de datos
     * 
     * @param carreraId El ID de la carrera a eliminar
     * @param onComplete Callback que se ejecutará cuando se complete la operación
     */
    fun eliminarCarrera(carreraId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Eliminando carrera con ID: $carreraId")
                carreraDao.deleteCarreraById(carreraId)
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar carrera", e)
            }
        }
    }
    
    /**
     * Reinicia el estado de guardado
     */
    fun resetSaveState() {
        _saveState.value = SaveCarreraState.Idle
    }

    /**
     * Obtiene la suma de importes de todas las carreras asociadas a un turno
     */
    suspend fun obtenerSumaImportesPorTurno(turnoId: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                val carrerasFlow = carreraDao.getCarrerasByTurno(turnoId)
                val carreras = carrerasFlow.first()
                var suma = 0.0
                for (carrera in carreras) {
                    suma += carrera.importeReal
                }
                Log.d("CarreraViewModel", "Suma de importes para turno $turnoId: $suma")
                suma
            } catch (e: Exception) {
                Log.e("CarreraViewModel", "Error al obtener suma de importes", e)
                0.0
            }
        }
    }
} 