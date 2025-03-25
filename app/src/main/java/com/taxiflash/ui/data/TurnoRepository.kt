package com.taxiflash.ui.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

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
        return turnoDao.getTurnosByFechaExacta(fecha)
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