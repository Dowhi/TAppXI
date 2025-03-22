package com.taxiflash.ui.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarreraDao {
    @Query("SELECT * FROM carreras ORDER BY fecha DESC")
    fun getAllCarreras(): Flow<List<Carrera>>

    @Query("SELECT * FROM carreras ORDER BY fecha DESC")
    suspend fun getAllCarrerasList(): List<Carrera>

    @Query("SELECT * FROM carreras WHERE fecha = :fecha")
    fun getCarrerasByFecha(fecha: String): Flow<List<Carrera>>

    @Query("SELECT * FROM carreras WHERE turno = :turnoId")
    fun getCarrerasByTurno(turnoId: String): Flow<List<Carrera>>

    @Query("SELECT * FROM carreras WHERE turno LIKE :turnoPattern")
    fun getCarrerasByTurnoPattern(turnoPattern: String): Flow<List<Carrera>>

    @Query("SELECT * FROM carreras WHERE turno LIKE 'Turno %'")
    fun getCarrerasConTurnoSimple(): Flow<List<Carrera>>

    @Insert
    suspend fun insertCarrera(carrera: Carrera)

    @Update
    suspend fun updateCarrera(carrera: Carrera)

    @Delete
    suspend fun deleteCarrera(carrera: Carrera)

    @Query("SELECT * FROM carreras WHERE id = :carreraId")
    suspend fun getCarreraById(carreraId: Long): Carrera?

    @Query("DELETE FROM carreras WHERE id = :carreraId")
    suspend fun deleteCarreraById(carreraId: Long)

    @Query("SELECT * FROM carreras WHERE fecha = :fechaStr")
    suspend fun getCarrerasByFechaExacta(fechaStr: String): List<Carrera>

    @Query("SELECT * FROM carreras WHERE fecha BETWEEN :fechaInicioStr AND :fechaFinStr ORDER BY fecha ASC")
    fun getCarrerasByRangoFecha(fechaInicioStr: String, fechaFinStr: String): Flow<List<Carrera>>

    @Query("DELETE FROM carreras WHERE turno = :turnoId")
    suspend fun deleteCarrerasByTurno(turnoId: String)

    /**
     * Obtiene los ingresos mensuales
     */
    @Query("SELECT COALESCE(SUM(importeReal), 0) FROM carreras WHERE substr(fecha, 4, 2) = :mes AND substr(fecha, 7, 4) = :año")
    suspend fun getIngresosMensuales(mes: String, año: String): Double
}