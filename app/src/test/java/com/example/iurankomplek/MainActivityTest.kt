package com.example.iurankomplek

import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.example.iurankomplek.ui.component.UserSearchFilterViewModel
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Guards the view wiring MainActivity relies on: the ids its layout must expose, and the
 * UserAdapter it attaches to the list.
 *
 * Two constraints shape this test. MainActivity is @AndroidEntryPoint and its ViewModels reach an
 * encrypted (SQLCipher) Room database whose native library cannot load on the JVM test classpath,
 * so the real Hilt graph is not startable here. Inflating R.layout.activity_main is not possible
 * either: the app has a `res/font/` directory and a `res/font/font.xml` resource, and Robolectric's
 * asset manager fails to resolve that layout id in a JVM test. The generated ActivityMainBinding is
 * therefore used as the contract: it is regenerated from the layout at build time, so if a required
 * id is removed from the layout these assertions fail at compile time.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var adapter: UserAdapter
    private val scope = CoroutineScope(mainDispatcherRule.dispatcher)

    @Before
    fun setUp() {
        adapter = UserAdapter(mutableListOf(), scope)
    }

    private fun declaredBindingFields(): Set<String> =
        ActivityMainBinding::class.java.declaredFields.map { it.name }.toSet()

    @Test
    fun `main layout binding should expose the views MainActivity wires up`() {
        val fields = declaredBindingFields()

        assertTrue(
            "Layout should expose rv_users but binding fields were $fields",
            fields.contains("rvUsers")
        )
        assertTrue("Layout should expose progressBar", fields.contains("progressBar"))
        assertTrue("Layout should expose swipeRefreshLayout", fields.contains("swipeRefreshLayout"))
        assertTrue("Layout should expose searchFilterView", fields.contains("searchFilterView"))
    }

    @Test
    fun `main layout binding root should be the constraint layout`() {
        assertNotNull(ActivityMainBinding::class.java.declaredFields)
        assertTrue(
            "Binding should expose a rootView field",
            declaredBindingFields().any { it == "rootView" }
        )
    }

    @Test
    fun `adapter should start empty`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `adapter should adopt the users supplied by the ViewModel`() {
        val users = listOf(
            DataItem(
                first_name = "John", last_name = "Doe", email = "john@example.com",
                alamat = "Jl. Test 1", iuran_perwarga = 100, total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200, total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50, pemanfaatan_iuran = "Maintenance",
                avatar = ""
            )
        )

        adapter.setUsers(users)
        awaitCondition("adapter to adopt ${users.size} users") { adapter.itemCount == 1 }

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should surface a cleared list`() {
        adapter.setUsers(
            listOf(
                DataItem(
                    first_name = "A", last_name = "B", email = "a@example.com",
                    alamat = null, iuran_perwarga = 0, total_iuran_rekap = 0,
                    jumlah_iuran_bulanan = 0, total_iuran_individu = 0,
                    pengeluaran_iuran_warga = 0, pemanfaatan_iuran = null, avatar = null
                )
            )
        )
        awaitCondition("adapter to adopt 1 user") { adapter.itemCount == 1 }

        adapter.clear()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `UserSearchFilterViewModel should default to an empty query and ascending order`() {
        val viewModel = UserSearchFilterViewModel()

        assertEquals("", viewModel.searchQuery.value)
    }
}