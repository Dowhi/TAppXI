package com.taxiflash.ui.navigation

/**
 * Rutas de navegación para la aplicación TaxiFlash
 */
object NavRoutes {
    // Pantallas de autenticación
    const val LOGIN = "login"
    const val REGISTRO_USUARIO = "registro_usuario"
    const val RECUPERAR_PIN = "recuperar_pin"
    
    // Pantallas principales
    const val INICIO = "inicio"
    const val SPLASH = "splash"
    const val INGRESOS = "ingresos"
    const val TURNO_REGISTRO = "turno_registro"
    const val VISTA_CARRERAS = "vista_carreras/{turnoId}"
    const val CARRERA_FORMULARIO = "carrera_formulario/{turnoId}?carreraId={carreraId}"
    const val REGISTRO_CARRERA = "registro_carrera/{turnoId}"
    const val REGISTRO_CARRERA_EDITAR = "registro_carrera/{turnoId}?carreraId={carreraId}"
    const val DETALLE_CARRERA = "detalle_carrera/{carreraId}"
    const val OTROS_INGRESOS = "otros_ingresos"
    const val RESUMEN_DIARIO = "resumen_diario"
    const val RESUMEN_DIARIO_FECHA = "resumen_diario/{fecha}"
    const val RESUMEN_MENSUAL = "resumen_mensual"
    const val RESUMEN_MENSUAL_DETALLADO = "resumen_mensual_detallado"
    const val RESUMEN_ANUAL = "resumen_anual"
    const val CIERRE_TURNO = "cierre_turno/{turnoId}"
    
    // Pantallas de gestión de gastos
    const val GASTOS = "gastos"
    const val GASTO = "gasto"
    const val GASTOS_FORMULARIO = "gastos_formulario?gastoId={gastoId}"
    const val RESUMEN_GASTOS_MENSUAL = "resumen_gastos_mensual"
    
    // Pantallas añadidas
    const val HISTORICO = "historico"
    const val ESTADISTICAS = "estadisticas"
    const val ESTADISTICAS_AVANZADAS = "estadisticas_avanzadas"
    
    // Pantallas de configuración
    const val AJUSTES = "ajustes"
    const val RECORDATORIOS = "recordatorios"
    const val EDITAR_DIA = "editarDia"
    const val EDICION_DIA = "edicion_dia/{fecha}"
    
    // Funciones para generar rutas con parámetros
    fun vistaTurno(turnoId: String) = "vista_carreras/$turnoId"
    fun carreraFormulario(turnoId: String, carreraId: Long? = null) = 
        if (carreraId != null) "carrera_formulario/$turnoId?carreraId=$carreraId"
        else "carrera_formulario/$turnoId"
    fun cierreTurno(turnoId: String) = "cierre_turno/$turnoId"
    fun gastoFormulario(gastoId: Long? = null) = 
        if (gastoId != null) "gastos_formulario?gastoId=$gastoId"
        else "gastos_formulario"
} 