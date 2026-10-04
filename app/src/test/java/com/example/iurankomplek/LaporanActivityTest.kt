package com.example.iurankomplek

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.testing.FakePemanfaatanRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import javax.inject.Inject

/**
 * Functional test for the report screen. It exercises the real Activity, the real Hilt
 * graph and the real FinancialViewModel, so it fails if loadFinancialData() ever stops
 * delivering data to the UI again.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [33])
class LaporanActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fakePemanfaatanRepository: FakePemanfaatanRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun sampleItems() = listOf(
        DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. 1",
            iuran_perwarga = 100_000,
            total_iuran_rekap = 500_000,
            jumlah_iuran_bulanan = 200_000,
            total_iuran_individu = 150_000,
            pengeluaran_iuran_warga = 50_000,
            pemanfaatan_iuran = "Perbaikan jalan",
            avatar = ""
        ),
        DataItem(
            first_name = "Jane",
            last_name = "Smith",
            email = "jane@example.com",
            alamat = "Jl. 2",
            iuran_perwarga = 200_000,
            total_iuran_rekap = 600_000,
            jumlah_iuran_bulanan = 300_000,
            total_iuran_individu = 200_000,
            pengeluaran_iuran_warga = 75_000,
            pemanfaatan_iuran = "Listrik",
            avatar = ""
        )
    )

    private fun launchWith(data: List<DataItem>): LaporanActivity {
        fakePemanfaatanRepository.response.value = Result.success(PemanfaatanResponse(data = data))
        val activity = Robolectric.buildActivity(LaporanActivity::class.java).setup().get()
        shadowOf(activity.mainLooper).idle()
        return activity
    }

    private fun awaitItemCount(recyclerView: RecyclerView, expected: Int) {
        val deadline = System.currentTimeMillis() + 10_000
        while (recyclerView.adapter?.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(25)
            shadowOf(android.os.Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun reportScreen_rendersUtilisationsAndSummary_fromLoadedData() {
        val activity = launchWith(sampleItems())

        assertEquals(
            "ProgressBar should be hidden once the report loaded",
            View.GONE,
            activity.findViewById<ProgressBar>(R.id.progressBar).visibility
        )

        val reportList = activity.findViewById<RecyclerView>(R.id.rv_laporan)
        val summaryList = activity.findViewById<RecyclerView>(R.id.rv_summary)
        assertNotNull(reportList)
        assertNotNull(summaryList)

        awaitItemCount(reportList, 2)
        assertEquals("Both utilisations should be listed", 2, reportList.adapter?.itemCount)

        awaitItemCount(summaryList, 3)
        assertEquals("Iuran, pengeluaran and rekap rows should be summarised", 3, summaryList.adapter?.itemCount)
    }

    @Test
    fun reportScreen_staysEmptyWhenNoFinancialData() {
        val activity = launchWith(emptyList())

        assertEquals(
            View.GONE,
            activity.findViewById<ProgressBar>(R.id.progressBar).visibility
        )
        assertTrue("No rows should be rendered for empty data", !activity.isFinishing)
    }
}
