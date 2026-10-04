package com.example.iurankomplek.presentation.adapter

import com.example.iurankomplek.model.DataItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class UserAdapterTest {

    private lateinit var users: MutableList<DataItem>
    private lateinit var adapter: UserAdapter

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        users = mutableListOf()
        adapter = UserAdapter(users, CoroutineScope(UnconfinedTestDispatcher()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun user(
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        address: String = "123 Main St",
        perWarga: Int = 100
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = address,
        iuran_perwarga = perWarga,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )

    private fun awaitCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 5000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }

    @Test
    fun startsWithEmptyList() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun setUsersReplacesBackingData() {
        adapter.setUsers(
            listOf(
                user(),
                user(firstName = "Jane", lastName = "Smith", email = "jane@example.com")
            )
        )
        awaitCount(2)

        assertEquals(2, adapter.itemCount)
        assertEquals("John", users[0].first_name)
        assertEquals("jane@example.com", users[1].email)
    }

    @Test
    fun setUsersWithEmptyPayloadClearsAdapter() {
        adapter.setUsers(listOf(user()))
        awaitCount(1)

        adapter.setUsers(emptyList())
        awaitCount(0)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun addUserAppendsValidUser() {
        adapter.addUser(user(firstName = "Test", lastName = "User", email = "test.user@example.com"))

        assertEquals(1, adapter.itemCount)
        assertEquals("Test", users.last().first_name)
    }

    @Test
    fun addUserIgnoresNullInput() {
        adapter.addUser(null)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun addUserIgnoresBlankEmail() {
        adapter.addUser(user(email = ""))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun addUserIgnoresBothNameFieldsBlank() {
        adapter.addUser(user(firstName = "", lastName = ""))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun addUserAcceptsSinglePopulatedNameField() {
        adapter.addUser(user(firstName = "Test", lastName = ""))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun clearEmptiesAdapter() {
        adapter.setUsers(listOf(user()))
        awaitCount(1)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
        assertTrue(users.isEmpty())
    }

    @Test
    fun diffCallbackTreatsIdenticalEntryAsSameItemAndContent() {
        val callback = UserAdapter.UserDiffCallback(listOf(user()), listOf(user()))

        assertTrue(callback.areItemsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun diffCallbackSeparatesEntriesDifferingByNameOrAddress() {
        val byName = UserAdapter.UserDiffCallback(listOf(user()), listOf(user(firstName = "Jane")))
        val byAddress = UserAdapter.UserDiffCallback(listOf(user()), listOf(user(address = "Other St")))

        assertFalse(byName.areItemsTheSame(0, 0))
        assertFalse(byAddress.areItemsTheSame(0, 0))
    }

    @Test
    fun diffCallbackReportsDifferentContentsForSameIdentity() {
        val callback = UserAdapter.UserDiffCallback(listOf(user()), listOf(user(perWarga = 999)))

        assertTrue(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun diffCallbackReportsListSizes() {
        val callback = UserAdapter.UserDiffCallback(listOf(user(), user()), listOf(user()))

        assertEquals(2, callback.oldListSize)
        assertEquals(1, callback.newListSize)
    }
}
