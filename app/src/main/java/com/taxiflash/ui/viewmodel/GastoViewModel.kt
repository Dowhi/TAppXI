package com.taxiflash.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.Gasto
import com.taxiflash.ui.data.GastoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extensión para DataStore
val Context.proveedoresDataStore: DataStore<Preferences> by preferencesDataStore(name = "proveedores")

@HiltViewModel
class GastoViewModel @Inject constructor(
    private val gastoDao: GastoDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _gastos = MutableStateFlow<List<Gasto>>(emptyList())
    val gastos: StateFlow<List<Gasto>> = _gastos.asStateFlow()
    
    private val _proveedores = MutableStateFlow<List<String>>(emptyList())
    val proveedores: StateFlow<List<String>> = _proveedores.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Clave para almacenar la lista de proveedores en DataStore
    private val PROVEEDORES_KEY = stringPreferencesKey("proveedores_list")

    init {
        cargarGastos()
        cargarProveedores()
    }

    private fun cargarGastos() {
        viewModelScope.launch {
            gastoDao.getAllGastos().collect { listaGastos ->
                _gastos.value = listaGastos
            }
        }
    }
    
    private fun cargarProveedores() {
        viewModelScope.launch {
            context.proveedoresDataStore.data.map { preferences ->
                preferences[PROVEEDORES_KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            }.collect { listaProveedores ->
                _proveedores.value = listaProveedores
            }
        }
    }

    fun guardarGasto(
        factura: String,
        proveedor: String,
        importeTotal: Double,
        iva: Double,
        kilometros: Int,
        tipoGasto: String,
        tipoGastoEspecifico: String,
        descripcion: String
    ) {
        val fechaActual = dateFormat.format(Calendar.getInstance().time)
        
        val gasto = Gasto(
            factura = factura,
            proveedor = proveedor,
            fecha = fechaActual,
            importeTotal = importeTotal,
            iva = iva,
            kilometros = kilometros,
            tipoGasto = tipoGasto,
            tipoGastoEspecifico = tipoGastoEspecifico,
            descripcion = descripcion
        )

        viewModelScope.launch {
            gastoDao.insertGasto(gasto)
            
            // Si el proveedor no está en la lista, añadirlo
            if (proveedor.isNotBlank() && !_proveedores.value.contains(proveedor)) {
                guardarProveedor(proveedor)
            }
        }
    }
    
    fun guardarProveedor(nombreProveedor: String) {
        if (nombreProveedor.isBlank()) return
        
        viewModelScope.launch {
            val proveedoresActuales = _proveedores.value.toMutableList()
            
            // Verificar si el proveedor ya existe
            if (!proveedoresActuales.contains(nombreProveedor)) {
                proveedoresActuales.add(nombreProveedor)
                
                // Ordenar alfabéticamente
                proveedoresActuales.sort()
                
                // Actualizar el estado
                _proveedores.value = proveedoresActuales
                
                // Guardar en DataStore
                context.proveedoresDataStore.edit { preferences ->
                    preferences[PROVEEDORES_KEY] = proveedoresActuales.joinToString(",")
                }
            }
        }
    }
    
    fun eliminarProveedor(nombreProveedor: String) {
        viewModelScope.launch {
            val proveedoresActuales = _proveedores.value.toMutableList()
            
            if (proveedoresActuales.remove(nombreProveedor)) {
                // Actualizar el estado
                _proveedores.value = proveedoresActuales
                
                // Guardar en DataStore
                context.proveedoresDataStore.edit { preferences ->
                    preferences[PROVEEDORES_KEY] = proveedoresActuales.joinToString(",")
                }
            }
        }
    }

    fun eliminarGasto(gasto: Gasto) {
        viewModelScope.launch {
            gastoDao.deleteGasto(gasto)
        }
    }

    fun obtenerGastosPorFecha(fechaInicio: String, fechaFin: String) {
        viewModelScope.launch {
            gastoDao.getGastosEntreFechas(fechaInicio, fechaFin).collect { listaGastos ->
                _gastos.value = listaGastos
            }
        }
    }
}

