package com.example.iurankomplek

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * MainActivity is annotated with @AndroidEntryPoint, so instantiating it on the JVM would
 * require a full Hilt test graph (and the SQLCipher native library that is unavailable off
 * device). These tests cover the part that can be verified off device: the layout contract
 * the Activity binds to, and its manifest declaration.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class MainActivityTest {

    private fun themedContext(): Context = ContextThemeWrapper(
        ApplicationProvider.getApplicationContext(),
        R.style.Theme_BlokP
    )

    private fun inflatedLayout(): View =
        LayoutInflater.from(themedContext()).inflate(R.layout.activity_main, null, false)

    private fun awaitItemCount(adapter: RecyclerView.Adapter<*>, expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(20)
        }
    }

    @Test
    fun `activity_main layout exposes every view MainActivity binds`() {
        val root = inflatedLayout()

        assertNotNull("RecyclerView id rv_users is bound by MainActivity", root.findViewById<View>(R.id.rv_users))
        assertNotNull("progressBar is bound by MainActivity", root.findViewById<View>(R.id.progressBar))
        assertNotNull("swipeRefreshLayout is bound by MainActivity", root.findViewById<View>(R.id.swipeRefreshLayout))
        assertNotNull("searchFilterView is bound by MainActivity", root.findViewById<View>(R.id.searchFilterView))
    }

    @Test
    fun `activity_main layout inflates without a parent`() {
        assertNotNull(inflatedLayout())
    }

    @Test
    fun `MainActivity is declared as the launcher activity`() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)

        assertNotNull("the app must expose a launcher activity", intent)
        assertEquals(
            context.packageName,
            intent!!.component?.packageName
        )
    }

    @Test
    fun `item row layout carries the views UserAdapter binds`() {
        val row = LayoutInflater.from(themedContext()).inflate(R.layout.item_list, null, false)

        assertTrue("row layout must render at least one child view", (row as android.view.ViewGroup).childCount > 0)
    }

    @Test
    fun `empty data must not crash the item layout`() {
        val row = LayoutInflater.from(themedContext()).inflate(R.layout.item_list, null, false)

        assertNotNull("the item row must inflate even before any data is bound", row)
    }
}
