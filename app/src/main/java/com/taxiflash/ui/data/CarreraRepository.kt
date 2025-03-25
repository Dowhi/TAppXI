package com.taxiflash.ui.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarreraRepository @Inject constructor(
    private val carreraDao: CarreraDao
) {
    /**
     * Obtiene los ingresos mensuales
     */
    suspend fun obtenerIngresosMensuales(mes: Int, año: Int): Double {
        val mesStr = String.format("%02d", mes)
        val añoStr = año.toString()
        
        // Log para depuración
        println("TaxiFlash - Consultando ingresos para mes: $mesStr, año: $añoStr")
        
        // Obtener ingresos de la base de datos
        val ingresos = carreraDao.getIngresosMensuales(mesStr, añoStr) ?: 0.0
        
        // Log del resultado
        println("TaxiFlash - Ingresos obtenidos: $ingresos")
        
        return ingresos
    }
    
    suspend fun deleteCarrerasByTurno(turnoId: String) {
        carreraDao.deleteCarrerasByTurnoId(turnoId)
    }
} 