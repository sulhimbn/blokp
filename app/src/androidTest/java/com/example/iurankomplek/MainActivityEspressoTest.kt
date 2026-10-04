package com.example.iurankomplek

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryImpl
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.data.repository.VendorRepositoryImpl
import com.example.iurankomplek.di.AppModule
import com.example.iurankomplek.model.User
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.session.EncryptedPrefsSessionStore
import com.example.iurankomplek.session.SessionStore
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.transaction.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

private class EspressoSessionStore : SessionStore {
    private var stored: User? = null
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
object EspressoGraphModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService = Retrofit.Builder()
        .baseUrl("https://localhost/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideSessionStore(@ApplicationContext context: Context): SessionStore =
        EspressoSessionStore()

    @Provides
    @Singleton
    fun provideUserSessionManager(store: SessionStore): UserSessionManager =
        UserSessionManager(store)

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        sessionManager: UserSessionManager
    ): UserRepository = UserRepositoryImpl(apiService, sessionManager)

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

@LargeTest
@HiltAndroidTest
@UninstallModules(AppModule::class)
@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        hiltRule.inject()
        com.example.iurankomplek.utils.CacheManager.getInstance().clearSync()
    }

    @Test
    fun mainActivity_shouldDisplayListViews() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun mainActivity_shouldShowProgressBar() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.progressBar)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun mainActivity_recyclerViewShouldScroll() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0))
        }
    }

    @Test
    fun mainActivity_survivesNetworkFailure() {
        runBlocking { runCatching { repository.getUsers() } }

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users)).check(matches(isDisplayed()))
        }
    }
}