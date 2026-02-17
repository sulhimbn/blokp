package com.example.iurankomplek.export

enum class ExportFormat {
    PDF,
    CSV;

    fun getFileExtension(): String {
        return when (this) {
            PDF -> "pdf"
            CSV -> "csv"
        }
    }

    fun getMimeType(): String {
        return when (this) {
            PDF -> "application/pdf"
            CSV -> "text/csv"
        }
    }

    fun getDisplayName(): String {
        return when (this) {
            PDF -> "PDF Document"
            CSV -> "CSV Spreadsheet"
        }
    }
}
