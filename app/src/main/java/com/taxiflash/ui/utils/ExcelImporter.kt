@file:OptIn(ExperimentalStdlibApi::class)
package com.taxiflash.ui.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.taxiflash.ui.data.*
import jxl.Cell
import jxl.Workbook
import jxl.read.biff.BiffException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ExcelImporter(private val context: Context) {
    private val TAG = "ExcelImporter"
    private val database = TaxiFlashDatabase.getDatabase(context)
    private val carreraDao = database.carreraDao()
    private val turnoDao = database.turnoDao()
    private val gastoDao = database.gastoDao()

    suspend fun importarCarrerasDesdeExcel(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando importación de carreras desde Excel: $uri")
            var carrerasImportadas = 0
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                try {
                    val workbook = Workbook.getWorkbook(inputStream)
                    
                    // Buscar la hoja correcta - ya sea la primera o una llamada "Carreras"
                    var sheet = workbook.getSheet(0)  // Por defecto, usar la primera hoja
                    
                    // Intentar encontrar una hoja específica
                    val nombreHojas = workbook.sheetNames
                    Log.d(TAG, "Hojas disponibles en el archivo: ${nombreHojas.joinToString()}")
                    
                    // Buscar una hoja con nombre relevante
                    val hojaConCarreras = nombreHojas.firstOrNull { nombre -> 
                        nombre.lowercase().contains("carrera") || nombre.lowercase().contains("viaje")
                    }
                    
                    if (hojaConCarreras != null) {
                        Log.d(TAG, "Encontrada hoja específica para carreras: $hojaConCarreras")
                        sheet = workbook.getSheet(hojaConCarreras)
                    }
                    
                    if (sheet.rows <= 1) {
                        Log.e(TAG, "La hoja no contiene datos suficientes. Se esperan al menos encabezados y una fila de datos.")
                        workbook.close()
                        return@withContext Result.failure(Exception("La hoja seleccionada no contiene datos suficientes"))
                    }
                    
                    Log.d(TAG, "Hoja seleccionada: ${sheet.name}, filas: ${sheet.rows}, columnas: ${sheet.columns}")
                    
                    Log.d(TAG, "Archivo Excel abierto correctamente, procesando datos...")

                    // Leer encabezados desde la primera fila
                    val encabezados = mutableListOf<String>()
                    for (i in 0 until sheet.columns) {
                        encabezados.add(getCellValue(sheet.getCell(i, 0)).lowercase())
                    }
                    
                    Log.d(TAG, "Encabezados encontrados: $encabezados")

                    // Verificar que contiene los campos necesarios
                    val encabezadosRequeridos = listOf("fecha", "hora", "taximetro", "importereal", "propina", "formapago", "emisora", "aeropuerto", "turno")
                    if (!encabezadosRequeridos.all { campo -> encabezados.any { it.contains(campo) } }) {
                        val mensaje = "El formato del archivo Excel no es correcto. Los encabezados deberían incluir: fecha, hora, taximetro, importeReal, propina, formaPago, emisora, aeropuerto, turno"
                        Log.e(TAG, mensaje)
                        workbook.close()
                        return@withContext Result.failure(Exception(mensaje))
                    }

                    // Obtener los índices de cada campo
                    val fechaIndex = encabezados.indexOfFirst { it.contains("fecha") }
                    val horaIndex = encabezados.indexOfFirst { it.contains("hora") }
                    val taximetroIndex = encabezados.indexOfFirst { it.contains("taximetro") }
                    val importeRealIndex = encabezados.indexOfFirst { it.contains("importereal") }
                    val propinaIndex = encabezados.indexOfFirst { it.contains("propina") }
                    val formaPagoIndex = encabezados.indexOfFirst { it.contains("formapago") }
                    val emisoraIndex = encabezados.indexOfFirst { it.contains("emisora") }
                    val aeropuertoIndex = encabezados.indexOfFirst { it.contains("aeropuerto") }
                    val turnoIndex = encabezados.indexOfFirst { it.contains("turno") }

                    // Procesar cada fila (excepto la primera que contiene los encabezados)
                    for (i in 1 until sheet.rows) {
                        try {
                            Log.d(TAG, "Procesando fila $i")
                            
                            // Extraer el turno y verificar su formato
                            val turnoId = getCellValue(sheet.getCell(turnoIndex, i))
                            Log.d(TAG, "Valor turno encontrado: $turnoId")
                            
                            // Intentar diferentes formatos de identificación de turnos
                            val numeroTurno: Int
                            var fechaConsulta: String
                            
                            if (turnoId.contains("-")) {
                                // El formato ya parece ser yyyyMMdd-numeroTurno
                                try {
                                    val partes = turnoId.split("-")
                                    if (partes.size >= 2) {
                                        fechaConsulta = partes[0]
                                        val numTurnoTemp = partes[1].toIntOrNull()
                                        if (numTurnoTemp == null) {
                                            Log.w(TAG, "Número de turno inválido en el formato yyyyMMdd-numeroTurno: ${partes[1]}")
                                            continue
                                        }
                                        numeroTurno = numTurnoTemp
                                    } else {
                                        Log.w(TAG, "Formato de turno inválido, no se puede separar la fecha del número: $turnoId")
                                        continue
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al procesar el ID de turno con formato de fecha: $turnoId", e)
                                    continue
                                }
                            } else if (turnoId.startsWith("Turno")) {
                                // Formato "Turno X" - necesitamos asignar una fecha
                                val numTurnoTemp = turnoId.substringAfter("Turno ").trim().toIntOrNull()
                                if (numTurnoTemp == null) {
                                    Log.w(TAG, "Formato de turno inválido, no contiene un número válido: $turnoId")
                                    continue
                                }
                                numeroTurno = numTurnoTemp
                                
                                // Usar la fecha de la carrera para construir el ID
                                val fechaCarrera = getCellValue(sheet.getCell(fechaIndex, i))
                                try {
                                    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaCarrera)
                                    fechaConsulta = if (date != null) {
                                        FechaUtils.formatearFechaParaId(date)
                                    } else {
                                        Log.w(TAG, "No se pudo parsear la fecha de la carrera: $fechaCarrera, usando fecha actual")
                                        FechaUtils.formatearFechaParaId(Date())
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir la fecha de la carrera: $fechaCarrera, usando fecha actual", e)
                                    fechaConsulta = FechaUtils.formatearFechaParaId(Date())
                                }
                            } else {
                                // Intentar interpretar como un número simple
                                val numTurnoTemp = turnoId.toIntOrNull()
                                if (numTurnoTemp == null) {
                                    Log.w(TAG, "Valor de turno no reconocido: $turnoId")
                                    continue
                                }
                                numeroTurno = numTurnoTemp
                                
                                // Usar la fecha de la carrera para construir el ID
                                val fechaCarrera = getCellValue(sheet.getCell(fechaIndex, i))
                                try {
                                    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaCarrera)
                                    fechaConsulta = if (date != null) {
                                        FechaUtils.formatearFechaParaId(date)
                                    } else {
                                        Log.w(TAG, "No se pudo parsear la fecha de la carrera: $fechaCarrera, usando fecha actual")
                                        FechaUtils.formatearFechaParaId(Date())
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir la fecha de la carrera: $fechaCarrera, usando fecha actual", e)
                                    fechaConsulta = FechaUtils.formatearFechaParaId(Date())
                                }
                            }
                            
                            // Construir el ID del turno con el formato correcto
                            val idTurnoFormateado = "$fechaConsulta-$numeroTurno"
                            Log.d(TAG, "ID de turno generado: $idTurnoFormateado")

                            // Verificar si el turno existe
                            val turnoExiste = turnoDao.getTurnoById(idTurnoFormateado) != null
                            if (!turnoExiste) {
                                Log.w(TAG, "El turno $idTurnoFormateado no existe en la base de datos. Primero debes importar los turnos.")
                                continue
                            }
                            
                            Log.d(TAG, "Turno verificado: $idTurnoFormateado existe en la base de datos")

                            val carrera = Carrera(
                                fecha = getCellValue(sheet.getCell(fechaIndex, i)),
                                hora = getCellValue(sheet.getCell(horaIndex, i)),
                                taximetro = getCellValue(sheet.getCell(taximetroIndex, i)).toDoubleOrNull() ?: 0.0,
                                importeReal = getCellValue(sheet.getCell(importeRealIndex, i)).toDoubleOrNull() ?: 0.0,
                                propina = getCellValue(sheet.getCell(propinaIndex, i)).toDoubleOrNull() ?: 0.0,
                                formaPago = when (getCellValue(sheet.getCell(formaPagoIndex, i)).uppercase()) {
                                    "TARJETA" -> FormaPago.TARJETA
                                    "BIZUM" -> FormaPago.BIZUM
                                    "VALES" -> FormaPago.VALES
                                    else -> FormaPago.EFECTIVO
                                },
                                emisora = isTrue(getCellValue(sheet.getCell(emisoraIndex, i))),
                                aeropuerto = isTrue(getCellValue(sheet.getCell(aeropuertoIndex, i))),
                                turno = idTurnoFormateado
                            )
                            carreraDao.insertCarrera(carrera)
                            carrerasImportadas++
                            Log.d(TAG, "Carrera importada: $carrera")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al procesar fila $i", e)
                        }
                    }

                    workbook.close()
                    Log.d(TAG, "Importación finalizada. Total importadas: $carrerasImportadas")
                } catch (e: BiffException) {
                    Log.e(TAG, "Error al leer el archivo Excel. Es posible que el formato no sea compatible.", e)
                    return@withContext Result.failure(Exception("El archivo no es un Excel válido o es incompatible. Intenta exportar como XLS (Excel 97-2003) o CSV."))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al procesar el archivo Excel", e)
                    return@withContext Result.failure(e)
                }
            } ?: run {
                val mensaje = "No se pudo abrir el archivo"
                Log.e(TAG, mensaje)
                return@withContext Result.failure(Exception(mensaje))
            }

            if (carrerasImportadas == 0) {
                return@withContext Result.failure(Exception("No se importó ninguna carrera. Verifica que el formato sea correcto y que los turnos existan previamente en la base de datos."))
            }

            return@withContext Result.success(carrerasImportadas)
        } catch (e: Exception) {
            Log.e(TAG, "Error general durante la importación de Excel", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun importarGastosDesdeExcel(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando importación de gastos desde Excel: $uri")
            var gastosImportados = 0
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                try {
                    val workbook = Workbook.getWorkbook(inputStream)
                    
                    // Buscar la hoja correcta - ya sea la primera o una llamada "Gastos"
                    var sheet = workbook.getSheet(0)  // Por defecto, usar la primera hoja
                    
                    // Intentar encontrar una hoja específica
                    val nombreHojas = workbook.sheetNames
                    Log.d(TAG, "Hojas disponibles en el archivo: ${nombreHojas.joinToString()}")
                    
                    // Buscar una hoja con nombre relevante
                    val hojaConGastos = nombreHojas.firstOrNull { nombre -> 
                        nombre.lowercase().contains("gasto") || nombre.lowercase().contains("expense")
                    }
                    
                    if (hojaConGastos != null) {
                        Log.d(TAG, "Encontrada hoja específica para gastos: $hojaConGastos")
                        sheet = workbook.getSheet(hojaConGastos)
                    }
                    
                    if (sheet.rows <= 1) {
                        Log.e(TAG, "La hoja no contiene datos suficientes. Se esperan al menos encabezados y una fila de datos.")
                        workbook.close()
                        return@withContext Result.failure(Exception("La hoja seleccionada no contiene datos suficientes"))
                    }
                    
                    Log.d(TAG, "Hoja seleccionada: ${sheet.name}, filas: ${sheet.rows}, columnas: ${sheet.columns}")
                    
                    Log.d(TAG, "Archivo Excel abierto correctamente, procesando gastos...")

                    // Leer encabezados desde la primera fila
                    val encabezados = mutableListOf<String>()
                    for (i in 0 until sheet.columns) {
                        encabezados.add(getCellValue(sheet.getCell(i, 0)).lowercase())
                    }
                    
                    Log.d(TAG, "Encabezados encontrados: $encabezados")

                    // Verificar que contiene los campos necesarios
                    val encabezadosRequeridos = listOf("factura", "proveedor", "fecha", "importetotal", "iva", "kilometros", "tipogasto", "tipogastoespecifico", "descripcion")
                    if (!encabezadosRequeridos.all { campo -> encabezados.any { it.contains(campo) } }) {
                        val mensaje = "El formato del archivo Excel no es correcto. Los encabezados deberían incluir: factura, proveedor, fecha, importeTotal, iva, kilometros, tipoGasto, tipoGastoEspecifico, descripcion"
                        Log.e(TAG, mensaje)
                        workbook.close()
                        return@withContext Result.failure(Exception(mensaje))
                    }

                    // Obtener los índices de cada campo
                    val facturaIndex = encabezados.indexOfFirst { it.contains("factura") }
                    val proveedorIndex = encabezados.indexOfFirst { it.contains("proveedor") }
                    val fechaIndex = encabezados.indexOfFirst { it.contains("fecha") }
                    val importeTotalIndex = encabezados.indexOfFirst { it.contains("importetotal") }
                    val ivaIndex = encabezados.indexOfFirst { it.contains("iva") }
                    val kilometrosIndex = encabezados.indexOfFirst { it.contains("kilometros") }
                    val tipoGastoIndex = encabezados.indexOfFirst { it.contains("tipogasto") && !it.contains("especifico") }
                    val tipoGastoEspecificoIndex = encabezados.indexOfFirst { it.contains("tipogastoespecifico") }
                    val descripcionIndex = encabezados.indexOfFirst { it.contains("descripcion") }

                    // Procesar cada fila (excepto la primera que contiene los encabezados)
                    for (i in 1 until sheet.rows) {
                        try {
                            Log.d(TAG, "Procesando fila de gasto $i")
                            
                            val gasto = Gasto(
                                factura = getCellValue(sheet.getCell(facturaIndex, i)),
                                proveedor = getCellValue(sheet.getCell(proveedorIndex, i)),
                                fecha = getCellValue(sheet.getCell(fechaIndex, i)),
                                importeTotal = getCellValue(sheet.getCell(importeTotalIndex, i)).toDoubleOrNull() ?: 0.0,
                                iva = getCellValue(sheet.getCell(ivaIndex, i)).toDoubleOrNull() ?: 0.0,
                                kilometros = getCellValue(sheet.getCell(kilometrosIndex, i)).toIntOrNull(),
                                tipoGasto = getCellValue(sheet.getCell(tipoGastoIndex, i)),
                                tipoGastoEspecifico = getCellValue(sheet.getCell(tipoGastoEspecificoIndex, i)),
                                descripcion = getCellValue(sheet.getCell(descripcionIndex, i)).takeIf { it.isNotEmpty() }
                            )
                            gastoDao.insertGasto(gasto)
                            gastosImportados++
                            Log.d(TAG, "Gasto importado: $gasto")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al procesar fila de gasto $i", e)
                        }
                    }

                    workbook.close()
                    Log.d(TAG, "Importación de gastos finalizada. Total importados: $gastosImportados")
                } catch (e: BiffException) {
                    Log.e(TAG, "Error al leer el archivo Excel. Es posible que el formato no sea compatible.", e)
                    return@withContext Result.failure(Exception("El archivo no es un Excel válido o es incompatible. Intenta exportar como XLS (Excel 97-2003) o CSV."))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al procesar el archivo Excel", e)
                    return@withContext Result.failure(e)
                }
            } ?: run {
                val mensaje = "No se pudo abrir el archivo"
                Log.e(TAG, mensaje)
                return@withContext Result.failure(Exception(mensaje))
            }

            if (gastosImportados == 0) {
                return@withContext Result.failure(Exception("No se importó ningún gasto. Verifica que el formato sea correcto."))
            }

            return@withContext Result.success(gastosImportados)
        } catch (e: Exception) {
            Log.e(TAG, "Error general durante la importación de gastos", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun importarTurnosDesdeExcel(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando importación de turnos desde Excel: $uri")
            var turnosImportados = 0
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                try {
                    Log.d(TAG, "Abriendo archivo Excel para turnos...")
                    val workbook = Workbook.getWorkbook(inputStream)
                    
                    // Buscar la hoja correcta - ya sea la primera o una llamada "Turnos"
                    var sheet = workbook.getSheet(0)  // Por defecto, usar la primera hoja
                    
                    // Intentar encontrar una hoja llamada "Turnos" o similar
                    val nombreHojas = workbook.sheetNames
                    Log.d(TAG, "Hojas disponibles en el archivo: ${nombreHojas.joinToString()}")
                    
                    // Buscar una hoja con nombre que contenga "turno" independiente de mayúsculas/minúsculas
                    // Ampliamos la búsqueda para soportar más variaciones de nombres
                    val hojaConTurnos = nombreHojas.firstOrNull { nombre -> 
                        nombre.lowercase().contains("turno") || 
                        nombre.lowercase() == "turnos" || 
                        nombre.lowercase() == "shifts" ||
                        nombre.lowercase() == "sheet1" || // Nombre por defecto en Google Sheets
                        nombre.lowercase() == "hoja1" ||  // Nombre en español
                        nombre.lowercase().contains("sheet") // Cualquier hoja con "sheet" en el nombre
                    }
                    
                    if (hojaConTurnos != null) {
                        Log.d(TAG, "Encontrada hoja específica para turnos: $hojaConTurnos")
                        sheet = workbook.getSheet(hojaConTurnos)
                    }
                    
                    if (sheet.rows <= 1) {
                        Log.e(TAG, "La hoja no contiene datos suficientes. Se esperan al menos encabezados y una fila de datos.")
                        workbook.close()
                        return@withContext Result.failure(Exception("La hoja seleccionada no contiene datos suficientes"))
                    }
                    
                    Log.d(TAG, "Hoja seleccionada: ${sheet.name}, filas: ${sheet.rows}, columnas: ${sheet.columns}")

                    // Validar encabezados
                    val encabezadosRequeridos = listOf("fecha", "horainicio", "horafin", "kminicio", "kmfin", "numeroturno", "activo")
                    
                    Log.d(TAG, "Verificando encabezados para turnos...")
                    // Extraer nombres de columnas de la primera fila (encabezados)
                    val encabezados = mutableListOf<String>()
                    for (i in 0 until sheet.columns) {
                        encabezados.add(getCellValue(sheet.getCell(i, 0)).lowercase())
                    }
                    
                    Log.d(TAG, "Encabezados encontrados en Excel para turnos: $encabezados")

                    // Verificar que estén todos los encabezados requeridos
                    val encabezadosFaltantes = encabezadosRequeridos.filter { requerido ->
                        encabezados.none { it.contains(requerido) }
                    }

                    if (encabezadosFaltantes.isNotEmpty()) {
                        val mensaje = "Faltan encabezados requeridos en el archivo Excel: $encabezadosFaltantes"
                        Log.e(TAG, mensaje)
                        return@withContext Result.failure(Exception(mensaje))
                    }

                    Log.d(TAG, "Todos los encabezados requeridos para turnos están presentes")

                    // Obtener índices de columnas basados en los encabezados
                    val fechaIndex = encabezados.indexOfFirst { it.contains("fecha") }
                    val horaInicioIndex = encabezados.indexOfFirst { it.contains("horainicio") }
                    val horaFinIndex = encabezados.indexOfFirst { it.contains("horafin") }
                    val kmInicioIndex = encabezados.indexOfFirst { it.contains("kminicio") }
                    val kmFinIndex = encabezados.indexOfFirst { it.contains("kmfin") }
                    val numeroTurnoIndex = encabezados.indexOfFirst { it.contains("numeroturno") }
                    val activoIndex = encabezados.indexOfFirst { it.contains("activo") }

                    Log.d(TAG, "Índices de columnas para turnos: fecha=$fechaIndex, horaInicio=$horaInicioIndex, " +
                            "horaFin=$horaFinIndex, kmInicio=$kmInicioIndex, kmFin=$kmFinIndex, " +
                            "numeroTurno=$numeroTurnoIndex, activo=$activoIndex")

                    // Procesar cada fila (excepto la primera que son los encabezados)
                    for (i in 1 until sheet.rows) {
                        try {
                            Log.d(TAG, "Procesando fila de turno $i")
                            
                            // Obtener y validar el número de turno
                            val numeroTurnoStr = getCellValue(sheet.getCell(numeroTurnoIndex, i))
                            val numeroTurno = numeroTurnoStr.toIntOrNull()
                            if (numeroTurno == null) {
                                Log.w(TAG, "Número de turno inválido en la fila $i: '$numeroTurnoStr'. Debe ser un número entero.")
                                continue
                            }
                            
                            Log.d(TAG, "Número de turno: $numeroTurno")
                            
                            // Obtener y validar fecha
                            val fechaOriginal = getCellValue(sheet.getCell(fechaIndex, i))
                            if (fechaOriginal.isBlank()) {
                                Log.w(TAG, "Fecha vacía en la fila $i. Se omitirá este turno.")
                                continue
                            }
                            
                            // Convertir fecha a formato dd/MM/yyyy
                            val fecha = try {
                                // Intentar diferentes formatos de fecha
                                val formatosComunes = listOf(
                                    "dd/MM/yyyy",
                                    "yyyy-MM-dd",
                                    "MM/dd/yyyy",
                                    "dd-MM-yyyy",
                                    "d/M/yyyy",
                                    "M/d/yyyy",
                                    "yyyy/MM/dd",
                                    "dd MMM yyyy",
                                    "MMM dd, yyyy"
                                )
                                
                                var fechaFormateada = fechaOriginal
                                for (formato in formatosComunes) {
                                    try {
                                        val sdf = SimpleDateFormat(formato, Locale.getDefault())
                                        val date = sdf.parse(fechaOriginal)
                                        if (date != null) {
                                            fechaFormateada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                                            Log.d(TAG, "Fecha convertida de formato '$formato': $fechaOriginal -> $fechaFormateada")
                                            break
                                        }
                                    } catch (e: Exception) {
                                        // Intentar con el siguiente formato
                                    }
                                }
                                fechaFormateada
                            } catch (e: Exception) {
                                Log.w(TAG, "Formato de fecha no reconocido: $fechaOriginal, se usará como está.")
                                fechaOriginal
                            }
                            
                            // Calcular el formato de fecha para consultas en base de datos (yyyyMMdd)
                            val fechaConsulta = try {
                                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha)
                                FechaUtils.formatearFechaParaId(date ?: Date())
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al convertir fecha para consulta: $fecha", e)
                                SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                            }
                            
                            // Obtener resto de campos
                            val horaInicio = getCellValue(sheet.getCell(horaInicioIndex, i))
                            val horaFin = getCellValue(sheet.getCell(horaFinIndex, i))
                            
                            // Conversión segura de valores numéricos
                            val kmInicio = try {
                                getCellValue(sheet.getCell(kmInicioIndex, i)).toIntOrNull() ?: 0
                            } catch (e: Exception) {
                                Log.w(TAG, "Error al convertir kmInicio en la fila $i. Se usará 0 como valor predeterminado.", e)
                                0
                            }
                            
                            val kmFin = try {
                                getCellValue(sheet.getCell(kmFinIndex, i)).toIntOrNull() ?: 0
                            } catch (e: Exception) {
                                Log.w(TAG, "Error al convertir kmFin en la fila $i. Se usará 0 como valor predeterminado.", e)
                                0
                            }
                            
                            // Generar ID de turno según el formato: "yyyyMMdd-numeroTurno"
                            val idTurno = "$fechaConsulta-$numeroTurno"
                            Log.d(TAG, "ID de turno generado: $idTurno")
                            
                            val turno = Turno(
                                idTurno = idTurno,
                                numeroTurno = numeroTurno,
                                fecha = fecha,
                                horaInicio = horaInicio,
                                horaFin = horaFin,
                                kmInicio = kmInicio,
                                kmFin = kmFin,
                                activo = isTrue(getCellValue(sheet.getCell(activoIndex, i)))
                            )
                            
                            try {
                                // Verificar si el turno ya existe
                                val turnoExistente = turnoDao.getTurnoById(idTurno)
                                if (turnoExistente != null) {
                                    Log.w(TAG, "El turno $idTurno ya existe, actualizando...")
                                    turnoDao.updateTurno(turno)
                                } else {
                                    Log.d(TAG, "Insertando nuevo turno: $idTurno")
                                    turnoDao.insertTurno(turno)
                                }
                                
                                turnosImportados++
                                Log.d(TAG, "Turno importado correctamente: $turno")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al guardar el turno en la base de datos: $turno", e)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al procesar fila de turno $i", e)
                        }
                    }

                    workbook.close()
                    Log.d(TAG, "Importación de turnos finalizada. Total importados: $turnosImportados")
                } catch (e: BiffException) {
                    Log.e(TAG, "Error al leer el archivo Excel. Es posible que el formato no sea compatible.", e)
                    return@withContext Result.failure(Exception("El archivo no es un Excel válido o es incompatible. Intenta exportar como XLS (Excel 97-2003) o CSV."))
                } catch (e: Exception) {
                    Log.e(TAG, "Error al procesar el archivo Excel para turnos", e)
                    return@withContext Result.failure(Exception("Error al procesar el archivo Excel: ${e.message}"))
                }
            } ?: run {
                val mensaje = "No se pudo abrir el archivo Excel para importar turnos"
                Log.e(TAG, mensaje)
                return@withContext Result.failure(Exception(mensaje))
            }

            if (turnosImportados == 0) {
                val mensaje = "No se importó ningún turno. Verifica que el formato del archivo sea correcto y que los datos sean válidos."
                Log.w(TAG, mensaje)
                return@withContext Result.failure(Exception(mensaje))
            }

            return@withContext Result.success(turnosImportados)
        } catch (e: Exception) {
            Log.e(TAG, "Error general durante la importación de turnos desde Excel", e)
            return@withContext Result.failure(Exception("Error al importar turnos desde Excel: ${e.message}"))
        }
    }

    // Métodos auxiliares para obtener valores de celdas
    private fun getCellValue(cell: Cell?): String {
        return cell?.contents ?: ""
    }

    // Función auxiliar para interpretar valores booleanos
    private fun isTrue(value: String): Boolean {
        val lowerValue = value.lowercase()
        return lowerValue == "true" || lowerValue == "1" || 
               lowerValue == "verdadero" || lowerValue == "sí" || 
               lowerValue == "si"
    }
} 