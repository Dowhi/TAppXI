package com.taxiflash.ui.data

data class DatosDiarios(
    val dia: String,
    val ingresos: Float,
    val gastos: Float
)

data class DatosGastosPorCategoria(
    val categoria: String,
    val porcentaje: Float,
    val monto: Float
)

data class EstadisticasData(
    val datosDiarios: List<DatosDiarios> = emptyList(),
    val datosGastosPorCategoria: List<DatosGastosPorCategoria> = emptyList(),
    val ingresoPromedio: Float = 0f,
    val gastoPromedio: Float = 0f,
    val mejorDia: String = "",
    val margen: Float = 0f
) 