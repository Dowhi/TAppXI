package com.taxiflash.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
/*
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
*/
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.Gasto
import com.taxiflash.ui.data.Turno
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidad para exportar datos de la aplicación a diferentes formatos
 */
object ExportUtils {

    /**
     * Exporta turnos a formato CSV
     *
     * @param context Contexto de la aplicación
     * @param turnos Lista de turnos a exportar
     * @return Uri del archivo generado
     */
    fun exportarTurnosCSV(context: Context, turnos: List<Turno>): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "turnos_$timeStamp.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            OutputStreamWriter(FileOutputStream(file)).use { writer ->
                // Cabecera
                writer.append("ID,Número,Fecha,Hora Inicio,Hora Fin,KM Inicio,KM Fin\n")
                
                // Datos
                turnos.forEach { turno ->
                    writer.append("${turno.id},")
                    writer.append("${turno.numeroTurno},")
                    writer.append("${turno.fecha},")
                    writer.append("${turno.horaInicio},")
                    writer.append("${turno.horaFin ?: ""},")
                    writer.append("${turno.kmInicio},")
                    writer.append("${turno.kmFin}\n")
                }
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e("ExportUtils", "Error al exportar turnos a CSV", e)
            null
        }
    }
    
    /**
     * Exporta carreras a formato CSV
     *
     * @param context Contexto de la aplicación
     * @param carreras Lista de carreras a exportar
     * @return Uri del archivo generado
     */
    fun exportarCarrerasCSV(context: Context, carreras: List<Carrera>): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "carreras_$timeStamp.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            OutputStreamWriter(FileOutputStream(file)).use { writer ->
                // Cabecera muy simplificada
                writer.append("ID,Hora\n")
                
                // Datos muy simplificados
                carreras.forEach { carrera ->
                    writer.append("${carrera.id},")
                    writer.append("${carrera.hora}\n")
                }
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e("ExportUtils", "Error al exportar carreras a CSV", e)
            null
        }
    }
    
    /**
     * Exporta gastos a formato CSV
     *
     * @param context Contexto de la aplicación
     * @param gastos Lista de gastos a exportar
     * @return Uri del archivo generado
     */
    fun exportarGastosCSV(context: Context, gastos: List<Gasto>): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "gastos_$timeStamp.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            OutputStreamWriter(FileOutputStream(file)).use { writer ->
                // Cabecera simplificada
                writer.append("Fecha,Tipo,Importe\n")
                
                // Datos simplificados
                gastos.forEach { gasto ->
                    writer.append("${gasto.fecha},")
                    writer.append("${gasto.tipoGasto},")
                    writer.append("${gasto.importeTotal}\n")
                }
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e("ExportUtils", "Error al exportar gastos a CSV", e)
            null
        }
    }
    
    /**
     * Exporta un resumen mensual a PDF
     * Nota: Implementación comentada temporalmente para facilitar la compilación
     */
    fun exportarResumenMensualPDF(
        context: Context,
        mes: Int,
        anio: Int,
        ingresos: Double,
        gastos: Double,
        detalleIngresos: List<Pair<String, Double>>,
        detalleGastos: List<Triple<String, Double, Double>>
    ): Uri? {
        // Implementación simplificada para permitir la compilación
        return null
        
        /* Implementación completa comentada
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val nombreMes = SimpleDateFormat("MMMM", Locale("es", "ES")).format(
                SimpleDateFormat("MM", Locale.getDefault()).parse("$mes")!!
            )
            val fileName = "resumen_${nombreMes}_${anio}_$timeStamp.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            PdfWriter(file).use { writer ->
                val pdfDoc = PdfDocument(writer)
                val document = Document(pdfDoc, PageSize.A4)
                document.setMargins(36f, 36f, 36f, 36f)
                
                // Título
                val titulo = Paragraph("Resumen Mensual: $nombreMes $anio")
                    .setFontSize(18f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                document.add(titulo)
                
                document.add(Paragraph("\n"))
                
                // Resumen global
                val resumenTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
                    .setWidth(UnitValue.createPercentValue(100f))
                
                resumenTable.addCell(Cell().add(Paragraph("Total Ingresos")).setBold())
                resumenTable.addCell(Cell().add(Paragraph("${String.format("%.2f", ingresos)} €")).setTextAlignment(TextAlignment.RIGHT))
                
                resumenTable.addCell(Cell().add(Paragraph("Total Gastos")).setBold())
                resumenTable.addCell(Cell().add(Paragraph("${String.format("%.2f", gastos)} €")).setTextAlignment(TextAlignment.RIGHT))
                
                resumenTable.addCell(Cell().add(Paragraph("Beneficio Neto")).setBold())
                val beneficio = ingresos - gastos
                val colorBeneficio = if (beneficio >= 0) ColorConstants.GREEN else ColorConstants.RED
                val cellBeneficio = Cell().add(
                    Paragraph("${String.format("%.2f", beneficio)} €")
                        .setFontColor(colorBeneficio)
                ).setTextAlignment(TextAlignment.RIGHT)
                resumenTable.addCell(cellBeneficio)
                
                document.add(resumenTable)
                
                document.add(Paragraph("\n"))
                
                // Detalle de ingresos
                document.add(
                    Paragraph("Detalle de Ingresos")
                        .setFontSize(14f)
                        .setBold()
                )
                
                val ingresosTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
                    .setWidth(UnitValue.createPercentValue(100f))
                
                ingresosTable.addCell(Cell().add(Paragraph("Fecha")).setBold())
                ingresosTable.addCell(Cell().add(Paragraph("Importe")).setBold().setTextAlignment(TextAlignment.RIGHT))
                
                detalleIngresos.forEach { (fecha, importe) ->
                    ingresosTable.addCell(Cell().add(Paragraph(fecha)))
                    ingresosTable.addCell(Cell().add(Paragraph("${String.format("%.2f", importe)} €")).setTextAlignment(TextAlignment.RIGHT))
                }
                
                document.add(ingresosTable)
                
                document.add(Paragraph("\n"))
                
                // Detalle de gastos
                document.add(
                    Paragraph("Detalle de Gastos")
                        .setFontSize(14f)
                        .setBold()
                )
                
                val gastosTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 30f, 30f)))
                    .setWidth(UnitValue.createPercentValue(100f))
                
                gastosTable.addCell(Cell().add(Paragraph("Categoría")).setBold())
                gastosTable.addCell(Cell().add(Paragraph("Importe")).setBold().setTextAlignment(TextAlignment.RIGHT))
                gastosTable.addCell(Cell().add(Paragraph("Porcentaje")).setBold().setTextAlignment(TextAlignment.RIGHT))
                
                detalleGastos.forEach { (categoria, importe, porcentaje) ->
                    gastosTable.addCell(Cell().add(Paragraph(categoria)))
                    gastosTable.addCell(Cell().add(Paragraph("${String.format("%.2f", importe)} €")).setTextAlignment(TextAlignment.RIGHT))
                    gastosTable.addCell(Cell().add(Paragraph("${String.format("%.1f", porcentaje)} %")).setTextAlignment(TextAlignment.RIGHT))
                }
                
                document.add(gastosTable)
                
                // Pie de página
                document.add(Paragraph("\n\n"))
                document.add(
                    Paragraph("Generado por TaxiFlash el ${
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    }")
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                )
                
                document.close()
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e("ExportUtils", "Error al exportar resumen mensual a PDF", e)
            null
        }
        */
    }
    
    /**
     * Comparte un archivo mediante un intent
     *
     * @param context Contexto de la aplicación
     * @param uri Uri del archivo a compartir
     * @param titulo Título para el selector de apps
     * @param tipoMime Tipo MIME del archivo
     */
    fun compartirArchivo(context: Context, uri: Uri, titulo: String, tipoMime: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = tipoMime
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, titulo))
    }
} 