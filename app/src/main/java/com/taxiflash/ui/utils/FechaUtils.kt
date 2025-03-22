package com.taxiflash.ui.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Clase de utilidad para el manejo de fechas en la aplicación.
 * 
 * Proporciona métodos para formatear, parsear y manipular fechas de manera consistente
 * en toda la aplicación, evitando duplicación de código y posibles errores.
 */
object FechaUtils {
    
    // Formatos de fecha utilizados en la aplicación
    private const val FORMATO_FECHA_DB = "dd/MM/yyyy"
    private const val FORMATO_FECHA_ID = "yyyyMMdd"
    private const val FORMATO_HORA = "HH:mm"
    
    /**
     * Obtiene la fecha actual en formato dd/MM/yyyy
     * 
     * @return String con la fecha actual formateada
     */
    fun obtenerFechaActual(): String {
        return SimpleDateFormat(FORMATO_FECHA_DB, Locale.getDefault()).format(Date())
    }
    
    /**
     * Obtiene la hora actual en formato HH:mm
     * 
     * @return String con la hora actual formateada
     */
    fun obtenerHoraActual(): String {
        return SimpleDateFormat(FORMATO_HORA, Locale.getDefault()).format(Date())
    }
    
    /**
     * Formatea una fecha para su uso en consultas a la base de datos
     * 
     * @param fecha Fecha a formatear
     * @return String con la fecha formateada en formato dd/MM/yyyy
     */
    fun formatearFechaParaConsulta(fecha: Date): String {
        return SimpleDateFormat(FORMATO_FECHA_DB, Locale.getDefault()).format(fecha)
    }
    
    /**
     * Formatea una fecha para su uso en IDs
     * 
     * @param fecha Fecha a formatear
     * @return String con la fecha formateada en formato yyyyMMdd
     */
    fun formatearFechaParaId(fecha: Date): String {
        return SimpleDateFormat(FORMATO_FECHA_ID, Locale.getDefault()).format(fecha)
    }
    
    /**
     * Parsea una fecha en formato dd/MM/yyyy
     * 
     * @param fechaStr String con la fecha a parsear
     * @return Date con la fecha parseada o null si el formato es incorrecto
     */
    fun parsearFecha(fechaStr: String): Date? {
        return try {
            SimpleDateFormat(FORMATO_FECHA_DB, Locale.getDefault()).parse(fechaStr)
        } catch (e: ParseException) {
            null
        }
    }
    
    /**
     * Obtiene el primer día del mes para una fecha dada
     * 
     * @param fecha Fecha de referencia
     * @return Date correspondiente al primer día del mes
     */
    fun obtenerPrimerDiaMes(fecha: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = fecha
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
    
    /**
     * Obtiene el último día del mes para una fecha dada
     * 
     * @param fecha Fecha de referencia
     * @return Date correspondiente al último día del mes
     */
    fun obtenerUltimoDiaMes(fecha: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = fecha
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }
    
    /**
     * Obtiene el nombre del mes para una fecha dada
     * 
     * @param fecha Fecha de referencia
     * @return String con el nombre del mes en español
     */
    fun obtenerNombreMes(fecha: Date): String {
        val formatoMes = SimpleDateFormat("MMMM", Locale("es", "ES"))
        return formatoMes.format(fecha).capitalize(Locale.getDefault())
    }
    
    /**
     * Obtiene el año para una fecha dada
     * 
     * @param fecha Fecha de referencia
     * @return Int con el año
     */
    fun obtenerAnio(fecha: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = fecha
        return calendar.get(Calendar.YEAR)
    }
    
    /**
     * Suma días a una fecha
     * 
     * @param fecha Fecha base
     * @param dias Número de días a sumar (puede ser negativo)
     * @return Date con la nueva fecha
     */
    fun sumarDias(fecha: Date, dias: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = fecha
        calendar.add(Calendar.DAY_OF_MONTH, dias)
        return calendar.time
    }
    
    /**
     * Compara dos fechas ignorando la hora
     * 
     * @param fecha1 Primera fecha a comparar
     * @param fecha2 Segunda fecha a comparar
     * @return true si las fechas son iguales (ignorando la hora), false en caso contrario
     */
    fun sonFechasIguales(fecha1: Date, fecha2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = fecha1
        cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)
        cal1.set(Calendar.MILLISECOND, 0)
        
        val cal2 = Calendar.getInstance()
        cal2.time = fecha2
        cal2.set(Calendar.HOUR_OF_DAY, 0)
        cal2.set(Calendar.MINUTE, 0)
        cal2.set(Calendar.SECOND, 0)
        cal2.set(Calendar.MILLISECOND, 0)
        
        return cal1.time == cal2.time
    }
    
    /**
     * Convierte un String de fecha a formato para ID
     * 
     * @param fechaStr Fecha en formato dd/MM/yyyy
     * @return String con la fecha en formato yyyyMMdd o null si el formato es incorrecto
     */
    fun convertirFormatoFechaAId(fechaStr: String): String? {
        val fecha = parsearFecha(fechaStr) ?: return null
        return formatearFechaParaId(fecha)
    }
    
    /**
     * Obtiene una lista de todas las fechas en un rango
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de fechas en formato dd/MM/yyyy
     */
    fun obtenerFechasEnRango(fechaInicio: Date, fechaFin: Date): List<String> {
        val fechas = mutableListOf<String>()
        val formatoFecha = SimpleDateFormat(FORMATO_FECHA_DB, Locale.getDefault())
        
        val calendar = Calendar.getInstance()
        calendar.time = fechaInicio
        
        while (calendar.time <= fechaFin) {
            fechas.add(formatoFecha.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return fechas
    }
    
    /**
     * Métodos específicos para Android API 26+ (Java 8)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    object Api26 {
        private val FORMATO_FECHA_LOCAL = DateTimeFormatter.ofPattern(FORMATO_FECHA_DB)
        private val FORMATO_HORA_LOCAL = DateTimeFormatter.ofPattern(FORMATO_HORA)
        
        /**
         * Convierte una fecha Date a LocalDate
         */
        fun dateToLocalDate(date: Date): LocalDate {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        }
        
        /**
         * Convierte un LocalDate a Date
         */
        fun localDateToDate(localDate: LocalDate): Date {
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        }
        
        /**
         * Formatea un LocalDate para consultas
         */
        fun formatearLocalDate(localDate: LocalDate): String {
            return localDate.format(FORMATO_FECHA_LOCAL)
        }
    }
} 