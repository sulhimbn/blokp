package com.example.iurankomplek

import android.app.Application
import com.example.iurankomplek.model.DataItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.robolectric.RuntimeEnvironment
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.google.android.material.textfield.TextInputEditText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Layout contract for the home screen.
 *
 * `MainActivity` is a `@AndroidEntryPoint`, so booting it would require Hilt test
 * infrastructure (an `@HiltAndroidTest` application plus a custom runner). Inflating the
 * same layout with the production theme keeps the contract these tests care about - the
 * screen exposes a list, a search field, a progress bar and pull-to-refresh - without
 * needing a DI container.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    private lateinit var context: Application

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
    }

    private fun inflateMainScreen(): View {
        val themed = ContextThemeWrapper(context, R.style.Theme_BlokP)
        return LayoutInflater.from(themed).inflate(R.layout.activity_main, null, false)
    }

    @Test
    fun `home screen layout inflates successfully`() {
        assertNotNull(inflateMainScreen())
    }

    @Test
    fun `home screen exposes the user list`() {
        val view = inflateMainScreen()

        assertNotNull(view.findViewById<RecyclerView>(R.id.rv_users))
    }

    @Test
    fun `home screen exposes the search field`() {
        val view = inflateMainScreen()

        assertNotNull(view.findViewById<EditText>(R.id.searchFilterView))
        assertNotNull(view.findViewById<TextInputEditText>(R.id.searchFilterView))
    }

    @Test
    fun `home screen exposes a progress bar`() {
        val view = inflateMainScreen()

        assertNotNull(view.findViewById<ProgressBar>(R.id.progressBar))
    }

    @Test
    fun `home screen exposes pull to refresh`() {
        val view = inflateMainScreen()

        assertNotNull(view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout))
    }

    @Test
    fun `user list is wired to a UserAdapter`() {
        val view = inflateMainScreen()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_users)
        val adapter = UserAdapter(mutableListOf(), CoroutineScope(Dispatchers.Unconfined))

        recyclerView.adapter = adapter

        assertTrue(recyclerView.adapter is UserAdapter)
        assertEquals(0, recyclerView.adapter?.itemCount)
    }

    @Test
    fun `UserAdapter renders every row returned by the API`() {
        val adapter = UserAdapter(mutableListOf(), CoroutineScope(Dispatchers.Unconfined))

        adapter.setUsers(
            listOf(
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
        )

        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != 2 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }

        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `UserAdapter tolerates a row whose optional fields are absent`() {
        val adapter = UserAdapter(mutableListOf(), CoroutineScope(Dispatchers.Unconfined))

        adapter.setUsers(
            listOf(
                DataItem(
                    first_name = null,
                    last_name = null,
                    email = null,
                    alamat = null,
                    iuran_perwarga = 0,
                    total_iuran_rekap = 0,
                    jumlah_iuran_bulanan = 0,
                    total_iuran_individu = 0,
                    pengeluaran_iuran_warga = 0,
                    pemanfaatan_iuran = null,
                    avatar = null
                )
            )
        )

        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != 1 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `search field is empty by default`() {
        val view = inflateMainScreen()

        assertEquals("", view.findViewById<EditText>(R.id.searchFilterView).text.toString())
    }
}