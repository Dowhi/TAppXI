package com.taxiflash.ui.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Interface para acceder a la tabla de turnos
 */
@Dao
interface TurnoDao {
    @Query("SELECT COUNT(*) FROM turnos WHERE fecha = :fecha")
    suspend fun getTurnosCountByFecha(fecha: String): Int

    @Query("SELECT * FROM turnos WHERE fecha = :fecha")
    suspend fun getTurnosByFechaExacta(fecha: String): List<Turno>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurno(turno: Turno): Long

    @Update
    suspend fun updateTurno(turno: Turno)

    @Query("SELECT COUNT(*) FROM turnos WHERE activo = 1")
    suspend fun hayTurnoActivo(): Int

    @Query("SELECT * FROM turnos WHERE activo = 1 LIMIT 1")
    suspend fun getTurnoActivo(): Turno?

    @Query("UPDATE turnos SET activo = 0 WHERE id = :turnoId")
    suspend fun cerrarTurno(turnoId: Long)

    @Query("SELECT idTurno FROM turnos WHERE activo = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getUltimoTurnoActivoId(): String?

    @Query("SELECT IFNULL(MAX(numeroTurno), 0) FROM turnos WHERE fecha = :fecha")
    suspend fun getUltimoNumeroTurnoDia(fecha: String): Int?

    @Query("SELECT * FROM turnos WHERE idTurno = :turnoId")
    suspend fun getTurnoById(turnoId: String): Turno?

    @Query("DELETE FROM turnos WHERE idTurno = :turnoId")
    suspend fun deleteTurno(turnoId: String)

    @Query("SELECT * FROM turnos WHERE fecha BETWEEN :fechaInicio AND :fechaFin")
    suspend fun getTurnosEntreFechas(fechaInicio: String, fechaFin: String): List<Turno>
    
    @Query("SELECT * FROM turnos ORDER BY fecha DESC")
    suspend fun getAllTurnos(): List<Turno>

    @Query("UPDATE turnos SET activo = 0 WHERE idTurno = :turnoId")
    suspend fun desactivarTurno(turnoId: String)

    @Query("SELECT * FROM turnos WHERE fecha LIKE :fecha || '%'")
    suspend fun getTurnosPorFecha(fecha: String): List<Turno>

    @Query("SELECT * FROM turnos WHERE fecha LIKE :mes || '%'")
    suspend fun getTurnosPorMes(mes: String): List<Turno>

    @Query("SELECT * FROM turnos WHERE fecha = :fecha ORDER BY numeroTurno DESC LIMIT 1")
    suspend fun getUltimoTurnoDia(fecha: String): Turno?

    @Query("SELECT COUNT(*) FROM turnos")
    suspend fun getTurnosCount(): Int

    @Query("DELETE FROM turnos")
    suspend fun deleteAllTurnos()

    @Query("SELECT * FROM turnos WHERE fecha LIKE :year || '%'")
    suspend fun getTurnosPorAnio(year: String): List<Turno>

    @Query("SELECT * FROM turnos ORDER BY fecha DESC")
    fun getAllTurnosFlow(): Flow<List<Turno>>

    @Query("UPDATE turnos SET horaFin = :horaFin, kmFin = :kmFin, activo = 0 WHERE idTurno = :turnoId")
    suspend fun actualizarCierreTurno(turnoId: String, horaFin: String, kmFin: Int)
}