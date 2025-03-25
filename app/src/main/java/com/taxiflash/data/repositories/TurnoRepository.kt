package com.taxiflash.data.repositories

import com.taxiflash.data.models.Turno
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TurnoRepository @Inject constructor() {

    // Datos de prueba para desarrollo
    private val turnosDePrueba = mutableListOf(
        Turno(
            id = 1,
            fecha = "2023-11-20",
            horaInicio = "08:00",
            horaFin = "16:00",
            kmInicio = 100000,
            kmFin = 100150
        ),
        Turno(
            id = 2,
            fecha = "2023-11-21",
            horaInicio = "08:00",
            horaFin = "16:00",
            kmInicio = 100150,
            kmFin = 100300
        )
    )

    suspend fun obtenerTurnosPorFecha(fecha: String): List<Turno> {
        // Simulación de llamada a API o base de datos
        return turnosDePrueba.filter { it.fecha == fecha }
    }

    suspend fun obtenerTurnoPorId(id: Int): Turno? {
        return turnosDePrueba.find { it.id == id }
    }

    suspend fun actualizarKilometros(id: Int, kmInicio: Int, kmFin: Int) {
        val turno = turnosDePrueba.find { it.id == id }
        turno?.let {
            it.kmInicio = kmInicio
            it.kmFin = kmFin
        }
    }

    suspend fun actualizarHorarios(id: Int, horaInicio: String, horaFin: String) {
        val turno = turnosDePrueba.find { it.id == id }
        turno?.let {
            it.horaInicio = horaInicio
            it.horaFin = horaFin
        }
    }
} 