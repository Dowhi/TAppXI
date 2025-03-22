package com.taxiflash.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.components.TaxiFlashScaffold
import com.taxiflash.ui.theme.TaxiYellow
import com.taxiflash.ui.viewmodel.CarreraViewModel
import com.taxiflash.ui.viewmodel.TurnoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarreraRegistroScreen(
    navController: NavController,
    viewModel: CarreraViewModel = viewModel(),
    turnoViewModel: TurnoViewModel = viewModel(),
    carreraId: Long = -1L,
    turnoActual: String = ""
) {
    TaxiFlashScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (carreraId != -1L) "Editar Carrera" else "Nueva Carrera",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TaxiYellow,
                    titleContentColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { padding ->
        // Aquí iría el contenido de la pantalla
        Text(
            text = "Contenido de la pantalla de registro de carrera",
            modifier = Modifier.padding(padding)
        )
    }
} 