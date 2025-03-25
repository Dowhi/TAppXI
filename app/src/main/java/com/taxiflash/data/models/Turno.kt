package com.taxiflash.data.models

data class Turno(
    val id: Int,
    val fecha: String,
    var horaInicio: String,
    var horaFin: String,
    var kmInicio: Int,
    var kmFin: Int
) 