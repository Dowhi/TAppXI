package com.taxiflash.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa otros ingresos que no son carreras de taxi
 */
@Entity(tableName = "otros_ingresos")
data class OtrosIngresos(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val concepto: String,
    val fecha: String,
    val importe: Double,
    val descripcion: String?,
    val notas: String?
) 