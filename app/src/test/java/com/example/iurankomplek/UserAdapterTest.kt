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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class UserAdapterTest {

    private lateinit var adapter: UserAdapter
    private lateinit var testUsers: MutableList<DataItem>
    private lateinit var scope: CoroutineScope

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        scope = CoroutineScope(UnconfinedTestDispatcher())
        testUsers = mutableListOf()
        adapter = UserAdapter(testUsers, scope)
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
    fun `setUsers should update adapter data correctly`() {
        val newUsers = listOf(
            createTestDataItem("John", "Doe", "john.doe@example.com"),
            createTestDataItem("Jane", "Smith", "jane.smith@example.com")
        )

        adapter.setUsers(newUsers)
        awaitItemCount(2)

        assertEquals(newUsers.size, adapter.itemCount)
        assertEquals("John", testUsers[0].first_name)
        assertEquals("jane.smith@example.com", testUsers[1].email)
    }

    @Test
    fun `addUser should add valid user to the list`() {
        val validUser = createTestDataItem("Test", "User", "test.user@example.com")

        val initialSize = adapter.itemCount
        adapter.addUser(validUser)

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
        adapter.addUser(createTestDataItem("Test", "User", ""))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank name fields`() {
        val initialSize = adapter.itemCount
        adapter.addUser(createTestDataItem("", "", "test.blank@example.com"))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should accept user with either first name or last name`() {
        val initialSize = adapter.itemCount
        adapter.addUser(createTestDataItem("Test", "", "test.first@example.com"))

        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `clear should remove all users from the adapter`() {
        adapter.setUsers(listOf(createTestDataItem("John", "Doe", "john@example.com")))
        awaitItemCount(1)
        assertEquals(1, adapter.itemCount)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `itemCount should return correct count`() {
        assertEquals(0, adapter.itemCount)

        adapter.setUsers(
            listOf(
                createTestDataItem("John", "Doe", "john@example.com"),
                createTestDataItem("Jane", "Smith", "jane@example.com")
            )
        )
        awaitItemCount(2)

        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `UserDiffCallback should treat identical rows as unchanged`() {
        val oldList = listOf(createTestDataItem("John", "Doe", "john@example.com"))
        val newList = listOf(createTestDataItem("John", "Doe", "john@example.com"))

        val diffCallback = UserAdapter.UserDiffCallback(oldList, newList)

        assertEquals(1, diffCallback.getOldListSize())
        assertEquals(1, diffCallback.getNewListSize())
        assert(diffCallback.areItemsTheSame(0, 0))
        assert(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `UserDiffCallback should detect changed contents for the same row`() {
        val oldList = listOf(createTestDataItem("John", "Doe", "john@example.com"))
        val newList = listOf(createTestDataItem("John", "Doe", "john.doe@example.com", iuran = 999))

        val diffCallback = UserAdapter.UserDiffCallback(oldList, newList)

        assert(diffCallback.areItemsTheSame(0, 0))
        assert(!diffCallback.areContentsTheSame(0, 0))
    }

    private fun createTestDataItem(
        firstName: String,
        lastName: String,
        email: String,
        iuran: Int = 100
    ): DataItem {
        return DataItem(
            first_name = firstName,
            last_name = lastName,
            email = email,
            alamat = "123 Main St",
            iuran_perwarga = iuran,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 150,
            pengeluaran_iuran_warga = 50,
            pemanfaatan_iuran = "Maintenance",
            avatar = "https://example.com/avatar.jpg"
        )
    }
}