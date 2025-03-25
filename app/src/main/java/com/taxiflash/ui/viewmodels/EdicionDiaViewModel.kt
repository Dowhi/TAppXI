package com.taxiflash.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.data.models.Turno
import com.taxiflash.data.repositories.TurnoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EdicionDiaViewModel @Inject constructor(
    private val turnoRepository: TurnoRepository
) : ViewModel() {

    private val _turnos = MutableLiveData<List<Turno>>(emptyList())
    val turnos: LiveData<List<Turno>> = _turnos

    private val _turnoSeleccionado = MutableLiveData<Turno?>(null)
    val turnoSeleccionado: LiveData<Turno?> = _turnoSeleccionado

    private val _mensajeError = MutableLiveData<String>("")
    val mensajeError: LiveData<String> = _mensajeError

    fun cargarTurnosFecha(fecha: String) {
        viewModelScope.launch {
            try {
                val turnos = turnoRepository.obtenerTurnosPorFecha(fecha)
                _turnos.postValue(turnos)
                
                // Si no hay turnos, notificar para que la UI pueda mostrar un mensaje apropiado
                if (turnos.isEmpty()) {
                    _mensajeError.postValue("No se encontraron turnos para la fecha $fecha")
                } else {
                    // Si encontramos turnos, seleccionamos el primero por defecto
                    _turnoSeleccionado.postValue(turnos.first())
                }
            } catch (e: Exception) {
                _mensajeError.postValue("Error al cargar turnos: ${e.message}")
            }
        }
    }

    fun seleccionarTurno(id: Int) {
        viewModelScope.launch {
            try {
                val turno = turnoRepository.obtenerTurnoPorId(id)
                _turnoSeleccionado.postValue(turno)
            } catch (e: Exception) {
                _mensajeError.postValue("Error al seleccionar turno: ${e.message}")
            }
        }
    }

    fun actualizarKilometros(id: Int, kmInicio: Int, kmFin: Int) {
        viewModelScope.launch {
            try {
                turnoRepository.actualizarKilometros(id, kmInicio, kmFin)
                // Recargar el turno para mostrar los cambios
                seleccionarTurno(id)
            } catch (e: Exception) {
                _mensajeError.postValue("Error al actualizar kilómetros: ${e.message}")
            }
        }
    }

    fun actualizarHorarios(id: Int, horaInicio: String, horaFin: String) {
        viewModelScope.launch {
            try {
                turnoRepository.actualizarHorarios(id, horaInicio, horaFin)
                // Recargar el turno para mostrar los cambios
                seleccionarTurno(id)
            } catch (e: Exception) {
                _mensajeError.postValue("Error al actualizar horarios: ${e.message}")
            }
        }
    }

    fun limpiarError() {
        _mensajeError.value = ""
    }
} 