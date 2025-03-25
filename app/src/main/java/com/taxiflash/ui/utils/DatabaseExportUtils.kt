package com.taxiflash.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.taxiflash.ui.data.TaxiFlashDatabase
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.FormaPago
import com.taxiflash.ui.data.Gasto
import com.taxiflash.ui.data.TipoGasto
import com.taxiflash.ui.data.Turno
import com.taxiflash.ui.data.OtrosIngresos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.first

/**
 * Utilidad para exportar e importar la base de datos completa
 */
object DatabaseExportUtils {

    private const val TAG = "DatabaseExportUtils"

    /**
     * Exporta toda la base de datos a un archivo Excel
     * 
     * @param context Contexto de la aplicación
     * @return Uri del archivo generado o null si ocurre un error
     */
    suspend fun exportarBaseDatos(context: Context): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                // Crear nombre de archivo con timestamp
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "taxiflash_database_$timeStamp.xlsx"
                
                Log.d(TAG, "Iniciando exportación de base de datos: $fileName")
                
                // Asegurar que el directorio existe
                val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                if (directory == null) {
                    Log.e(TAG, "Error: No se pudo acceder al directorio de documentos")
                    return@withContext null
                }
                
                if (!directory.exists()) {
                    directory.mkdirs()
                    Log.d(TAG, "Directorio creado: ${directory.absolutePath}")
                }
                
                // Crear archivo en la carpeta de documentos de la app
                val file = File(directory, fileName)
                Log.d(TAG, "Creando archivo en: ${file.absolutePath}")
                
                // Obtener instancia de la base de datos
                val database = TaxiFlashDatabase.getDatabase(context)
                
                // Crear workbook de Excel
                val workbook = XSSFWorkbook()
                Log.d(TAG, "Workbook de Excel creado correctamente")
                
                // Crear hojas para cada entidad
                try {
                    createTurnosSheet(workbook, database)
                    createCarrerasSheet(workbook, database)
                    createGastosSheet(workbook, database)
                    createOtrosIngresosSheet(workbook, database)
                    Log.d(TAG, "Todas las hojas de Excel creadas correctamente")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al crear hojas de Excel: ${e.message}", e)
                    throw e
                }
                
                // Guardar el archivo
                try {
                    FileOutputStream(file).use { outputStream ->
                        workbook.write(outputStream)
                        outputStream.flush()
                    }
                    workbook.close()
                    Log.d(TAG, "Archivo Excel guardado correctamente: ${file.absolutePath}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al guardar el archivo Excel: ${e.message}", e)
                    throw e
                }
                
                // Devolver la URI del archivo
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    Log.d(TAG, "URI generado correctamente: $uri")
                    uri
                } catch (e: Exception) {
                    Log.e(TAG, "Error al generar URI para el archivo: ${e.message}", e)
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error general al exportar la base de datos: ${e.message}", e)
                null
            }
        }
    }
    
    /**
     * Importa datos desde un archivo Excel a la base de datos
     * 
     * @param context Contexto de la aplicación
     * @param uri URI del archivo a importar
     * @return true si la importación fue exitosa, false en caso contrario
     */
    suspend fun importarBaseDatos(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Obtener instancia de la base de datos
                val database = TaxiFlashDatabase.getDatabase(context)
                
                // Abrir el archivo Excel
                val inputStream = context.contentResolver.openInputStream(uri)
                val workbook = XSSFWorkbook(inputStream)
                
                // Importar datos de cada hoja
                importTurnos(workbook, database)
                importCarreras(workbook, database)
                importGastos(workbook, database)
                importOtrosIngresos(workbook, database)
                
                workbook.close()
                inputStream?.close()
                
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error al importar la base de datos", e)
                false
            }
        }
    }
    
    // Métodos privados para crear las hojas del Excel
    
    private suspend fun createTurnosSheet(workbook: XSSFWorkbook, database: TaxiFlashDatabase) {
        val sheet = workbook.createSheet("Turnos")
        val turnos = database.turnoDao().getAllTurnos()
        
        // Crear cabecera
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("ID", "Número", "Fecha", "Hora Inicio", "Hora Fin", "KM Inicio", "KM Fin", "ID Turno", "Activo")
        for (i in headers.indices) {
            headerRow.createCell(i).setCellValue(headers[i])
        }
        
        // Llenar datos
        for (i in turnos.indices) {
            val turno = turnos[i]
            val row = sheet.createRow(i + 1)
            row.createCell(0).setCellValue(turno.id.toString())
            row.createCell(1).setCellValue(turno.numeroTurno.toString())
            row.createCell(2).setCellValue(turno.fecha)
            row.createCell(3).setCellValue(turno.horaInicio)
            row.createCell(4).setCellValue(turno.horaFin)
            row.createCell(5).setCellValue(turno.kmInicio.toString())
            row.createCell(6).setCellValue(turno.kmFin.toString())
            row.createCell(7).setCellValue(turno.idTurno)
            row.createCell(8).setCellValue(turno.activo.toString())
        }
    }
    
    private suspend fun createCarrerasSheet(workbook: XSSFWorkbook, database: TaxiFlashDatabase) {
        val sheet = workbook.createSheet("Carreras")
        val carreras = database.carreraDao().getAllCarreras().first()
        
        // Crear cabecera
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("ID", "Fecha", "Hora", "Taxímetro", "Importe Real", 
                             "Propina", "Forma Pago", "Emisora", "Aeropuerto", "Turno")
        for (i in headers.indices) {
            headerRow.createCell(i).setCellValue(headers[i])
        }
        
        // Llenar datos
        for (i in carreras.indices) {
            val carrera = carreras[i]
            val row = sheet.createRow(i + 1)
            row.createCell(0).setCellValue(carrera.id.toString())
            row.createCell(1).setCellValue(carrera.fecha)
            row.createCell(2).setCellValue(carrera.hora)
            row.createCell(3).setCellValue(carrera.taximetro.toString())
            row.createCell(4).setCellValue(carrera.importeReal.toString())
            row.createCell(5).setCellValue(carrera.propina.toString())
            row.createCell(6).setCellValue(carrera.formaPago.toString())
            row.createCell(7).setCellValue(carrera.emisora.toString())
            row.createCell(8).setCellValue(carrera.aeropuerto.toString())
            row.createCell(9).setCellValue(carrera.turno)
        }
    }
    
    private suspend fun createGastosSheet(workbook: XSSFWorkbook, database: TaxiFlashDatabase) {
        val sheet = workbook.createSheet("Gastos")
        val gastos = database.gastoDao().getAllGastos().first()
        
        // Crear cabecera
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("ID", "Factura", "Proveedor", "Fecha", "Importe Total", 
                             "IVA", "Kilometros", "Tipo Gasto", "Tipo Gasto Específico", "Descripción")
        for (i in headers.indices) {
            headerRow.createCell(i).setCellValue(headers[i])
        }
        
        // Llenar datos
        for (i in gastos.indices) {
            val gasto = gastos[i]
            val row = sheet.createRow(i + 1)
            row.createCell(0).setCellValue(gasto.id.toString())
            row.createCell(1).setCellValue(gasto.factura)
            row.createCell(2).setCellValue(gasto.proveedor)
            row.createCell(3).setCellValue(gasto.fecha)
            row.createCell(4).setCellValue(gasto.importeTotal.toString())
            row.createCell(5).setCellValue(gasto.iva.toString())
            row.createCell(6).setCellValue(gasto.kilometros?.toString() ?: "")
            row.createCell(7).setCellValue(gasto.tipoGasto)
            row.createCell(8).setCellValue(gasto.tipoGastoEspecifico)
            row.createCell(9).setCellValue(gasto.descripcion ?: "")
        }
    }
    
    private suspend fun createOtrosIngresosSheet(workbook: XSSFWorkbook, database: TaxiFlashDatabase) {
        val sheet = workbook.createSheet("OtrosIngresos")
        val otrosIngresos = database.otrosIngresosDao().getAllOtrosIngresos().first()
        
        // Crear cabecera
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("ID", "Concepto", "Fecha", "Importe", "Descripción", "Notas")
        for (i in headers.indices) {
            headerRow.createCell(i).setCellValue(headers[i])
        }
        
        // Llenar datos
        for (i in otrosIngresos.indices) {
            val ingreso = otrosIngresos[i]
            val row = sheet.createRow(i + 1)
            row.createCell(0).setCellValue(ingreso.id.toString())
            row.createCell(1).setCellValue(ingreso.concepto)
            row.createCell(2).setCellValue(ingreso.fecha)
            row.createCell(3).setCellValue(ingreso.importe.toString())
            row.createCell(4).setCellValue(ingreso.descripcion ?: "")
            row.createCell(5).setCellValue(ingreso.notas ?: "")
        }
    }
    
    // Métodos para importar desde Excel
    
    private suspend fun importTurnos(workbook: XSSFWorkbook, database: TaxiFlashDatabase) {
        val sheet = workbook.getSheet("Turnos") ?: return
        val turnoDao = database.turnoDao()
        
        // Borrar datos existentes
        turnoDao.deleteAllTurnos()
        
        // Importar nuevos datos
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            
            val turno = Turno(
                id = row.getCell(0)?.toString()?.toLongOrNull() ?: 0L,
                numeroTurno = row.getCell(1)?.toString()?.toIntOrNull() ?: 0,
                fecha = row.getCell(2)?.toString() ?: "",
                horaInicio = row.getCell(3)?.toString() ?: "",
                horaFin = row.getCell(4)?.toString() ?: "",
                kmInicio = row.getCell(5)?.toString()?.toIntOrNull() ?: 0,
                kmFin = row.getCell(6)?.toString()?.toIntOrNull() ?: 0,
                idTurno = row.getCell(7)?.toString() ?: "",
                activo = row.getCell(8)?.toString()?.toBoolean() ?: false
            )
            
            turnoDao.insertTurno(turno)
        }
    }
    
    private suspend fun importCarreras(workbook: XSSFWorkbook, database: TaxiFlashDatabase) {
        val sheet = workbook.getSheet("Carreras") ?: return
        val carreraDao = database.carreraDao()
        
        // Borrar datos existentes - implementamos nuestra propia función ya que no está en el DAO
        val carrerasList = carreraDao.getAllCarrerasList()
        for (carrera in carrerasList) {
            carreraDao.deleteCarrera(carrera)
        }
        
        // Importar nuevos datos
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            
            val carrera = Carrera(
                id = row.getCell(0)?.toString()?.toLongOrNull() ?: 0L,
                fecha = row.getCell(1)?.toString() ?: "",
                hora = row.getCell(2)?.toString() ?: "",
                taximetro = row.getCell(3)?.toString()?.toDoubleOrNull() ?: 0.0,
                importeReal = row.getCell(4)?.toString()?.toDoubleOrNull() ?: 0.0,
                propina = row.getCell(5)?.toString()?.toDoubleOrNull() ?: 0.0,
                formaPago = try {
                    FormaPago.valueOf(row.getCell(6)?.toString() ?: "EFECTIVO")
                } catch (e: Exception) {
                    FormaPago.EFECTIVO
                },
                emisora = row.getCell(7)?.toString()?.toBoolean() ?: false,
                aeropuerto = row.getCell(8)?.toString()?.toBoolean() ?: false,
                turno = row.getCell(9)?.toString() ?: ""
            )
            
            carreraDao.insertCarrera(carrera)
        }
    }
    
    private suspend fun importGastos(workbook: XSSFWorkbook, database: TaxiFlashDatabase) {
        val sheet = workbook.getSheet("Gastos") ?: return
        val gastoDao = database.gastoDao()
        
        // Borrar datos existentes - implementamos nuestra propia función ya que no está en el DAO
        val gastosList = gastoDao.getAllGastosList()
        for (gasto in gastosList) {
            gastoDao.deleteGasto(gasto)
        }
        
        // Importar nuevos datos
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            
            val gasto = Gasto(
                id = row.getCell(0)?.toString()?.toLongOrNull() ?: 0L,
                factura = row.getCell(1)?.toString() ?: "",
                proveedor = row.getCell(2)?.toString() ?: "",
                fecha = row.getCell(3)?.toString() ?: "",
                importeTotal = row.getCell(4)?.toString()?.toDoubleOrNull() ?: 0.0,
                iva = row.getCell(5)?.toString()?.toDoubleOrNull() ?: 0.0,
                kilometros = row.getCell(6)?.toString()?.toIntOrNull(),
                tipoGasto = row.getCell(7)?.toString() ?: "",
                tipoGastoEspecifico = row.getCell(8)?.toString() ?: "",
                descripcion = row.getCell(9)?.toString()
            )
            
            gastoDao.insertGasto(gasto)
        }
    }
    
    private suspend fun importOtrosIngresos(workbook: XSSFWorkbook, database: TaxiFlashDatabase) {
        val sheet = workbook.getSheet("OtrosIngresos") ?: return
        val otrosIngresosDao = database.otrosIngresosDao()
        
        // Borrar datos existentes - implementamos nuestra propia función ya que no está en el DAO
        val otrosIngresosList = otrosIngresosDao.getAllOtrosIngresos().first()
        for (otrosIngresos in otrosIngresosList) {
            otrosIngresosDao.deleteOtrosIngresos(otrosIngresos)
        }
        
        // Importar nuevos datos
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            
            val otroIngreso = OtrosIngresos(
                id = row.getCell(0)?.toString()?.toLongOrNull() ?: 0L,
                concepto = row.getCell(1)?.toString() ?: "",
                fecha = row.getCell(2)?.toString() ?: "",
                importe = row.getCell(3)?.toString()?.toDoubleOrNull() ?: 0.0,
                descripcion = row.getCell(4)?.toString(),
                notas = row.getCell(5)?.toString()
            )
            
            otrosIngresosDao.insertOtrosIngresos(otroIngreso)
        }
    }
    
    /**
     * Abre la selección de archivos para importar
     * 
     * @param context Contexto de la aplicación
     * @return Intent para abrir el selector de archivos
     */
    fun obtenerIntentImportacion(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
    }
    
    /**
     * Comparte un archivo Excel con otras aplicaciones
     * 
     * @param context Contexto de la aplicación
     * @param uri Uri del archivo a exportar
     */
    fun exportarAGoogleDrive(context: Context, uri: Uri) {
        try {
            // Usar la clase FileExporter para manejar la compartición con todas las redes disponibles
            if (FileExporter.shareExcel(context, uri)) {
                Log.d(TAG, "Archivo compartido correctamente a través de FileExporter")
            } else {
                // Plan alternativo si falla FileExporter
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    
                    // Añadir permisos adicionales para asegurar que funcione con datos móviles
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                    }
                }
                
                val chooser = Intent.createChooser(intent, "Compartir archivo Excel")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                Log.d(TAG, "Intent de compartir iniciado mediante método alternativo")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general al preparar el intent: ${e.message}", e)
            Toast.makeText(context, "No se pudo preparar el archivo para compartir", Toast.LENGTH_LONG).show()
        }
    }
} 