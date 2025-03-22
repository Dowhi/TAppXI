package com.taxiflash.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Entidad que representa un turno de trabajo
 */
@Entity(tableName = "turnos")
data class Turno(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "fecha")
    val fecha: String,
    
    @ColumnInfo(name = "horaInicio")
    val horaInicio: String,
    
    @ColumnInfo(name = "horaFin")
    val horaFin: String,
    
    @ColumnInfo(name = "kmInicio")
    val kmInicio: Int,
    
    @ColumnInfo(name = "kmFin", defaultValue = "0")
    val kmFin: Int,
    
    @ColumnInfo(name = "numeroTurno", defaultValue = "1")
    val numeroTurno: Int,
    
    @ColumnInfo(name = "idTurno")
    val idTurno: String,
    
    @ColumnInfo(name = "activo")
    val activo: Boolean
) 