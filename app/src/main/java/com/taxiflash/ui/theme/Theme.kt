package com.taxiflash.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 🎨 Paleta personalizada TaxiFlash - Colores principales
val TaxiBlue = Color(0xFF1E3F8B)
val TaxiBlueLight = Color(0xFF687DB6)
val TaxiBlueDark = Color(0xFF0D2454)

// Alias para mantener compatibilidad
val TaxiLightBlue = TaxiBlueLight
val TaxiDarkBlue = TaxiBlueDark

val TaxiYellow = Color(0xFFFFC400)
val TaxiYellowLight = Color(0xFFFFE082)
val TaxiYellowDark = Color(0xFFC79100)

// Alias para mantener compatibilidad
val TaxiLightYellow = TaxiYellowLight
val TaxiDarkYellow = TaxiYellowDark

val TaxiGreen = Color(0xFF00BFA5)
val TaxiRed = Color(0xFFFC0B05)

// Colores adicionales
val TaxiLightGreen = Color(0xFFA5D6A7)
val TaxiDarkGreen = Color(0xFF388E3C)

val TaxiLightRed = Color(0xF7FD0321)
val TaxiDarkRed = Color(0xFFFA0202)

// Fondos
val BackgroundLight = Color(0xFFF8F9FA)
val SurfaceLight = Color(0xFFFFFFFF)

val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)

// Colores de grises y neutros
val TaxiWhite = Color(0xFFFFFFFF)
val TaxiLightGray = Color(0xFFE0E0E0)
val TaxiGray = Color(0xFF9E9E9E)
val TaxiDarkGray = Color(0xFF212121)
val TaxiBlack = Color(0xFF000000)

// 🌞 Tema claro
private val LightColorScheme = lightColorScheme(
    primary = TaxiBlue,
    onPrimary = TaxiWhite,
    primaryContainer = TaxiBlueLight,
    onPrimaryContainer = Color(0xFF001A41),

    secondary = TaxiYellow,
    onSecondary = TaxiBlack,
    secondaryContainer = TaxiYellowLight,
    onSecondaryContainer = Color(0xFF261900),

    tertiary = TaxiGreen,
    onTertiary = TaxiWhite,
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF00332C),

    error = TaxiRed,
    onError = TaxiWhite,
    errorContainer = TaxiLightRed,
    onErrorContainer = Color(0xFF410002),

    background = BackgroundLight,
    onBackground = TaxiDarkGray,
    surface = SurfaceLight,
    onSurface = TaxiDarkGray,

    surfaceVariant = TaxiLightGray,
    onSurfaceVariant = TaxiGray,
    outline = TaxiGray
)

// 🌙 Tema oscuro
private val DarkColorScheme = darkColorScheme(
    primary = TaxiBlueDark,
    onPrimary = TaxiWhite,
    primaryContainer = TaxiBlue,
    onPrimaryContainer = TaxiBlueLight,

    secondary = TaxiYellowDark,
    onSecondary = TaxiBlack,
    secondaryContainer = TaxiYellow,
    onSecondaryContainer = TaxiYellowLight,

    tertiary = TaxiGreen,
    onTertiary = TaxiBlack,
    tertiaryContainer = TaxiDarkGreen,
    onTertiaryContainer = TaxiLightGreen,

    error = TaxiLightRed,
    onError = TaxiBlack,
    errorContainer = TaxiRed,
    onErrorContainer = TaxiLightRed,

    background = BackgroundDark,
    onBackground = Color(0xFFECECEC),
    surface = SurfaceDark,
    onSurface = Color(0xFFECECEC),

    surfaceVariant = TaxiDarkGray,
    onSurfaceVariant = TaxiLightGray,
    outline = TaxiGray
)

@Composable
fun TaxiFlashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 🪟 Cambia el color de la barra de estado
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
