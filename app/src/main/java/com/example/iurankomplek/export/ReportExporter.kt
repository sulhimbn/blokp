package com.example.iurankomplek.export

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.LaporanSummaryItem
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportExporter(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    fun exportFinancialReport(
        dataItems: List<DataItem>,
        summaryItems: List<LaporanSummaryItem>,
        format: ExportFormat
    ): Result<ExportResult> {
        return try {
            val timestamp = dateFormat.format(Date())
            val fileName = "Financial_Report_$timestamp.${format.getFileExtension()}"
            val file = when (format) {
                ExportFormat.PDF -> generatePdfReport(dataItems, summaryItems, fileName)
                ExportFormat.CSV -> generateCsvReport(dataItems, summaryItems, fileName)
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Result.success(ExportResult(file, uri, format))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generatePdfReport(
        dataItems: List<DataItem>,
        summaryItems: List<LaporanSummaryItem>,
        fileName: String
    ): File {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        PdfWriter(file.absolutePath).use { writer ->
            PdfDocument(writer).use { pdfDoc ->
                Document(pdfDoc).use { document ->
                    addPdfHeader(document)
                    addPdfSummary(document, summaryItems)
                    addPdfDetails(document, dataItems)
                }
            }
        }
        
        return file
    }

    private fun addPdfHeader(document: Document) {
        document.add(
            Paragraph("Laporan Keuangan Iuran")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
        )
        
        document.add(
            Paragraph("Generated: ${displayDateFormat.format(Date())}")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10f)
        )
        
        document.add(Paragraph("").setHeight(20f))
    }

    private fun addPdfSummary(document: Document, summaryItems: List<LaporanSummaryItem>) {
        document.add(
            Paragraph("Ringkasan")
                .setFontSize(14f)
                .setBold()
        )
        
        val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .setWidth(UnitValue.createPercentValue(100f))
        
        summaryItems.forEach { item ->
            summaryTable.addCell(createCell(item.label))
            summaryTable.addCell(createCell(item.value, TextAlignment.RIGHT))
        }
        
        document.add(summaryTable)
        document.add(Paragraph("").setHeight(20f))
    }

    private fun addPdfDetails(document: Document, dataItems: List<DataItem>) {
        document.add(
            Paragraph("Detail Pemanfaatan Iuran")
                .setFontSize(14f)
                .setBold()
        )
        
        val detailTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 40f, 30f)))
            .setWidth(UnitValue.createPercentValue(100f))
        
        detailTable.addHeaderCell(createHeaderCell("Nama"))
        detailTable.addHeaderCell(createHeaderCell("Pemanfaatan"))
        detailTable.addHeaderCell(createHeaderCell("Pengeluaran"))
        
        dataItems.forEach { item ->
            detailTable.addCell(createCell("${item.first_name} ${item.last_name}"))
            detailTable.addCell(createCell(item.pemanfaatan_iuran))
            detailTable.addCell(createCell(
                "Rp ${item.pengeluaran_iuran_warga}",
                TextAlignment.RIGHT
            ))
        }
        
        document.add(detailTable)
    }

    private fun createCell(text: String, alignment: TextAlignment = TextAlignment.LEFT): Cell {
        return Cell().add(Paragraph(text))
            .setTextAlignment(alignment)
            .setPadding(5f)
    }

    private fun createHeaderCell(text: String): Cell {
        return Cell().add(Paragraph(text).setBold())
            .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
            .setPadding(5f)
    }

    private fun generateCsvReport(
        dataItems: List<DataItem>,
        summaryItems: List<LaporanSummaryItem>,
        fileName: String
    ): File {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        FileWriter(file).use { writer ->
            val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
            
            csvPrinter.printRecord("LAPORAN KEUANGAN IURAN")
            csvPrinter.printRecord("Generated:", displayDateFormat.format(Date()))
            csvPrinter.println()
            
            csvPrinter.printRecord("RINGKASAN")
            summaryItems.forEach { item ->
                csvPrinter.printRecord(item.label, item.value)
            }
            csvPrinter.println()
            
            csvPrinter.printRecord("DETAIL PEMANFAATAN IURAN")
            csvPrinter.printRecord("Nama", "Email", "Pemanfaatan", "Pengeluaran")
            
            dataItems.forEach { item ->
                csvPrinter.printRecord(
                    "${item.first_name} ${item.last_name}",
                    item.email,
                    item.pemanfaatan_iuran,
                    item.pengeluaran_iuran_warga
                )
            }
            
            csvPrinter.flush()
        }
        
        return file
    }

    fun createShareIntent(uri: Uri, format: ExportFormat): android.content.Intent {
        return android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = format.getMimeType()
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(
                android.content.Intent.EXTRA_SUBJECT,
                "Financial Report ${displayDateFormat.format(Date())}"
            )
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    data class ExportResult(
        val file: File,
        val uri: Uri,
        val format: ExportFormat
    )
}
