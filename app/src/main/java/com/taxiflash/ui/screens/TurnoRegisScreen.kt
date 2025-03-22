package com.taxiflash.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.viewmodel.TurnoViewModel
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnoRegisScreen(
    onNavigateBack: () -> Unit,
    onTurnoGuardado: (String) -> Unit,
    navController: NavHostController,
    viewModel: TurnoViewModel = viewModel()
) {
    val kmInicio by viewModel.kmInicio.collectAsState()
    val horaInicio by viewModel.horaInicio.collectAsState()
    val numeroTurno by viewModel.numeroTurno.collectAsState()
    val turnoGuardado by viewModel.turnoGuardado.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val kmInicioError by viewModel.kmInicioError.collectAsState()
    val turnoActivoId by viewModel.turnoActivoId.collectAsState()
    val turnoActivoInfo by viewModel.turnoActivoInfo.collectAsState()

    LaunchedEffect(turnoGuardado) {
        turnoGuardado?.let { onTurnoGuardado(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comenzar Turno") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Si hay un turno activo, mostrar información
            turnoActivoInfo?.let { turnoActivo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Turno ${turnoActivo.numeroTurno} actualmente activo",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Iniciado a las ${turnoActivo.horaInicio}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { 
                                navController.navigate("vista_carreras/${turnoActivo.idTurno}") {
                                    popUpTo("inicio")
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Continuar con Turno ${turnoActivo.numeroTurno}")
                        }
                    }
                }
            }

            if (turnoActivoInfo == null) {
                Text(
                    text = "Turno $numeroTurno",
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedTextField(
                    value = kmInicio,
                    onValueChange = { viewModel.updateKmInicio(it) },
                    label = { Text("Km Inicio") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = kmInicioError,
                    supportingText = if (kmInicioError) {
                        { Text("Este campo es obligatorio") }
                    } else null
                )

                OutlinedTextField(
                    value = horaInicio,
                    onValueChange = { },
                    label = { Text("Hora Inicio") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )

                Button(
                    onClick = { viewModel.guardarTurno() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Comenzar Turno")
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
} 