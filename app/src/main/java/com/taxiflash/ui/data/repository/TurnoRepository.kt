package com.taxiflash.ui.data.repository

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.taxiflash.ui.data.Turno
import com.taxiflash.ui.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TurnoRepository(private val application: Application) {
    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "taxiflash-db"
    ).build()
    
    private val turnoDao = database.turnoDao()
    private val carreraRepository = CarreraRepository(application)
    
    // Método para obtener todos los turnos de una fecha específica
    suspend fun getTurnosByFecha(fecha: String): List<Turno> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("TurnoRepository", "Buscando turnos para fecha: $fecha")
                
                // Intentar primero con el formato exacto
                var turnos = turnoDao.getTurnosByFechaExacta(fecha)
                Log.d("TurnoRepository", "Turnos encontrados con formato exacto: ${turnos.size}")
                
                // Si no hay resultados, intentar con búsqueda por patrón
                if (turnos.isEmpty()) {
                    Log.d("TurnoRepository", "Intentando búsqueda por patrón para: $fecha")
                    turnos = turnoDao.getTurnosPorFecha(fecha)
                    Log.d("TurnoRepository", "Turnos encontrados con patrón: ${turnos.size}")
                }
                
                turnos
            } catch (e: Exception) {
                Log.e("TurnoRepository", "Error al obtener turnos por fecha: ${e.message}", e)
                emptyList()
            }
        }
    }

    // Método para obtener un turno por su ID
    suspend fun getTurnoById(turnoId: String): Turno? {
        return withContext(Dispatchers.IO) {
            try {
                turnoDao.getTurnoById(turnoId)
            } catch (e: Exception) {
                Log.e("TurnoRepository", "Error al obtener turno por ID: ${e.message}")
                null
            }
        }
    }

    // Método para actualizar un turno existente
    suspend fun updateTurno(turno: Turno): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                turnoDao.updateTurno(turno)
                true
            } catch (e: Exception) {
                Log.e("TurnoRepository", "Error al actualizar turno: ${e.message}")
                false
            }
        }
    }
    
    // Método para eliminar un turno y sus carreras
    suspend fun deleteTurno(turnoId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Eliminar todas las carreras del turno
                carreraRepository.deleteCarrerasByTurno(turnoId)
                
                // Eliminar el turno
                turnoDao.deleteTurno(turnoId)
                true
            } catch (e: Exception) {
                Log.e("TurnoRepository", "Error al eliminar turno: ${e.message}")
                false
            }
        }
    }

    // Método para obtener todos los turnos 
    suspend fun getAllTurnos(): List<Turno> {
        return withContext(Dispatchers.IO) {
            try {
                turnoDao.getAllTurnos()
            } catch (e: Exception) {
                Log.e("TurnoRepository", "Error al obtener todos los turnos: ${e.message}")
                emptyList()
            }
        }
    }
} 