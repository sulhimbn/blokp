package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
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

/**
 * Covers [UserAdapter] and its DiffUtil callback.
 *
 * The adapter mutates the [MutableList] handed to its constructor in place, so these
 * tests hold their own reference to that list and assert against it rather than the
 * adapter's private field.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserAdapterTest {

    private lateinit var users: MutableList<DataItem>
    private lateinit var adapter: UserAdapter
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        users = mutableListOf()
        adapter = UserAdapter(users, TestScope(testDispatcher))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun awaitItemCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }

    @Test
    fun `setUsers replaces the adapter contents`() {
        val newUsers = listOf(
            TestFixtures.dataItem(firstName = "John", lastName = "Doe", email = "john.doe@example.com"),
            TestFixtures.dataItem(firstName = "Jane", lastName = "Smith", email = "jane.smith@example.com")
        )

        adapter.setUsers(newUsers)
        awaitItemCount(2)

        assertEquals(2, adapter.itemCount)
        assertEquals("John", users[0].first_name)
        assertEquals("jane.smith@example.com", users[1].email)
    }

    @Test
    fun `itemCount starts at zero`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `addUser appends a valid user`() {
        val validUser = TestFixtures.dataItem(
            firstName = "Test",
            lastName = "User",
            email = "test.user@example.com",
            alamat = "789 Test St"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(validUser)

        assertEquals(initialSize + 1, adapter.itemCount)
        assertEquals("Test", users.last().first_name)
    }

    @Test
    fun `addUser ignores null`() {
        val initialSize = adapter.itemCount

        adapter.addUser(null)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser rejects a user without an email`() {
        val initialSize = adapter.itemCount

        adapter.addUser(TestFixtures.dataItem(email = "   ", firstName = "Test"))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser rejects a user with no name at all`() {
        val initialSize = adapter.itemCount

        adapter.addUser(TestFixtures.dataItem(firstName = "", lastName = "", email = "x@example.com"))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `clear empties the adapter`() {
        adapter.setUsers(
            listOf(
                TestFixtures.dataItem(firstName = "A"),
                TestFixtures.dataItem(firstName = "B")
            )
        )
        awaitItemCount(2)
        assertEquals(2, adapter.itemCount)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
        assertTrue(users.isEmpty())
    }

    @Test
    fun `diff callback keys identity on name and address`() {
        val original = TestFixtures.dataItem(firstName = "John", lastName = "Doe", alamat = "Jl. 1")
        val moved = TestFixtures.dataItem(firstName = "John", lastName = "Doe", alamat = "Jl. 2")

        val callback = UserAdapter.UserDiffCallback(listOf(original), listOf(moved))

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff callback reports content changes under a stable identity`() {
        val before = TestFixtures.dataItem(firstName = "John", lastName = "Doe", alamat = "Jl. 1", email = "a@x.com")
        val after = TestFixtures.dataItem(firstName = "John", lastName = "Doe", alamat = "Jl. 1", email = "b@x.com")

        val callback = UserAdapter.UserDiffCallback(listOf(before), listOf(after))

        assertTrue(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff callback reports list sizes`() {
        val callback = UserAdapter.UserDiffCallback(
            listOf(TestFixtures.dataItem(), TestFixtures.dataItem()),
            listOf(TestFixtures.dataItem())
        )

        assertEquals(2, callback.getOldListSize())
        assertEquals(1, callback.getNewListSize())
    }
}