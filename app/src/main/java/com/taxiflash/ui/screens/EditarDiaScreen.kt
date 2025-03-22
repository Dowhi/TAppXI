package com.taxiflash.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.viewmodel.EditarDiaViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarDiaScreen(
    fecha: String,
    onNavigateBack: () -> Unit,
    viewModel: EditarDiaViewModel = viewModel()
) {
    LaunchedEffect(fecha) {
        viewModel.cargarCarrerasDia(fecha)
    }

    val carreras by viewModel.carreras.collectAsState()
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar día: $fecha") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(carreras) { carrera ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text("Hora: ${carrera.hora}")
                        Text("Importe: ${numberFormat.format(carrera.importeReal)}")
                        Text("Forma de pago: ${carrera.formaPago}")
                        if (carrera.propina > 0) {
                            Text("Propina: ${numberFormat.format(carrera.propina)}")
                        }
                        if (carrera.emisora) {
                            Text("Emisora: Sí")
                        }
                        if (carrera.aeropuerto) {
                            Text("Aeropuerto: Sí")
                        }
                    }
                }
            }
        }
    }
} 