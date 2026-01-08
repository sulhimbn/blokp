package com.example.iurankomplek.data.cache

import com.example.iurankomplek.data.dao.FinancialRecordDao
import com.example.iurankomplek.data.dao.UserDao
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CacheHelperTest {

    @Mock
    private lateinit var mockUserDao: UserDao

    @Mock
    private lateinit var mockFinancialRecordDao: FinancialRecordDao

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun saveEntityWithFinancialRecords_emptyList_returnsEarly() = runTest {
        whenever(mockUserDao.getUsersByEmails(any())).thenReturn(emptyList())
        whenever(mockFinancialRecordDao.getFinancialRecordsByUserIds(any())).thenReturn(emptyList())

        CacheHelper.saveEntityWithFinancialRecords(emptyList())

        verify(mockUserDao, never()).insertAll(any())
        verify(mockUserDao, never()).updateAll(any())
        verify(mockFinancialRecordDao, never()).insertAll(any())
        verify(mockFinancialRecordDao, never()).updateAll(any())
    }

    @Test
    fun saveEntityWithFinancialRecords_singleNewUser_insertsUserAndFinancial() = runTest {
        val user = UserEntity(
            id = 0,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            alamat = "Test Address",
            avatar = "test.jpg"
        )
        val financial = FinancialRecordEntity(
            id = 0,
            userId = 0,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 50,
            pengeluaranIuranWarga = 25,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Test"
        )
        val pairs = listOf(Pair(user, financial))

        whenever(mockUserDao.getUsersByEmails(listOf("test@example.com"))).thenReturn(emptyList())
        whenever(mockUserDao.insertAll(any())).thenReturn(listOf(1L))
        whenever(mockFinancialRecordDao.getFinancialRecordsByUserIds(listOf(1L))).thenReturn(emptyList())
        whenever(mockFinancialRecordDao.insertAll(any())).thenReturn(emptyList())

        CacheHelper.saveEntityWithFinancialRecords(pairs)

        verify(mockUserDao, times(1)).insertAll(listOf(user))
        verify(mockUserDao, never()).updateAll(any())
        verify(mockFinancialRecordDao, times(1)).insertAll(any())
        verify(mockFinancialRecordDao, never()).updateAll(any())
    }

    @Test
    fun saveEntityWithFinancialRecords_existingUser_updatesUserAndFinancial() = runTest {
        val existingUser = UserEntity(
            id = 1L,
            email = "existing@example.com",
            firstName = "Existing",
            lastName = "User",
            alamat = "Old Address",
            avatar = "old.jpg"
        )
        val user = UserEntity(
            id = 0,
            email = "existing@example.com",
            firstName = "Existing",
            lastName = "User",
            alamat = "New Address",
            avatar = "new.jpg"
        )
        val financial = FinancialRecordEntity(
            id = 0,
            userId = 0,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 50,
            pengeluaranIuranWarga = 25,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Test"
        )
        val existingFinancial = FinancialRecordEntity(
            id = 2L,
            userId = 1L,
            iuranPerwarga = 50,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 25,
            pengeluaranIuranWarga = 10,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Old"
        )
        val pairs = listOf(Pair(user, financial))

        whenever(mockUserDao.getUsersByEmails(listOf("existing@example.com"))).thenReturn(listOf(existingUser))
        whenever(mockFinancialRecordDao.getFinancialRecordsByUserIds(listOf(1L))).thenReturn(listOf(existingFinancial))

        CacheHelper.saveEntityWithFinancialRecords(pairs)

        verify(mockUserDao, never()).insertAll(any())
        verify(mockUserDao).updateAll(argThat { it.size == 1 && it[0].id == 1L && it[0].alamat == "New Address" })
        verify(mockFinancialRecordDao, never()).insertAll(any())
        verify(mockFinancialRecordDao).updateAll(argThat { it.size == 1 && it[0].id == 2L && it[0].userId == 1L })
    }

    @Test
    fun saveEntityWithFinancialRecords_mixedNewAndExistingUsers_handlesCorrectly() = runTest {
        val existingUser = UserEntity(
            id = 1L,
            email = "existing@example.com",
            firstName = "Existing",
            lastName = "User",
            alamat = "Address",
            avatar = "avatar.jpg"
        )
        val newUser = UserEntity(
            id = 0,
            email = "new@example.com",
            firstName = "New",
            lastName = "User",
            alamat = "Address",
            avatar = "avatar.jpg"
        )
        val newFinancial = FinancialRecordEntity(
            id = 0,
            userId = 0,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 50,
            pengeluaranIuranWarga = 25,
            totalIuranRekap = 0,
            pemanfaatanIuran = "New"
        )
        val existingFinancial = FinancialRecordEntity(
            id = 2L,
            userId = 1L,
            iuranPerwarga = 50,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 25,
            pengeluaranIuranWarga = 10,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Old"
        )
        val existingFinancial2 = FinancialRecordEntity(
            id = 0,
            userId = 0,
            iuranPerwarga = 50,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 25,
            pengeluaranIuranWarga = 10,
            totalIuranRekap = 0,
            pemanfaatanIuran = "New"
        )
        val pairs = listOf(
            Pair(newUser, newFinancial),
            Pair(existingUser, existingFinancial2)
        )

        whenever(mockUserDao.getUsersByEmails(listOf("new@example.com", "existing@example.com"))).thenReturn(listOf(existingUser))
        whenever(mockUserDao.insertAll(any())).thenReturn(listOf(3L))
        whenever(mockFinancialRecordDao.getFinancialRecordsByUserIds(any())).thenReturn(listOf(existingFinancial))
        whenever(mockFinancialRecordDao.insertAll(any())).thenReturn(emptyList())

        CacheHelper.saveEntityWithFinancialRecords(pairs)

        verify(mockUserDao, times(1)).insertAll(argThat { it.size == 1 })
        verify(mockUserDao).updateAll(argThat { it.size == 1 && it[0].id == 1L })
        verify(mockFinancialRecordDao, times(1)).insertAll(any())
        verify(mockFinancialRecordDao).updateAll(argThat { it.size == 1 })
    }

    @Test
    fun saveEntityWithFinancialRecords_preservesUserIdAssociation() = runTest {
        val user = UserEntity(
            id = 0,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            alamat = "Address",
            avatar = "avatar.jpg"
        )
        val financial = FinancialRecordEntity(
            id = 0,
            userId = 0,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 50,
            pengeluaranIuranWarga = 25,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Test"
        )
        val pairs = listOf(Pair(user, financial))

        whenever(mockUserDao.getUsersByEmails(listOf("test@example.com"))).thenReturn(emptyList())
        whenever(mockUserDao.insertAll(any())).thenReturn(listOf(5L))
        whenever(mockFinancialRecordDao.getFinancialRecordsByUserIds(listOf(5L))).thenReturn(emptyList())
        whenever(mockFinancialRecordDao.insertAll(argThat { it.size == 1 })).thenAnswer {
            val inserted = it.arguments[0] as List<FinancialRecordEntity>
            assertEquals(5L, inserted[0].userId)
            emptyList()
        }

        CacheHelper.saveEntityWithFinancialRecords(pairs)
    }

    @Test
    fun saveEntityWithFinancialRecords_updatesTimestamp() = runTest {
        val existingUser = UserEntity(
            id = 1L,
            email = "existing@example.com",
            firstName = "Existing",
            lastName = "User",
            alamat = "Address",
            avatar = "avatar.jpg",
            createdAt = Date(System.currentTimeMillis() - 100000),
            updatedAt = Date(System.currentTimeMillis() - 50000)
        )
        val user = UserEntity(
            id = 0,
            email = "existing@example.com",
            firstName = "Existing",
            lastName = "User",
            alamat = "Address",
            avatar = "avatar.jpg",
            createdAt = Date(System.currentTimeMillis() - 100000),
            updatedAt = Date(System.currentTimeMillis() - 50000)
        )
        val financial = FinancialRecordEntity(
            id = 0,
            userId = 0,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 50,
            pengeluaranIuranWarga = 25,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Test"
        )
        val existingFinancial = FinancialRecordEntity(
            id = 2L,
            userId = 1L,
            iuranPerwarga = 50,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 25,
            pengeluaranIuranWarga = 10,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Old",
            updatedAt = Date(System.currentTimeMillis() - 50000)
        )
        val pairs = listOf(Pair(user, financial))

        val beforeSave = System.currentTimeMillis()
        whenever(mockUserDao.getUsersByEmails(listOf("existing@example.com"))).thenReturn(listOf(existingUser))
        whenever(mockFinancialRecordDao.getFinancialRecordsByUserIds(listOf(1L))).thenReturn(listOf(existingFinancial))
        whenever(mockUserDao.updateAll(argThat { it.size == 1 })).thenAnswer {
            val updated = it.arguments[0] as List<UserEntity>
            assertTrue(updated[0].updatedAt.time >= beforeSave)
            Unit
        }
        whenever(mockFinancialRecordDao.updateAll(argThat { it.size == 1 })).thenAnswer {
            val updated = it.arguments[0] as List<FinancialRecordEntity>
            assertTrue(updated[0].updatedAt.time >= beforeSave)
            emptyList()
        }

        CacheHelper.saveEntityWithFinancialRecords(pairs)

        val afterSave = System.currentTimeMillis()
        verify(mockUserDao).updateAll(argThat { it[0].updatedAt.time >= beforeSave && it[0].updatedAt.time <= afterSave })
    }

    @Test
    fun saveEntityWithFinancialRecords_multipleUsers_insertsAll() = runTest {
        val users = (1..5).map { i ->
            UserEntity(
                id = 0,
                email = "test$i@example.com",
                firstName = "Test",
                lastName = "User$i",
                alamat = "Address $i",
                avatar = "avatar$i.jpg"
            )
        }
        val financials = (1..5).map { i ->
            FinancialRecordEntity(
                id = 0,
                userId = 0,
                iuranPerwarga = i * 100,
                jumlahIuranBulanan = 0,
                totalIuranIndividu = i * 50,
                pengeluaranIuranWarga = i * 25,
                totalIuranRekap = 0,
                pemanfaatanIuran = "Test $i"
            )
        }
        val pairs = users.zip(financials)

        whenever(mockUserDao.getUsersByEmails(any())).thenReturn(emptyList())
        whenever(mockUserDao.insertAll(any())).thenReturn(listOf(1L, 2L, 3L, 4L, 5L))
        whenever(mockFinancialRecordDao.getFinancialRecordsByUserIds(any())).thenReturn(emptyList())
        whenever(mockFinancialRecordDao.insertAll(any())).thenReturn(emptyList())

        CacheHelper.saveEntityWithFinancialRecords(pairs)

        verify(mockUserDao, times(1)).insertAll(argThat { it.size == 5 })
        verify(mockUserDao, never()).updateAll(any())
        verify(mockFinancialRecordDao, times(1)).insertAll(argThat { it.size == 5 })
        verify(mockFinancialRecordDao, never()).updateAll(any())
    }

    @Test
    fun saveEntityWithFinancialRecords_multipleExistingUsers_updatesAll() = runTest {
        val existingUsers = (1..5).map { i ->
            UserEntity(
                id = i.toLong(),
                email = "test$i@example.com",
                firstName = "Test",
                lastName = "User$i",
                alamat = "Address $i",
                avatar = "avatar$i.jpg"
            )
        }
        val users = (1..5).map { i ->
            UserEntity(
                id = 0,
                email = "test$i@example.com",
                firstName = "Test",
                lastName = "User$i",
                alamat = "New Address $i",
                avatar = "newavatar$i.jpg"
            )
        }
        val existingFinancials = (1..5).map { i ->
            FinancialRecordEntity(
                id = i.toLong(),
                userId = i.toLong(),
                iuranPerwarga = i * 50,
                jumlahIuranBulanan = 0,
                totalIuranIndividu = i * 25,
                pengeluaranIuranWarga = i * 10,
                totalIuranRekap = 0,
                pemanfaatanIuran = "Old $i"
            )
        }
        val financials = (1..5).map { i ->
            FinancialRecordEntity(
                id = 0,
                userId = 0,
                iuranPerwarga = i * 100,
                jumlahIuranBulanan = 0,
                totalIuranIndividu = i * 50,
                pengeluaranIuranWarga = i * 25,
                totalIuranRekap = 0,
                pemanfaatanIuran = "New $i"
            )
        }
        val pairs = users.zip(financials)

        whenever(mockUserDao.getUsersByEmails(any())).thenReturn(existingUsers)
        whenever(mockFinancialRecordDao.getFinancialRecordsByUserIds(any())).thenReturn(existingFinancials)

        CacheHelper.saveEntityWithFinancialRecords(pairs)

        verify(mockUserDao, never()).insertAll(any())
        verify(mockUserDao).updateAll(argThat { it.size == 5 })
        verify(mockFinancialRecordDao, never()).insertAll(any())
        verify(mockFinancialRecordDao).updateAll(argThat { it.size == 5 })
    }

    @Test
    fun saveEntityWithFinancialRecords_existingUserNewFinancial_insertsFinancialOnly() = runTest {
        val existingUser = UserEntity(
            id = 1L,
            email = "existing@example.com",
            firstName = "Existing",
            lastName = "User",
            alamat = "Address",
            avatar = "avatar.jpg"
        )
        val user = UserEntity(
            id = 0,
            email = "existing@example.com",
            firstName = "Existing",
            lastName = "User",
            alamat = "Address",
            avatar = "avatar.jpg"
        )
        val financial = FinancialRecordEntity(
            id = 0,
            userId = 0,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 50,
            pengeluaranIuranWarga = 25,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Test"
        )
        val pairs = listOf(Pair(user, financial))

        whenever(mockUserDao.getUsersByEmails(listOf("existing@example.com"))).thenReturn(listOf(existingUser))
        whenever(mockFinancialRecordDao.getFinancialRecordsByUserIds(listOf(1L))).thenReturn(emptyList())

        CacheHelper.saveEntityWithFinancialRecords(pairs)

        verify(mockUserDao, never()).insertAll(any())
        verify(mockUserDao).updateAll(argThat { it.size == 1 })
        verify(mockFinancialRecordDao).insertAll(argThat { it.size == 1 && it[0].userId == 1L })
        verify(mockFinancialRecordDao, never()).updateAll(any())
    }

    @Test
    fun saveEntityWithFinancialRecords_handlesMultipleFinancialsForSameUser() = runTest {
        val existingUser = UserEntity(
            id = 1L,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            alamat = "Address",
            avatar = "avatar.jpg"
        )
        val user = UserEntity(
            id = 0,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            alamat = "Address",
            avatar = "avatar.jpg"
        )
        val financial = FinancialRecordEntity(
            id = 0,
            userId = 0,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 50,
            pengeluaranIuranWarga = 25,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Latest"
        )
        val existingFinancials = listOf(
            FinancialRecordEntity(
                id = 2L,
                userId = 1L,
                iuranPerwarga = 50,
                jumlahIuranBulanan = 0,
                totalIuranIndividu = 25,
                pengeluaranIuranWarga = 10,
                totalIuranRekap = 0,
                pemanfaatanIuran = "Old 1"
            ),
            FinancialRecordEntity(
                id = 3L,
                userId = 1L,
                iuranPerwarga = 75,
                jumlahIuranBulanan = 0,
                totalIuranIndividu = 30,
                pengeluaranIuranWarga = 15,
                totalIuranRekap = 0,
                pemanfaatanIuran = "Old 2"
            )
        )
        val pairs = listOf(Pair(user, financial))

        whenever(mockUserDao.getUsersByEmails(listOf("test@example.com"))).thenReturn(listOf(existingUser))
        whenever(mockFinancialRecordDao.getFinancialRecordsByUserIds(listOf(1L))).thenReturn(existingFinancials)

        CacheHelper.saveEntityWithFinancialRecords(pairs)

        verify(mockFinancialRecordDao).updateAll(argThat {
            it.size == 1 &&
            it[0].id == 2L &&
            it[0].pemanfaatanIuran == "Latest"
        })
    }
}
