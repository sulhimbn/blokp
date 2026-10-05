package com.example.iurankomplek.network

import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Contract-drift guard between the mock API fixtures and the Kotlin models.
 *
 * The mock server (mock-api/app.py) is what the app talks to in debug and Docker mode, so a change
 * to its JSON that no longer matches DataItem/UserResponse/PemanfaatanResponse would silently break
 * every screen. Parsing the checked-in fixtures through the same Gson configuration the app uses
 * turns that into a test failure.
 *
 * Live route coverage is asserted separately in docs: app.py serves only /users and /pemanfaatan,
 * while ApiService also declares announcements, messages, vendors and work orders.
 */
class MockApiContractTest {

    private val gson = Gson()

    private fun fixture(name: String): String {
        val candidates = listOf(
            File("../mock-api/mock-data/$name"),
            File("mock-api/mock-data/$name"),
            File(System.getProperty("user.dir"), "../mock-api/mock-data/$name")
        )
        val file = candidates.firstOrNull { it.isFile }
            ?: error("Fixture $name not found; looked in ${candidates.map { it.absolutePath }}")
        return file.readText()
    }

    @Test
    fun `users fixture should parse into UserResponse with every DataItem field populated`() {
        val parsed = gson.fromJson(fixture("users.json"), UserResponse::class.java)

        assertEquals(2, parsed.data.size)
        val john = parsed.data.first()
        assertEquals("John", john.first_name)
        assertEquals("Doe", john.last_name)
        assertEquals("john@example.com", john.email)
        assertEquals("Jl. Contoh No. 1", john.alamat)
        assertEquals(150000, john.iuran_perwarga)
        assertEquals(1800000, john.total_iuran_rekap)
        assertEquals(150000, john.jumlah_iuran_bulanan)
        assertEquals(150000, john.total_iuran_individu)
        assertEquals(50000, john.pengeluaran_iuran_warga)
        assertEquals("Perbaikan jalan komplek", john.pemanfaatan_iuran)
        assertEquals("https://example.com/avatar1.jpg", john.avatar)
    }

    @Test
    fun `pemanfaatan fixture should parse into PemanfaatanResponse`() {
        val parsed = gson.fromJson(fixture("pemanfaatan.json"), PemanfaatanResponse::class.java)

        assertEquals(2, parsed.data.size)
        assertEquals("Pembangunan taman", parsed.data[1].pemanfaatan_iuran)
        assertEquals(75000, parsed.data[1].pengeluaran_iuran_warga)
    }

    @Test
    fun `every fixture row should carry all eleven DataItem keys`() {
        val expectedKeys = setOf(
            "first_name", "last_name", "email", "alamat", "iuran_perwarga",
            "total_iuran_rekap", "jumlah_iuran_bulanan", "total_iuran_individu",
            "pengeluaran_iuran_warga", "pemanfaatan_iuran", "avatar"
        )

        listOf("users.json", "pemanfaatan.json").forEach { name ->
            val root = gson.fromJson(fixture(name), Map::class.java) as Map<*, *>
            @Suppress("UNCHECKED_CAST")
            val rows = root["data"] as List<Map<String, Any?>>
            assertTrue("$name should have rows", rows.isNotEmpty())
            rows.forEachIndexed { index, row ->
                assertEquals(
                    "$name row $index keys drifted from the DataItem contract",
                    expectedKeys,
                    row.keys
                )
            }
        }
    }

    @Test
    fun `fixture financials should satisfy FinancialCalculator validation`() {
        val parsed = gson.fromJson(fixture("users.json"), UserResponse::class.java)

        assertTrue(
            "Fixture rows must pass validation, otherwise screens render an invalid summary",
            com.example.iurankomplek.utils.FinancialCalculator.validateDataItems(parsed.data)
        )
        assertTrue(
            "Fixture totals must be positive",
            com.example.iurankomplek.utils.FinancialCalculator
                .calculateTotalIuranIndividu(parsed.data) > 0
        )
        assertNotNull(parsed.data.first().avatar)
    }
}