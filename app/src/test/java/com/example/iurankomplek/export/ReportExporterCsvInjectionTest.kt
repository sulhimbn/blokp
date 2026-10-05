package com.example.iurankomplek.export

import org.junit.Assert.assertEquals
import org.junit.Test

class ReportExporterCsvInjectionTest {

    @Test
    fun `neutralises leading formula characters`() {
        listOf(
            "=1+1",
            "+1+1",
            "-1+1",
            "@SUM(A1:A2)",
            "\t=cmd",
            "\r=cmd"
        ).forEach { payload ->
            val cell = csvCell(payload)
            assertEquals("'$payload", cell)
            assertEquals(
                "cell must not start with an executable formula character",
                '\'',
                cell.first()
            )
        }
    }

    @Test
    fun `leaves ordinary values untouched`() {
        assertEquals("John Doe", csvCell("John Doe"))
        assertEquals("Perbaikan jalan", csvCell("Perbaikan jalan"))
        assertEquals("john@example.com", csvCell("john@example.com"))
        assertEquals("10000", csvCell(10000))
        assertEquals("", csvCell(null))
        assertEquals("", csvCell(""))
    }

    @Test
    fun `only the leading character matters`() {
        assertEquals("total=100", csvCell("total=100"))
        assertEquals("a+b", csvCell("a+b"))
    }
}