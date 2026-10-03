package com.example.iurankomplek

import androidx.recyclerview.widget.RecyclerView
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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserAdapterTest {

    private val testScope = CoroutineScope(UnconfinedTestDispatcher())
    private lateinit var adapter: UserAdapter

    private fun user(
        firstName: String? = "John",
        lastName: String? = "Doe",
        email: String? = "john.doe@example.com",
        alamat: String? = "123 Main St"
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
        avatar = "https://example.com/avatar.jpg"
    )

    private object NoOpObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() = Unit
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = Unit
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = Unit
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = Unit
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = Unit
    }

    private fun awaitItemCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        adapter = UserAdapter(mutableListOf(), testScope)
        adapter.registerAdapterDataObserver(NoOpObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts with an empty list`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `setUsers replaces adapter contents`() {
        adapter.setUsers(listOf(user(), user(email = "jane@example.com")))

        awaitItemCount(2)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `setUsers with an empty list clears the adapter`() {
        adapter.setUsers(listOf(user()))
        awaitItemCount(1)

        adapter.setUsers(emptyList())

        awaitItemCount(0)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `setUsers is idempotent for identical content`() {
        val users = listOf(user())

        adapter.setUsers(users)
        awaitItemCount(1)
        adapter.setUsers(users)

        awaitItemCount(1)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `addUser appends a valid user`() {
        adapter.addUser(user())

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `addUser ignores null`() {
        adapter.addUser(null)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser rejects a user without an email`() {
        adapter.addUser(user(email = null))
        adapter.addUser(user(email = "   "))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser rejects a user without any name`() {
        adapter.addUser(user(firstName = null, lastName = null))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser accepts a user with only a first name`() {
        adapter.addUser(user(lastName = null))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `clear empties the adapter`() {
        adapter.setUsers(listOf(user(), user(email = "other@example.com")))
        awaitItemCount(2)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `diff callback keeps the same row when only the email changes`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(user()),
            listOf(user(email = "john.new@example.com"))
        )

        assertTrue(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff callback treats a moved resident as a different row`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(user()),
            listOf(user(alamat = "999 Other St"))
        )

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff callback treats a renamed resident as a different row`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(user()),
            listOf(user(firstName = "Jane"))
        )

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff callback reports sizes correctly`() {
        val callback = UserAdapter.UserDiffCallback(listOf(user()), listOf(user(), user()))

        assertEquals(1, callback.oldListSize)
        assertEquals(2, callback.newListSize)
    }
}