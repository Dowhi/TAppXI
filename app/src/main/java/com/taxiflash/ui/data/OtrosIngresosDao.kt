package com.taxiflash.ui.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceder a la tabla de otros ingresos
 */
@Dao
interface OtrosIngresosDao {
    @Query("SELECT * FROM otros_ingresos ORDER BY fecha DESC")
    fun getAllOtrosIngresos(): Flow<List<OtrosIngresos>>

    @Query("SELECT * FROM otros_ingresos WHERE fecha = :fecha")
    fun getOtrosIngresosByFecha(fecha: String): Flow<List<OtrosIngresos>>

    @Query("SELECT * FROM otros_ingresos WHERE id = :id")
    suspend fun getOtrosIngresosById(id: Long): OtrosIngresos?

    @Insert
    suspend fun insertOtrosIngresos(otrosIngresos: OtrosIngresos): Long

    @Update
    suspend fun updateOtrosIngresos(otrosIngresos: OtrosIngresos)

    @Delete
    suspend fun deleteOtrosIngresos(otrosIngresos: OtrosIngresos)

    @Query("DELETE FROM otros_ingresos WHERE id = :id")
    suspend fun deleteOtrosIngresosById(id: Long)
    
    /**
     * Obtiene la suma de otros ingresos para un mes y año específicos
     */
    @Query("SELECT COALESCE(SUM(importe), 0) FROM otros_ingresos WHERE substr(fecha, 4, 2) = :mes AND substr(fecha, 7, 4) = :año")
    suspend fun getOtrosIngresosMensuales(mes: String, año: String): Double
    
    /**
     * Obtiene los otros ingresos para un rango de fechas
     */
    @Query("SELECT * FROM otros_ingresos WHERE fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha ASC")
    fun getOtrosIngresosByRangoFecha(fechaInicio: String, fechaFin: String): Flow<List<OtrosIngresos>>

    /**
     * Obtiene los otros ingresos para un mes específico usando el nombre del mes y el año
     */
    @Query("SELECT * FROM otros_ingresos WHERE " +
           "(substr(fecha, 4, 2) = '01' AND :nombreMes = 'Enero') OR " +
           "(substr(fecha, 4, 2) = '02' AND :nombreMes = 'Febrero') OR " +
           "(substr(fecha, 4, 2) = '03' AND :nombreMes = 'Marzo') OR " +
           "(substr(fecha, 4, 2) = '04' AND :nombreMes = 'Abril') OR " +
           "(substr(fecha, 4, 2) = '05' AND :nombreMes = 'Mayo') OR " +
           "(substr(fecha, 4, 2) = '06' AND :nombreMes = 'Junio') OR " +
           "(substr(fecha, 4, 2) = '07' AND :nombreMes = 'Julio') OR " +
           "(substr(fecha, 4, 2) = '08' AND :nombreMes = 'Agosto') OR " +
           "(substr(fecha, 4, 2) = '09' AND :nombreMes = 'Septiembre') OR " +
           "(substr(fecha, 4, 2) = '10' AND :nombreMes = 'Octubre') OR " +
           "(substr(fecha, 4, 2) = '11' AND :nombreMes = 'Noviembre') OR " +
           "(substr(fecha, 4, 2) = '12' AND :nombreMes = 'Diciembre') " +
           "AND substr(fecha, 7, 4) = :año ORDER BY fecha ASC")
    fun getOtrosIngresosByMes(nombreMes: String, año: String): Flow<List<OtrosIngresos>>
} 