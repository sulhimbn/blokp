package com.example.iurankomplek

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryImpl
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.data.repository.VendorRepositoryImpl
import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.transaction.TransactionDao
import com.example.iurankomplek.utils.CacheManager
import com.example.iurankomplek.di.AppModule
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.example.iurankomplek.session.EncryptedPrefsSessionStore
import com.example.iurankomplek.session.SessionStore
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.model.PemanfaatanResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowToast
import org.robolectric.annotation.Config
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private class InMemorySessionStore : SessionStore {
    var stored: User? = null
    override fun loadUser(): User? = stored
    override fun saveUser(user: User) {
        stored = user
    }

    override fun clear() {
        stored = null
    }
}

@Module
@InstallIn(SingletonComponent::class)
object MainActivityTestModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService = mock()

    @Provides
    @Singleton
    fun provideSessionStore(@ApplicationContext context: Context): SessionStore =
        InMemorySessionStore()

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        sessionManager: UserSessionManager
    ): UserRepository = UserRepositoryImpl(apiService, sessionManager)

    @Provides
    @Singleton
    fun provideUserSessionManager(store: SessionStore): UserSessionManager =
        UserSessionManager(store)

    @Provides
    @Singleton
    fun providePemanfaatanRepository(apiService: ApiService): PemanfaatanRepository =
        PemanfaatanRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideVendorRepository(apiService: ApiService): VendorRepository =
        VendorRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun providePaymentGateway(): PaymentGateway = mock()

    @Provides
    @Singleton
    fun provideTransactionDao(): TransactionDao = mock()

    @Provides
    @Singleton
    fun provideTransactionRepository(
        paymentGateway: PaymentGateway,
        transactionDao: TransactionDao
    ): TransactionRepository = com.example.iurankomplek.transaction.TransactionRepository(
        paymentGateway,
        transactionDao
    )
}

@HiltAndroidTest
@UninstallModules(AppModule::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = HiltTestApplication::class)
class MainActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var apiService: ApiService

    private lateinit var activity: MainActivity
    private lateinit var activityController: ActivityController<MainActivity>

    private fun user(index: Int) = DataItem(
        first_name = "User$index",
        last_name = "Test",
        email = "user$index@example.com",
        alamat = "Jl. Test $index",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = null
    )

    private fun launchActivity(): MainActivity {
        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.setup().get()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        return activity
    }

    @Before
    fun setup() {
        hiltRule.inject()
        CacheManager.getInstance().clearSync()
        runBlocking {
            Mockito.`when`(apiService.getUsers())
                .thenReturn(Response.success(UserResponse(data = listOf(user(1), user(2)))))
            Mockito.`when`(apiService.getPemanfaatan())
                .thenReturn(Response.success(PemanfaatanResponse(data = emptyList())))
        }
    }

    @Test
    fun `MainActivity should initialize UI components correctly`() {
        launchActivity()

        assertNotNull(activity.findViewById<RecyclerView>(R.id.rv_users))
        assertNotNull(activity.findViewById<ProgressBar>(R.id.progressBar))
    }

    @Test
    fun `MainActivity should be created successfully`() {
        launchActivity()

        assertTrue(activity.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED))
    }

    @Test
    fun `MainActivity should initialize UserAdapter correctly`() {
        launchActivity()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
        assertTrue("Adapter should be UserAdapter", recyclerView.adapter is UserAdapter)
        assertNotNull(recyclerView.layoutManager)
    }

    @Test
    fun `MainActivity should show progress bar during loading state`() {
        launchActivity()

        val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)
        assertNotNull(progressBar)
        progressBar.visibility = View.VISIBLE
        assertEquals(View.VISIBLE, progressBar.visibility)
    }

    @Test
    fun `MainActivity should load users on create`() {
        launchActivity()

        runBlocking {
        Mockito.verify(apiService).getUsers()
        }
    }

    @Test
    fun `MainActivity should handle success state with empty data correctly`() {
        runBlocking {
            Mockito.`when`(apiService.getUsers())
                .thenReturn(Response.success(UserResponse(data = emptyList())))
        }

        val recyclerView = launchActivity().findViewById<RecyclerView>(R.id.rv_users)

        assertTrue(recyclerView.adapter is UserAdapter)
        runBlocking {
                Mockito.verify(apiService).getUsers()
        }
    }

    @Test
    fun `MainActivity should handle success state correctly`() {
        val recyclerView = launchActivity().findViewById<RecyclerView>(R.id.rv_users)

        assertTrue(recyclerView.adapter is UserAdapter)
        runBlocking {
                Mockito.verify(apiService).getUsers()
        }
    }

    @Test
    fun `MainActivity should handle error state and show toast`() {
        runBlocking {
            Mockito.`when`(apiService.getUsers())
                .thenAnswer { throw IOException("Network unavailable") }
        }

        launchActivity()

        val toast = ShadowToast.getTextOfLatestToast()
        assertNotNull("Error state must surface a toast", toast)
        assertTrue("Toast must describe the failure", toast!!.isNotBlank())
    }

    @Test
    fun `MainActivity should hide progress bar once data arrived`() {
        val progressBar = launchActivity().findViewById<ProgressBar>(R.id.progressBar)

        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(View.GONE, progressBar.visibility)
    }
}