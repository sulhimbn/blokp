package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    private lateinit var backingList: MutableList<DataItem>

    @Before
    fun setup() {
        backingList = mutableListOf()
        adapter = UserAdapter(backingList, TestScope())
    }

    private fun item(
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        alamat: String = "123 Main St",
        avatar: String = "https://example.com/avatar.jpg"
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = alamat,
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = avatar
    )

    @Test
    fun `adapter starts empty`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should add valid user to the list`() {
        adapter.addUser(item(firstName = "Test", lastName = "User", email = "test.user@example.com"))

        assertEquals(1, adapter.itemCount)
        assertEquals("Test", backingList.last().first_name)
    }

    @Test
    fun `addUser should ignore null input`() {
        adapter.addUser(null)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank email`() {
        adapter.addUser(item(email = ""))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank name fields`() {
        adapter.addUser(item(firstName = "", lastName = "", email = "test.blank@example.com"))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should accept user with either first name or last name`() {
        adapter.addUser(item(firstName = "Test", lastName = "", email = "test.first@example.com"))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `clear should remove all users from the adapter`() {
        adapter.addUser(item())
        assertEquals(1, adapter.itemCount)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser should accumulate across calls`() {
        adapter.addUser(item(firstName = "John", email = "john@example.com"))
        adapter.addUser(item(firstName = "Jane", email = "jane@example.com"))

        assertEquals(2, adapter.itemCount)
        assertEquals("jane@example.com", backingList[1].email)
    }

    @Test
    fun `UserDiffCallback should treat same name and address as same item`() {
        val old = listOf(item(firstName = "John", lastName = "Doe", email = "old@example.com"))
        val new = listOf(item(firstName = "John", lastName = "Doe", email = "new@example.com"))
        val callback = UserAdapter.UserDiffCallback(old, new)

        assertEquals(1, callback.oldListSize)
        assertEquals(1, callback.newListSize)
        assertTrue("email may change without changing identity", callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback should treat different address as different item`() {
        val old = listOf(item(alamat = "123 Main St"))
        val new = listOf(item(alamat = "456 Oak Ave"))
        val callback = UserAdapter.UserDiffCallback(old, new)

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback should report content change when dues change`() {
        val old = listOf(item())
        val new = listOf(item().copy(pengeluaran_iuran_warga = 999))
        val callback = UserAdapter.UserDiffCallback(old, new)

        assertTrue(callback.areItemsTheSame(0, 0))
        assertFalse("changed dues must be seen as a content change", callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback should report no content change for equal items`() {
        val old = listOf(item())
        val new = listOf(item())
        val callback = UserAdapter.UserDiffCallback(old, new)

        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback should handle growth from empty list`() {
        val callback = UserAdapter.UserDiffCallback(emptyList(), listOf(item(), item(firstName = "Jane")))

        assertEquals(0, callback.oldListSize)
        assertEquals(2, callback.newListSize)
    }
}