package com.taxiflash.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase de aplicación principal para TaxiFlash.
 * Se encarga de la configuración global de la app, incluyendo los canales de notificación.
 */
@HiltAndroidApp
class TaxiFlashApplication : Application() {

    companion object {
        // Identificadores de canales de notificación
        const val CHANNEL_RECORDATORIOS_ID = "recordatorios_channel"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Crear canales de notificación (requerido para Android 8.0+)
        createNotificationChannels()
    }
    
    /**
     * Crea los canales de notificación necesarios para la aplicación.
     * Esta configuración es requerida para Android 8.0 (API 26) y versiones superiores.
     */
    private fun createNotificationChannels() {
        // Sólo crear canales en Android 8.0 (API 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para recordatorios
            val recordatoriosChannel = NotificationChannel(
                CHANNEL_RECORDATORIOS_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para recordatorios programados"
                enableVibration(true)
                enableLights(true)
            }
            
            // Registrar el canal con el sistema
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(recordatoriosChannel)
        }
    }
} 