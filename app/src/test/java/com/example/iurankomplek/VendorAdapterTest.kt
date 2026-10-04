package com.example.iurankomplek

import android.app.Application
import android.widget.FrameLayout
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.presentation.adapter.VendorAdapter
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
class VendorAdapterTest {

    private fun parent(): FrameLayout =
        FrameLayout(RuntimeEnvironment.getApplication())

    @Test
    fun `starts empty`() {
        assertEquals(0, VendorAdapter {}.itemCount)
    }

    @Test
    fun `submitting vendors grows the adapter`() {
        val adapter = VendorAdapter {}

        adapter.submitList(listOf(TestFixtures.vendor(), TestFixtures.vendor(id = "v-2")))

        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `binding renders name specialty contact and rating`() {
        val vendor = TestFixtures.vendor(
            name = "CV Sinar Jaya",
            specialty = "plumbing",
            phoneNumber = "0812000000",
            rating = 4.5
        )
        val adapter = VendorAdapter {}
        adapter.submitList(listOf(vendor))
        val holder = adapter.createViewHolder(parent(), 0)

        adapter.bindViewHolder(holder, 0)
        val view = holder.itemView

        assertEquals("CV Sinar Jaya", view.findViewById<android.widget.TextView>(R.id.vendorName).text.toString())
        assertEquals("plumbing", view.findViewById<android.widget.TextView>(R.id.vendorSpecialty).text.toString())
        assertEquals("0812000000", view.findViewById<android.widget.TextView>(R.id.vendorContact).text.toString())
        assertEquals("Rating: 4.5/5.0", view.findViewById<android.widget.TextView>(R.id.vendorRating).text.toString())
    }

    @Test
    fun `clicking a row invokes the callback with that vendor`() {
        val vendor: Vendor = TestFixtures.vendor()
        var clicked: Vendor? = null
        val adapter = VendorAdapter { clicked = it }
        adapter.submitList(listOf(vendor))
        val holder = adapter.createViewHolder(parent(), 0)
        adapter.bindViewHolder(holder, 0)

        holder.itemView.performClick()

        assertEquals(vendor, clicked)
    }

    @Test
    fun `diff identifies a vendor by id`() {
        val callback = VendorAdapter.VendorDiffCallback()

        assertTrue(callback.areItemsTheSame(TestFixtures.vendor(id = "v-1"), TestFixtures.vendor(id = "v-1", name = "Baru")))
        assertFalse(callback.areItemsTheSame(TestFixtures.vendor(id = "v-1"), TestFixtures.vendor(id = "v-2")))
    }

    @Test
    fun `diff reports equal contents for an unchanged vendor`() {
        val callback = VendorAdapter.VendorDiffCallback()
        val vendor = TestFixtures.vendor()

        assertTrue(callback.areItemsTheSame(vendor, vendor))
        assertTrue(callback.areContentsTheSame(vendor, vendor.copy()))
        assertFalse(callback.areContentsTheSame(vendor, vendor.copy(rating = 1.0)))
    }
}
