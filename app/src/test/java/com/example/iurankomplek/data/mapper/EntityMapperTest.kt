package com.example.iurankomplek.data.mapper

import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class EntityMapperTest {

    private lateinit var testDto: LegacyDataItemDto
    private lateinit var testUserEntity: UserEntity
    private lateinit var testFinancialEntity: FinancialRecordEntity

    @Before
    fun setup() {
        testDto = LegacyDataItemDto(
            first_name = "John",
            last_name = "Doe",
            email = "john.doe@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100000.0,
            total_iuran_rekap = 300000.0,
            jumlah_iuran_bulanan = 100000.0,
            total_iuran_individu = 200000.0,
            pengeluaran_iuran_warga = 50000.0,
            pemanfaatan_iuran = 250000.0,
            avatar = "https://example.com/avatar.jpg"
        )

        testUserEntity = UserEntity(
            id = 1,
            email = "john.doe@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg",
            createdAt = Date(),
            updatedAt = Date()
        )

        testFinancialEntity = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100000.0,
            jumlahIuranBulanan = 100000.0,
            totalIuranIndividu = 200000.0,
            pengeluaranIuranWarga = 50000.0,
            totalIuranRekap = 300000.0,
            pemanfaatanIuran = 250000.0,
            createdAt = Date(),
            updatedAt = Date()
        )
    }

    @Test
    fun `fromLegacyDto maps all fields correctly`() {
        // Act
        val result = EntityMapper.fromLegacyDto(testDto, userId = 1)

        // Assert - UserEntity
        assertEquals(1L, result.first.id)
        assertEquals(testDto.email, result.first.email)
        assertEquals(testDto.first_name, result.first.firstName)
        assertEquals(testDto.last_name, result.first.lastName)
        assertEquals(testDto.alamat, result.first.alamat)
        assertEquals(testDto.avatar, result.first.avatar)
        assertNotNull(result.first.createdAt)
        assertNotNull(result.first.updatedAt)

        // Assert - FinancialRecordEntity
        assertEquals(0L, result.second.id) // Should be 0 for new records
        assertEquals(1L, result.second.userId)
        assertEquals(testDto.iuran_perwarga, result.second.iuranPerwarga, 0.0)
        assertEquals(testDto.jumlah_iuran_bulanan, result.second.jumlahIuranBulanan, 0.0)
        assertEquals(testDto.total_iuran_individu, result.second.totalIuranIndividu, 0.0)
        assertEquals(testDto.pengeluaran_iuran_warga, result.second.pengeluaranIuranWarga, 0.0)
        assertEquals(testDto.total_iuran_rekap, result.second.totalIuranRekap, 0.0)
        assertEquals(testDto.pemanfaatan_iuran, result.second.pemanfaatanIuran, 0.0)
        assertNotNull(result.second.createdAt)
        assertNotNull(result.second.updatedAt)
    }

    @Test
    fun `fromLegacyDto handles null avatar field`() {
        // Arrange
        val dtoWithNullAvatar = testDto.copy(avatar = null)

        // Act
        val result = EntityMapper.fromLegacyDto(dtoWithNullAvatar, userId = 1)

        // Assert
        assertNull(result.first.avatar)
    }

    @Test
    fun `fromLegacyDto handles empty strings correctly`() {
        // Arrange
        val dtoWithEmptyFields = testDto.copy(
            first_name = "",
            last_name = "",
            alamat = "",
            avatar = ""
        )

        // Act
        val result = EntityMapper.fromLegacyDto(dtoWithEmptyFields, userId = 1)

        // Assert
        assertEquals("", result.first.firstName)
        assertEquals("", result.first.lastName)
        assertEquals("", result.first.alamat)
        assertEquals("", result.first.avatar)
    }

    @Test
    fun `fromLegacyDto handles zero values correctly`() {
        // Arrange
        val dtoWithZeroValues = testDto.copy(
            iuran_perwarga = 0.0,
            jumlah_iuran_bulanan = 0.0,
            total_iuran_individu = 0.0,
            pengeluaran_iuran_warga = 0.0,
            total_iuran_rekap = 0.0,
            pemanfaatan_iuran = 0.0
        )

        // Act
        val result = EntityMapper.fromLegacyDto(dtoWithZeroValues, userId = 1)

        // Assert
        assertEquals(0.0, result.second.iuranPerwarga, 0.0)
        assertEquals(0.0, result.second.jumlahIuranBulanan, 0.0)
        assertEquals(0.0, result.second.totalIuranIndividu, 0.0)
        assertEquals(0.0, result.second.pengeluaranIuranWarga, 0.0)
        assertEquals(0.0, result.second.totalIuranRekap, 0.0)
        assertEquals(0.0, result.second.pemanfaatanIuran, 0.0)
    }

    @Test
    fun `toLegacyDto maps all fields correctly`() {
        // Arrange
        val userWithFinancial = UserWithFinancialRecords(
            user = testUserEntity,
            financialRecords = listOf(testFinancialEntity)
        )

        // Act
        val result = EntityMapper.toLegacyDto(userWithFinancial)

        // Assert
        assertEquals(testUserEntity.firstName, result.first_name)
        assertEquals(testUserEntity.lastName, result.last_name)
        assertEquals(testUserEntity.email, result.email)
        assertEquals(testUserEntity.alamat, result.alamat)
        assertEquals(testUserEntity.avatar, result.avatar)
        assertEquals(testFinancialEntity.iuranPerwarga, result.iuran_perwarga, 0.0)
        assertEquals(testFinancialEntity.totalIuranRekap, result.total_iuran_rekap, 0.0)
        assertEquals(testFinancialEntity.jumlahIuranBulanan, result.jumlah_iuran_bulanan, 0.0)
        assertEquals(testFinancialEntity.totalIuranIndividu, result.total_iuran_individu, 0.0)
        assertEquals(testFinancialEntity.pengeluaranIuranWarga, result.pengeluaran_iuran_warga, 0.0)
        assertEquals(testFinancialEntity.pemanfaatanIuran, result.pemanfaatan_iuran, 0.0)
    }

    @Test
    fun `toLegacyDto throws exception when financial records are empty`() {
        // Arrange
        val userWithNoFinancial = UserWithFinancialRecords(
            user = testUserEntity,
            financialRecords = emptyList()
        )

        // Act & Assert
        try {
            EntityMapper.toLegacyDto(userWithNoFinancial)
            fail("Expected IllegalStateException to be thrown")
        } catch (e: IllegalStateException) {
            assertEquals("User must have at least one financial record", e.message)
        }
    }

    @Test
    fun `toLegacyDto throws exception when financial records are null`() {
        // Arrange
        val userWithNullFinancial = UserWithFinancialRecords(
            user = testUserEntity,
            financialRecords = null
        )

        // Act & Assert
        try {
            EntityMapper.toLegacyDto(userWithNullFinancial)
            fail("Expected IllegalStateException to be thrown")
        } catch (e: IllegalStateException) {
            assertEquals("User must have at least one financial record", e.message)
        }
    }

    @Test
    fun `toLegacyDto handles multiple financial records by using latest`() {
        // Arrange
        val oldRecord = testFinancialEntity.copy(
            id = 1,
            totalIuranRekap = 100000.0,
            updatedAt = Date(System.currentTimeMillis() - 1000000)
        )
        val newRecord = testFinancialEntity.copy(
            id = 2,
            totalIuranRekap = 300000.0,
            updatedAt = Date()
        )
        val userWithMultipleFinancial = UserWithFinancialRecords(
            user = testUserEntity,
            financialRecords = listOf(oldRecord, newRecord)
        )

        // Act
        val result = EntityMapper.toLegacyDto(userWithMultipleFinancial)

        // Assert - Should use the latest record based on updatedAt
        assertEquals(300000.0, result.total_iuran_rekap, 0.0)
    }

    @Test
    fun `fromLegacyDtoList maps list correctly with indexed userIds`() {
        // Arrange
        val dtoList = listOf(
            testDto.copy(email = "user1@example.com"),
            testDto.copy(email = "user2@example.com"),
            testDto.copy(email = "user3@example.com")
        )

        // Act
        val result = EntityMapper.fromLegacyDtoList(dtoList)

        // Assert
        assertEquals(3, result.size)
        assertEquals(1L, result[0].first.id)
        assertEquals(2L, result[1].first.id)
        assertEquals(3L, result[2].first.id)
        assertEquals("user1@example.com", result[0].first.email)
        assertEquals("user2@example.com", result[1].first.email)
        assertEquals("user3@example.com", result[2].first.email)
    }

    @Test
    fun `fromLegacyDtoList handles empty list`() {
        // Arrange
        val emptyList = emptyList<LegacyDataItemDto>()

        // Act
        val result = EntityMapper.fromLegacyDtoList(emptyList)

        // Assert
        assertEquals(0, result.size)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toLegacyDtoList maps list correctly`() {
        // Arrange
        val usersWithFinancials = listOf(
            UserWithFinancialRecords(
                user = testUserEntity.copy(id = 1, email = "user1@example.com"),
                financialRecords = listOf(testFinancialEntity.copy(userId = 1))
            ),
            UserWithFinancialRecords(
                user = testUserEntity.copy(id = 2, email = "user2@example.com"),
                financialRecords = listOf(testFinancialEntity.copy(userId = 2))
            )
        )

        // Act
        val result = EntityMapper.toLegacyDtoList(usersWithFinancials)

        // Assert
        assertEquals(2, result.size)
        assertEquals("user1@example.com", result[0].email)
        assertEquals("user2@example.com", result[1].email)
    }

    @Test
    fun `toLegacyDtoList handles empty list`() {
        // Arrange
        val emptyList = emptyList<UserWithFinancialRecords>()

        // Act
        val result = EntityMapper.toLegacyDtoList(emptyList)

        // Assert
        assertEquals(0, result.size)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `fromLegacyDto then toLegacyDto maintains data integrity`() {
        // Arrange
        val originalDto = testDto

        // Act
        val (user, financial) = EntityMapper.fromLegacyDto(originalDto, userId = 1)
        val userWithFinancial = UserWithFinancialRecords(
            user = user,
            financialRecords = listOf(financial)
        )
        val reconstructedDto = EntityMapper.toLegacyDto(userWithFinancial)

        // Assert
        assertEquals(originalDto.email, reconstructedDto.email)
        assertEquals(originalDto.first_name, reconstructedDto.first_name)
        assertEquals(originalDto.last_name, reconstructedDto.last_name)
        assertEquals(originalDto.alamat, reconstructedDto.alamat)
        assertEquals(originalDto.iuran_perwarga, reconstructedDto.iuran_perwarga, 0.0)
        assertEquals(originalDto.total_iuran_rekap, reconstructedDto.total_iuran_rekap, 0.0)
        assertEquals(originalDto.jumlah_iuran_bulanan, reconstructedDto.jumlah_iuran_bulanan, 0.0)
        assertEquals(originalDto.total_iuran_individu, reconstructedDto.total_iuran_individu, 0.0)
        assertEquals(originalDto.pengeluaran_iuran_warga, reconstructedDto.pengeluaran_iuran_warga, 0.0)
        assertEquals(originalDto.pemanfaatan_iuran, reconstructedDto.pemanfaatan_iuran, 0.0)
        assertEquals(originalDto.avatar, reconstructedDto.avatar)
    }

    @Test
    fun `userId parameter correctly sets userId in financial record`() {
        // Arrange
        val customUserId = 42L

        // Act
        val result = EntityMapper.fromLegacyDto(testDto, userId = customUserId)

        // Assert
        assertEquals(customUserId, result.first.id)
        assertEquals(customUserId, result.second.userId)
    }

    @Test
    fun `handles special characters in strings`() {
        // Arrange
        val dtoWithSpecialChars = testDto.copy(
            first_name = "José",
            last_name = "Niño",
            alamat = "Café #123",
            email = "test+user@example.com"
        )

        // Act
        val result = EntityMapper.fromLegacyDto(dtoWithSpecialChars, userId = 1)

        // Assert
        assertEquals("José", result.first.firstName)
        assertEquals("Niño", result.first.lastName)
        assertEquals("Café #123", result.first.alamat)
        assertEquals("test+user@example.com", result.first.email)
    }

    @Test
    fun `handles very large numeric values`() {
        // Arrange
        val dtoWithLargeValues = testDto.copy(
            iuran_perwarga = Double.MAX_VALUE / 2,
            total_iuran_rekap = Double.MAX_VALUE / 3
        )

        // Act
        val result = EntityMapper.fromLegacyDto(dtoWithLargeValues, userId = 1)

        // Assert
        assertEquals(Double.MAX_VALUE / 2, result.second.iuranPerwarga, 0.0)
        assertEquals(Double.MAX_VALUE / 3, result.second.totalIuranRekap, 0.0)
    }

    @Test
    fun `handles negative numeric values`() {
        // Arrange
        val dtoWithNegativeValues = testDto.copy(
            iuran_perwarga = -100.0,
            pengeluaran_iuran_warga = -50.0
        )

        // Act
        val result = EntityMapper.fromLegacyDto(dtoWithNegativeValues, userId = 1)

        // Assert - Note: This tests that the mapper preserves values even if they're invalid
        // Validation should be handled by DataValidator
        assertEquals(-100.0, result.second.iuranPerwarga, 0.0)
        assertEquals(-50.0, result.second.pengeluaranIuranWarga, 0.0)
    }
}
