package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.app.Application
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class UserAdapterTest {

    private lateinit var adapter: UserAdapter
    private lateinit var testUsers: MutableList<DataItem>

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        testUsers = mutableListOf()
        adapter = UserAdapter(testUsers, CoroutineScope(Dispatchers.Unconfined))
    }

    private fun awaitItemCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun user(
        firstName: String? = "Test",
        lastName: String? = "User",
        email: String? = "test.user@example.com"
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = "789 Test St",
        iuran_perwarga = 150,
        total_iuran_rekap = 400,
        jumlah_iuran_bulanan = 250,
        total_iuran_individu = 175,
        pengeluaran_iuran_warga = 60,
        pemanfaatan_iuran = "Utilities",
        avatar = null
    )

    @Test
    fun `setUsers should update adapter data correctly`() {
        val newUsers = listOf(
            user(firstName = "John", lastName = "Doe", email = "john.doe@example.com"),
            user(firstName = "Jane", lastName = "Smith", email = "jane.smith@example.com")
        )

        adapter.setUsers(newUsers)

        awaitItemCount(2)

        assertEquals(2, adapter.itemCount)
        assertEquals(2, testUsers.size)
        assertEquals("john.doe@example.com", testUsers[0].email)
        assertEquals("jane.smith@example.com", testUsers[1].email)
    }

    @Test
    fun `addUser should add valid user to the list`() {
        val initialSize = adapter.itemCount

        adapter.addUser(user(firstName = "Test", lastName = "User"))

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
        val initialSize = adapter.itemCount

        adapter.addUser(user(email = ""))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with null email`() {
        val initialSize = adapter.itemCount

        adapter.addUser(user(email = null))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank name fields`() {
        val initialSize = adapter.itemCount

        adapter.addUser(user(firstName = "", lastName = ""))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should accept user with either first name or last name`() {
        val initialSize = adapter.itemCount

        adapter.addUser(user(firstName = "Test", lastName = ""))

        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `clear should remove all users from the adapter`() {
        adapter.setUsers(listOf(user()))
        awaitItemCount(1)
        assertEquals(1, adapter.itemCount)

        adapter.clear()

        awaitItemCount(0)

        assertEquals(0, adapter.itemCount)
        assertEquals(0, testUsers.size)
    }

    @Test
    fun `itemCount should return correct count`() {
        awaitItemCount(0)
        assertEquals(0, adapter.itemCount)

        adapter.setUsers(
            listOf(
                user(firstName = "John", lastName = "Doe", email = "john@example.com"),
                user(firstName = "Jane", lastName = "Smith", email = "jane@example.com")
            )
        )

        awaitItemCount(2)

        assertEquals(2, adapter.itemCount)
    }
}