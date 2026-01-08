package com.example.iurankomplek.data.validation

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

class DatabaseIntegrityValidatorTest {

    private lateinit var userDao: com.example.iurankomplek.data.dao.UserDao
    private lateinit var financialDao: com.example.iurankomplek.data.dao.FinancialRecordDao

    @Before
    fun setup() {
        val context = androidx.test.InstrumentationRegistry.getInstrumentation().targetContext
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
        val db = com.example.iurankomplek.data.database.AppDatabase.getDatabase(context, scope)
        userDao = db.userDao()
        financialDao = db.financialRecordDao()
        runBlocking {
            userDao.deleteAll()
            financialDao.deleteAll()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            userDao.deleteAll()
            financialDao.deleteAll()
        }
    }

    @Test
    fun validateUserBeforeInsert_withValidUser_returnsValid() = runBlocking {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Test St",
            avatar = "https://example.com/avatar.png"
        )

        val result = DatabaseIntegrityValidator.validateUserBeforeInsert(user)

        assert(result.isValid)
        assert(result.error == null)
    }

    @Test
    fun validateUserBeforeInsert_withDuplicateEmail_returnsInvalid() = runBlocking {
        val user1 = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Test St",
            avatar = "https://example.com/avatar.png"
        )
        userDao.insert(user1)

        val user2 = UserEntity(
            email = "test@example.com",
            firstName = "Jane",
            lastName = "Smith",
            alamat = "456 Test St",
            avatar = "https://example.com/avatar2.png"
        )

        val result = DatabaseIntegrityValidator.validateUserBeforeInsert(user2)

        assert(!result.isValid)
        assert(result.error?.contains("already exists") == true)
    }

    @Test
    fun validateUserBeforeInsert_withBlankEmail_returnsInvalid() = runBlocking {
        val user = UserEntity(
            email = "",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Test St",
            avatar = "https://example.com/avatar.png"
        )

        val result = DatabaseIntegrityValidator.validateUserBeforeInsert(user)

        assert(!result.isValid)
        assert(result.error?.contains("Email") == true)
    }

    @Test
    fun validateFinancialRecordBeforeInsert_withValidRecord_returnsValid() = runBlocking {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Test St",
            avatar = "https://example.com/avatar.png"
        )
        val userId = userDao.insert(user)

        val record = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Test"
        )

        val result = DatabaseIntegrityValidator.validateFinancialRecordBeforeInsert(record)

        assert(result.isValid)
        assert(result.error == null)
    }

    @Test
    fun validateFinancialRecordBeforeInsert_withNonExistentUser_returnsInvalid() = runBlocking {
        val record = FinancialRecordEntity(
            userId = 999,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Test"
        )

        val result = DatabaseIntegrityValidator.validateFinancialRecordBeforeInsert(record)

        assert(!result.isValid)
        assert(result.error?.contains("does not exist") == true)
    }

    @Test
    fun validateFinancialRecordBeforeInsert_withDeletedUser_returnsInvalid() = runBlocking {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Test St",
            avatar = "https://example.com/avatar.png"
        )
        val userId = userDao.insert(user)
        userDao.softDeleteById(userId)

        val record = FinancialRecordEntity(
            userId = userId,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Test"
        )

        val result = DatabaseIntegrityValidator.validateFinancialRecordBeforeInsert(record)

        assert(!result.isValid)
        assert(result.error?.contains("deleted user") == true)
    }

    @Test
    fun validateUserDelete_withValidUser_returnsValid() = runBlocking {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Test St",
            avatar = "https://example.com/avatar.png"
        )
        val userId = userDao.insert(user)

        val result = DatabaseIntegrityValidator.validateUserDelete(userId)

        assert(result.isValid)
        assert(result.error == null)
    }

    @Test
    fun validateUserDelete_withNonExistentUser_returnsInvalid() = runBlocking {
        val result = DatabaseIntegrityValidator.validateUserDelete(999)

        assert(!result.isValid)
        assert(result.error?.contains("does not exist") == true)
    }

    @Test
    fun validateUserDelete_withAlreadyDeletedUser_returnsInvalid() = runBlocking {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Test St",
            avatar = "https://example.com/avatar.png"
        )
        val userId = userDao.insert(user)
        userDao.softDeleteById(userId)

        val result = DatabaseIntegrityValidator.validateUserDelete(userId)

        assert(!result.isValid)
        assert(result.error?.contains("already deleted") == true)
    }
}
