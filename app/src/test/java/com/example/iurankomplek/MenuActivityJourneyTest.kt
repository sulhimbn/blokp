package com.example.iurankomplek

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import org.robolectric.RobolectricTestRunner
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@UninstallModules(DataModule::class, SessionModule::class)
@Config(sdk = [34], application = HiltTestApplication::class)
class MenuActivityJourneyTest {

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

    private fun launchMenu(): MenuActivity =
        Robolectric.buildActivity(MenuActivity::class.java).setup().get()

    @Test
    fun menuScreenRendersAllFourDestinationCards() {
        val activity = launchMenu()

        assertNotNull(activity.findViewById<android.view.View>(R.id.cdMenu1))
        assertNotNull(activity.findViewById<android.view.View>(R.id.cdMenu2))
        assertNotNull(activity.findViewById<android.view.View>(R.id.cdMenu3))
        assertNotNull(activity.findViewById<android.view.View>(R.id.cdMenu4))
    }

    @Test
    fun firstMenuCardOpensTheResidentList() {
        val activity = launchMenu()

        activity.findViewById<android.view.View>(R.id.cdMenu1).performClick()

        val started = shadowOf(activity).nextStartedActivity
        assertNotNull("cdMenu1 should start an activity", started)
        assertEquals(MainActivity::class.java.name, started.component?.className)
    }

    @Test
    fun secondMenuCardOpensTheFinancialReport() {
        val activity = launchMenu()

        activity.findViewById<android.view.View>(R.id.cdMenu2).performClick()

        val started = shadowOf(activity).nextStartedActivity
        assertNotNull("cdMenu2 should start an activity", started)
        assertEquals(LaporanActivity::class.java.name, started.component?.className)
    }

    @Test
    fun thirdMenuCardOpensCommunication() {
        val activity = launchMenu()

        activity.findViewById<android.view.View>(R.id.cdMenu3).performClick()

        val started = shadowOf(activity).nextStartedActivity
        assertNotNull("cdMenu3 should start an activity", started)
        assertEquals(CommunicationActivity::class.java.name, started.component?.className)
    }

    @Test
    fun fourthMenuCardOpensPayments() {
        val activity = launchMenu()

        activity.findViewById<android.view.View>(R.id.cdMenu4).performClick()

        val started = shadowOf(activity).nextStartedActivity
        assertNotNull("cdMenu4 should start an activity", started)
        assertEquals(PaymentActivity::class.java.name, started.component?.className)
    }
}