package com.taxiflash.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.TaxiFlashDatabase
import com.taxiflash.ui.data.FormaPago
import com.taxiflash.ui.data.AjustesDataStore
import com.taxiflash.ui.data.AjustesDataStore.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers

data class ResumenDia(
    val totalImporte: Double = 0.0,
    val totalCarreras: Int = 0,
    val sumaTarjetas: Double = 0.0,
    val carrerasTarjeta: Int = 0,
    val faltaPara100: Double = 100.0,
    val horaInicio: String = "",
    val carrerasEmisora: Int = 0,
    val kmInicio: Int = 0,
    val totalPropinas: Double = 0.0,
    val sumaEmisora: Double = 0.0
)

class VistaCarrerasViewModel(
    application: Application,
    private val turnoId: String
) : AndroidViewModel(application) {
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val carreraDao = database.carreraDao()
    private val turnoDao = database.turnoDao()
    private val context = application.applicationContext

    private val _carreras = MutableStateFlow<List<Carrera>>(emptyList())
    val carreras: StateFlow<List<Carrera>> = _carreras

    private val _resumenDia = MutableStateFlow(ResumenDia())
    val resumenDia: StateFlow<ResumenDia> = _resumenDia
    
    private val _turnoActivo = MutableStateFlow(false)
    val turnoActivo: StateFlow<Boolean> = _turnoActivo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        cargarDatos()
        verificarTurnoActivo()
    }
    
    private fun verificarTurnoActivo() {
        viewModelScope.launch {
            try {
                val turno = turnoDao.getTurnoById(turnoId)
                _turnoActivo.value = turno?.activo ?: false
            } catch (e: Exception) {
                // Manejar el error
            }
        }
    }
    
    private fun cargarDatos() {
        viewModelScope.launch {
            try {
            // Obtener el turno actual
            val turnoActual = turnoDao.getTurnoById(turnoId)
            val horaInicio = turnoActual?.horaInicio ?: ""
                val kmInicio = turnoActual?.kmInicio ?: 0
                
                // Determinar la fecha para buscar carreras
                val fecha = if (turnoActual != null) {
                    turnoActual.fecha  // Formato dd/MM/yyyy
                } else if (turnoId.startsWith("Turno ")) {
                    // Si es formato "Turno X", usar la fecha actual
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                } else if (turnoId.contains("-")) {
                    // Si es formato "yyyyMMdd-X", extraer la fecha y convertirla
                    val fechaParte = turnoId.split("-")[0]
                    try {
                        val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(fechaParte)
                        if (date != null) {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                        } else {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        }
                    } catch (e: Exception) {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    }
                } else {
                    // Formato desconocido, usar fecha actual
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                }
                
                // Obtener carreras para este turno específico
                val carrerasFlow = if (turnoId.startsWith("Turno ")) {
                    // Si el ID es "Turno X", necesitamos hacer una búsqueda especial
                    val numeroTurno = turnoId.substringAfter("Turno ").trim()
                    carreraDao.getCarrerasByTurnoPattern("Turno $numeroTurno")
                } else {
                    // ID normal, búsqueda directa
                    carreraDao.getCarrerasByTurno(turnoId)
                }

                // Obtener el valor del objetivo de las preferencias
                val objetivoImporte = context.dataStore.data.first()[AjustesDataStore.OBJETIVO_IMPORTE_KEY] ?: 100.0

            // Crear un Flow combinado que reaccione a cualquier cambio en las carreras
            combine(
                    carreraDao.getCarrerasByFecha(fecha),
                    carrerasFlow
            ) { carrerasDia, carrerasTurno ->
                // Actualizar la lista de carreras del turno
                _carreras.value = carrerasTurno

                // Calcular el resumen con las carreras del día
                val totalImporteDia = carrerasDia.sumOf { it.importeReal }
                val faltaPara100 = maxOf(0.0, objetivoImporte - totalImporteDia)

                ResumenDia(
                    totalImporte = totalImporteDia,
                    totalCarreras = carrerasDia.size,
                    sumaTarjetas = carrerasDia.filter { it.formaPago == FormaPago.TARJETA }
                        .sumOf { it.importeReal },
                    carrerasTarjeta = carrerasDia.count { it.formaPago == FormaPago.TARJETA },
                    faltaPara100 = faltaPara100,
                    horaInicio = horaInicio,
                    carrerasEmisora = carrerasDia.count { it.emisora },
                        kmInicio = kmInicio,
                    totalPropinas = carrerasDia.sumOf { it.propina },
                    sumaEmisora = carrerasDia.filter { it.emisora }
                        .sumOf { it.importeReal }
                )
            }.collect { resumenDia ->
                _resumenDia.value = resumenDia
                }
            } catch (e: Exception) {
                // Manejar el error
            }
        }
    }

    fun eliminarTurnoCompleto(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("VistaCarrerasViewModel", "Iniciando eliminación del turno: $turnoId")
                
                // Primero eliminamos todas las carreras del turno
                val carrerasEliminadas = carreraDao.deleteCarrerasByTurnoId(turnoId)
                Log.d("VistaCarrerasViewModel", "Carreras eliminadas: $carrerasEliminadas")
                
                // Luego eliminamos el turno
                turnoDao.deleteTurno(turnoId)
                Log.d("VistaCarrerasViewModel", "Turno eliminado correctamente: $turnoId")
                
                // Llamar al callback en el hilo principal
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("VistaCarrerasViewModel", "Error al eliminar turno: ${e.message}", e)
                // Llamar al callback incluso si hay error, para que la UI pueda manejar el error
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onComplete()
                }
            }
        }
    }

    /**
     * Agrega una carrera rápida con el importe ingresado desde el teclado numérico
     */
    fun agregarCarreraRapida(importe: Double, turnoId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (importe <= 0) {
                return@launch
            }
            
            // Crear una nueva carrera con datos básicos
            val carrera = Carrera(
                turno = turnoId,
                taximetro = importe,
                importeReal = importe,
                propina = 0.0,
                formaPago = FormaPago.EFECTIVO, // Por defecto efectivo
                hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()), // Fecha actual
                aeropuerto = false,
                emisora = false
            )
            
            // Guardar en la base de datos
            val id = carreraDao.insertCarrera(carrera)
            
            // Recargar datos
            cargarDatos()
            
            // Llamar al callback
            onComplete()
        }
    }
}

class VistaCarrerasViewModelFactory(
    private val application: Application,
    private val turnoId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VistaCarrerasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VistaCarrerasViewModel(application, turnoId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 