package com.taxiflash.ui.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.taxiflash.ui.R
import com.taxiflash.ui.TaxiFlashApplication
import com.taxiflash.ui.MainActivity
import com.taxiflash.ui.receivers.NotificationReceiver
import com.taxiflash.ui.screens.Recordatorio
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Clase utilitaria para manejar notificaciones en la aplicación.
 */
object NotificationUtil {

    /**
     * Programa una notificación para un recordatorio.
     * 
     * @param context Contexto de la aplicación
     * @param recordatorio El recordatorio para el cual programar la notificación
     */
    fun programarNotificacion(context: Context, recordatorio: Recordatorio) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Crear un intent para el NotificationReceiver
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("RECORDATORIO_ID", recordatorio.id)
            putExtra("RECORDATORIO_TITULO", recordatorio.titulo)
            putExtra("RECORDATORIO_DESCRIPCION", recordatorio.descripcion)
        }
        
        // Crear un PendingIntent que será activado cuando sea hora de mostrar la notificación
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            recordatorio.id.hashCode(), // Usar el ID como request code para hacerlo único
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE 
            else 
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Calcular el tiempo para la notificación
        val calendar = Calendar.getInstance().apply {
            timeInMillis = recordatorio.fecha
            
            // Ajustar la hora según el formato HH:mm en horaAviso
            if (recordatorio.horaAviso.isNotEmpty()) {
                try {
                    val partes = recordatorio.horaAviso.split(":")
                    if (partes.size == 2) {
                        set(Calendar.HOUR_OF_DAY, partes[0].toInt())
                        set(Calendar.MINUTE, partes[1].toInt())
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                } catch (e: Exception) {
                    // Si hay un error en el formato, usar la hora actual
                    Log.e("NotificationUtil", "Error al parsear hora", e)
                }
            }
        }
        
        val timeInMillis = calendar.timeInMillis
        
        // Log para depuración
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        Log.d("NotificationUtil", "Programando notificación para: ${sdf.format(Date(timeInMillis))}")
        Log.d("NotificationUtil", "ID del recordatorio: ${recordatorio.id}")
        Log.d("NotificationUtil", "Título: ${recordatorio.titulo}")
        Log.d("NotificationUtil", "Hora de aviso: ${recordatorio.horaAviso}")
        
        // Programar la alarma
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    }
    
    /**
     * Cancela una notificación programada para un recordatorio.
     * 
     * @param context Contexto de la aplicación
     * @param recordatorioId ID del recordatorio cuya notificación se desea cancelar
     */
    fun cancelarNotificacion(context: Context, recordatorioId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        
        // Crear un PendingIntent con los mismos parámetros que el original
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            recordatorioId.hashCode(),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE 
            else 
                PendingIntent.FLAG_NO_CREATE
        )
        
        // Si existe el PendingIntent, cancelarlo
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        
        // También eliminar cualquier notificación ya mostrada
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(recordatorioId.hashCode())
    }
    
    /**
     * Muestra una notificación para un recordatorio.
     * Este método es llamado por el NotificationReceiver cuando se activa la alarma.
     * 
     * @param context Contexto de la aplicación
     * @param recordatorioId ID del recordatorio
     * @param titulo Título del recordatorio
     * @param descripcion Descripción del recordatorio
     */
    fun mostrarNotificacion(context: Context, recordatorioId: String, titulo: String, descripcion: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Convertir el ID a entero para usar en notificaciones
        val notificationId = recordatorioId.hashCode()
        
        // Crear un intent para cuando el usuario toque la notificación
        val intent = Intent(context, com.taxiflash.ui.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE 
            else 
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Construir la notificación
        val notification = NotificationCompat.Builder(context, TaxiFlashApplication.CHANNEL_RECORDATORIOS_ID)
            .setContentTitle(titulo)
            .setContentText(descripcion)
            .setSmallIcon(R.drawable.ic_notification) // Asegúrate de tener este icono en tu proyecto
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Mostrar la notificación
        notificationManager.notify(notificationId, notification)
    }
} 