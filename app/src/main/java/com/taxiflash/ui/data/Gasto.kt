package com.taxiflash.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "gastos")
data class Gasto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val factura: String,
    val proveedor: String,
    val fecha: String,
    val importeTotal: Double,
    val iva: Double,
    val kilometros: Int?,
    val tipoGasto: String,
    val tipoGastoEspecifico: String,
    val descripcion: String?
)

enum class TipoGasto {
    COMBUSTIBLE,
    MANTENIMIENTO,
    LIMPIEZA,
    PARKING,
    OTROS
} 