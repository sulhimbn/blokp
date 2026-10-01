package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class UserAdapterTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var adapter: UserAdapter
    private lateinit var testUsers: MutableList<DataItem>
    private lateinit var scope: CoroutineScope

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        testUsers = mutableListOf()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        adapter = UserAdapter(testUsers, scope)
    }

    @After
    fun tearDown() {
        scope.cancel()
        Dispatchers.resetMain()
    }

    private fun awaitItemCount(expected: Int) {
        val deadline = System.nanoTime() + 5_000_000_000L
        while (adapter.itemCount != expected && System.nanoTime() < deadline) {
            Thread.sleep(10)
        }
    }

    @Test
    fun `setUsers should update adapter data correctly`() {
        val newUsers = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Smith",
                email = "jane.smith@example.com",
                alamat = "456 Oak Ave",
                iuran_perwarga = 200,
                total_iuran_rekap = 600,
                jumlah_iuran_bulanan = 300,
                total_iuran_individu = 200,
                pengeluaran_iuran_warga = 75,
                pemanfaatan_iuran = "Repairs",
                avatar = "https://example.com/avatar2.jpg"
            )
        )

        adapter.setUsers(newUsers)

        awaitItemCount(newUsers.size)

        assertEquals(newUsers.size, adapter.itemCount)
        assertEquals("John", testUsers[0].first_name)
        assertEquals("jane.smith@example.com", testUsers[1].email)
    }

    @Test
    fun `addUser should add valid user to the list`() {
        val validUser = DataItem(
            first_name = "Test",
            last_name = "User",
            email = "test.user@example.com",
            alamat = "789 Test St",
            iuran_perwarga = 150,
            total_iuran_rekap = 400,
            jumlah_iuran_bulanan = 250,
            total_iuran_individu = 175,
            pengeluaran_iuran_warga = 60,
            pemanfaatan_iuran = "Utilities",
            avatar = "https://example.com/test-avatar.jpg"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(validUser)

        assertEquals(initialSize + 1, adapter.itemCount)
        assertEquals("Test", testUsers.last().first_name)
    }

    @Test
    fun `addUser should ignore null input`() {
        val initialSize = adapter.itemCount
        adapter.addUser(null)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank email`() {
        val userWithBlankEmail = DataItem(
            first_name = "Test",
            last_name = "User",
            email = "", // Blank email
            alamat = "789 Test St",
            iuran_perwarga = 150,
            total_iuran_rekap = 400,
            jumlah_iuran_bulanan = 250,
            total_iuran_individu = 175,
            pengeluaran_iuran_warga = 60,
            pemanfaatan_iuran = "Utilities",
            avatar = "https://example.com/test-avatar.jpg"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(userWithBlankEmail)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank name fields`() {
        val userWithBlankNames = DataItem(
            first_name = "", // Blank first name
            last_name = "",  // Blank last name
            email = "test.blank@example.com",
            alamat = "789 Test St",
            iuran_perwarga = 150,
            total_iuran_rekap = 400,
            jumlah_iuran_bulanan = 250,
            total_iuran_individu = 175,
            pengeluaran_iuran_warga = 60,
            pemanfaatan_iuran = "Utilities",
            avatar = "https://example.com/test-avatar.jpg"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(userWithBlankNames)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should accept user with either first name or last name`() {
        val userWithFirstNameOnly = DataItem(
            first_name = "Test",
            last_name = "",  // Blank last name
            email = "test.first@example.com",
            alamat = "789 Test St",
            iuran_perwarga = 150,
            total_iuran_rekap = 400,
            jumlah_iuran_bulanan = 250,
            total_iuran_individu = 175,
            pengeluaran_iuran_warga = 60,
            pemanfaatan_iuran = "Utilities",
            avatar = "https://example.com/test-avatar.jpg"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(userWithFirstNameOnly)

        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `clear should remove all users from the adapter`() {
        val users = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        adapter.setUsers(users)
        awaitItemCount(users.size)
        assertEquals(1, adapter.itemCount)

        adapter.clear()

        awaitItemCount(0)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `itemCount should return correct count`() {
        assertEquals(0, adapter.itemCount)

        val users = listOf(
            createTestDataItem("John", "Doe", "john@example.com"),
            createTestDataItem("Jane", "Smith", "jane@example.com")
        )
        adapter.setUsers(users)
        awaitItemCount(users.size)

        assertEquals(2, adapter.itemCount)
    }

    private fun createTestDataItem(firstName: String, lastName: String, email: String): DataItem {
        return DataItem(
            first_name = firstName,
            last_name = lastName,
            email = email,
            alamat = "Test Address",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 150,
            pengeluaran_iuran_warga = 50,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )
    }
}