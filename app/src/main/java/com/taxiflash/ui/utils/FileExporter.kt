package com.taxiflash.ui.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Clase utilitaria para manejar la exportación de archivos
 */
object FileExporter {
    private const val TAG = "FileExporter"
    
    /**
     * Habilita el uso de datos móviles y WiFi para descargas
     * 
     * @param context Contexto de la aplicación
     */
    private fun enableAllNetworkConnections(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                
                val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build()
                
                try {
                    connectivityManager.requestNetwork(networkRequest, android.net.ConnectivityManager.NetworkCallback())
                    Log.d(TAG, "Solicitud de red enviada para habilitar datos móviles y WiFi")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al solicitar red: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al habilitar conexiones de red: ${e.message}", e)
        }
    }
    
    /**
     * Comparte un archivo con otras aplicaciones
     *
     * @param context Contexto de la aplicación
     * @param file Archivo a compartir
     * @param mimeType Tipo MIME del archivo
     * @param title Título para el selector de aplicaciones
     * @return true si se pudo iniciar el intent de compartir, false en caso contrario
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String): Boolean {
        // Habilitar todas las conexiones de red
        enableAllNetworkConnections(context)
        
        return try {
            // Obtener URI del archivo usando FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Log.d(TAG, "URI generado para compartir: $uri")
            
            // Crear y lanzar el intent de compartir
            shareUri(context, uri, mimeType, title)
        } catch (e: Exception) {
            Log.e(TAG, "Error al compartir archivo: ${e.message}", e)
            Toast.makeText(context, "Error al compartir archivo: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
    
    /**
     * Comparte un URI con otras aplicaciones
     *
     * @param context Contexto de la aplicación
     * @param uri URI a compartir
     * @param mimeType Tipo MIME del archivo
     * @param title Título para el selector de aplicaciones
     * @return true si se pudo iniciar el intent de compartir, false en caso contrario
     */
    fun shareUri(context: Context, uri: Uri, mimeType: String, title: String): Boolean {
        // Habilitar todas las conexiones de red
        enableAllNetworkConnections(context)
        
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uri)
                type = mimeType
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Permitir el uso de datos móviles
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                }
            }
            
            val chooser = Intent.createChooser(intent, title)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(chooser)
            Log.d(TAG, "Intent de compartir iniciado correctamente")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar intent de compartir: ${e.message}", e)
            Toast.makeText(context, "No se pudo abrir el selector: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
    
    /**
     * Comparte un archivo Excel
     *
     * @param context Contexto de la aplicación 
     * @param uri URI del archivo Excel
     * @return true si se pudo iniciar el intent de compartir, false en caso contrario
     */
    fun shareExcel(context: Context, uri: Uri): Boolean {
        // Habilitar todas las conexiones de red
        enableAllNetworkConnections(context)
        
        return shareUri(
            context, 
            uri, 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "Compartir archivo Excel"
        )
    }
} 