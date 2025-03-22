package com.taxiflash.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxiflash.ui.R
import com.taxiflash.ui.data.FormaPago
import com.taxiflash.ui.theme.TaxiFlashTheme
import com.taxiflash.ui.viewmodel.CarreraViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.shadow

/**
 * Pantalla para registrar o editar una carrera.
 *
 * @param turnoActual El turno al que se asociará la carrera
 * @param carreraId ID de la carrera a editar, o -1L para una nueva carrera
 * @param viewModel ViewModel que gestiona los datos de la carrera
 * @param onNavigateBack Callback para navegar hacia atrás
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroCarreraScreen(
    turnoActual: String,
    carreraId: Long = -1L,
    viewModel: CarreraViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    // Colores de la aplicación
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val greenColor = MaterialTheme.colorScheme.tertiary

    // Efecto para cargar datos si estamos editando
    LaunchedEffect(carreraId) {
        if (carreraId != -1L) {
            viewModel.cargarCarrera(carreraId)
        }
    }

    // Estados de la UI
    val taximetro by viewModel.taximetro.collectAsState()
    val importeReal by viewModel.importeReal.collectAsState()
    val propina by viewModel.propina.collectAsState()
    val formaPago by viewModel.formaPago.collectAsState()
    val emisora by viewModel.emisora.collectAsState()
    val aeropuerto by viewModel.aeropuerto.collectAsState()

    // Título dinámico basado en si estamos creando o editando
    val screenTitle = if (carreraId == -1L) "Nueva Carrera" else "Editar Carrera"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = { // ✅ Usa `navigationIcon` para el botón de volver
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Volver atrás"
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = { // ✅ Ahora solo hay un `actions`
                    if (carreraId != -1L) {
                        IconButton(
                            onClick = {
                                viewModel.eliminarCarrera(carreraId) {
                                    onNavigateBack() // ✅ Ahora `onComplete` tiene un valor
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar Carrera",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },

        containerColor = backgroundColor
    ) { padding ->
        CarreraContent(
            padding = padding,
            taximetro = taximetro,
            importeReal = importeReal,
            propina = propina,
            formaPago = formaPago,
            emisora = emisora,
            aeropuerto = aeropuerto,
            primaryColor = primaryColor,
            greenColor = greenColor,
            onTaximetroChange = viewModel::updateTaximetro,
            onImporteRealChange = viewModel::updateImporteReal,
            onFormaPagoChange = viewModel::updateFormaPago,
            onEmisoraChange = viewModel::updateEmisora,
            onAeropuertoChange = viewModel::updateAeropuerto,
            onGuardarClick = {
                viewModel.guardarCarrera(turnoActual)
                onNavigateBack()
            }
        )
    }
}

/**
 * Contenido principal de la pantalla de registro de carrera.
 * Extraído como función separada para mejorar la legibilidad y mantenibilidad.
 */
@Composable
private fun CarreraContent(
    padding: PaddingValues,
    taximetro: String,
    importeReal: String,
    propina: String,
    formaPago: FormaPago,
    emisora: Boolean,
    aeropuerto: Boolean,
    primaryColor: Color,
    greenColor: Color,
    onTaximetroChange: (String) -> Unit,
    onImporteRealChange: (String) -> Unit,
    onFormaPagoChange: (FormaPago) -> Unit,
    onEmisoraChange: (Boolean) -> Unit,
    onAeropuertoChange: (Boolean) -> Unit,
    onGuardarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Campos de entrada
            ImporteFields(
                taximetro = taximetro,
                importeReal = importeReal,
                onTaximetroChange = onTaximetroChange,
                onImporteRealChange = onImporteRealChange,
                primaryColor = primaryColor
            )

            // Propina (mostrar solo si es positiva)
            PropinaDisplay(propina = propina, greenColor = greenColor)

            // Opciones de forma de pago
            FormaPagoOptions(
                formaPago = formaPago,
                onFormaPagoChange = onFormaPagoChange,
                primaryColor = primaryColor
            )

            // Switches para Emisora y Aeropuerto
            OpcionesAdicionales(
                emisora = emisora,
                aeropuerto = aeropuerto,
                onEmisoraChange = onEmisoraChange,
                onAeropuertoChange = onAeropuertoChange,
                primaryColor = primaryColor
            )

            // Botón de Guardar
            Button(
                onClick = onGuardarClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                )
            ) {
                Icon(
                    Icons.Rounded.Save,
                    contentDescription = "Guardar",
                    tint = Color.White // Icono en blanco
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Guardar Carrera",
                    color = Color.White // Texto en blanco
                )
            }
        }
    }
}

@Composable
private fun NumPadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)), // Amarillo dorado
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

/**
 * Campos para ingresar el importe del taxímetro y el importe real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImporteFields(
    taximetro: String,
    importeReal: String,
    onTaximetroChange: (String) -> Unit,
    onImporteRealChange: (String) -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = taximetro,
            onValueChange = { newValue ->
                onTaximetroChange(newValue)
                onImporteRealChange(newValue) // Se sincroniza el valor
            },
            label = { Text("Taxím.") },
            leadingIcon = {
                Icon(Icons.Rounded.Payments, contentDescription = "Taxímetro")
            },
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Campo para ingresar el importe del taxímetro" },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
            )
        )

        OutlinedTextField(
            value = importeReal,
            onValueChange = onImporteRealChange,
            label = { Text("Importe") },
            leadingIcon = {
                Icon(Icons.Rounded.Euro, contentDescription = "Importe")
            },
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Campo para ingresar el importe real cobrado" },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
            )
        )
    }
}

/**
 * Muestra la propina calculada si es mayor que cero.
 */
@Composable
private fun PropinaDisplay(propina: String, greenColor: Color) {
    val propinaValue = propina.toDoubleOrNull() ?: 0.0
    if (propinaValue > 0) {
        Text(
            text = "Propina: ${String.format("%.2f€", propinaValue)}",
            color = greenColor,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Opciones para seleccionar la forma de pago.
 */
@Composable
private fun FormaPagoOptions(
    formaPago: FormaPago,
    onFormaPagoChange: (FormaPago) -> Unit,
    primaryColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "Forma de Pago",
            style = MaterialTheme.typography.titleMedium,
            color = primaryColor,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Primera fila: Bizum y Vales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Opción Bizum
            FormaPagoChip(
                selected = formaPago == FormaPago.BIZUM,
                onClick = { onFormaPagoChange(FormaPago.BIZUM) },
                label = "Bizum",
                icon = { 
                    Icon(
                        imageVector = Icons.Rounded.PhoneAndroid,
                        contentDescription = "Bizum"
                    )
                },
                modifier = Modifier.weight(1f),
                selectedColor = MaterialTheme.colorScheme.secondaryContainer
            )

            // Opción Vales
            FormaPagoChip(
                selected = formaPago == FormaPago.VALES,
                onClick = { onFormaPagoChange(FormaPago.VALES) },
                label = "Vales",
                icon = { 
                    Icon(
                        imageVector = Icons.Rounded.Redeem,
                        contentDescription = "Vales"
                    )
                },
                modifier = Modifier.weight(1f),
                selectedColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Segunda fila: Efectivo y Tarjeta
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Opción Efectivo
            FormaPagoChip(
                selected = formaPago == FormaPago.EFECTIVO,
                onClick = { onFormaPagoChange(FormaPago.EFECTIVO) },
                label = "Efectivo",
                icon = {
                    Image(
                        painter = painterResource(R.drawable.monedasbuenas),
                        contentDescription = "Monedas",
                        modifier = Modifier.size(30.dp)
                    )
                },
                modifier = Modifier.weight(1f),
                selectedColor = MaterialTheme.colorScheme.secondaryContainer
            )

            // Opción Tarjeta
            FormaPagoChip(
                selected = formaPago == FormaPago.TARJETA,
                onClick = { onFormaPagoChange(FormaPago.TARJETA) },
                label = "Tarjeta",
                icon = { 
                    Icon(
                        imageVector = Icons.Rounded.CreditCard,
                        contentDescription = "Tarjeta"
                    )
                },
                modifier = Modifier.weight(1f),
                selectedColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }
    }
}

/**
 * Chip para seleccionar una forma de pago.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormaPagoChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    ElevatedFilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = icon,
        modifier = modifier.semantics {
            contentDescription = "Opción de pago: $label"
        },
        colors = FilterChipDefaults.elevatedFilterChipColors(
            selectedContainerColor = selectedColor
        )
    )
}

/**
 * Opciones adicionales: Emisora y Aeropuerto.
 */
@Composable
private fun OpcionesAdicionales(
    emisora: Boolean,
    aeropuerto: Boolean,
    onEmisoraChange: (Boolean) -> Unit,
    onAeropuertoChange: (Boolean) -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Grupo de Emisora (Ícono + Switch)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.svgviewer_output__6_),
                contentDescription = "Emisora",
                modifier = Modifier.size(30.dp)
            )
            Switch(
                checked = emisora,
                onCheckedChange = onEmisoraChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black, // Color del botón deslizante cuando está activo
                    checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer, // Color del fondo cuando está activo
                )
            )
        }

        Spacer(modifier = Modifier.width(42.dp)) // Espacio entre los dos grupos

        // Grupo de Aeropuerto (Ícono + Switch)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.svgviewer_output__3_),
                contentDescription = "Aeropuerto",
                modifier = Modifier.size(32.dp)
            )
            Switch(
                checked = aeropuerto,
                onCheckedChange = onAeropuertoChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black, // Color del botón deslizante cuando está activo
                    checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroCarreraScreenPreview() {
    TaxiFlashTheme {
        RegistroCarreraScreen(
            turnoActual = "Turno 1",
            onNavigateBack = {}
        )
    }
}
