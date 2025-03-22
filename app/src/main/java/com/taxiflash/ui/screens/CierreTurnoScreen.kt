package com.taxiflash.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.taxiflash.ui.navigation.NavRoutes
import com.taxiflash.ui.viewmodel.CierreTurnoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CierreTurnoScreen(
    navController: NavController,
    turnoId: String,
    viewModel: CierreTurnoViewModel = viewModel()
) {
    val kmFin by viewModel.kmFin.collectAsState()
    val horaFin by viewModel.horaFin.collectAsState()
    val kmFinError by viewModel.kmFinError.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val turnoInfo by viewModel.turnoInfo.collectAsState()
    val cierreExitoso by viewModel.cierreExitoso.collectAsState()
    
    // Cargar información del turno cuando se inicia la pantalla
    LaunchedEffect(turnoId) {
        viewModel.cargarTurno(turnoId)
    }
    
    // Navegar a inicio cuando se completa el cierre
    LaunchedEffect(cierreExitoso) {
        if (cierreExitoso) {
            navController.navigate(NavRoutes.INICIO) {
                popUpTo(NavRoutes.VISTA_CARRERAS.replace("{turnoId}", turnoId)) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cerrar Turno") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Información del turno
            turnoInfo?.let { turno ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Turno #${turno.numeroTurno}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Fecha: ${turno.fecha}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Text(
                            text = "Hora inicio: ${turno.horaInicio}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Text(
                            text = "Kilómetros inicio: ${turno.kmInicio}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Campo para kilómetros finales
            OutlinedTextField(
                value = kmFin,
                onValueChange = { viewModel.updateKmFin(it) },
                label = { Text("Kilómetros finales") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = kmFinError,
                supportingText = if (kmFinError) {
                    { Text("Los kilómetros finales deben ser mayores que los iniciales") }
                } else null
            )
            
            // Mostrar hora de fin
            Text(
                text = "Hora de fin: $horaFin",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            
            // Mostrar mensaje de error si existe
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = { viewModel.cerrarTurno(turnoId) { } },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !kmFinError
                ) {
                    Text("Confirmar Cierre")
                }
            }
        }
    }
} 