package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
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
import org.robolectric.RobolectricTestRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class UserAdapterTest {

    private lateinit var adapter: UserAdapter
    private lateinit var testUsers: MutableList<DataItem>

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        testUsers = mutableListOf()
        adapter = UserAdapter(testUsers, CoroutineScope(Dispatchers.Unconfined))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun awaitUsers(expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (testUsers.size != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }

    private fun item(
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john@example.com",
        avatar: String = "https://example.com/avatar.jpg",
        iuranPerwarga: Int = 100
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = "123 Main St",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = avatar
    )

    @Test
    fun `setUsers replaces the backing list`() {
        val newUsers = listOf(item(), item("Jane", "Smith", "jane.smith@example.com"))

        adapter.setUsers(newUsers)
        awaitUsers(newUsers.size)

        assertEquals(newUsers.size, adapter.itemCount)
        assertEquals("John", testUsers[0].first_name)
        assertEquals("jane.smith@example.com", testUsers[1].email)
    }

    @Test
    fun `setUsers with an empty list clears the adapter`() {
        adapter.setUsers(listOf(item()))
        awaitUsers(1)

        adapter.setUsers(emptyList())
        awaitUsers(0)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser appends a valid user`() {
        val initialSize = adapter.itemCount

        adapter.addUser(item(firstName = "Test", lastName = "User", email = "test.user@example.com"))

        assertEquals(initialSize + 1, adapter.itemCount)
        assertEquals("Test", testUsers.last().first_name)
    }

    @Test
    fun `addUser ignores null input`() {
        val initialSize = adapter.itemCount

        adapter.addUser(null)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser ignores a user with blank email`() {
        val initialSize = adapter.itemCount

        adapter.addUser(item(email = ""))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser ignores a user whose name fields are both blank`() {
        val initialSize = adapter.itemCount

        adapter.addUser(item(firstName = "", lastName = "", email = "blank@example.com"))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser accepts a user with only a first name`() {
        val initialSize = adapter.itemCount

        adapter.addUser(item(firstName = "Solo", lastName = "", email = "solo@example.com"))

        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `addUser accepts a user with only a last name`() {
        val initialSize = adapter.itemCount

        adapter.addUser(item(firstName = "", lastName = "Surname", email = "sur@example.com"))

        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `clear empties the adapter`() {
        adapter.setUsers(listOf(item()))
        awaitUsers(1)
        assertEquals(1, adapter.itemCount)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
        assertEquals(0, testUsers.size)
    }

    @Test
    fun `itemCount tracks the backing list`() {
        assertEquals(0, adapter.itemCount)

        adapter.setUsers(listOf(item(), item("Jane", "Smith", "jane@example.com")))
        awaitUsers(2)

        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `UserDiffCallback treats same name and address as the same item`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(item()),
            listOf(item(avatar = "different-avatar.jpg", email = "other@example.com"))
        )

        assertEquals(1, callback.oldListSize)
        assertEquals(1, callback.newListSize)
        assertTrue(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback separates items with different names`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(item()),
            listOf(item(firstName = "Different"))
        )

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback reports changed contents for edited rows`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(item()),
            listOf(item(iuranPerwarga = 999))
        )

        assertTrue(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }
}