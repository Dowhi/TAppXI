package com.taxiflash.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.taxiflash.ui.theme.TaxiBlue
import com.taxiflash.ui.theme.TaxiYellow
import com.taxiflash.ui.theme.TaxiLightGray
import com.taxiflash.ui.theme.TaxiDarkGray
import com.taxiflash.ui.theme.TaxiGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Barra superior estándar para la aplicación
 * 
 * @param title Título a mostrar en la barra
 * @param onBackClick Acción a realizar al hacer clic en el botón de retroceso
 * @param actions Acciones adicionales a mostrar en la barra
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiFlashTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = TaxiBlue
        )
    )
}

/**
 * Indicador de carga para operaciones asíncronas
 * 
 * @param isLoading Si es true, se muestra el indicador de carga
 * @param modifier Modificador para personalizar el aspecto
 */
@Composable
fun LoadingIndicator(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * Tipos de mensajes para el componente MessageBar
 */
enum class MessageType {
    INFO, SUCCESS, WARNING, ERROR
}

/**
 * Barra de mensaje temporal
 * 
 * @param message Mensaje a mostrar
 * @param type Tipo de mensaje (INFO, SUCCESS, WARNING, ERROR)
 * @param duration Duración en milisegundos (0 para no ocultar automáticamente)
 * @param onDismiss Acción a realizar al cerrar el mensaje
 */
@Composable
fun MessageBar(
    message: String,
    type: MessageType = MessageType.INFO,
    duration: Long = 3000,
    onDismiss: () -> Unit = {}
) {
    val backgroundColor = when (type) {
        MessageType.INFO -> MaterialTheme.colorScheme.primary
        MessageType.SUCCESS -> MaterialTheme.colorScheme.tertiary
        MessageType.WARNING -> MaterialTheme.colorScheme.secondary
        MessageType.ERROR -> MaterialTheme.colorScheme.error
    }
    
    val icon = when (type) {
        MessageType.INFO -> Icons.Default.Info
        MessageType.SUCCESS -> Icons.Default.CheckCircle
        MessageType.WARNING -> Icons.Default.Warning
        MessageType.ERROR -> Icons.Default.Error
    }
    
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(message) {
        if (duration > 0) {
            coroutineScope.launch {
                delay(duration)
                onDismiss()
            }
        }
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Diálogo de confirmación
 * 
 * @param title Título del diálogo
 * @param message Mensaje del diálogo
 * @param confirmText Texto del botón de confirmación
 * @param dismissText Texto del botón de cancelación
 * @param icon Icono a mostrar (opcional)
 * @param onConfirm Acción a realizar al confirmar
 * @param onDismiss Acción a realizar al cancelar
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Aceptar",
    dismissText: String = "Cancelar",
    icon: ImageVector? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = message) },
        icon = icon?.let { { Icon(imageVector = it, contentDescription = null) } },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        }
    )
}

/**
 * Pantalla de estado vacío
 * 
 * @param title Título a mostrar
 * @param message Mensaje descriptivo
 * @param icon Icono a mostrar (opcional)
 * @param action Acción opcional a mostrar como botón
 * @param actionText Texto del botón de acción
 */
@Composable
fun EmptyStateScreen(
    title: String,
    message: String,
    icon: ImageVector? = null,
    action: (() -> Unit)? = null,
    actionText: String = "Acción"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = action,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = actionText)
            }
        }
    }
}

/**
 * Diálogo de carga para operaciones largas
 * 
 * @param isVisible Si es true, se muestra el diálogo
 * @param message Mensaje a mostrar
 */
@Composable
fun LoadingDialog(
    isVisible: Boolean,
    message: String = "Cargando...",
    onDismissRequest: () -> Unit = {}
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de estadísticas para mostrar datos numéricos
 * 
 * @param title Título de la estadística
 * @param value Valor numérico a mostrar
 * @param icon Icono opcional
 * @param backgroundColor Color de fondo de la tarjeta
 * @param contentColor Color del contenido
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Tarjeta de resumen para mostrar información resumida
 * 
 * @param title Título del resumen
 * @param content Contenido a mostrar
 * @param icon Icono opcional
 * @param actions Acciones opcionales
 */
@Composable
fun SummaryCard(
    title: String,
    content: @Composable () -> Unit,
    icon: ImageVector? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = TaxiBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TaxiBlue
                )
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.LightGray,
                thickness = 1.dp
            )
            
            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }
            
            if (actions != {}) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    content = actions
                )
            }
        }
    }
}

/**
 * Botón de acción principal con gradiente
 * 
 * @param text Texto del botón
 * @param onClick Acción a realizar al hacer clic
 * @param icon Icono opcional
 * @param enabled Si es false, el botón aparece deshabilitado
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            TaxiBlue,
            TaxiBlue.copy(alpha = 0.8f)
        )
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Chip de filtro para seleccionar opciones
 * 
 * @param text Texto del chip
 * @param selected Si es true, el chip aparece seleccionado
 * @param onClick Acción a realizar al hacer clic
 */
@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(end = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) TaxiYellow else TaxiLightGray.copy(alpha = 0.3f),
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (selected) Color.Black else TaxiDarkGray,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

/**
 * Tarjeta de información con icono
 * 
 * @param title Título de la tarjeta
 * @param subtitle Subtítulo opcional
 * @param icon Icono a mostrar
 * @param onClick Acción a realizar al hacer clic (opcional)
 */
@Composable
fun InfoCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    
    Card(
        modifier = cardModifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TaxiBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TaxiBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TaxiGray
                    )
                }
            }
            
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver más",
                    tint = TaxiGray
                )
            }
        }
    }
} 