package com.example.iurankomplek.presentation.adapter

import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.model.DataItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Covers the adapter's list bookkeeping and its DiffUtil identity rules. RecyclerView.Adapter
 * notify paths need a real runtime because AGP's mockable android.jar strips the field
 * initialisers of android.database.Observable.
 */
@RunWith(RobolectricTestRunner::class)
class UserAdapterTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private fun adapter(vararg items: DataItem) =
        UserAdapter(items.toMutableList(), scope)

    @Test
    fun `itemCount reflects the initial list`() {
        assertEquals(0, adapter().itemCount)
        assertEquals(2, adapter(TestFixtures.dataItem(), TestFixtures.dataItem(firstName = "Jane")).itemCount)
    }

    @Test
    fun `addUser appends a valid user`() {
        val adapter = adapter()

        adapter.addUser(TestFixtures.dataItem())

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `addUser ignores a null argument`() {
        val adapter = adapter()

        adapter.addUser(null)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser rejects a user without an email`() {
        val adapter = adapter()

        adapter.addUser(TestFixtures.dataItem(email = "  "))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser rejects a user with no first and no last name`() {
        val adapter = adapter()

        adapter.addUser(TestFixtures.dataItem(firstName = "", lastName = ""))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser accepts a user identified by last name only`() {
        val adapter = adapter()

        adapter.addUser(TestFixtures.dataItem(firstName = "", lastName = "Doe"))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `clear empties the list`() {
        val adapter = adapter(TestFixtures.dataItem(), TestFixtures.dataItem())

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `diff callback reports list sizes`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(TestFixtures.dataItem()),
            listOf(TestFixtures.dataItem(), TestFixtures.dataItem(firstName = "Jane"))
        )

        assertEquals(1, callback.oldListSize)
        assertEquals(2, callback.newListSize)
    }

    @Test
    fun `diff callback keys identity on name and address`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(TestFixtures.dataItem()),
            listOf(TestFixtures.dataItem(email = "moved@example.com"))
        )

        assertTrue(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff callback reports a different item when the name changes`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(TestFixtures.dataItem()),
            listOf(TestFixtures.dataItem(firstName = "Jane"))
        )

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff callback treats identical items as same content`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(TestFixtures.dataItem()),
            listOf(TestFixtures.dataItem())
        )

        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff callback detects changed content on the same item`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(TestFixtures.dataItem(pengeluaranIuranWarga = 10)),
            listOf(TestFixtures.dataItem(pengeluaranIuranWarga = 99))
        )

        assertTrue(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }
}