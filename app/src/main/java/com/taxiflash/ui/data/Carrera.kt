package com.taxiflash.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carreras")
data class Carrera(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: String = "",  // TEXT en la base de datos, formato dd/MM/yyyy
    val hora: String = "00:00",
    val taximetro: Double = 0.0,
    val importeReal: Double = 0.0,
    val propina: Double = 0.0,
    val formaPago: FormaPago = FormaPago.EFECTIVO,
    val emisora: Boolean = false,
    val aeropuerto: Boolean = false,
    val turno: String = "Turno 0"
) {

}

enum class FormaPago {
    EFECTIVO,
    TARJETA,
    BIZUM,
    VALES
} 