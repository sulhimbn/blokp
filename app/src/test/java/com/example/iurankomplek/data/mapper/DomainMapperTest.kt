package com.example.iurankomplek.data.mapper

import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.domain.model.FinancialRecord
import com.example.iurankomplek.domain.model.User
import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DomainMapperTest {

    @Test
    fun toDomainModel_convertsUserEntityToUser() {
        val entity = UserEntity(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg",
            createdAt = Date(),
            updatedAt = Date()
        )

        val domainModel = DomainMapper.toDomainModel(entity)

        assertEquals(1L, domainModel.id)
        assertEquals("test@example.com", domainModel.email)
        assertEquals("John", domainModel.firstName)
        assertEquals("Doe", domainModel.lastName)
        assertEquals("123 Main St", domainModel.alamat)
        assertEquals("http://example.com/avatar.jpg", domainModel.avatar)
    }

    @Test
    fun toDomainModelList_convertsUserEntityListToUserList() {
        val entities = listOf(
            UserEntity(
                id = 1L,
                email = "test1@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar1.jpg",
                createdAt = Date(),
                updatedAt = Date()
            ),
            UserEntity(
                id = 2L,
                email = "test2@example.com",
                firstName = "Jane",
                lastName = "Smith",
                alamat = "456 Oak Ave",
                avatar = "http://example.com/avatar2.jpg",
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        val domainModels = DomainMapper.toDomainModelList(entities)

        assertEquals(2, domainModels.size)
        assertEquals(1L, domainModels[0].id)
        assertEquals("test1@example.com", domainModels[0].email)
        assertEquals(2L, domainModels[1].id)
        assertEquals("test2@example.com", domainModels[1].email)
    }

    @Test
    fun toDomainModelList_convertsEmptyUserEntityList() {
        val entities = emptyList<UserEntity>()

        val domainModels = DomainMapper.toDomainModelList(entities)

        assertEquals(0, domainModels.size)
    }

    @Test
    fun fromDomainModel_convertsUserToUserEntity() {
        val domainModel = User(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg"
        )

        val entity = DomainMapper.fromDomainModel(domainModel)

        assertEquals(1L, entity.id)
        assertEquals("test@example.com", entity.email)
        assertEquals("John", entity.firstName)
        assertEquals("Doe", entity.lastName)
        assertEquals("123 Main St", entity.alamat)
        assertEquals("http://example.com/avatar.jpg", entity.avatar)
        assertNotNull(entity.createdAt)
        assertNotNull(entity.updatedAt)
    }

    @Test
    fun fromDomainModelList_convertsUserListToUserEntityList() {
        val domainModels = listOf(
            User(
                id = 1L,
                email = "test1@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar1.jpg"
            ),
            User(
                id = 2L,
                email = "test2@example.com",
                firstName = "Jane",
                lastName = "Smith",
                alamat = "456 Oak Ave",
                avatar = "http://example.com/avatar2.jpg"
            )
        )

        val entities = DomainMapper.fromDomainModelList(domainModels)

        assertEquals(2, entities.size)
        assertEquals(1L, entities[0].id)
        assertEquals("test1@example.com", entities[0].email)
        assertEquals(2L, entities[1].id)
        assertEquals("test2@example.com", entities[1].email)
        assertNotNull(entities[0].createdAt)
        assertNotNull(entities[0].updatedAt)
        assertNotNull(entities[1].createdAt)
        assertNotNull(entities[1].updatedAt)
    }

    @Test
    fun fromDomainModelList_convertsEmptyUserList() {
        val domainModels = emptyList<User>()

        val entities = DomainMapper.fromDomainModelList(domainModels)

        assertEquals(0, entities.size)
    }

    @Test
    fun toDomainModel_convertsFinancialRecordEntityToFinancialRecord() {
        val entity = FinancialRecordEntity(
            id = 1L,
            userId = 100L,
            iuranPerwarga = 50000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 150000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Pembayaran listrik dan air bersih",
            createdAt = Date(),
            updatedAt = Date()
        )

        val domainModel = DomainMapper.toDomainModel(entity)

        assertEquals(1L, domainModel.id)
        assertEquals(100L, domainModel.userId)
        assertEquals(50000, domainModel.iuranPerwarga)
        assertEquals(100000, domainModel.jumlahIuranBulanan)
        assertEquals(300000, domainModel.totalIuranIndividu)
        assertEquals(150000, domainModel.pengeluaranIuranWarga)
        assertEquals(450000, domainModel.totalIuranRekap)
        assertEquals("Pembayaran listrik dan air bersih", domainModel.pemanfaatanIuran)
    }

    @Test
    fun toDomainModelList_convertsFinancialRecordEntityListToFinancialRecordList() {
        val entities = listOf(
            FinancialRecordEntity(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik",
                createdAt = Date(),
                updatedAt = Date()
            ),
            FinancialRecordEntity(
                id = 2L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran air bersih",
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        val domainModels = DomainMapper.toDomainModelList(entities)

        assertEquals(2, domainModels.size)
        assertEquals(1L, domainModels[0].id)
        assertEquals("Pembayaran listrik", domainModels[0].pemanfaatanIuran)
        assertEquals(2L, domainModels[1].id)
        assertEquals("Pembayaran air bersih", domainModels[1].pemanfaatanIuran)
    }

    @Test
    fun toDomainModelList_convertsEmptyFinancialRecordEntityList() {
        val entities = emptyList<FinancialRecordEntity>()

        val domainModels = DomainMapper.toDomainModelList(entities)

        assertEquals(0, domainModels.size)
    }

    @Test
    fun fromDomainModel_convertsFinancialRecordToFinancialRecordEntity() {
        val domainModel = FinancialRecord(
            id = 1L,
            userId = 100L,
            iuranPerwarga = 50000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 150000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Pembayaran listrik dan air bersih"
        )

        val entity = DomainMapper.fromDomainModel(domainModel)

        assertEquals(1L, entity.id)
        assertEquals(100L, entity.userId)
        assertEquals(50000, entity.iuranPerwarga)
        assertEquals(100000, entity.jumlahIuranBulanan)
        assertEquals(300000, entity.totalIuranIndividu)
        assertEquals(150000, entity.pengeluaranIuranWarga)
        assertEquals(450000, entity.totalIuranRekap)
        assertEquals("Pembayaran listrik dan air bersih", entity.pemanfaatanIuran)
        assertNotNull(entity.createdAt)
        assertNotNull(entity.updatedAt)
    }

    @Test
    fun fromDomainModelList_convertsFinancialRecordListToFinancialRecordEntityList() {
        val domainModels = listOf(
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            ),
            FinancialRecord(
                id = 2L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran air bersih"
            )
        )

        val entities = DomainMapper.fromDomainModelList(domainModels)

        assertEquals(2, entities.size)
        assertEquals(1L, entities[0].id)
        assertEquals("Pembayaran listrik", entities[0].pemanfaatanIuran)
        assertEquals(2L, entities[1].id)
        assertEquals("Pembayaran air bersih", entities[1].pemanfaatanIuran)
        assertNotNull(entities[0].createdAt)
        assertNotNull(entities[0].updatedAt)
        assertNotNull(entities[1].createdAt)
        assertNotNull(entities[1].updatedAt)
    }

    @Test
    fun fromDomainModelList_convertsEmptyFinancialRecordList() {
        val domainModels = emptyList<FinancialRecord>()

        val entities = DomainMapper.fromDomainModelList(domainModels)

        assertEquals(0, entities.size)
    }

    @Test
    fun toDomainModel_preservesUserEntityAllFields() {
        val entity = UserEntity(
            id = 999L,
            email = "very.long.email.address@example.test.domain.com",
            firstName = "Christopher",
            lastName = "Alexander",
            alamat = "Very Long Street Address with Numbers and Special Characters, Apt 123",
            avatar = "https://example.com/images/avatars/very-long-avatar-path.jpg",
            createdAt = Date(123456789),
            updatedAt = Date(987654321)
        )

        val domainModel = DomainMapper.toDomainModel(entity)

        assertEquals(999L, domainModel.id)
        assertEquals("very.long.email.address@example.test.domain.com", domainModel.email)
        assertEquals("Christopher", domainModel.firstName)
        assertEquals("Alexander", domainModel.lastName)
        assertEquals("Very Long Street Address with Numbers and Special Characters, Apt 123", domainModel.alamat)
        assertEquals("https://example.com/images/avatars/very-long-avatar-path.jpg", domainModel.avatar)
    }

    @Test
    fun toDomainModel_preservesFinancialRecordEntityAllFields() {
        val entity = FinancialRecordEntity(
            id = 999L,
            userId = 888L,
            iuranPerwarga = 999999999,
            jumlahIuranBulanan = 888888888,
            totalIuranIndividu = 777777777,
            pengeluaranIuranWarga = 666666666,
            totalIuranRekap = 555555555,
            pemanfaatanIuran = "Very long description of pemanfaatan iuran with details and numbers 12345",
            createdAt = Date(123456789),
            updatedAt = Date(987654321)
        )

        val domainModel = DomainMapper.toDomainModel(entity)

        assertEquals(999L, domainModel.id)
        assertEquals(888L, domainModel.userId)
        assertEquals(999999999, domainModel.iuranPerwarga)
        assertEquals(888888888, domainModel.jumlahIuranBulanan)
        assertEquals(777777777, domainModel.totalIuranIndividu)
        assertEquals(666666666, domainModel.pengeluaranIuranWarga)
        assertEquals(555555555, domainModel.totalIuranRekap)
        assertEquals("Very long description of pemanfaatan iuran with details and numbers 12345", domainModel.pemanfaatanIuran)
    }

    @Test
    fun fromDomainModel_toDomainModel_roundTripForUser() {
        val originalDomainModel = User(
            id = 123L,
            email = "roundtrip@example.com",
            firstName = "Round",
            lastName = "Trip",
            alamat = "Test Street",
            avatar = "http://example.com/avatar.jpg"
        )

        val entity = DomainMapper.fromDomainModel(originalDomainModel)
        val backToDomainModel = DomainMapper.toDomainModel(entity)

        assertEquals(originalDomainModel.id, backToDomainModel.id)
        assertEquals(originalDomainModel.email, backToDomainModel.email)
        assertEquals(originalDomainModel.firstName, backToDomainModel.firstName)
        assertEquals(originalDomainModel.lastName, backToDomainModel.lastName)
        assertEquals(originalDomainModel.alamat, backToDomainModel.alamat)
        assertEquals(originalDomainModel.avatar, backToDomainModel.avatar)
    }

    @Test
    fun fromDomainModel_toDomainModel_roundTripForFinancialRecord() {
        val originalDomainModel = FinancialRecord(
            id = 123L,
            userId = 456L,
            iuranPerwarga = 100000,
            jumlahIuranBulanan = 200000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 150000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Round trip test"
        )

        val entity = DomainMapper.fromDomainModel(originalDomainModel)
        val backToDomainModel = DomainMapper.toDomainModel(entity)

        assertEquals(originalDomainModel.id, backToDomainModel.id)
        assertEquals(originalDomainModel.userId, backToDomainModel.userId)
        assertEquals(originalDomainModel.iuranPerwarga, backToDomainModel.iuranPerwarga)
        assertEquals(originalDomainModel.jumlahIuranBulanan, backToDomainModel.jumlahIuranBulanan)
        assertEquals(originalDomainModel.totalIuranIndividu, backToDomainModel.totalIuranIndividu)
        assertEquals(originalDomainModel.pengeluaranIuranWarga, backToDomainModel.pengeluaranIuranWarga)
        assertEquals(originalDomainModel.totalIuranRekap, backToDomainModel.totalIuranRekap)
        assertEquals(originalDomainModel.pemanfaatanIuran, backToDomainModel.pemanfaatanIuran)
    }

    @Test
    fun fromDomainModel_toDomainModelList_roundTripForUserList() {
        val originalDomainModels = listOf(
            User(
                id = 1L,
                email = "test1@example.com",
                firstName = "User",
                lastName = "One",
                alamat = "Address 1",
                avatar = "http://example.com/avatar1.jpg"
            ),
            User(
                id = 2L,
                email = "test2@example.com",
                firstName = "User",
                lastName = "Two",
                alamat = "Address 2",
                avatar = "http://example.com/avatar2.jpg"
            ),
            User(
                id = 3L,
                email = "test3@example.com",
                firstName = "User",
                lastName = "Three",
                alamat = "Address 3",
                avatar = "http://example.com/avatar3.jpg"
            )
        )

        val entities = DomainMapper.fromDomainModelList(originalDomainModels)
        val backToDomainModels = DomainMapper.toDomainModelList(entities)

        assertEquals(originalDomainModels.size, backToDomainModels.size)
        for (i in originalDomainModels.indices) {
            assertEquals(originalDomainModels[i].id, backToDomainModels[i].id)
            assertEquals(originalDomainModels[i].email, backToDomainModels[i].email)
            assertEquals(originalDomainModels[i].firstName, backToDomainModels[i].firstName)
            assertEquals(originalDomainModels[i].lastName, backToDomainModels[i].lastName)
            assertEquals(originalDomainModels[i].alamat, backToDomainModels[i].alamat)
            assertEquals(originalDomainModels[i].avatar, backToDomainModels[i].avatar)
        }
    }

    @Test
    fun fromDomainModel_toDomainModelList_roundTripForFinancialRecordList() {
        val originalDomainModels = listOf(
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Record 1"
            ),
            FinancialRecord(
                id = 2L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Record 2"
            ),
            FinancialRecord(
                id = 3L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Record 3"
            )
        )

        val entities = DomainMapper.fromDomainModelList(originalDomainModels)
        val backToDomainModels = DomainMapper.toDomainModelList(entities)

        assertEquals(originalDomainModels.size, backToDomainModels.size)
        for (i in originalDomainModels.indices) {
            assertEquals(originalDomainModels[i].id, backToDomainModels[i].id)
            assertEquals(originalDomainModels[i].userId, backToDomainModels[i].userId)
            assertEquals(originalDomainModels[i].iuranPerwarga, backToDomainModels[i].iuranPerwarga)
            assertEquals(originalDomainModels[i].jumlahIuranBulanan, backToDomainModels[i].jumlahIuranBulanan)
            assertEquals(originalDomainModels[i].totalIuranIndividu, backToDomainModels[i].totalIuranIndividu)
            assertEquals(originalDomainModels[i].pengeluaranIuranWarga, backToDomainModels[i].pengeluaranIuranWarga)
            assertEquals(originalDomainModels[i].totalIuranRekap, backToDomainModels[i].totalIuranRekap)
            assertEquals(originalDomainModels[i].pemanfaatanIuran, backToDomainModels[i].pemanfaatanIuran)
        }
    }
}
