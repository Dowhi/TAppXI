package com.taxiflash.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.taxiflash.ui.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clase para generar informes PDF
 */
class PdfGenerator {
    companion object {
        private const val TAG = "PdfGenerator"
        private val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
        
        /**
         * Genera un informe mensual en formato PDF
         * @param context Contexto de la aplicación
         * @param año Año del informe
         * @param mes Mes del informe (1-12)
         * @param ingresos Total de ingresos
         * @param gastos Total de gastos
         * @param detalles Detalles por categoría: concepto -> importe
         * @param datosEstadisticos Datos estadísticos adicionales: concepto -> valor (opcional)
         * @param datosPorDia Datos agrupados por día: día -> mapa con datos diarios (opcional)
         * @return Uri del archivo generado o null si hubo un error
         */
        fun generarInformeMensual(
            context: Context,
            año: Int,
            mes: Int,
            ingresos: Double,
            gastos: Double,
            detalles: Map<String, Double>,
            datosEstadisticos: Map<String, String>? = null,
            datosPorDia: Map<Int, Map<String, Any>>? = null
        ): Uri? {
            try {
                // Nombre del mes
                val mesesEspañol = arrayOf(
                    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
                )
                val nombreMes = mesesEspañol[mes - 1]
                
                // Crear documento
                val document = Document(PageSize.A4)
                
                // Crear directorio si no existe
                val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "TaxiFlash")
                if (!storageDir.exists()) {
                    storageDir.mkdirs()
                }
                
                // Nombre del archivo
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val pdfFileName = "Informe_${nombreMes}_${año}_$timeStamp.pdf"
                val pdfFile = File(storageDir, pdfFileName)

                Log.d(TAG, "Guardando PDF en: ${pdfFile.absolutePath}")
                
                // Abrir documento
                val fileOutputStream = FileOutputStream(pdfFile)
                PdfWriter.getInstance(document, fileOutputStream)
                document.open()
                
                // Configurar fuentes
                val fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)
                val fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f, BaseColor.BLACK)
                val fontSection = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, BaseColor(0, 90, 170))
                val fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10f, BaseColor.BLACK)
                val fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, BaseColor.BLACK)
                val fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 8f, BaseColor.GRAY)
                
                // Título del informe
                val title = Paragraph("Informe Mensual TaxiFlash", fontTitle)
                title.alignment = Element.ALIGN_CENTER
                title.spacingAfter = 20f
                document.add(title)
                
                // Subtítulo con mes y año
                val subtitle = Paragraph("$nombreMes $año", fontSubtitle)
                subtitle.alignment = Element.ALIGN_CENTER
                subtitle.spacingAfter = 20f
                document.add(subtitle)
                
                // Fecha de generación
                val fechaGeneracion = Paragraph(
                    "Generado el ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
                    fontSmall
                )
                fechaGeneracion.alignment = Element.ALIGN_RIGHT
                fechaGeneracion.spacingAfter = 30f
                document.add(fechaGeneracion)
                
                // ==== SECCIÓN 1: RESUMEN GENERAL ====
                val resumenGeneral = Paragraph("1. Resumen General", fontSection)
                resumenGeneral.spacingAfter = 10f
                document.add(resumenGeneral)
                
                // Tabla de resumen
                val tablaResumen = PdfPTable(2)
                tablaResumen.widthPercentage = 60f
                tablaResumen.setWidths(floatArrayOf(1.5f, 1f))
                
                // Celda de encabezado para ingresos
                val celdaIngresos = PdfPCell(Phrase("Total Ingresos", fontBold))
                celdaIngresos.horizontalAlignment = Element.ALIGN_LEFT
                celdaIngresos.border = Rectangle.NO_BORDER
                celdaIngresos.paddingBottom = 8f
                tablaResumen.addCell(celdaIngresos)
                
                // Celda de valor para ingresos
                val celdaIngresoValor = PdfPCell(Phrase(numberFormat.format(ingresos), fontBold))
                celdaIngresoValor.horizontalAlignment = Element.ALIGN_RIGHT
                celdaIngresoValor.border = Rectangle.NO_BORDER
                celdaIngresoValor.paddingBottom = 8f
                tablaResumen.addCell(celdaIngresoValor)
                
                // Celda de encabezado para gastos
                val celdaGastos = PdfPCell(Phrase("Total Gastos", fontBold))
                celdaGastos.horizontalAlignment = Element.ALIGN_LEFT
                celdaGastos.border = Rectangle.NO_BORDER
                celdaGastos.paddingBottom = 8f
                tablaResumen.addCell(celdaGastos)
                
                // Celda de valor para gastos
                val celdaGastosValor = PdfPCell(Phrase(numberFormat.format(gastos), fontBold))
                celdaGastosValor.horizontalAlignment = Element.ALIGN_RIGHT
                celdaGastosValor.border = Rectangle.NO_BORDER
                celdaGastosValor.paddingBottom = 8f
                tablaResumen.addCell(celdaGastosValor)
                
                // Línea separadora
                val celdaLinea1 = PdfPCell()
                celdaLinea1.border = Rectangle.TOP
                celdaLinea1.borderColor = BaseColor.LIGHT_GRAY
                celdaLinea1.colspan = 2
                celdaLinea1.fixedHeight = 2f
                tablaResumen.addCell(celdaLinea1)
                
                // Celda de encabezado para balance
                val celdaBalance = PdfPCell(Phrase("Balance", fontBold))
                celdaBalance.horizontalAlignment = Element.ALIGN_LEFT
                celdaBalance.border = Rectangle.NO_BORDER
                celdaBalance.paddingTop = 8f
                tablaResumen.addCell(celdaBalance)
                
                // Celda de valor para balance
                val balance = ingresos - gastos
                val balanceColor = if (balance >= 0) BaseColor(0, 150, 0) else BaseColor(200, 0, 0)
                val fontBalance = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, balanceColor)
                val celdaBalanceValor = PdfPCell(Phrase(numberFormat.format(balance), fontBalance))
                celdaBalanceValor.horizontalAlignment = Element.ALIGN_RIGHT
                celdaBalanceValor.border = Rectangle.NO_BORDER
                celdaBalanceValor.paddingTop = 8f
                tablaResumen.addCell(celdaBalanceValor)
                
                document.add(tablaResumen)
                
                // ==== SECCIÓN 2: DETALLES POR CATEGORÍA ====
                if (detalles.isNotEmpty()) {
                    document.add(Paragraph("\n"))
                    val detallesTitle = Paragraph("2. Detalles por Categoría", fontSection)
                    detallesTitle.spacingAfter = 10f
                    document.add(detallesTitle)
                    
                    // Tabla de detalles
                    val tablaDetalles = PdfPTable(2)
                    tablaDetalles.widthPercentage = 80f
                    tablaDetalles.setWidths(floatArrayOf(3f, 1f))
                    
                    // Encabezados
                    val celdaConcepto = PdfPCell(Phrase("Concepto", fontBold))
                    celdaConcepto.horizontalAlignment = Element.ALIGN_LEFT
                    celdaConcepto.backgroundColor = BaseColor.LIGHT_GRAY
                    celdaConcepto.setPadding(5f)
                    tablaDetalles.addCell(celdaConcepto)
                    
                    val celdaImporte = PdfPCell(Phrase("Importe", fontBold))
                    celdaImporte.horizontalAlignment = Element.ALIGN_RIGHT
                    celdaImporte.backgroundColor = BaseColor.LIGHT_GRAY
                    celdaImporte.setPadding(5f)
                    tablaDetalles.addCell(celdaImporte)
                    
                    // Filas de detalles
                    detalles.forEach { (concepto, importe) ->
                        val celdaConceptoValor = PdfPCell(Phrase(concepto, fontNormal))
                        celdaConceptoValor.horizontalAlignment = Element.ALIGN_LEFT
                        celdaConceptoValor.setPadding(5f)
                        tablaDetalles.addCell(celdaConceptoValor)
                        
                        val celdaImporteValor = PdfPCell(Phrase(numberFormat.format(importe), fontNormal))
                        celdaImporteValor.horizontalAlignment = Element.ALIGN_RIGHT
                        celdaImporteValor.setPadding(5f)
                        tablaDetalles.addCell(celdaImporteValor)
                    }
                    
                    document.add(tablaDetalles)
                }

                // ==== SECCIÓN 3: ESTADÍSTICAS ADICIONALES
                if (datosEstadisticos != null && datosEstadisticos.isNotEmpty()) {
                    document.add(Paragraph("\n"))
                    val estadisticasTitle = Paragraph("3. Estadísticas", fontSection)
                    estadisticasTitle.spacingAfter = 10f
                    document.add(estadisticasTitle)
                    
                    // Tabla de estadísticas
                    val tablaEstadisticas = PdfPTable(2)
                    tablaEstadisticas.widthPercentage = 80f
                    tablaEstadisticas.setWidths(floatArrayOf(3f, 1f))
                    
                    // Encabezados
                    val celdaIndicador = PdfPCell(Phrase("Indicador", fontBold))
                    celdaIndicador.horizontalAlignment = Element.ALIGN_LEFT
                    celdaIndicador.backgroundColor = BaseColor.LIGHT_GRAY
                    celdaIndicador.setPadding(5f)
                    tablaEstadisticas.addCell(celdaIndicador)
                    
                    val celdaValor = PdfPCell(Phrase("Valor", fontBold))
                    celdaValor.horizontalAlignment = Element.ALIGN_RIGHT
                    celdaValor.backgroundColor = BaseColor.LIGHT_GRAY
                    celdaValor.setPadding(5f)
                    tablaEstadisticas.addCell(celdaValor)
                    
                    // Filas de estadísticas
                    datosEstadisticos.forEach { (indicador, valor) ->
                        val celdaIndicadorValor = PdfPCell(Phrase(indicador, fontNormal))
                        celdaIndicadorValor.horizontalAlignment = Element.ALIGN_LEFT
                        celdaIndicadorValor.setPadding(5f)
                        tablaEstadisticas.addCell(celdaIndicadorValor)
                        
                        val celdaValorTexto = PdfPCell(Phrase(valor, fontNormal))
                        celdaValorTexto.horizontalAlignment = Element.ALIGN_RIGHT
                        celdaValorTexto.setPadding(5f)
                        tablaEstadisticas.addCell(celdaValorTexto)
                    }
                    
                    document.add(tablaEstadisticas)
                }
                
                // ==== SECCIÓN 4: RESUMEN POR DÍAS ====
                if (datosPorDia != null && datosPorDia.isNotEmpty()) {
                    document.add(Paragraph("\n"))
                    val diarioTitle = Paragraph("4. Resumen por Días", fontSection)
                    diarioTitle.spacingAfter = 10f
                    document.add(diarioTitle)
                    
                    // Tabla de resumen diario
                    val tablaDiario = PdfPTable(4)
                    tablaDiario.widthPercentage = 100f
                    tablaDiario.setWidths(floatArrayOf(0.8f, 1.2f, 1.2f, 1.2f))
                    
                    // Encabezados
                    val celdaDia = PdfPCell(Phrase("Día", fontBold))
                    celdaDia.horizontalAlignment = Element.ALIGN_CENTER
                    celdaDia.backgroundColor = BaseColor.LIGHT_GRAY
                    celdaDia.setPadding(5f)
                    tablaDiario.addCell(celdaDia)
                    
                    val celdaIngresoDia = PdfPCell(Phrase("Ingresos", fontBold))
                    celdaIngresoDia.horizontalAlignment = Element.ALIGN_CENTER
                    celdaIngresoDia.backgroundColor = BaseColor.LIGHT_GRAY
                    celdaIngresoDia.setPadding(5f)
                    tablaDiario.addCell(celdaIngresoDia)
                    
                    val celdaGastoDia = PdfPCell(Phrase("Gastos", fontBold))
                    celdaGastoDia.horizontalAlignment = Element.ALIGN_CENTER
                    celdaGastoDia.backgroundColor = BaseColor.LIGHT_GRAY
                    celdaGastoDia.setPadding(5f)
                    tablaDiario.addCell(celdaGastoDia)
                    
                    val celdaBalanceDia = PdfPCell(Phrase("Balance", fontBold))
                    celdaBalanceDia.horizontalAlignment = Element.ALIGN_CENTER
                    celdaBalanceDia.backgroundColor = BaseColor.LIGHT_GRAY
                    celdaBalanceDia.setPadding(5f)
                    tablaDiario.addCell(celdaBalanceDia)
                    
                    // Ordenar días
                    val diasOrdenados = datosPorDia.keys.sorted()
                    
                    // Filas de datos diarios
                    for (dia in diasOrdenados) {
                        val datosDia = datosPorDia[dia] ?: continue
                        
                        // Celda día
                        val celdaDiaValor = PdfPCell(Phrase("$dia", fontNormal))
                        celdaDiaValor.horizontalAlignment = Element.ALIGN_CENTER
                        celdaDiaValor.setPadding(5f)
                        tablaDiario.addCell(celdaDiaValor)
                        
                        // Ingresos del día
                        val ingresosDia = (datosDia["ingresos"] as? Double) ?: 0.0
                        val celdaIngresosDiaValor = PdfPCell(Phrase(numberFormat.format(ingresosDia), fontNormal))
                        celdaIngresosDiaValor.horizontalAlignment = Element.ALIGN_RIGHT
                        celdaIngresosDiaValor.setPadding(5f)
                        tablaDiario.addCell(celdaIngresosDiaValor)
                        
                        // Gastos del día
                        val gastosDia = (datosDia["gastos"] as? Double) ?: 0.0
                        val celdaGastosDiaValor = PdfPCell(Phrase(numberFormat.format(gastosDia), fontNormal))
                        celdaGastosDiaValor.horizontalAlignment = Element.ALIGN_RIGHT
                        celdaGastosDiaValor.setPadding(5f)
                        tablaDiario.addCell(celdaGastosDiaValor)
                        
                        // Balance del día
                        val balanceDia = ingresosDia - gastosDia
                        val colorBalanceDia = if (balanceDia >= 0) BaseColor(0, 150, 0) else BaseColor(200, 0, 0)
                        val fontBalanceDia = FontFactory.getFont(FontFactory.HELVETICA, 10f, colorBalanceDia)
                        val celdaBalanceDiaValor = PdfPCell(Phrase(numberFormat.format(balanceDia), fontBalanceDia))
                        celdaBalanceDiaValor.horizontalAlignment = Element.ALIGN_RIGHT
                        celdaBalanceDiaValor.setPadding(5f)
                        tablaDiario.addCell(celdaBalanceDiaValor)
                    }
                    
                    document.add(tablaDiario)
                    
                    // Añadir nota explicativa
                    document.add(Paragraph("\n"))
                    val notaDias = Paragraph(
                        "Nota: Los días sin actividad no se muestran en la tabla.",
                        fontSmall
                    )
                    notaDias.alignment = Element.ALIGN_LEFT
                    document.add(notaDias)
                }
                
                // Pie de página
                document.add(Paragraph("\n\n"))
                val footer = Paragraph("TaxiFlash - Gestión Eficiente para Taxistas", fontSmall)
                footer.alignment = Element.ALIGN_CENTER
                document.add(footer)
                
                // Cerrar el documento
                document.close()
                
                // Devolver Uri del archivo generado
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    pdfFile
                )
                
                Log.d(TAG, "PDF generado con éxito. URI: $uri")
                return uri
            } catch (e: Exception) {
                Log.e(TAG, "Error al generar PDF: ${e.message}", e)
                e.printStackTrace()
                return null
            }
        }
        
        /**
         * Abre el PDF generado
         * @param context Contexto de la aplicación
         * @param pdfUri Uri del archivo PDF
         */
        fun abrirPdf(context: Context, pdfUri: Uri) {
            Log.d(TAG, "Intentando abrir PDF con URI: $pdfUri")
            
            // Verificar que el archivo existe
            try {
                val inputStream = context.contentResolver.openInputStream(pdfUri)
                inputStream?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error al verificar el archivo PDF: ${e.message}", e)
                Toast.makeText(context, "Error: el archivo PDF no existe o no es accesible", Toast.LENGTH_LONG).show()
                return
            }
            
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(pdfUri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            try {
                Log.d(TAG, "Iniciando actividad para visualizar el PDF")
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo abrir el PDF: ${e.message}", e)
                
                // Mostrar mensaje sugiriendo instalar una app para ver PDFs
                val errorMsg = "No se pudo abrir el PDF. ¿Tienes instalada una aplicación para visualizar PDFs?"
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
} 