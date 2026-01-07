package com.example.iurankomplek.data.entity

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class UserWithFinancialRecordsTest {

    @Test
    fun `create user with financial records should succeed`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val records = listOf(
            FinancialRecordEntity(
                id = 1,
                userId = 1,
                iuranPerwarga = 100,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 300,
                pengeluaranIuranWarga = 400,
                totalIuranRekap = 500,
                pemanfaatanIuran = "Maintenance"
            )
        )

        val userWithRecords = UserWithFinancialRecords(user, records)

        assertEquals(user, userWithRecords.user)
        assertEquals(1, userWithRecords.financialRecords.size)
        assertEquals(records[0], userWithRecords.financialRecords[0])
    }

    @Test
    fun `create user with empty financial records should succeed`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val userWithRecords = UserWithFinancialRecords(user, emptyList())

        assertEquals(user, userWithRecords.user)
        assertTrue(userWithRecords.financialRecords.isEmpty())
    }

    @Test
    fun `create user with multiple financial records should succeed`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val now = Date()
        val earlier = Date(now.time - 1000000)

        val records = listOf(
            FinancialRecordEntity(
                id = 1,
                userId = 1,
                iuranPerwarga = 100,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 300,
                pengeluaranIuranWarga = 400,
                totalIuranRekap = 500,
                pemanfaatanIuran = "Maintenance 1",
                updatedAt = earlier
            ),
            FinancialRecordEntity(
                id = 2,
                userId = 1,
                iuranPerwarga = 150,
                jumlahIuranBulanan = 250,
                totalIuranIndividu = 350,
                pengeluaranIuranWarga = 450,
                totalIuranRekap = 550,
                pemanfaatanIuran = "Maintenance 2",
                updatedAt = now
            )
        )

        val userWithRecords = UserWithFinancialRecords(user, records)

        assertEquals(user, userWithRecords.user)
        assertEquals(2, userWithRecords.financialRecords.size)
    }

    @Test
    fun `latestFinancialRecord should return most recent by updatedAt`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val now = Date()
        val earlier = Date(now.time - 1000000)
        val evenEarlier = Date(now.time - 2000000)

        val records = listOf(
            FinancialRecordEntity(
                id = 1,
                userId = 1,
                iuranPerwarga = 100,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 300,
                pengeluaranIuranWarga = 400,
                totalIuranRekap = 500,
                pemanfaatanIuran = "Maintenance 1",
                updatedAt = evenEarlier
            ),
            FinancialRecordEntity(
                id = 2,
                userId = 1,
                iuranPerwarga = 150,
                jumlahIuranBulanan = 250,
                totalIuranIndividu = 350,
                pengeluaranIuranWarga = 450,
                totalIuranRekap = 550,
                pemanfaatanIuran = "Maintenance 2",
                updatedAt = now
            ),
            FinancialRecordEntity(
                id = 3,
                userId = 1,
                iuranPerwarga = 200,
                jumlahIuranBulanan = 300,
                totalIuranIndividu = 400,
                pengeluaranIuranWarga = 500,
                totalIuranRekap = 600,
                pemanfaatanIuran = "Maintenance 3",
                updatedAt = earlier
            )
        )

        val userWithRecords = UserWithFinancialRecords(user, records)

        val latest = userWithRecords.latestFinancialRecord
        assertNotNull(latest)
        assertEquals(2L, latest?.id)
        assertEquals("Maintenance 2", latest?.pemanfaatanIuran)
    }

    @Test
    fun `latestFinancialRecord with single record should return that record`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )

        val userWithRecords = UserWithFinancialRecords(user, listOf(record))

        val latest = userWithRecords.latestFinancialRecord
        assertNotNull(latest)
        assertEquals(1L, latest?.id)
        assertEquals(record, latest)
    }

    @Test
    fun `latestFinancialRecord with empty list should return null`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val userWithRecords = UserWithFinancialRecords(user, emptyList())

        assertNull(userWithRecords.latestFinancialRecord)
    }

    @Test
    fun `latestFinancialRecord with same timestamps should return one of them`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val sameTime = Date()

        val records = listOf(
            FinancialRecordEntity(
                id = 1,
                userId = 1,
                iuranPerwarga = 100,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 300,
                pengeluaranIuranWarga = 400,
                totalIuranRekap = 500,
                pemanfaatanIuran = "Maintenance 1",
                updatedAt = sameTime
            ),
            FinancialRecordEntity(
                id = 2,
                userId = 1,
                iuranPerwarga = 150,
                jumlahIuranBulanan = 250,
                totalIuranIndividu = 350,
                pengeluaranIuranWarga = 450,
                totalIuranRekap = 550,
                pemanfaatanIuran = "Maintenance 2",
                updatedAt = sameTime
            )
        )

        val userWithRecords = UserWithFinancialRecords(user, records)

        val latest = userWithRecords.latestFinancialRecord
        assertNotNull(latest)
        assertTrue(latest!!.id == 1L || latest.id == 2L)
        assertTrue(records.contains(latest))
    }

    @Test
    fun `data class equality should work correctly`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val records = listOf(
            FinancialRecordEntity(
                id = 1,
                userId = 1,
                iuranPerwarga = 100,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 300,
                pengeluaranIuranWarga = 400,
                totalIuranRekap = 500,
                pemanfaatanIuran = "Maintenance"
            )
        )

        val userWithRecords1 = UserWithFinancialRecords(user, records)
        val userWithRecords2 = UserWithFinancialRecords(user, records)

        assertEquals(userWithRecords1, userWithRecords2)
        assertEquals(userWithRecords1.hashCode(), userWithRecords2.hashCode())
    }

    @Test
    fun `data class copy should work correctly`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val records = listOf(
            FinancialRecordEntity(
                id = 1,
                userId = 1,
                iuranPerwarga = 100,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 300,
                pengeluaranIuranWarga = 400,
                totalIuranRekap = 500,
                pemanfaatanIuran = "Maintenance"
            )
        )

        val userWithRecords1 = UserWithFinancialRecords(user, records)
        val userWithRecords2 = userWithRecords1.copy(
            user = user.copy(firstName = "Jane")
        )

        assertEquals("John", userWithRecords1.user.firstName)
        assertEquals("Jane", userWithRecords2.user.firstName)
        assertEquals(records, userWithRecords2.financialRecords)
    }

    @Test
    fun `user properties should be accessible through embedded user`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val userWithRecords = UserWithFinancialRecords(user, emptyList())

        assertEquals(1L, userWithRecords.user.id)
        assertEquals("test@example.com", userWithRecords.user.email)
        assertEquals("John", userWithRecords.user.firstName)
        assertEquals("Doe", userWithRecords.user.lastName)
        assertEquals("123 Main St", userWithRecords.user.alamat)
        assertEquals("https://example.com/avatar.jpg", userWithRecords.user.avatar)
    }

    @Test
    fun `user fullName should work through embedded user`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val userWithRecords = UserWithFinancialRecords(user, emptyList())

        assertEquals("John Doe", userWithRecords.user.fullName)
    }

    @Test
    fun `create with large number of financial records should succeed`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val records = (1..100).map { i ->
            FinancialRecordEntity(
                id = i.toLong(),
                userId = 1,
                iuranPerwarga = 100 * i,
                jumlahIuranBulanan = 200 * i,
                totalIuranIndividu = 300 * i,
                pengeluaranIuranWarga = 400 * i,
                totalIuranRekap = 500 * i,
                pemanfaatanIuran = "Maintenance $i",
                updatedAt = Date(System.currentTimeMillis() - i * 1000L)
            )
        }

        val userWithRecords = UserWithFinancialRecords(user, records)

        assertEquals(100, userWithRecords.financialRecords.size)
        assertEquals(1L, userWithRecords.latestFinancialRecord?.id)
    }

    @Test
    fun `should handle financial records with different user ids`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val records = listOf(
            FinancialRecordEntity(
                id = 1,
                userId = 1,
                iuranPerwarga = 100,
                jumlahIuranBulanan = 200,
                totalIuranIndividu = 300,
                pengeluaranIuranWarga = 400,
                totalIuranRekap = 500,
                pemanfaatanIuran = "Maintenance"
            ),
            FinancialRecordEntity(
                id = 2,
                userId = 2,
                iuranPerwarga = 150,
                jumlahIuranBulanan = 250,
                totalIuranIndividu = 350,
                pengeluaranIuranWarga = 450,
                totalIuranRekap = 550,
                pemanfaatanIuran = "Repair"
            )
        )

        val userWithRecords = UserWithFinancialRecords(user, records)

        assertEquals(2, userWithRecords.financialRecords.size)
        assertNotNull(userWithRecords.latestFinancialRecord)
    }
}
