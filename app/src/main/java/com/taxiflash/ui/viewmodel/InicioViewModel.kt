package com.taxiflash.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.CarreraRepository
import com.taxiflash.ui.data.GastoRepository
import com.taxiflash.ui.data.Turno
import com.taxiflash.ui.data.TurnoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Modelo de datos para el resumen mensual
 */
data class ResumenMensualInicio(
    val ingresos: Double = 0.0,
    val gastos: Double = 0.0
)

/**
 * ViewModel para la pantalla de inicio
 */
@HiltViewModel
class InicioViewModel @Inject constructor(
    private val carreraRepository: CarreraRepository,
    private val gastoRepository: GastoRepository,
    private val turnoRepository: TurnoRepository
) : ViewModel() {
    
    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Resumen mensual
    private val _resumenMensual = MutableStateFlow<ResumenMensualInicio?>(null)
    val resumenMensual: StateFlow<ResumenMensualInicio?> = _resumenMensual.asStateFlow()
    
    // Turno activo
    private val _turnoActivo = MutableStateFlow<String?>(null)
    val turnoActivo: StateFlow<String?> = _turnoActivo.asStateFlow()
    
    // Turno activo completo
    private val _turnoActivoCompleto = MutableStateFlow<Turno?>(null)
    val turnoActivoCompleto: StateFlow<Turno?> = _turnoActivoCompleto.asStateFlow()
    
    init {
        cargarDatos()
    }
    
    /**
     * Carga todos los datos necesarios para la pantalla de inicio
     */
    private fun cargarDatos() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Cargar resumen mensual
                cargarResumenMensual()
                
                // Cargar turno activo
                cargarTurnoActivo()
            } catch (e: Exception) {
                // Manejar errores
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carga el resumen mensual de ingresos y gastos
     */
    private suspend fun cargarResumenMensual() {
        try {
            // Obtener el mes y año actual
            val calendar = Calendar.getInstance()
            val mesActual = calendar.get(Calendar.MONTH) + 1
            val añoActual = calendar.get(Calendar.YEAR)
            
            // Calcular ingresos del mes - Forzar actualización desde la base de datos
            val ingresos = carreraRepository.obtenerIngresosMensuales(mesActual, añoActual)
            
            // Calcular gastos del mes - Forzar actualización desde la base de datos
            val gastos = gastoRepository.obtenerGastosMensuales(mesActual, añoActual)
            
            // Log para depuración
            println("TaxiFlash - Ingresos: $ingresos, Gastos: $gastos, Mes: $mesActual, Año: $añoActual")
            
            // Actualizar el estado con valores reales
            _resumenMensual.value = ResumenMensualInicio(
                ingresos = ingresos,
                gastos = gastos
            )
        } catch (e: Exception) {
            e.printStackTrace()
            println("TaxiFlash - Error al cargar resumen mensual: ${e.message}")
            // En caso de error, establecer valores por defecto
            _resumenMensual.value = ResumenMensualInicio(0.0, 0.0)
        }
    }
    
    /**
     * Carga el turno activo si existe
     */
    private suspend fun cargarTurnoActivo() {
        val turnoActivo = turnoRepository.getTurnoActivo()
        _turnoActivoCompleto.value = turnoActivo
        _turnoActivo.value = turnoActivo?.idTurno
    }
    
    /**
     * Recarga los datos
     */
    fun recargarDatos() {
        viewModelScope.launch {
            cargarDatos()
        }
    }
}
