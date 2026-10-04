package com.example.iurankomplek

import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class UserAdapterTest {

    private lateinit var adapter: UserAdapter
    private lateinit var testUsers: MutableList<DataItem>
    private val scope: CoroutineScope by lazy { CoroutineScope(Dispatchers.Main) }
    private val recyclerView = RecyclerView(RuntimeEnvironment.getApplication())

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        testUsers = mutableListOf()
        adapter = UserAdapter(testUsers, scope)
        recyclerView.adapter = adapter
    }


    private fun awaitItemCount(expected: Int, adapter: RecyclerView.Adapter<*>): Int {
        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            Thread.sleep(20)
        }
        return adapter.itemCount
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setUsers should update adapter data correctly`() {
        val newUsers = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Smith",
                email = "jane.smith@example.com",
                alamat = "456 Oak Ave",
                iuran_perwarga = 200,
                total_iuran_rekap = 600,
                jumlah_iuran_bulanan = 300,
                total_iuran_individu = 200,
                pengeluaran_iuran_warga = 75,
                pemanfaatan_iuran = "Repairs",
                avatar = "https://example.com/avatar2.jpg"
            )
        )

        adapter.setUsers(newUsers)

        assertEquals(newUsers.size, awaitItemCount(newUsers.size, adapter))
        val changed = newUsers.mapIndexed { index, user ->
            if (index == 1) user.copy(pengeluaran_iuran_warga = 999) else user
        }
        val diff = UserAdapter.UserDiffCallback(newUsers, changed)
        assertEquals(newUsers.size, diff.getNewListSize())
        assertTrue("identity is name + address, so the row is still the same person",
            diff.areItemsTheSame(1, 1))
        assertTrue("a changed amount must be treated as changed content",
            !diff.areContentsTheSame(1, 1))
    }

    @Test
    fun `addUser should add valid user to the list`() {
        val validUser = DataItem(
            first_name = "Test",
            last_name = "User",
            email = "test.user@example.com",
            alamat = "789 Test St",
            iuran_perwarga = 150,
            total_iuran_rekap = 400,
            jumlah_iuran_bulanan = 250,
            total_iuran_individu = 175,
            pengeluaran_iuran_warga = 60,
            pemanfaatan_iuran = "Utilities",
            avatar = "https://example.com/test-avatar.jpg"
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
        val userWithBlankEmail = DataItem(
            first_name = "Test",
            last_name = "User",
            email = "", // Blank email
            alamat = "789 Test St",
            iuran_perwarga = 150,
            total_iuran_rekap = 400,
            jumlah_iuran_bulanan = 250,
            total_iuran_individu = 175,
            pengeluaran_iuran_warga = 60,
            pemanfaatan_iuran = "Utilities",
            avatar = "https://example.com/test-avatar.jpg"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(userWithBlankEmail)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should ignore user with blank name fields`() {
        val userWithBlankNames = DataItem(
            first_name = "", // Blank first name
            last_name = "",  // Blank last name
            email = "test.blank@example.com",
            alamat = "789 Test St",
            iuran_perwarga = 150,
            total_iuran_rekap = 400,
            jumlah_iuran_bulanan = 250,
            total_iuran_individu = 175,
            pengeluaran_iuran_warga = 60,
            pemanfaatan_iuran = "Utilities",
            avatar = "https://example.com/test-avatar.jpg"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(userWithBlankNames)

        assertEquals(initialSize, adapter.itemCount)
    }

    @Test
    fun `addUser should accept user with either first name or last name`() {
        val userWithFirstNameOnly = DataItem(
            first_name = "Test",
            last_name = "",  // Blank last name
            email = "test.first@example.com",
            alamat = "789 Test St",
            iuran_perwarga = 150,
            total_iuran_rekap = 400,
            jumlah_iuran_bulanan = 250,
            total_iuran_individu = 175,
            pengeluaran_iuran_warga = 60,
            pemanfaatan_iuran = "Utilities",
            avatar = "https://example.com/test-avatar.jpg"
        )

        val initialSize = adapter.itemCount
        adapter.addUser(userWithFirstNameOnly)

        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `clear should remove all users from the adapter`() {
        val users = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        adapter.setUsers(users)
        assertEquals(1, awaitItemCount(1, adapter))

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `itemCount should return correct count`() {
        assertEquals(0, adapter.itemCount)

        val users = listOf(
            createTestDataItem("John", "Doe", "john@example.com"),
            createTestDataItem("Jane", "Smith", "jane@example.com")
        )
        adapter.setUsers(users)

        assertEquals(2, awaitItemCount(2, adapter))
    }

    private fun createTestDataItem(firstName: String, lastName: String, email: String): DataItem {
        return DataItem(
            first_name = firstName,
            last_name = lastName,
            email = email,
            alamat = "Test Address",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 150,
            pengeluaran_iuran_warga = 50,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )
    }
}