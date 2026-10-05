package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserAdapterTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var adapter: UserAdapter
    private lateinit var testUsers: MutableList<DataItem>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testUsers = mutableListOf()
        adapter = UserAdapter(testUsers, testScope)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun testItem(
        firstName: String,
        lastName: String,
        email: String
    ) = DataItem(
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

    @Test
    fun `setUsers should update adapter data correctly`() = runTest(testDispatcher) {
        val newUsers = listOf(
            testItem("John", "Doe", "john.doe@example.com"),
            testItem("Jane", "Smith", "jane.smith@example.com")
        )

        adapter.setUsers(newUsers)
        advanceUntilIdle()

        assertEquals(2, adapter.itemCount)
        assertEquals("John", testUsers[0].first_name)
        assertEquals("jane.smith@example.com", testUsers[1].email)
    }

    @Test
    fun `addUser should add valid user to the list`() {
        val initialSize = adapter.itemCount

        adapter.addUser(testItem("Test", "User", "test.user@example.com"))

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

        adapter.addUser(testItem("Test", "User", ""))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank name fields`() {
        val initialSize = adapter.itemCount

        adapter.addUser(testItem("", "", "test.blank@example.com"))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should accept user with either first name or last name`() {
        val initialSize = adapter.itemCount

        adapter.addUser(testItem("Test", "", "test.first@example.com"))

        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `clear should remove all users from the adapter`() = runTest(testDispatcher) {
        adapter.setUsers(listOf(testItem("John", "Doe", "john@example.com")))
        advanceUntilIdle()
        assertEquals(1, adapter.itemCount)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `itemCount should return correct count`() = runTest(testDispatcher) {
        assertEquals(0, adapter.itemCount)

        adapter.setUsers(
            listOf(
                testItem("John", "Doe", "john@example.com"),
                testItem("Jane", "Smith", "jane@example.com")
            )
        )
        advanceUntilIdle()

        assertEquals(2, adapter.itemCount)
    }
}