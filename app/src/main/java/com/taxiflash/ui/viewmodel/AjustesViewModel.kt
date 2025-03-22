package com.taxiflash.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.AjustesDataStore
import com.taxiflash.ui.data.AjustesDataStore.dataStore
import com.taxiflash.ui.utils.DatabaseExportUtils
import com.taxiflash.ui.utils.FileExporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.taxiflash.ui.data.TaxiFlashDatabase

/**
 * ViewModel para la pantalla de Ajustes
 */
class AjustesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    
    // Base de datos
    private val database = TaxiFlashDatabase.getDatabase(context)
    
    // Preferencias
    val temaOscuro: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[AjustesDataStore.THEME_KEY] ?: false }
    
    val notificaciones: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[AjustesDataStore.NOTIFICATIONS_KEY] ?: true }
    
    val formatoMoneda: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[AjustesDataStore.CURRENCY_FORMAT_KEY] ?: "€" }
    
    val formatoFecha: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[AjustesDataStore.DATE_FORMAT_KEY] ?: "DD/MM/YYYY" }
    
    val objetivoImporte: Flow<Double> = context.dataStore.data
        .map { preferences -> preferences[AjustesDataStore.OBJETIVO_IMPORTE_KEY] ?: 100.0 }
    
    // Estado para la exportación/importación
    private val _exportandoDatos = MutableStateFlow(false)
    val exportandoDatos: StateFlow<Boolean> = _exportandoDatos.asStateFlow()
    
    private val _importandoDatos = MutableStateFlow(false)
    val importandoDatos: StateFlow<Boolean> = _importandoDatos.asStateFlow()
    
    private val _exportacionExitosa = MutableStateFlow<Uri?>(null)
    val exportacionExitosa: StateFlow<Uri?> = _exportacionExitosa.asStateFlow()
    
    private val _importacionExitosa = MutableStateFlow(false)
    val importacionExitosa: StateFlow<Boolean> = _importacionExitosa.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Métodos para cambiar preferencias
    fun setTemaOscuro(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[AjustesDataStore.THEME_KEY] = value
            }
        }
    }
    
    fun setNotificaciones(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[AjustesDataStore.NOTIFICATIONS_KEY] = value
            }
        }
    }
    
    fun setFormatoMoneda(value: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[AjustesDataStore.CURRENCY_FORMAT_KEY] = value
            }
        }
    }
    
    fun setFormatoFecha(value: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[AjustesDataStore.DATE_FORMAT_KEY] = value
            }
        }
    }
    
    fun setObjetivoImporte(value: Double) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[AjustesDataStore.OBJETIVO_IMPORTE_KEY] = value
            }
        }
    }
    
    // Método para borrar todos los datos
    fun borrarTodosLosDatos() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Borrar datos de la base de datos
                    database.clearAllTables()
                }
                Log.d("AjustesViewModel", "Todos los datos han sido eliminados")
            } catch (e: Exception) {
                Log.e("AjustesViewModel", "Error al borrar datos", e)
            }
        }
    }
    
    /**
     * Exporta toda la base de datos a un archivo Excel
     */
    fun exportarBaseDatos() {
        viewModelScope.launch {
            try {
                _exportandoDatos.value = true
                _error.value = null
                
                val uri = DatabaseExportUtils.exportarBaseDatos(context)
                if (uri != null) {
                    _exportacionExitosa.value = uri
                    Log.d("AjustesViewModel", "Base de datos exportada correctamente: $uri")
                } else {
                    _error.value = "No se pudo exportar la base de datos"
                    Log.e("AjustesViewModel", "Error al exportar la base de datos")
                }
            } catch (e: Exception) {
                _error.value = "Error al exportar: ${e.message}"
                Log.e("AjustesViewModel", "Error al exportar la base de datos", e)
            } finally {
                _exportandoDatos.value = false
            }
        }
    }
    
    /**
     * Importa datos desde un archivo Excel a la base de datos
     * 
     * @param uri URI del archivo a importar
     */
    fun importarBaseDatos(uri: Uri) {
        viewModelScope.launch {
            try {
                _importandoDatos.value = true
                _error.value = null
                
                val resultado = DatabaseExportUtils.importarBaseDatos(context, uri)
                _importacionExitosa.value = resultado
                
                if (resultado) {
                    Log.d("AjustesViewModel", "Base de datos importada correctamente")
                } else {
                    _error.value = "No se pudo importar la base de datos"
                    Log.e("AjustesViewModel", "Error al importar la base de datos")
                }
            } catch (e: Exception) {
                _error.value = "Error al importar: ${e.message}"
                Log.e("AjustesViewModel", "Error al importar la base de datos", e)
            } finally {
                _importandoDatos.value = false
            }
        }
    }
    
    /**
     * Obtiene el intent para seleccionar un archivo para importar
     */
    fun obtenerIntentImportacion(): Intent {
        return DatabaseExportUtils.obtenerIntentImportacion()
    }
    
    /**
     * Exporta el archivo a Google Drive
     * 
     * @param uri URI del archivo a exportar
     */
    fun exportarAGoogleDrive(uri: Uri) {
        try {
            FileExporter.shareExcel(context, uri)
        } catch (e: Exception) {
            Log.e("AjustesViewModel", "Error al compartir archivo", e)
            _error.value = "Error al compartir: ${e.message}"
        }
    }
    
    /**
     * Reinicia el estado de exportación/importación
     */
    fun reiniciarEstado() {
        _exportacionExitosa.value = null
        _importacionExitosa.value = false
        _error.value = null
    }
} 