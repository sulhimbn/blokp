package com.example.iurankomplek

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VendorAdapterTest {

    private lateinit var adapter: VendorAdapter
    private var clicked: Vendor? = null

    @Before
    fun setup() {
        clicked = null
        adapter = VendorAdapter { clicked = it }
    }

    private fun vendor(
        id: String,
        name: String,
        specialty: String,
        phone: String,
        rating: Double
    ) = Vendor(
        id = id,
        name = name,
        contactPerson = "John Smith",
        phoneNumber = phone,
        email = "contact@example.com",
        specialty = specialty,
        address = "123 Main St",
        licenseNumber = "PL-12345",
        insuranceInfo = "General liability coverage",
        certifications = listOf("Licensed", "Bonded"),
        rating = rating,
        totalReviews = 25,
        contractStart = "2023-01-01",
        contractEnd = "2024-12-31",
        isActive = true
    )

    private fun plumbing() =
        vendor("1", "Plumbing Services Inc", "plumbing", "123-456-7890", 4.5)

    private fun electrical() =
        vendor("2", "Electrical Services Co", "electrical", "098-765-4321", 4.2)

    private fun parent(): ViewGroup = FrameLayout(RuntimeEnvironment.getApplication())

    private fun textOf(holder: RecyclerView.ViewHolder, viewId: Int): String =
        holder.itemView.findViewById<TextView>(viewId).text.toString()

    @Test
    fun `diff callback matches rows by vendor id`() {
        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertTrue(diffCallback.areItemsTheSame(plumbing(), plumbing().copy(name = "Renamed")))
        assertFalse(diffCallback.areItemsTheSame(plumbing(), electrical()))
    }

    @Test
    fun `diff callback reports a renamed vendor as changed content`() {
        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertFalse(diffCallback.areContentsTheSame(plumbing(), plumbing().copy(name = "Renamed")))
        assertTrue(diffCallback.areContentsTheSame(plumbing(), plumbing()))
    }

    @Test
    fun `bind renders the vendor name specialty phone and rating`() {
        adapter.submitList(listOf(plumbing()))
        shadowOfMainLooper()

        assertEquals(1, adapter.itemCount)

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder, 0)

        assertEquals("Plumbing Services Inc", textOf(holder, R.id.vendorName))
        assertEquals("plumbing", textOf(holder, R.id.vendorSpecialty))
        assertEquals("123-456-7890", textOf(holder, R.id.vendorContact))
        assertEquals("Rating: 4.5/5.0", textOf(holder, R.id.vendorRating))
    }

    @Test
    fun `tapping a row reports the vendor behind that row`() {
        adapter.submitList(listOf(plumbing(), electrical()))
        shadowOfMainLooper()

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder, 1)
        holder.itemView.performClick()

        assertEquals("2", clicked?.id)
    }

    private fun shadowOfMainLooper() =
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
}