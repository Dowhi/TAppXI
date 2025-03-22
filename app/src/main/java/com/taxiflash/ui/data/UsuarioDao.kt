package com.taxiflash.ui.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO para acceder a la tabla de usuarios
 */
@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario): Long

    @Update
    suspend fun actualizarUsuario(usuario: Usuario)

    @Query("UPDATE usuarios SET isActivo = :isActivo WHERE id = :id")
    suspend fun actualizarEstadoActivo(id: Long, isActivo: Boolean)

    @Query("UPDATE usuarios SET isActivo = 0")
    suspend fun desactivarTodosUsuarios()

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getUsuarioPorId(id: Long): Usuario?

    @Query("SELECT * FROM usuarios WHERE dni = :dni")
    suspend fun getUsuarioPorDni(dni: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE dni = :dni AND fechaNacimiento = :fechaNacimiento")
    suspend fun validarUsuarioPorDniYFecha(dni: String, fechaNacimiento: LocalDate): Usuario?

    @Query("SELECT * FROM usuarios WHERE pin = :pin")
    suspend fun validarUsuarioPorPin(pin: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE isActivo = 1 LIMIT 1")
    suspend fun getUsuarioActivo(): Usuario?

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun getTodosUsuarios(): Flow<List<Usuario>>
} 