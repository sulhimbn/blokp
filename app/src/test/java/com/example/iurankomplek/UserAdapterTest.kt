package com.example.iurankomplek

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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserAdapterTest {

    private lateinit var adapter: UserAdapter
    private lateinit var testUsers: MutableList<DataItem>

    @Before
    fun setup() {
        // setUsers hops through Dispatchers.Main; a plain JVM test has no Main dispatcher.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        testUsers = mutableListOf()
        adapter = UserAdapter(testUsers, CoroutineScope(Dispatchers.Unconfined))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `adapter should start empty`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `setUsers should update item count`() {
        val newUsers = listOf(
            TestFixtures.dataItem(firstName = "John", lastName = "Doe", email = "john.doe@example.com"),
            TestFixtures.dataItem(firstName = "Jane", lastName = "Smith", email = "jane.smith@example.com")
        )

        adapter.setUsers(newUsers)

        assertEquals(2, awaitItemCount(2))
    }

    @Test
    fun `addUser should add valid user to the list`() {
        val validUser = TestFixtures.dataItem(
            firstName = "Test",
            lastName = "User",
            email = "test.user@example.com"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(validUser)

        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore null input`() {
        val initialSize = adapter.itemCount
        adapter.addUser(null)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank email`() {
        val userWithBlankEmail = TestFixtures.dataItem(email = "")

        val initialSize = adapter.itemCount
        adapter.addUser(userWithBlankEmail)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank name fields`() {
        val userWithBlankNames = TestFixtures.dataItem(
            firstName = "",
            lastName = "",
            email = "test.blank@example.com"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(userWithBlankNames)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should accept user with either first name or last name`() {
        val userWithFirstNameOnly = TestFixtures.dataItem(
            firstName = "Test",
            lastName = "",
            email = "test.first@example.com"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(userWithFirstNameOnly)

        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with null email`() {
        val userWithNullEmail = TestFixtures.dataItem(email = null)

        val initialSize = adapter.itemCount
        adapter.addUser(userWithNullEmail)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `clear should remove all users from the adapter`() {
        adapter.setUsers(
            listOf(TestFixtures.dataItem(firstName = "John", lastName = "Doe"))
        )
        assertEquals(1, awaitItemCount(1))

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `itemCount should return correct count after setUsers`() {
        assertEquals(0, adapter.itemCount)

        adapter.setUsers(
            listOf(
                TestFixtures.dataItem(firstName = "John", lastName = "Doe", email = "john@example.com"),
                TestFixtures.dataItem(firstName = "Jane", lastName = "Smith", email = "jane@example.com")
            )
        )

        assertEquals(2, awaitItemCount(2))
    }

    @Test
    fun `UserDiffCallback should treat unchanged users as same and equal`() {
        val old = listOf(TestFixtures.dataItem(firstName = "John", lastName = "Doe", email = "john@example.com"))
        val new = listOf(TestFixtures.dataItem(firstName = "John", lastName = "Doe", email = "john@example.com"))

        val diffCallback = UserAdapter.UserDiffCallback(old, new)

        assertEquals(1, diffCallback.oldListSize)
        assertEquals(1, diffCallback.newListSize)
        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertTrue(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback should match on name and address, not email`() {
        val old = listOf(
            TestFixtures.dataItem(firstName = "John", lastName = "Doe", email = "old@example.com")
        )
        // Same person, changed email: the adapter treats this as the same row.
        val new = listOf(
            TestFixtures.dataItem(firstName = "John", lastName = "Doe", email = "new@example.com")
        )

        val diffCallback = UserAdapter.UserDiffCallback(old, new)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertFalse(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback should report different people as different items`() {
        val old = listOf(TestFixtures.dataItem(firstName = "John", lastName = "Doe"))
        val new = listOf(TestFixtures.dataItem(firstName = "Jane", lastName = "Smith"))

        val diffCallback = UserAdapter.UserDiffCallback(old, new)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback should report correct list sizes`() {
        val old = listOf(
            TestFixtures.dataItem(firstName = "John", lastName = "Doe"),
            TestFixtures.dataItem(firstName = "Jane", lastName = "Smith")
        )
        val new = listOf(TestFixtures.dataItem(firstName = "John", lastName = "Doe"))

        val diffCallback = UserAdapter.UserDiffCallback(old, new)

        assertEquals(2, diffCallback.oldListSize)
        assertEquals(1, diffCallback.newListSize)
    }

    /**
     * setUsers diffs on Dispatchers.Default before hopping to Main, so itemCount settles
     * asynchronously. Poll instead of assuming it already landed.
     */
    private fun awaitItemCount(expected: Int, timeoutMs: Long = 5_000): Int {
        val deadline = System.currentTimeMillis() + timeoutMs
        var count = adapter.itemCount
        while (count != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
            count = adapter.itemCount
        }
        return count
    }
}
