package com.taxiflash.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Formas para la aplicación TaxiFlash
 * 
 * Define las formas utilizadas en los componentes de la aplicación
 * siguiendo las directrices de Material Design 3.
 */
val Shapes = Shapes(
    // Formas pequeñas (chips, botones pequeños)
    small = RoundedCornerShape(4.dp),
    
    // Formas medianas (tarjetas, campos de texto)
    medium = RoundedCornerShape(8.dp),
    
    // Formas grandes (hojas de diálogo, tarjetas grandes)
    large = RoundedCornerShape(16.dp),
    
    // Formas extra grandes (hojas inferiores)
    extraLarge = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
) 