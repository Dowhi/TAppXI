package com.taxiflash.ui.utils

import android.content.Context
import android.util.Log
import com.taxiflash.ui.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class CsvImporter(private val context: Context) {
    private val TAG = "CsvImporter"
    private val database = TaxiFlashDatabase.getDatabase(context)
    private val carreraDao = database.carreraDao()
    private val turnoDao = database.turnoDao()
    private val gastoDao = database.gastoDao()

    suspend fun importarCarrerasDesdeCSV(uri: android.net.Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando importación de carreras desde CSV: $uri")
            var carrerasImportadas = 0
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    // Verificar el encabezado
                    val encabezado = reader.readLine()?.split(",")?.map { it.trim() }
                    Log.d(TAG, "Encabezado CSV: $encabezado")
                    
                    // Permitimos más flexibilidad en los encabezados, verificamos si contienen los campos necesarios
                    val encabezadosRequeridos = listOf("fecha", "hora", "taximetro", "importereal", "propina", "formapago", "emisora", "aeropuerto", "turno")
                    val encabezadosValidos = encabezado?.map { it.lowercase() }
                    
                    if (encabezadosValidos == null || !encabezadosRequeridos.all { campo -> encabezadosValidos.any { it.contains(campo) } }) {
                        Log.e(TAG, "El formato del archivo CSV no es correcto. Encabezados esperados: $encabezadosRequeridos, recibidos: $encabezadosValidos")
                        return@withContext Result.failure(Exception("El formato del archivo CSV no es correcto. Los encabezados deberían ser: fecha, hora, taximetro, importeReal, propina, formaPago, emisora, aeropuerto, turno"))
                    }
                    
                    Log.d(TAG, "Encabezados validados correctamente")
                    
                    // Obtener los índices de cada campo
                    val fechaIndex = encabezadosValidos.indexOfFirst { it.contains("fecha") }
                    val horaIndex = encabezadosValidos.indexOfFirst { it.contains("hora") }
                    val taximetroIndex = encabezadosValidos.indexOfFirst { it.contains("taximetro") }
                    val importeRealIndex = encabezadosValidos.indexOfFirst { it.contains("importereal") }
                    val propinaIndex = encabezadosValidos.indexOfFirst { it.contains("propina") }
                    val formaPagoIndex = encabezadosValidos.indexOfFirst { it.contains("formapago") }
                    val emisoraIndex = encabezadosValidos.indexOfFirst { it.contains("emisora") }
                    val aeropuertoIndex = encabezadosValidos.indexOfFirst { it.contains("aeropuerto") }
                    val turnoIndex = encabezadosValidos.indexOfFirst { it.contains("turno") }
                    
                    // Verificar y mostrar los índices
                    Log.d(TAG, "Índices de campos: fecha=$fechaIndex, hora=$horaIndex, taximetro=$taximetroIndex, " +
                            "importeReal=$importeRealIndex, propina=$propinaIndex, formaPago=$formaPagoIndex, " +
                            "emisora=$emisoraIndex, aeropuerto=$aeropuertoIndex, turno=$turnoIndex")
                    
                    var lineaActual = 2 // Empezamos en 2 porque la línea 1 es el encabezado
                    reader.lineSequence().forEach { line ->
                        try {
                            if (line.isBlank() || line == encabezado?.joinToString(",")) {
                                Log.d(TAG, "Línea $lineaActual ignorada (en blanco o es encabezado)")
                                lineaActual++
                                return@forEach
                            }
                            
                            val campos = line.split(",")
                            Log.d(TAG, "Procesando línea $lineaActual: $line")
                            
                            if (campos.size >= encabezadosValidos.size) {
                                // Extraer el número del turno - puede ser en formato "20250303-1" o "Turno 1"
                                val turnoId = campos[turnoIndex].trim()
                                Log.d(TAG, "Valor turno encontrado: $turnoId")
                                
                                val numeroTurno = if (turnoId.startsWith("Turno")) {
                                    turnoId.substringAfter("Turno ").trim().toIntOrNull()
                                } else if (turnoId.contains("-")) {
                                    turnoId.substringAfter("-").toIntOrNull()
                                } else {
                                    turnoId.toIntOrNull()
                                }
                                
                                if (numeroTurno == null) {
                                    Log.w(TAG, "Formato de turno inválido en línea $lineaActual: $turnoId")
                                    lineaActual++
                                    return@forEach
                                }
                                
                                // Obtener la fecha de la carrera para construir el ID del turno
                                val fechaCarrera = campos[fechaIndex].trim()
                                val fechaId = try {
                                    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaCarrera)
                                    if (date != null) {
                                        FechaUtils.formatearFechaParaId(date)
                                    } else {
                                        FechaUtils.formatearFechaParaId(Date())
                                    }
                                } catch (e: Exception) {
                                    FechaUtils.formatearFechaParaId(Date())
                                }
                                
                                // Generar ID único para el turno: yyyyMMdd-numeroTurno
                                val idTurnoFormateado = "$fechaId-$numeroTurno"
                                val turnoExiste = turnoDao.getTurnoById(idTurnoFormateado) != null
                                if (!turnoExiste) {
                                    Log.w(TAG, "El turno $idTurnoFormateado no existe en la base de datos. Primero debes importar los turnos.")
                                    lineaActual++
                                    return@forEach
                                }
                                
                                Log.d(TAG, "Turno verificado: $idTurnoFormateado existe en la base de datos")

                                // Obtener valores con validación
                                val taximetro = try {
                                    campos[taximetroIndex].trim().toDoubleOrNull() ?: 0.0
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir taxímetro en línea $lineaActual: ${campos[taximetroIndex]}", e)
                                    0.0
                                }
                                
                                val importeReal = try {
                                    campos[importeRealIndex].trim().toDoubleOrNull() ?: 0.0
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir importe real en línea $lineaActual: ${campos[importeRealIndex]}", e)
                                    0.0
                                }
                                
                                val propina = try {
                                    campos[propinaIndex].trim().toDoubleOrNull() ?: 0.0
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir propina en línea $lineaActual: ${campos[propinaIndex]}", e)
                                    0.0
                                }
                                
                                val formaPago = try {
                                    when (campos[formaPagoIndex].trim().uppercase()) {
                                        "TARJETA" -> FormaPago.TARJETA
                                        "BIZUM" -> FormaPago.BIZUM
                                        "VALES" -> FormaPago.VALES
                                        else -> FormaPago.EFECTIVO
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir forma de pago en línea $lineaActual: ${campos[formaPagoIndex]}", e)
                                    FormaPago.EFECTIVO
                                }
                                
                                val emisora = try {
                                    campos[emisoraIndex].trim().let { 
                                        when (it.lowercase()) {
                                            "true", "1", "verdadero", "sí", "si" -> true
                                            else -> false
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir emisora en línea $lineaActual: ${campos[emisoraIndex]}", e)
                                    false
                                }
                                
                                val aeropuerto = try {
                                    campos[aeropuertoIndex].trim().let { 
                                        when (it.lowercase()) {
                                            "true", "1", "verdadero", "sí", "si" -> true
                                            else -> false
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir aeropuerto en línea $lineaActual: ${campos[aeropuertoIndex]}", e)
                                    false
                                }

                                val carrera = Carrera(
                                    fecha = campos[fechaIndex].trim(),
                                    hora = campos[horaIndex].trim(),
                                    taximetro = taximetro,
                                    importeReal = importeReal,
                                    propina = propina,
                                    formaPago = formaPago,
                                    emisora = emisora,
                                    aeropuerto = aeropuerto,
                                    turno = idTurnoFormateado
                                )
                                
                                Log.d(TAG, "Insertando carrera: $carrera")
                                carreraDao.insertCarrera(carrera)
                                carrerasImportadas++
                                Log.d(TAG, "Carrera importada correctamente: $carrera")
                            } else {
                                Log.w(TAG, "Línea $lineaActual ignorada por formato incorrecto. Campos esperados: ${encabezadosValidos.size}, encontrados: ${campos.size}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al procesar línea $lineaActual: $line", e)
                        }
                        lineaActual++
                    }
                }
            } ?: run {
                Log.e(TAG, "No se pudo abrir el archivo")
                return@withContext Result.failure(Exception("No se pudo abrir el archivo"))
            }

            Log.d(TAG, "Importación finalizada. Total de carreras importadas: $carrerasImportadas")
            
            if (carrerasImportadas == 0) {
                return@withContext Result.failure(Exception("No se importó ninguna carrera. Verifica el formato del archivo y que los turnos existan previamente en la base de datos."))
            }
            
            return@withContext Result.success(carrerasImportadas)
        } catch (e: Exception) {
            Log.e(TAG, "Error general durante la importación", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun importarGastosDesdeCSV(uri: android.net.Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando importación de gastos desde CSV: $uri")
            var gastosImportados = 0
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    // Verificar el encabezado
                    val encabezado = reader.readLine()?.split(",")?.map { it.trim() }
                    Log.d(TAG, "Encabezado CSV: $encabezado")
                    
                    // Permitimos más flexibilidad en los encabezados
                    val encabezadosRequeridos = listOf("factura", "proveedor", "fecha", "importetotal", "iva", "kilometros", "tipogasto", "tipogastoespecifico", "descripcion")
                    val encabezadosValidos = encabezado?.map { it.lowercase() }
                    
                    if (encabezadosValidos == null || !encabezadosRequeridos.all { campo -> encabezadosValidos.any { it.contains(campo) } }) {
                        Log.e(TAG, "El formato del archivo CSV no es correcto. Encabezados esperados: $encabezadosRequeridos, recibidos: $encabezadosValidos")
                        return@withContext Result.failure(Exception("El formato del archivo CSV no es correcto. Los encabezados deberían ser: factura, proveedor, fecha, importeTotal, iva, kilometros, tipoGasto, tipoGastoEspecifico, descripcion"))
                    }
                    
                    Log.d(TAG, "Encabezados validados correctamente")
                    
                    // Obtener los índices de cada campo
                    val facturaIndex = encabezadosValidos.indexOfFirst { it.contains("factura") }
                    val proveedorIndex = encabezadosValidos.indexOfFirst { it.contains("proveedor") }
                    val fechaIndex = encabezadosValidos.indexOfFirst { it.contains("fecha") }
                    val importeTotalIndex = encabezadosValidos.indexOfFirst { it.contains("importetotal") }
                    val ivaIndex = encabezadosValidos.indexOfFirst { it.contains("iva") }
                    val kilometrosIndex = encabezadosValidos.indexOfFirst { it.contains("kilometros") }
                    val tipoGastoIndex = encabezadosValidos.indexOfFirst { it.contains("tipogasto") && !it.contains("especifico") }
                    val tipoGastoEspecificoIndex = encabezadosValidos.indexOfFirst { it.contains("tipogastoespecifico") }
                    val descripcionIndex = encabezadosValidos.indexOfFirst { it.contains("descripcion") }
                    
                    // Verificar y mostrar los índices
                    Log.d(TAG, "Índices de campos: factura=$facturaIndex, proveedor=$proveedorIndex, fecha=$fechaIndex, " +
                            "importeTotal=$importeTotalIndex, iva=$ivaIndex, kilometros=$kilometrosIndex, " +
                            "tipoGasto=$tipoGastoIndex, tipoGastoEspecifico=$tipoGastoEspecificoIndex, descripcion=$descripcionIndex")
                    
                    var lineaActual = 2 // Empezamos en 2 porque la línea 1 es el encabezado
                    reader.lineSequence().forEach { line ->
                        try {
                            if (line.isBlank() || line == encabezado?.joinToString(",")) {
                                Log.d(TAG, "Línea $lineaActual ignorada (en blanco o es encabezado)")
                                lineaActual++
                                return@forEach
                            }
                            
                            val campos = line.split(",")
                            Log.d(TAG, "Procesando línea $lineaActual: $line")
                            
                            if (campos.size >= encabezadosValidos.size) {
                                val importeTotal = try {
                                    campos[importeTotalIndex].trim().toDoubleOrNull() ?: 0.0
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir importe total en línea $lineaActual: ${campos[importeTotalIndex]}", e)
                                    0.0
                                }
                                
                                val iva = try {
                                    campos[ivaIndex].trim().toDoubleOrNull() ?: 0.0
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir IVA en línea $lineaActual: ${campos[ivaIndex]}", e)
                                    0.0
                                }
                                
                                val kilometros = try {
                                    campos[kilometrosIndex].trim().toIntOrNull()
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir kilómetros en línea $lineaActual: ${campos[kilometrosIndex]}", e)
                                    null
                                }
                                
                                val tipoGasto = try {
                                    when (campos[tipoGastoIndex].trim().uppercase()) {
                                        "COMBUSTIBLE" -> "COMBUSTIBLE"
                                        "MANTENIMIENTO" -> "MANTENIMIENTO"
                                        "LIMPIEZA" -> "LIMPIEZA"
                                        "PARKING" -> "PARKING"
                                        else -> "OTROS"
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir tipo de gasto en línea $lineaActual: ${campos[tipoGastoIndex]}", e)
                                    "OTROS"
                                }
                                
                                val tipoGastoEspecifico = try {
                                    campos[tipoGastoEspecificoIndex].trim().uppercase()
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir tipo de gasto específico en línea $lineaActual: ${campos[tipoGastoEspecificoIndex]}", e)
                                    "OTRO"
                                }

                                val gasto = Gasto(
                                    factura = campos[facturaIndex].trim(),
                                    proveedor = campos[proveedorIndex].trim(),
                                    fecha = campos[fechaIndex].trim(),
                                    importeTotal = importeTotal,
                                    iva = iva,
                                    kilometros = kilometros,
                                    tipoGasto = tipoGasto,
                                    tipoGastoEspecifico = tipoGastoEspecifico,
                                    descripcion = campos[descripcionIndex].trim()
                                )
                                
                                Log.d(TAG, "Insertando gasto: $gasto")
                                gastoDao.insertGasto(gasto)
                                gastosImportados++
                                Log.d(TAG, "Gasto importado correctamente: $gasto")
                            } else {
                                Log.w(TAG, "Línea $lineaActual ignorada por formato incorrecto. Campos esperados: ${encabezadosValidos.size}, encontrados: ${campos.size}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al procesar línea $lineaActual: $line", e)
                        }
                        lineaActual++
                    }
                }
            } ?: run {
                Log.e(TAG, "No se pudo abrir el archivo")
                return@withContext Result.failure(Exception("No se pudo abrir el archivo"))
            }

            Log.d(TAG, "Importación finalizada. Total de gastos importados: $gastosImportados")
            
            if (gastosImportados == 0) {
                return@withContext Result.failure(Exception("No se importó ningún gasto. Verifica el formato del archivo."))
            }
            
            return@withContext Result.success(gastosImportados)
        } catch (e: Exception) {
            Log.e(TAG, "Error general durante la importación", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun importarTurnosDesdeCSV(uri: android.net.Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando importación de turnos desde CSV: $uri")
            var turnosImportados = 0
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    // Verificar el encabezado
                    val encabezado = reader.readLine()?.split(",")?.map { it.trim() }
                    Log.d(TAG, "Encabezado CSV para turnos: $encabezado")
                    
                    // Permitimos más flexibilidad en los encabezados
                    val encabezadosRequeridos = listOf("fecha", "horainicio", "horafin", "kminicio", "kmfin", "numeroturno", "activo")
                    val encabezadosValidos = encabezado?.map { it.lowercase() }
                    
                    if (encabezadosValidos == null || !encabezadosRequeridos.all { campo -> encabezadosValidos.any { it.contains(campo) } }) {
                        val mensaje = "El formato del archivo CSV para turnos no es correcto. Encabezados esperados: $encabezadosRequeridos, encabezados recibidos: $encabezadosValidos"
                        Log.e(TAG, mensaje)
                        return@withContext Result.failure(Exception(mensaje))
                    }
                    
                    Log.d(TAG, "Encabezados de turnos validados correctamente")
                    
                    // Obtener los índices de cada campo
                    val fechaIndex = encabezadosValidos.indexOfFirst { it.contains("fecha") }
                    val horaInicioIndex = encabezadosValidos.indexOfFirst { it.contains("horainicio") }
                    val horaFinIndex = encabezadosValidos.indexOfFirst { it.contains("horafin") }
                    val kmInicioIndex = encabezadosValidos.indexOfFirst { it.contains("kminicio") }
                    val kmFinIndex = encabezadosValidos.indexOfFirst { it.contains("kmfin") }
                    val numeroTurnoIndex = encabezadosValidos.indexOfFirst { it.contains("numeroturno") }
                    val activoIndex = encabezadosValidos.indexOfFirst { it.contains("activo") }
                    
                    // Verificar y mostrar los índices
                    Log.d(TAG, "Índices de campos para turnos: fecha=$fechaIndex, horaInicio=$horaInicioIndex, horaFin=$horaFinIndex, " +
                            "kmInicio=$kmInicioIndex, kmFin=$kmFinIndex, numeroTurno=$numeroTurnoIndex, activo=$activoIndex")
                    
                    var lineaActual = 2 // Empezamos en 2 porque la línea 1 es el encabezado
                    reader.lineSequence().forEach { line ->
                        try {
                            if (line.isBlank() || line == encabezado?.joinToString(",")) {
                                Log.d(TAG, "Línea $lineaActual ignorada (en blanco o es encabezado)")
                                lineaActual++
                                return@forEach
                            }
                            
                            val campos = line.split(",")
                            Log.d(TAG, "Procesando línea $lineaActual para turno: $line")
                            
                            if (campos.size >= encabezadosValidos.size) {
                                val numeroTurnoStr = campos[numeroTurnoIndex].trim()
                                Log.d(TAG, "Número de turno (texto): $numeroTurnoStr")
                                
                                val numeroTurno = numeroTurnoStr.toIntOrNull()
                                if (numeroTurno == null) {
                                    Log.w(TAG, "Número de turno inválido en línea $lineaActual: '$numeroTurnoStr'. Debe ser un número entero.")
                                    lineaActual++
                                    return@forEach
                                }
                                
                                val kmInicio = try {
                                    campos[kmInicioIndex].trim().toIntOrNull() ?: 0
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir kmInicio en línea $lineaActual: '${campos[kmInicioIndex]}'. Se usará 0 como valor predeterminado.", e)
                                    0
                                }
                                
                                val kmFin = try {
                                    campos[kmFinIndex].trim().toIntOrNull() ?: 0
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir kmFin en línea $lineaActual: '${campos[kmFinIndex]}'. Se usará 0 como valor predeterminado.", e)
                                    0
                                }
                                
                                val activo = try {
                                    campos[activoIndex].trim().let { 
                                        when (it.lowercase()) {
                                            "true", "1", "verdadero", "sí", "si" -> true
                                            else -> false
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al convertir activo en línea $lineaActual: '${campos[activoIndex]}'. Se usará false como valor predeterminado.", e)
                                    false
                                }
                                
                                val fecha = campos[fechaIndex].trim()
                                val horaInicio = campos[horaInicioIndex].trim()
                                val horaFin = campos[horaFinIndex].trim()
                                
                                if (fecha.isBlank()) {
                                    Log.w(TAG, "Fecha vacía en línea $lineaActual. Se omitirá este turno.")
                                    lineaActual++
                                    return@forEach
                                }
                                
                                // Obtener la fecha para construir el ID del turno
                                val fechaId = try {
                                    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha)
                                    if (date != null) {
                                        FechaUtils.formatearFechaParaId(date)
                                    } else {
                                        FechaUtils.formatearFechaParaId(Date())
                                    }
                                } catch (e: Exception) {
                                    FechaUtils.formatearFechaParaId(Date())
                                }
                                
                                // Generar ID único para el turno: yyyyMMdd-numeroTurno
                                val idTurnoFormateado = "$fechaId-$numeroTurno"
                                
                                val turno = Turno(
                                    idTurno = idTurnoFormateado,
                                    numeroTurno = numeroTurno,
                                    fecha = fecha,
                                    horaInicio = horaInicio,
                                    horaFin = horaFin,
                                    kmInicio = kmInicio,
                                    kmFin = kmFin,
                                    activo = activo
                                )
                                
                                try {
                                    // Verificar si el turno ya existe
                                    val turnoExistente = turnoDao.getTurnoById(idTurnoFormateado)
                                    if (turnoExistente != null) {
                                        Log.w(TAG, "El turno $idTurnoFormateado ya existe, actualizando...")
                                        turnoDao.updateTurno(turno)
                                    } else {
                                        Log.d(TAG, "Insertando turno: $turno")
                                        turnoDao.insertTurno(turno)
                                    }
                                    
                                    turnosImportados++
                                    Log.d(TAG, "Turno importado correctamente: $turno")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error al guardar el turno en la base de datos: $turno", e)
                                    throw e
                                }
                            } else {
                                Log.w(TAG, "Línea $lineaActual ignorada por formato incorrecto. Campos esperados: ${encabezadosValidos.size}, encontrados: ${campos.size}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al procesar línea $lineaActual para turno: $line", e)
                        }
                        lineaActual++
                    }
                }
            } ?: run {
                val mensaje = "No se pudo abrir el archivo CSV para importar turnos"
                Log.e(TAG, mensaje)
                return@withContext Result.failure(Exception(mensaje))
            }

            Log.d(TAG, "Importación de turnos finalizada. Total de turnos importados: $turnosImportados")
            
            if (turnosImportados == 0) {
                val mensaje = "No se importó ningún turno. Verifica el formato del archivo y que los datos sean válidos."
                Log.w(TAG, mensaje)
                return@withContext Result.failure(Exception(mensaje))
            }
            
            return@withContext Result.success(turnosImportados)
        } catch (e: Exception) {
            Log.e(TAG, "Error general durante la importación de turnos", e)
            return@withContext Result.failure(Exception("Error al importar turnos: ${e.message}"))
        }
    }
} 