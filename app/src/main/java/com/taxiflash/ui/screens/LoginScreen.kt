package com.taxiflash.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.R
import com.taxiflash.ui.data.UsuarioRepository
import com.taxiflash.ui.viewmodel.UsuarioViewModel
import com.taxiflash.ui.theme.TaxiDarkGray
import com.taxiflash.ui.theme.TaxiGray
import com.taxiflash.ui.theme.TaxiLightGray

/**
 * Pantalla de login por PIN
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onRecuperarClick: () -> Unit,
    viewModel: UsuarioViewModel = viewModel(),
) {
    val pin by viewModel.pin.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val pinLength = pin.length

    val primaryColor = MaterialTheme.colorScheme.primary
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.fullogo_azul),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Buenas tardes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TaxiDarkGray
            )
        }
        
        // Instrucciones
        Text(
            text = "Introduce un código PIN para poder acceder de forma rápida y segura a la aplicación",
            color = TaxiGray,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Indicadores de PIN
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            for (i in 0 until 4) {
                val isFilled = i < pinLength
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (isFilled) primaryColor else TaxiLightGray
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        
        // Mensaje de error
        mensajeError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botón para cambiar de usuario
        TextButton(
            onClick = {
                viewModel.cambiarUsuario()
                onRegisterClick()
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "Cambiar de usuario",
                color = primaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Teclado numérico
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila 1: 1, 2, 3
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                NumberButton(number = "1", onClick = { viewModel.updatePin(pin + "1") })
                NumberButton(number = "2", onClick = { viewModel.updatePin(pin + "2") })
                NumberButton(number = "3", onClick = { viewModel.updatePin(pin + "3") })
            }
            
            // Fila 2: 4, 5, 6
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                NumberButton(number = "4", onClick = { viewModel.updatePin(pin + "4") })
                NumberButton(number = "5", onClick = { viewModel.updatePin(pin + "5") })
                NumberButton(number = "6", onClick = { viewModel.updatePin(pin + "6") })
            }
            
            // Fila 3: 7, 8, 9
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                NumberButton(number = "7", onClick = { viewModel.updatePin(pin + "7") })
                NumberButton(number = "8", onClick = { viewModel.updatePin(pin + "8") })
                NumberButton(number = "9", onClick = { viewModel.updatePin(pin + "9") })
            }
            
            // Fila 4: Vacío, 0, Borrar
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(72.dp)) {
                    // Espacio vacío
                }
                
                NumberButton(number = "0", onClick = { viewModel.updatePin(pin + "0") })
                
                IconButton(
                    onClick = {
                        if (pin.isNotEmpty()) {
                            viewModel.updatePin(pin.substring(0, pin.length - 1))
                        }
                    },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        Icons.Default.Backspace,
                        contentDescription = "Borrar",
                        tint = primaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
    
    // Verificar PIN completo automáticamente
    LaunchedEffect(pin) {
        viewModel.resetError()
        if (pin.length == 4) {
            viewModel.validarPin(onSuccess = onLoginSuccess)
        }
    }
}

@Composable
private fun NumberButton(
    number: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = number,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
} 