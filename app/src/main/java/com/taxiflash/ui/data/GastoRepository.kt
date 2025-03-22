package com.taxiflash.ui.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GastoRepository @Inject constructor(
    private val gastoDao: GastoDao
) {
    /**
     * Obtiene los gastos mensuales
     */
    suspend fun obtenerGastosMensuales(mes: Int, año: Int): Double {
        val mesStr = String.format("%02d", mes)
        val añoStr = año.toString()
        
        // Log para depuración
        println("TaxiFlash - Consultando gastos para mes: $mesStr, año: $añoStr")
        
        // Obtener gastos de la base de datos
        val gastos = gastoDao.getGastosMensuales(mesStr, añoStr) ?: 0.0
        
        // Log del resultado
        println("TaxiFlash - Gastos obtenidos: $gastos")
        
        return gastos
    }
} 