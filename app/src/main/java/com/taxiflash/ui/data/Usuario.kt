package com.taxiflash.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Entidad que representa un usuario en la aplicación.
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val pin: String,
    val dni: String,
    val fechaNacimiento: LocalDate,
    val isActivo: Boolean = false // Para identificar el usuario actualmente seleccionado
) 