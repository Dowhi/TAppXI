package com.taxiflash.ui.workers

import android.content.Context
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ExportDatabaseWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ExportDatabaseWorker"
        private const val WORK_NAME = "ExportDatabaseWork"
        private const val DB_NAME = "taxiflash-db"

        fun schedulePeriodicExport(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Permite WiFi y datos móviles
                .build()

            val exportWorkRequest = PeriodicWorkRequestBuilder<ExportDatabaseWorker>(
                1, TimeUnit.DAYS // Programar la exportación diaria
            )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                exportWorkRequest
            )

            Log.d(TAG, "Exportación de base de datos programada")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando exportación de base de datos")
            
            // Obtener la fecha actual para el nombre del archivo
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val backupFileName = "taxiflash_backup_$timestamp.db"
            
            // Obtener la ruta de la base de datos
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                Log.e(TAG, "Base de datos no encontrada")
                return@withContext Result.failure()
            }

            // Crear directorio de respaldo si no existe
            val backupDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "TaxiFlash/Backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Crear archivo de respaldo
            val backupFile = File(backupDir, backupFileName)
            
            // Copiar la base de datos
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Añadir el archivo al MediaStore para que sea visible
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, backupFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/x-sqlite3")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/TaxiFlash/Backups")
            }

            context.contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                contentValues
            )

            Log.d(TAG, "Exportación completada exitosamente: ${backupFile.absolutePath}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la exportación: ${e.message}", e)
            Result.retry()
        }
    }
} 