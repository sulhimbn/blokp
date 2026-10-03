package com.example.iurankomplek

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryImpl
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.data.repository.VendorRepositoryImpl
import com.example.iurankomplek.di.DataModule
import com.example.iurankomplek.di.SessionModule
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.RealPaymentGateway
import com.example.iurankomplek.session.InMemorySessionStore
import com.example.iurankomplek.session.SessionStore
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.CacheManager
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@UninstallModules(DataModule::class, SessionModule::class)
@Config(sdk = [34], application = HiltTestApplication::class)
class LaporanActivityJourneyTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    var sessionStore: SessionStore = InMemorySessionStore()

    @BindValue
    @JvmField
    var apiService: ApiService = retrofitService("http://localhost:1/")

    @BindValue
    @JvmField
    var userRepository: UserRepository = UserRepositoryImpl(apiService, UserSessionManager(sessionStore))

    @BindValue
    @JvmField
    var pemanfaatanRepository: PemanfaatanRepository = PemanfaatanRepositoryImpl(apiService)

    @BindValue
    @JvmField
    var vendorRepository: VendorRepository = VendorRepositoryImpl(apiService)

    @BindValue
    @JvmField
    var paymentGateway: PaymentGateway = RealPaymentGateway(apiService)

    @BindValue
    @JvmField
    var transactionRepository: TransactionRepository = mock(TransactionRepository::class.java)

    private lateinit var server: MockWebServer

    companion object {
        private fun retrofitService(baseUrl: String): ApiService = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val financeBody = """{"data":[
        {"first_name":"John","last_name":"Doe","email":"john@example.com",
         "alamat":"123 Main St","iuran_perwarga":100,"total_iuran_rekap":500,
         "jumlah_iuran_bulanan":200,"total_iuran_individu":150,
         "pengeluaran_iuran_warga":50,"pemanfaatan_iuran":"Maintenance","avatar":null},
        {"first_name":"Jane","last_name":"Smith","email":"jane@example.com",
         "alamat":"456 Oak Ave","iuran_perwarga":200,"total_iuran_rekap":600,
         "jumlah_iuran_bulanan":300,"total_iuran_individu":210,
         "pengeluaran_iuran_warga":75,"pemanfaatan_iuran":"Repairs","avatar":null}]}"""

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        apiService = retrofitService(server.url("/").toString())
        userRepository = UserRepositoryImpl(apiService, UserSessionManager(sessionStore))
        pemanfaatanRepository = PemanfaatanRepositoryImpl(apiService)
        vendorRepository = VendorRepositoryImpl(apiService)
        paymentGateway = RealPaymentGateway(apiService)
        CacheManager.getInstance().clearSync()
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
        server.shutdown()
    }

    private fun enqueueFinance(code: Int = 200, body: String = financeBody) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    private fun launchLaporan(): LaporanActivity =
        Robolectric.buildActivity(LaporanActivity::class.java).setup().get()

    private fun awaitReportItemCount(expected: Int): Int {
        val detail = screen.findViewById<RecyclerView>(R.id.rv_laporan)
        val deadline = System.currentTimeMillis() + 20_000
        var count = detail?.adapter?.itemCount ?: -1
        while (count != expected && System.currentTimeMillis() < deadline) {
            shadowOf(android.os.Looper.getMainLooper()).idle()
            Thread.sleep(20)
            count = detail?.adapter?.itemCount ?: -1
        }
        return count
    }

    private lateinit var screen: LaporanActivity

    private fun awaitGone(id: Int): Boolean {
        val view = screen.findViewById<View>(id)
        val deadline = System.currentTimeMillis() + 20_000
        while (view.visibility != View.GONE && System.currentTimeMillis() < deadline) {
            shadowOf(android.os.Looper.getMainLooper()).idle()
            Thread.sleep(20)
        }
        return view.visibility == View.GONE
    }

    @Test
    fun reportScreenInflatesDetailAndSummaryLists() {
        enqueueFinance()
        screen = launchLaporan()

        assertNotNull(screen.findViewById<RecyclerView>(R.id.rv_laporan))
        assertNotNull(screen.findViewById<RecyclerView>(R.id.rv_summary))
    }

    @Test
    fun financialDataPopulatesTheDetailList() {
        enqueueFinance()
        screen = launchLaporan()

        assertEquals(2, awaitReportItemCount(2))
    }

    @Test
    fun financialDataPopulatesTheSummaryList() {
        enqueueFinance()
        screen = launchLaporan()

        awaitReportItemCount(2)
        val summary = screen.findViewById<RecyclerView>(R.id.rv_summary)
        assertTrue("Summary should render at least one row", (summary?.adapter?.itemCount ?: 0) >= 1)
    }

    @Test
    fun progressBarHidesOnceFinancialDataArrives() {
        enqueueFinance()
        screen = launchLaporan()

        awaitReportItemCount(2)
        assertTrue("Progress bar should hide", awaitGone(R.id.progressBar))
    }

    @Test
    fun emptyFinancialDataRendersNoDetailRows() {
        enqueueFinance(body = """{"data":[]}""")
        screen = launchLaporan()

        assertEquals(0, awaitReportItemCount(0))
    }

    @Test
    fun serverErrorLeavesTheReportEmptyAndHidesProgress() {
        repeat(4) { enqueueFinance(code = 500) }
        screen = launchLaporan()

        assertEquals(0, awaitReportItemCount(0))
        assertTrue("Progress bar should hide on failure", awaitGone(R.id.progressBar))
    }
}