package com.taxiflash.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.screens.Recordatorio
import com.taxiflash.ui.screens.TipoRecordatorio
import com.taxiflash.ui.util.NotificationUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

// DataStore para recordatorios
private val Context.recordatoriosDataStore by preferencesDataStore(name = "recordatorios")
private val RECORDATORIOS_KEY = stringPreferencesKey("recordatorios_lista")

/**
 * ViewModel para la pantalla de Recordatorios
 */
class RecordatoriosViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val dataStore: DataStore<Preferences> = context.recordatoriosDataStore
    
    // Estado para la lista de recordatorios
    private val _recordatorios = MutableStateFlow<List<Recordatorio>>(emptyList())
    val recordatorios: StateFlow<List<Recordatorio>> = _recordatorios
    
    // Estado para el recordatorio seleccionado (para edición)
    private val _recordatorioSeleccionado = MutableStateFlow<Recordatorio?>(null)
    val recordatorioSeleccionado: StateFlow<Recordatorio?> = _recordatorioSeleccionado
    
    /**
     * Carga los recordatorios desde DataStore
     */
    fun cargarRecordatorios() {
        viewModelScope.launch {
            try {
                dataStore.data.map { preferences ->
                    val recordatoriosJson = preferences[RECORDATORIOS_KEY] ?: "[]"
                    parseRecordatoriosFromJson(recordatoriosJson)
                }.collect { listaRecordatorios ->
                    _recordatorios.value = listaRecordatorios
                    Log.d("RecordatoriosViewModel", "Recordatorios cargados: ${listaRecordatorios.size}")
                }
            } catch (e: Exception) {
                Log.e("RecordatoriosViewModel", "Error al cargar recordatorios", e)
                _recordatorios.value = emptyList()
            }
        }
    }
    
    /**
     * Guarda un recordatorio
     */
    fun guardarRecordatorio(tipo: TipoRecordatorio, titulo: String, fecha: Long, descripcion: String, horaAviso: String, horaAviso2: String = "") {
        viewModelScope.launch {
            try {
                // Si hay un recordatorio seleccionado, lo actualizamos
                val recordatorioActual = _recordatorioSeleccionado.value
                
                // Si estamos editando, cancelar la notificación anterior
                recordatorioActual?.let {
                    NotificationUtil.cancelarNotificacion(context, it.id)
                    // Cancelar la segunda notificación si existe
                    if (it.horaAviso2.isNotEmpty()) {
                        NotificationUtil.cancelarNotificacion(context, "${it.id}_2")
                    }
                }
                
                val nuevoRecordatorio = if (recordatorioActual != null) {
                    recordatorioActual.copy(
                        tipo = tipo,
                        titulo = titulo,
                        fecha = fecha,
                        descripcion = descripcion,
                        horaAviso = horaAviso,
                        horaAviso2 = horaAviso2
                    )
                } else {
                    Recordatorio(
                        id = UUID.randomUUID().toString(),
                        tipo = tipo,
                        titulo = titulo,
                        fecha = fecha,
                        descripcion = descripcion,
                        horaAviso = horaAviso,
                        horaAviso2 = horaAviso2
                    )
                }
                
                // Actualizar la lista
                val listaActualizada = if (recordatorioActual != null) {
                    _recordatorios.value.map { 
                        if (it.id == recordatorioActual.id) nuevoRecordatorio else it 
                    }
                } else {
                    _recordatorios.value + nuevoRecordatorio
                }
                
                // Guardar en DataStore
                guardarRecordatoriosEnDataStore(listaActualizada)
                
                // Actualizar el estado
                _recordatorios.value = listaActualizada
                _recordatorioSeleccionado.value = null
                
                // Programar notificación para el recordatorio
                NotificationUtil.programarNotificacion(context, nuevoRecordatorio)
                
                Log.d("RecordatoriosViewModel", "Recordatorio guardado: $titulo")
            } catch (e: Exception) {
                Log.e("RecordatoriosViewModel", "Error al guardar recordatorio", e)
            }
        }
    }
    
    /**
     * Elimina un recordatorio
     */
    fun eliminarRecordatorio(id: String) {
        viewModelScope.launch {
            try {
                // Cancelar notificación asociada
                NotificationUtil.cancelarNotificacion(context, id)
                
                // Filtrar la lista
                val listaActualizada = _recordatorios.value.filter { it.id != id }
                
                // Guardar en DataStore
                guardarRecordatoriosEnDataStore(listaActualizada)
                
                // Actualizar el estado
                _recordatorios.value = listaActualizada
                
                // Si el recordatorio eliminado era el seleccionado, limpiar selección
                if (_recordatorioSeleccionado.value?.id == id) {
                    _recordatorioSeleccionado.value = null
                }
                
                Log.d("RecordatoriosViewModel", "Recordatorio eliminado: $id")
            } catch (e: Exception) {
                Log.e("RecordatoriosViewModel", "Error al eliminar recordatorio", e)
            }
        }
    }
    
    /**
     * Selecciona un recordatorio para edición
     */
    fun seleccionarRecordatorio(recordatorio: Recordatorio) {
        _recordatorioSeleccionado.value = recordatorio
    }
    
    /**
     * Limpia la selección actual
     */
    fun limpiarSeleccion() {
        _recordatorioSeleccionado.value = null
    }
    
    /**
     * Guarda la lista de recordatorios en DataStore
     */
    private suspend fun guardarRecordatoriosEnDataStore(recordatorios: List<Recordatorio>) {
        val jsonArray = JSONArray()
        
        recordatorios.forEach { recordatorio ->
            val jsonObject = JSONObject().apply {
                put("id", recordatorio.id)
                put("tipo", recordatorio.tipo.name)
                put("titulo", recordatorio.titulo)
                put("fecha", recordatorio.fecha)
                put("descripcion", recordatorio.descripcion)
                put("horaAviso", recordatorio.horaAviso)
                put("horaAviso2", recordatorio.horaAviso2)
            }
            jsonArray.put(jsonObject)
        }
        
        dataStore.edit { preferences ->
            preferences[RECORDATORIOS_KEY] = jsonArray.toString()
        }
    }
    
    /**
     * Parsea la lista de recordatorios desde JSON
     */
    private fun parseRecordatoriosFromJson(json: String): List<Recordatorio> {
        if (json.isEmpty()) return emptyList()
        
        val recordatorios = mutableListOf<Recordatorio>()
        val jsonArray = JSONArray(json)
        
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            
            val recordatorio = Recordatorio(
                id = jsonObject.getString("id"),
                tipo = TipoRecordatorio.valueOf(jsonObject.getString("tipo")),
                titulo = jsonObject.getString("titulo"),
                fecha = jsonObject.getLong("fecha"),
                descripcion = jsonObject.optString("descripcion", ""),
                horaAviso = jsonObject.optString("horaAviso", ""),
                horaAviso2 = jsonObject.optString("horaAviso2", "")
            )
            
            recordatorios.add(recordatorio)
        }
        
        return recordatorios
    }
} 