package com.taxiflash.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.OtrosIngresos
import com.taxiflash.ui.data.OtrosIngresosDao
import com.taxiflash.ui.data.TaxiFlashDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * ViewModel para la pantalla de Otros Ingresos
 */
class OtrosIngresosViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val otrosIngresosDao: OtrosIngresosDao = database.otrosIngresosDao()
    
    // Estados para el formulario
    private val _concepto = MutableStateFlow("")
    val concepto: StateFlow<String> = _concepto.asStateFlow()
    
    private val _fecha = MutableStateFlow(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    )
    val fecha: StateFlow<String> = _fecha.asStateFlow()
    
    private val _importe = MutableStateFlow("")
    val importe: StateFlow<String> = _importe.asStateFlow()
    
    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion.asStateFlow()
    
    private val _notas = MutableStateFlow("")
    val notas: StateFlow<String> = _notas.asStateFlow()
    
    private val _guardadoExitoso = MutableStateFlow(false)
    val guardadoExitoso: StateFlow<Boolean> = _guardadoExitoso.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Estado para la pestaña seleccionada (0=Formulario, 1=Resumen)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    
    // Estados para el resumen
    private val _ingresoSeleccionadoId = MutableStateFlow<Long?>(null)
    val ingresoSeleccionadoId: StateFlow<Long?> = _ingresoSeleccionadoId.asStateFlow()
    
    private val _mesSeleccionado = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val mesSeleccionado: StateFlow<Int> = _mesSeleccionado.asStateFlow()
    
    private val _anioSeleccionado = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val anioSeleccionado: StateFlow<Int> = _anioSeleccionado.asStateFlow()
    
    private val _listaIngresos = MutableStateFlow<List<OtrosIngresos>>(emptyList())
    val listaIngresos: StateFlow<List<OtrosIngresos>> = _listaIngresos.asStateFlow()
    
    private val _totalIngresosMes = MutableStateFlow(0.0)
    val totalIngresosMes: StateFlow<Double> = _totalIngresosMes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()
    
    init {
        cargarIngresosDelMes()
    }
    
    fun cargarIngresosDelMes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val nombreMes = getNombreMes(_mesSeleccionado.value)
                val anioStr = _anioSeleccionado.value.toString()
                
                otrosIngresosDao.getOtrosIngresosByMes(nombreMes, anioStr).collectLatest { ingresos ->
                    _listaIngresos.value = ingresos
                    calcularTotalMes(ingresos)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar ingresos: ${e.message}"
                _isLoading.value = false
                Log.e("OtrosIngresosViewModel", "Error al cargar ingresos", e)
            }
        }
    }
    
    private fun getNombreMes(mes: Int): String {
        val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                           "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        return meses[mes - 1]
    }
    
    private fun calcularTotalMes(ingresos: List<OtrosIngresos>) {
        _totalIngresosMes.value = ingresos.sumOf { it.importe }
    }
    
    // Métodos para actualizar los campos del formulario
    fun updateConcepto(newConcepto: String) {
        _concepto.value = newConcepto
    }
    
    fun updateFecha(newFecha: String) {
        _fecha.value = newFecha
    }
    
    fun updateImporte(newImporte: String) {
        _importe.value = newImporte
    }
    
    fun updateDescripcion(newDescripcion: String) {
        _descripcion.value = newDescripcion
    }
    
    fun updateNotas(newNotas: String) {
        _notas.value = newNotas
    }
    
    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }
    
    fun setMesSeleccionado(mes: Int) {
        _mesSeleccionado.value = mes
        cargarIngresosDelMes()
    }
    
    fun setAnioSeleccionado(anio: Int) {
        _anioSeleccionado.value = anio
        cargarIngresosDelMes()
    }
    
    fun setIngresoSeleccionado(id: Long?) {
        _ingresoSeleccionadoId.value = id
        if (id != null) {
            cargarIngreso(id)
        }
    }
    
    fun toggleDeleteDialog(show: Boolean) {
        _showDeleteDialog.value = show
    }
    
    private fun cargarIngreso(id: Long) {
        viewModelScope.launch {
            try {
                val ingreso = otrosIngresosDao.getOtrosIngresosById(id)
                ingreso?.let {
                    _concepto.value = it.concepto
                    _fecha.value = it.fecha
                    _importe.value = it.importe.toString()
                    _descripcion.value = it.descripcion ?: ""
                    _notas.value = it.notas ?: ""
                    _selectedTab.value = 0  // Cambiar a la pestaña de formulario
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar ingreso: ${e.message}"
                Log.e("OtrosIngresosViewModel", "Error al cargar ingreso", e)
            }
        }
    }
    
    fun guardarIngreso() {
        viewModelScope.launch {
            try {
                val importeDouble = _importe.value.toDoubleOrNull() ?: 0.0
                
                if (_concepto.value.isBlank()) {
                    _errorMessage.value = "El concepto no puede estar vacío"
                    return@launch
                }
                
                if (importeDouble <= 0) {
                    _errorMessage.value = "El importe debe ser mayor que cero"
                    return@launch
                }
                
                val otrosIngresos = OtrosIngresos(
                    id = _ingresoSeleccionadoId.value ?: 0,
                    concepto = _concepto.value,
                    importe = importeDouble,
                    fecha = _fecha.value,
                    descripcion = if (_descripcion.value.isBlank()) null else _descripcion.value,
                    notas = if (_notas.value.isBlank()) null else _notas.value
                )
                
                if (_ingresoSeleccionadoId.value == null) {
                    otrosIngresosDao.insertOtrosIngresos(otrosIngresos)
                } else {
                    otrosIngresosDao.updateOtrosIngresos(otrosIngresos)
                }
                
                // Resetear los campos
                limpiarCampos()
                _guardadoExitoso.value = true
                _selectedTab.value = 1  // Cambiar a la pestaña de resumen después de guardar
                cargarIngresosDelMes()  // Recargar la lista de ingresos
                
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar: ${e.message}"
                Log.e("OtrosIngresosViewModel", "Error al guardar ingreso", e)
            }
        }
    }
    
    fun eliminarIngreso() {
        viewModelScope.launch {
            try {
                _ingresoSeleccionadoId.value?.let { id ->
                    otrosIngresosDao.deleteOtrosIngresosById(id)
                    limpiarCampos()
                    _showDeleteDialog.value = false
                    cargarIngresosDelMes()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar: ${e.message}"
                Log.e("OtrosIngresosViewModel", "Error al eliminar ingreso", e)
            }
        }
    }
    
    private fun limpiarCampos() {
        _concepto.value = ""
        _fecha.value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        _importe.value = ""
        _descripcion.value = ""
        _notas.value = ""
        _ingresoSeleccionadoId.value = null
    }
    
    fun resetGuardadoExitoso() {
        _guardadoExitoso.value = false
    }
    
    fun resetErrorMessage() {
        _errorMessage.value = null
    }
} 