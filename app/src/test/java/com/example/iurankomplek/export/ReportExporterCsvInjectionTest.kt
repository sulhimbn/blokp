package com.example.iurankomplek.export

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The exporter writes API-supplied values straight into CSV, so every cell has to be
 * neutralised against spreadsheet formula execution before it reaches a file.
 */
class ReportExporterCsvInjectionTest {

    @Test
    fun `a leading equals sign is neutralised`() {
        assertEquals(
            "'=cmd|' /C calc'!A0",
            ReportExporter.sanitizeCsvCell("=cmd|' /C calc'!A0")
        )
    }

    @Test
    fun `a leading plus sign is neutralised`() {
        assertEquals("'+1+1", ReportExporter.sanitizeCsvCell("+1+1"))
    }

    @Test
    fun `a leading minus sign is neutralised`() {
        assertEquals("'-2+3+cmd|' /C calc'!A0", ReportExporter.sanitizeCsvCell("-2+3+cmd|' /C calc'!A0"))
    }

    @Test
    fun `a leading at sign is neutralised`() {
        assertEquals("'@SUM(1+1)*cmd|' /C calc'!A0", ReportExporter.sanitizeCsvCell("@SUM(1+1)*cmd|' /C calc'!A0"))
    }

    @Test
    fun `a leading tab is neutralised`() {
        assertEquals("'\t=1+1", ReportExporter.sanitizeCsvCell("\t=1+1"))
    }

    @Test
    fun `a formula that does not start in the first cell is untouched`() {
        assertEquals("John Doe", ReportExporter.sanitizeCsvCell("John Doe"))
        assertEquals("john@example.com", ReportExporter.sanitizeCsvCell("john@example.com"))
    }

    @Test
    fun `ordinary financial values survive unchanged`() {
        assertEquals("150000", ReportExporter.sanitizeCsvCell(150000))
        assertEquals("Rp.150.000", ReportExporter.sanitizeCsvCell("Rp.150.000"))
    }

    @Test
    fun `null becomes an empty cell`() {
        assertEquals("", ReportExporter.sanitizeCsvCell(null))
    }

    @Test
    fun `an empty cell stays empty`() {
        assertEquals("", ReportExporter.sanitizeCsvCell(""))
    }

    @Test
    fun `a name field carrying a payload is neutralised after concatenation`() {
        val attack = ReportExporter.sanitizeCsvCell("@HYPERLINK(\"http://evil.example\",\"tap\") Doe")

        assertEquals("'@HYPERLINK(\"http://evil.example\",\"tap\") Doe", attack)
        assertEquals("'", attack.first().toString())
    }
}