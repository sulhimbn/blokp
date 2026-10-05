package com.example.iurankomplek

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserAdapterTest {

    private lateinit var adapter: UserAdapter
    private lateinit var scope: CoroutineScope

    @Before
    fun setup() {
        scope = CoroutineScope(Dispatchers.Main.immediate)
        adapter = UserAdapter(mutableListOf(), scope)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun item(
        firstName: String? = "John",
        lastName: String? = "Doe",
        email: String? = "john@example.com"
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = "123 Main St",
        iuran_perwarga = 100_000,
        total_iuran_individu = 150_000,
        avatar = "https://example.com/avatar.jpg"
    )

    private fun parent(): ViewGroup = FrameLayout(RuntimeEnvironment.getApplication()).apply {
        layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun textOf(holder: RecyclerView.ViewHolder, viewId: Int): String =
        holder.itemView.findViewById<TextView>(viewId).text.toString()


    private fun awaitItems(adapter: RecyclerView.Adapter<*>, expected: Int, timeoutMs: Long = 10_000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            ShadowLooper.idleMainLooper()
            Thread.sleep(10)
        }
    }

    @Test
    fun `empty adapter reports zero items`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `bind renders name email address and dues into the item views`() {
        adapter = UserAdapter(mutableListOf(item()), scope)

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder as UserAdapter.ListViewHolder, 0)

        assertNotNull(holder.itemView.findViewById<TextView>(R.id.itemAvatar))
        assertEquals("John Doe", textOf(holder, R.id.itemName))
        assertEquals("john@example.com", textOf(holder, R.id.itemEmail))
        assertEquals("123 Main St", textOf(holder, R.id.itemAddress))
        assertEquals("Iuran Perwarga Rp.100,000", textOf(holder, R.id.itemIuranPerwarga))
        assertEquals("Total Iuran Individu Rp.150,000", textOf(holder, R.id.itemIuranIndividu))
    }

    @Test
    fun `bind falls back to placeholders when the payload has no text fields`() {
        adapter = UserAdapter(
            mutableListOf(item(firstName = null, lastName = null, email = null)),
            scope
        )

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder as UserAdapter.ListViewHolder, 0)

        assertEquals("Unknown User", textOf(holder, R.id.itemName))
        assertEquals("No email", textOf(holder, R.id.itemEmail))
    }

    @Test
    fun `addUser accepts a resident with an email and a name`() {
        adapter.addUser(item(firstName = "Test", lastName = "User"))

        assertEquals(1, adapter.itemCount)
        assertEquals("Test", adapter.currentUsers.last().first_name)
    }

    @Test
    fun `addUser ignores a null argument`() {
        adapter.addUser(null)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser ignores a resident without an email`() {
        adapter.addUser(item(firstName = "Test", email = ""))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser ignores a resident without any name`() {
        adapter.addUser(item(firstName = "", lastName = "", email = "blank@example.com"))

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser accepts a resident with only a last name`() {
        adapter.addUser(item(firstName = "", lastName = "Smith", email = "smith@example.com"))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `clear removes every resident`() {
        adapter = UserAdapter(mutableListOf(item(), item(email = "jane@example.com")), scope)
        assertEquals(2, adapter.itemCount)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `setUsers replaces the backing list with the new residents`() {
        val replacement = listOf(item(email = "a@example.com"), item(email = "b@example.com"))

        adapter.setUsers(replacement)
        awaitItems(adapter, replacement.size)

        assertEquals(2, adapter.itemCount)
        assertEquals("a@example.com", adapter.currentUsers.first().email)
    }

    @Test
    fun `UserDiffCallback treats a resident with the same name and address as unchanged`() {
        val callback = UserAdapter.UserDiffCallback(listOf(item()), listOf(item(email = "other@example.com")))

        assertEquals(1, callback.oldListSize)
        assertEquals(1, callback.newListSize)
        assertTrue(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback reports a changed address as a different row`() {
        val moved = item().copy(alamat = "456 Oak Ave")
        val callback = UserAdapter.UserDiffCallback(listOf(item()), listOf(moved))

        assertTrue(!callback.areItemsTheSame(0, 0))
    }
}