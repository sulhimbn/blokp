package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import androidx.recyclerview.widget.RecyclerView
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * The adapter keeps its backing list private, so these tests assert the observable
 * contract: item count and the validation rules applied by addUser.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
@LooperMode(LooperMode.Mode.LEGACY)
class UserAdapterTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    private lateinit var adapter: UserAdapter
    private lateinit var testUsers: MutableList<DataItem>

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        testUsers = mutableListOf()
        adapter = UserAdapter(testUsers, CoroutineScope(UnconfinedTestDispatcher()))
    }

    private fun user(
        firstName: String? = "Test",
        lastName: String? = "User",
        email: String? = "test.user@example.com"
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = "789 Test St",
        iuran_perwarga = 150,
        total_iuran_rekap = 400,
        jumlah_iuran_bulanan = 250,
        total_iuran_individu = 175,
        pengeluaran_iuran_warga = 60,
        pemanfaatan_iuran = "Utilities",
        avatar = "https://example.com/test-avatar.jpg"
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setUsers should update adapter data correctly`() {
        val newUsers = listOf(
            user(firstName = "John", lastName = "Doe", email = "john.doe@example.com"),
            user(firstName = "Jane", lastName = "Smith", email = "jane.smith@example.com")
        )

        adapter.setUsers(newUsers)
        awaitItemCount(adapter, newUsers.size)
        assertEquals(newUsers.size, adapter.itemCount)
    }

    @Test
    fun `addUser should add valid user to the list`() {
        val initialSize = adapter.itemCount

        adapter.addUser(user())

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
        val initialSize = adapter.itemCount

        adapter.addUser(user(email = ""))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank name fields`() {
        val initialSize = adapter.itemCount

        adapter.addUser(user(firstName = "", lastName = ""))

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should accept user with either first name or last name`() {
        val initialSize = adapter.itemCount

        adapter.addUser(user(firstName = "Test", lastName = ""))

        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `clear should remove all users from the adapter`() {
        adapter.setUsers(listOf(user()))
        awaitItemCount(adapter, 1)
        assertEquals(1, adapter.itemCount)

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `itemCount should return correct count`() {
        assertEquals(0, adapter.itemCount)

        adapter.setUsers(
            listOf(
                user(firstName = "John", lastName = "Doe", email = "john@example.com"),
                user(firstName = "Jane", lastName = "Smith", email = "jane@example.com")
            )
        )
        awaitItemCount(adapter, 2)
        assertEquals(2, adapter.itemCount)
    }
}

/** setUsers computes the diff on Dispatchers.Default and applies it on Main. */
private fun awaitItemCount(adapter: RecyclerView.Adapter<*>, expected: Int) {
    val deadline = System.currentTimeMillis() + 5_000
    while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
        Thread.sleep(10)
    }
}
