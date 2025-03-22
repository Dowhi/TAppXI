package com.taxiflash.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.taxiflash.ui.theme.TaxiBlue
import kotlinx.coroutines.delay

/**
 * Indicador de carga a pantalla completa con mensaje
 * 
 * @param isLoading Si es true, se muestra el indicador de carga
 * @param message Mensaje a mostrar durante la carga
 * @param content Contenido a mostrar cuando no está cargando
 */
@Composable
fun FullScreenLoading(
    isLoading: Boolean,
    message: String = "Cargando...",
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                LoadingContent(message = message)
            }
        }
    }
}

/**
 * Indicador de carga con esqueleto para mostrar mientras se cargan los datos
 * 
 * @param isLoading Si es true, se muestra el esqueleto de carga
 * @param content Contenido a mostrar cuando no está cargando
 * @param skeletonContent Contenido del esqueleto a mostrar durante la carga
 */
@Composable
fun SkeletonLoading(
    isLoading: Boolean,
    content: @Composable () -> Unit,
    skeletonContent: @Composable () -> Unit
) {
    if (isLoading) {
        skeletonContent()
    } else {
        content()
    }
}

/**
 * Indicador de carga con tiempo de espera y mensaje de error
 * 
 * @param isLoading Si es true, se muestra el indicador de carga
 * @param timeoutMillis Tiempo en milisegundos antes de mostrar un mensaje de error
 * @param timeoutMessage Mensaje a mostrar cuando se agota el tiempo
 * @param onTimeout Acción a realizar cuando se agota el tiempo
 * @param content Contenido a mostrar cuando no está cargando
 */
@Composable
fun TimeoutLoading(
    isLoading: Boolean,
    timeoutMillis: Long = 15000,
    timeoutMessage: String = "La operación está tardando más de lo esperado. ¿Desea continuar esperando?",
    onTimeout: () -> Unit,
    content: @Composable () -> Unit
) {
    var showTimeoutMessage by remember { mutableStateOf(false) }
    
    LaunchedEffect(isLoading) {
        if (isLoading) {
            showTimeoutMessage = false
            delay(timeoutMillis)
            if (isLoading) {
                showTimeoutMessage = true
            }
        } else {
            showTimeoutMessage = false
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                if (showTimeoutMessage) {
                    TimeoutMessage(
                        message = timeoutMessage,
                        onTimeout = onTimeout
                    )
                } else {
                    LoadingContent()
                }
            }
        }
    }
}

/**
 * Contenido del indicador de carga
 * 
 * @param message Mensaje a mostrar durante la carga
 */
@Composable
private fun LoadingContent(message: String = "Cargando...") {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_alpha"
    )
    
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .alpha(0.9f),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = TaxiBlue,
                strokeWidth = 4.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}

/**
 * Mensaje de tiempo de espera agotado
 * 
 * @param message Mensaje a mostrar
 * @param onTimeout Acción a realizar cuando se agota el tiempo
 */
@Composable
private fun TimeoutMessage(
    message: String,
    onTimeout: () -> Unit
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tiempo de espera excedido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                androidx.compose.material3.Button(
                    onClick = onTimeout,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Continuar")
                }
            }
        }
    }
}

/**
 * Componente de esqueleto para mostrar durante la carga
 * 
 * @param modifier Modificador para personalizar el aspecto
 */
@Composable
fun SkeletonItem(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
    )
} 