package com.example.iurankomplek

import android.app.Application
import android.widget.FrameLayout
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.example.iurankomplek.presentation.adapter.UserAdapter.UserDiffCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class UserAdapterTest {

    private fun adapterWith(items: List<DataItem>) = UserAdapter(items.toMutableList(), CoroutineScope(Dispatchers.Unconfined))

    private fun parent(): FrameLayout =
        FrameLayout(RuntimeEnvironment.getApplication())

    @Test
    fun `renders name email address and the two iuran figures`() {
        val adapter = adapterWith(
            listOf(
                TestFixtures.dataItem(
                    firstName = "John",
                    lastName = "Doe",
                    email = "john@example.com",
                    alamat = "Jl. Merdeka 1",
                    iuranPerwarga = 100000,
                    totalIuranIndividu = 150000
                )
            )
        )
        val holder = adapter.onCreateViewHolder(parent(), 0)

        adapter.onBindViewHolder(holder, 0)

        assertEquals("John Doe", holder.binding.itemName.text.toString())
        assertEquals("john@example.com", holder.binding.itemEmail.text.toString())
        assertEquals("Jl. Merdeka 1", holder.binding.itemAddress.text.toString())
        assertEquals("Iuran Perwarga Rp.100,000", holder.binding.itemIuranPerwarga.text.toString())
        assertEquals("Total Iuran Individu Rp.150,000", holder.binding.itemIuranIndividu.text.toString())
    }

    @Test
    fun `falls back to placeholders when the resident has no name email or address`() {
        val adapter = adapterWith(
            listOf(
                TestFixtures.dataItem(
                    firstName = null,
                    lastName = null,
                    email = null,
                    alamat = null
                )
            )
        )
        val holder = adapter.onCreateViewHolder(parent(), 0)

        adapter.onBindViewHolder(holder, 0)

        assertEquals("Unknown User", holder.binding.itemName.text.toString())
        assertEquals("No email", holder.binding.itemEmail.text.toString())
        assertEquals("No address", holder.binding.itemAddress.text.toString())
    }

    @Test
    fun `shows only the first name when the last name is absent`() {
        val adapter = adapterWith(
            listOf(TestFixtures.dataItem(firstName = "Siti", lastName = null))
        )
        val holder = adapter.onCreateViewHolder(parent(), 0)

        adapter.onBindViewHolder(holder, 0)

        assertEquals("Siti", holder.binding.itemName.text.toString())
    }

    @Test
    fun `clamps negative iuran figures to zero rather than rendering them`() {
        val adapter = adapterWith(
            listOf(
                TestFixtures.dataItem(iuranPerwarga = -1000, totalIuranIndividu = -500)
            )
        )
        val holder = adapter.onCreateViewHolder(parent(), 0)

        adapter.onBindViewHolder(holder, 0)

        assertEquals("Iuran Perwarga Rp.0", holder.binding.itemIuranPerwarga.text.toString())
        assertEquals("Total Iuran Individu Rp.0", holder.binding.itemIuranIndividu.text.toString())
    }

    @Test
    fun `clear removes every row`() {
        val adapter = adapterWith(listOf(TestFixtures.dataItem(), TestFixtures.dataItem()))

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser appends a valid resident`() {
        val adapter = adapterWith(emptyList())

        adapter.addUser(TestFixtures.dataItem())

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `addUser ignores a null resident`() {
        val adapter = adapterWith(emptyList())

        adapter.addUser(null)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser ignores a resident with neither name nor email`() {
        val adapter = adapterWith(emptyList())

        adapter.addUser(TestFixtures.dataItem(firstName = null, lastName = null, email = ""))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `diff identifies a row by name and address`() {
        val old = TestFixtures.dataItem()
        assertTrue(UserDiffCallback(listOf(old), listOf(old.copy())).areItemsTheSame(0, 0))

        val renamed = old.copy(alamat = "Jl. Baru 9")
        assertFalse(UserDiffCallback(listOf(old), listOf(renamed)).areItemsTheSame(0, 0))
    }

    @Test
    fun `diff reports a changed amount as the same row with new contents`() {
        val old = TestFixtures.dataItem(iuranPerwarga = 100)
        val diff = UserDiffCallback(listOf(old), listOf(old.copy(iuran_perwarga = 200)))

        assertTrue(diff.areItemsTheSame(0, 0))
        assertFalse(diff.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff reports sizes on both sides`() {
        val diff = UserDiffCallback(
            listOf(TestFixtures.dataItem(), TestFixtures.dataItem()),
            listOf(TestFixtures.dataItem())
        )

        assertEquals(2, diff.oldListSize)
        assertEquals(1, diff.newListSize)
    }
}
