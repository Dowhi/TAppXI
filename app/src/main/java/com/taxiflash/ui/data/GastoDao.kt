package com.taxiflash.ui.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    fun getAllGastos(): Flow<List<Gasto>>

    @Query("""
        SELECT * FROM gastos 
        WHERE strftime('%Y%m%d', substr(fecha,7,4) || '-' || substr(fecha,4,2) || '-' || substr(fecha,1,2)) 
        BETWEEN :fechaInicioStr AND :fechaFinStr 
        ORDER BY fecha DESC
    """)
    fun getGastosEntreFechas(fechaInicioStr: String, fechaFinStr: String): Flow<List<Gasto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGasto(gasto: Gasto)

    @Delete
    suspend fun deleteGasto(gasto: Gasto)

    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun getGastoById(id: Long): Gasto?

    /**
     * Obtiene los gastos mensuales
     */
    @Query("SELECT COALESCE(SUM(importeTotal), 0) FROM gastos WHERE substr(fecha, 4, 2) = :mes AND substr(fecha, 7, 4) = :año")
    suspend fun getGastosMensuales(mes: String, año: String): Double

    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    suspend fun getAllGastosList(): List<Gasto>
    
    /**
     * Obtiene los gastos por fecha específica
     */
    @Query("SELECT * FROM gastos WHERE fecha = :fecha ORDER BY id DESC")
    fun getGastosByFecha(fecha: String): Flow<List<Gasto>>
} 