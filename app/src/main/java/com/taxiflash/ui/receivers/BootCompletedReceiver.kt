package com.taxiflash.ui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.taxiflash.ui.screens.Recordatorio
import com.taxiflash.ui.screens.TipoRecordatorio
import com.taxiflash.ui.util.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray

// DataStore para recordatorios
private val Context.recordatoriosDataStore by preferencesDataStore(name = "recordatorios")
private val RECORDATORIOS_KEY = stringPreferencesKey("recordatorios_lista")

/**
 * BroadcastReceiver que se ejecuta cuando el dispositivo se reinicia.
 * Su función es restaurar todas las notificaciones programadas.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Usar una coroutine para operaciones asincrónicas
            val scope = CoroutineScope(Dispatchers.IO)
            
            scope.launch {
                try {
                    // Cargar recordatorios desde DataStore
                    val recordatorios = context.recordatoriosDataStore.data
                        .map { preferences ->
                            val recordatoriosJson = preferences[RECORDATORIOS_KEY] ?: "[]"
                            parseRecordatoriosFromJson(recordatoriosJson)
                        }
                        .first()
                    
                    // Reprogramar notificaciones para cada recordatorio
                    recordatorios.forEach { recordatorio ->
                        NotificationUtil.programarNotificacion(context, recordatorio)
                    }
                } catch (e: Exception) {
                    // Manejar errores silenciosamente
                    e.printStackTrace()
                }
            }
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
                horaAviso = jsonObject.optString("horaAviso", "")
            )
            
            recordatorios.add(recordatorio)
        }
        
        return recordatorios
    }
} 