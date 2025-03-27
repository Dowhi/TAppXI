package com.taxiflash.ui.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale

@Singleton
class TurnoRepository @Inject constructor(
    private val turnoDao: TurnoDao,
    private val carreraDao: CarreraDao
) {
    // Obtener turno activo
    suspend fun getTurnoActivo() = turnoDao.getTurnoActivo()
    
    // Obtener turno por ID
    suspend fun getTurnoById(id: String) = turnoDao.getTurnoById(id)
    
    // Obtener turnos por fecha - convertir a Flow
    fun getTurnosByFecha(fecha: String): Flow<List<Turno>> = flow {
        emit(turnoDao.getTurnosPorFecha(fecha))
    }
    
    // Obtener turnos por fecha exacta
    suspend fun getTurnosByFechaExacta(fecha: String): List<Turno> {
        return try {
            // La fecha viene en formato dd/MM/yyyy, necesitamos convertirla a dd/MM/yyyy para la búsqueda
            val formatoEntrada = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
            val date = formatoEntrada.parse(fecha)
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
            val fechaBusqueda = formatoSalida.format(date)
            Log.d("TurnoRepository", "Buscando turnos para fecha: $fechaBusqueda")
            turnoDao.getTurnosByFechaExacta(fechaBusqueda)
        } catch (e: Exception) {
            Log.e("TurnoRepository", "Error al formatear fecha: ${e.message}")
            emptyList()
        }
    }
    
    // Obtener turnos entre fechas - convertir a Flow
    fun getTurnosEntreFechas(fechaInicio: String, fechaFin: String): Flow<List<Turno>> = flow {
        emit(turnoDao.getTurnosEntreFechas(fechaInicio, fechaFin))
    }
    
    // Insertar turno
    suspend fun insertTurno(turno: Turno): Long {
        return turnoDao.insertTurno(turno)
    }
    
    // Actualizar turno
    suspend fun updateTurno(turno: Turno) {
        turnoDao.updateTurno(turno)
    }
    
    // Eliminar turno
    suspend fun deleteTurnoById(id: String) {
        turnoDao.deleteTurno(id)
    }
    
    // Eliminar turno y sus carreras
    suspend fun eliminarTurnoYCarreras(turnoId: String) {
        carreraDao.deleteCarrerasByTurnoId(turnoId)
        turnoDao.deleteTurno(turnoId)
    }
    
    // Actualizar cierre de turno
    suspend fun actualizarCierreTurno(turnoId: String, kmFin: Int, horaFin: String) {
        // Ahora pasamos kmFin al DAO
        turnoDao.actualizarCierreTurno(turnoId, horaFin, kmFin)
        // La desactivación del turno ya se realiza en el DAO, pero mantenemos esto por seguridad
        turnoDao.desactivarTurno(turnoId)
    }
} 