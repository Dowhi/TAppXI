package com.taxiflash.ui.data.repository

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CarreraRepository(private val application: Application) {
    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "taxiflash-db"
    ).build()
    
    private val carreraDao = database.carreraDao()
    
    // Método para obtener todas las carreras de un turno
    suspend fun getCarrerasForTurno(turnoId: String): List<Carrera> {
        return withContext(Dispatchers.IO) {
            try {
                carreraDao.getCarrerasByTurnoId(turnoId)
            } catch (e: Exception) {
                Log.e("CarreraRepository", "Error al obtener carreras por turnoId: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Método para obtener una carrera por su ID
    suspend fun getCarreraById(carreraId: Long): Carrera? {
        return withContext(Dispatchers.IO) {
            try {
                carreraDao.getCarreraById(carreraId)
            } catch (e: Exception) {
                Log.e("CarreraRepository", "Error al obtener carrera por ID: ${e.message}")
                null
            }
        }
    }
    
    // Método para actualizar una carrera existente
    suspend fun updateCarrera(carrera: Carrera): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                carreraDao.updateCarrera(carrera)
                true
            } catch (e: Exception) {
                Log.e("CarreraRepository", "Error al actualizar carrera: ${e.message}")
                false
            }
        }
    }

    // Método para eliminar todas las carreras de un turno
    suspend fun deleteCarrerasByTurno(turnoId: String) {
        withContext(Dispatchers.IO) {
            try {
                carreraDao.deleteCarrerasByTurnoId(turnoId)
            } catch (e: Exception) {
                Log.e("CarreraRepository", "Error al eliminar carreras: ${e.message}")
            }
        }
    }
} 