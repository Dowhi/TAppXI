package com.taxiflash.ui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.taxiflash.ui.util.NotificationUtil

/**
 * BroadcastReceiver para recibir alarmas y mostrar notificaciones de recordatorios.
 */
class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        // Extraer información del recordatorio
        val recordatorioId = intent.getStringExtra("RECORDATORIO_ID") ?: return
        val titulo = intent.getStringExtra("RECORDATORIO_TITULO") ?: "Recordatorio"
        val descripcion = intent.getStringExtra("RECORDATORIO_DESCRIPCION") ?: ""
        
        // Mostrar la notificación
        NotificationUtil.mostrarNotificacion(context, recordatorioId, titulo, descripcion)
    }
} 