package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
class UserAdapterTest {

    private lateinit var users: MutableList<DataItem>
    private lateinit var adapter: UserAdapter

    @Before
    fun setup() {
        users = mutableListOf()
        adapter = UserAdapter(users, CoroutineScope(Dispatchers.Unconfined))
    }

    @Test
    fun `constructor should start with an empty list`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should add valid user to the list`() {
        adapter.addUser(user(first_name = "Test", email = "test.user@example.com"))

        assertEquals(1, adapter.itemCount)
        assertEquals("Test", users.last().first_name)
    }

    @Test
    fun `addUser should ignore null input`() {
        adapter.addUser(null)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank email`() {
        adapter.addUser(user(first_name = "Test", last_name = "User", email = ""))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with null email`() {
        adapter.addUser(user(first_name = "Test", last_name = "User", email = null))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank name fields`() {
        adapter.addUser(user(first_name = "", last_name = "", email = "test.blank@example.com"))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with null name fields`() {
        adapter.addUser(user(first_name = null, last_name = null, email = "test.null@example.com"))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should accept user with either first name or last name`() {
        adapter.addUser(user(first_name = "Test", last_name = "", email = "test.first@example.com"))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `clear should remove all users from the adapter`() {
        adapter.addUser(user(first_name = "John", email = "john@example.com"))
        assertEquals(1, adapter.itemCount)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
        assertEquals(0, users.size)
    }

    @Test
    fun `UserDiffCallback should treat same name and address as the same item`() {
        val old = listOf(user(first_name = "John", last_name = "Doe", alamat = "123 Main St"))
        val new = listOf(
            user(
                first_name = "John",
                last_name = "Doe",
                alamat = "123 Main St",
                total_iuran_individu = 999
            )
        )

        val callback = UserAdapter.UserDiffCallback(old, new)

        assertTrue(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback should treat different addresses as different items`() {
        val old = listOf(user(first_name = "John", last_name = "Doe", alamat = "123 Main St"))
        val new = listOf(user(first_name = "John", last_name = "Doe", alamat = "456 Oak Ave"))

        val callback = UserAdapter.UserDiffCallback(old, new)

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback should report list sizes`() {
        val old = listOf(user(email = "a@example.com"), user(email = "b@example.com"))
        val new = listOf(user(email = "a@example.com"))

        val callback = UserAdapter.UserDiffCallback(old, new)

        assertEquals(2, callback.getOldListSize())
        assertEquals(1, callback.getNewListSize())
    }

    private fun user(
        first_name: String? = "John",
        last_name: String? = "Doe",
        email: String? = "john@example.com",
        alamat: String? = "123 Main St",
        total_iuran_individu: Int = 150
    ): DataItem = TestFixtures.dataItem(
        first_name = first_name,
        last_name = last_name,
        email = email,
        alamat = alamat,
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = total_iuran_individu,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )
}