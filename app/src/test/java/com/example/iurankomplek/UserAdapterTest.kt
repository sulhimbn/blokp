package com.example.iurankomplek
import android.app.Application

import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
@Config(sdk = [33], application = Application::class)
class UserAdapterTest {

    private lateinit var backing: MutableList<DataItem>
    private lateinit var adapter: UserAdapter
    private lateinit var scope: CoroutineScope

    private fun item(
        first: String? = "John",
        last: String? = "Doe",
        email: String? = "john@example.com",
        alamat: String? = "Jl. Test 1"
    ) = DataItem(
        first_name = first,
        last_name = last,
        email = email,
        alamat = alamat,
        iuran_perwarga = 100,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = 0,
        total_iuran_individu = 50,
        pengeluaran_iuran_warga = 25,
        pemanfaatan_iuran = "Test",
        avatar = ""
    )

    @Before
    fun setup() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        scope = CoroutineScope(dispatcher)
        backing = mutableListOf()
        adapter = UserAdapter(backing, scope)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `item count reflects the backing list`() {
        assertEquals(0, adapter.itemCount)
        backing += item()
        assertEquals(1, adapter.itemCount)
        backing += item(first = "Jane", last = "Smith", email = "jane@x.io", alamat = "Jl. 2")
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `addUser appends a valid user and notifies an insert`() {
        val observer = RecordingObserver()
        adapter.registerAdapterDataObserver(observer)

        adapter.addUser(item())

        assertEquals(1, adapter.itemCount)
        assertEquals(listOf(0), observer.inserted)
    }

    @Test
    fun `addUser ignores a null argument`() {
        adapter.addUser(null)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser rejects a record with no email`() {
        adapter.addUser(item(email = null))
        assertEquals("a user without an email must not be added", 0, adapter.itemCount)
    }

    @Test
    fun `addUser rejects a record with no name at all`() {
        adapter.addUser(item(first = null, last = null))
        assertEquals("a user with no name must not be added", 0, adapter.itemCount)
    }

    @Test
    fun `addUser accepts a record with only a last name`() {
        adapter.addUser(item(first = null, last = "Doe"))
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `clear empties the adapter and notifies a range removal`() {
        backing += item()
        backing += item(first = "Jane", last = "Smith", email = "jane@x.io", alamat = "Jl. 2")
        val observer = RecordingObserver()
        adapter.registerAdapterDataObserver(observer)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
        assertEquals(1, observer.removed.size)
        assertEquals(2, observer.removed.first())
    }

    @Test
    fun `setUsers replaces the content`() = runTest {
        val newUsers = listOf(
            item(first = "Jane", last = "Smith", email = "jane@x.io", alamat = "Jl. 2"),
            item(first = "Bob", last = "Jones", email = "bob@x.io", alamat = "Jl. 3")
        )

        adapter.setUsers(newUsers)

        awaitItemCount(2)
    }

    @Test
    fun `setUsers with an empty list empties the adapter`() = runTest {
        backing += item()

        adapter.setUsers(emptyList())

        awaitItemCount(0)
    }

    @Test
    fun `diff callback treats records with the same name and address as the same row`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(item()),
            listOf(item(alamat = "Jl. Test 1", email = "john.doe+baru@example.com"))
        )

        assertEquals(1, callback.oldListSize)
        assertEquals(1, callback.newListSize)
        assertTrue("same name and address means the same row", callback.areItemsTheSame(0, 0))
        assertFalse("a changed email means new contents", callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff callback treats a changed address as a different row`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(item()),
            listOf(item(alamat = "Jl. Berbeda"))
        )

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    private class RecordingObserver : RecyclerView.AdapterDataObserver() {
        val inserted = mutableListOf<Int>()
        val removed = mutableListOf<Int>()

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            inserted += positionStart
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            removed += itemCount
        }
    }

    private fun awaitItemCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 10_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(5)
        }
        assertEquals(expected, adapter.itemCount)
    }
}
