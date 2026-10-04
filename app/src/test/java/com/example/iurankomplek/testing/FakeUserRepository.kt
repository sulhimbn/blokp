package com.example.iurankomplek.testing

import android.content.Context
import com.example.iurankomplek.data.repository.TransactionRepository as TransactionRepositoryInterface
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.data.repository.VendorRepositoryImpl
import com.example.iurankomplek.di.AppModule
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.payment.PaymentGateway
import com.example.iurankomplek.payment.RealPaymentGateway
import com.example.iurankomplek.session.UserSessionManager
import dagger.Binds
import dagger.Module
import javax.inject.Inject
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

/**
 * Test double for [UserRepository] so Activity tests can drive the ViewModel state
 * without hitting the network.
 */
@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {
    val response = MutableStateFlow<Result<UserResponse>>(Result.success(UserResponse(data = emptyList())))

    override suspend fun getUsers(): Result<UserResponse> = response.value

    override suspend fun login(email: String, password: String): Result<User> =
        Result.failure(UnsupportedOperationException("not used in this test"))

    override suspend fun logout(): Result<Unit> = Result.success(Unit)

    override fun getCurrentUserFlow(): Flow<User?> = MutableStateFlow(null)
}

@Singleton
/**
 * The real TransactionRepository needs the SQLCipher database, which needs the
 * AndroidKeyStore; Robolectric has neither, so the report screen runs against this.
 */
class FakeTransactionRepository @Inject constructor() : TransactionRepositoryInterface {
    override suspend fun initiatePaymentViaApi(
        amount: String,
        description: String,
        customerId: String,
        paymentMethod: String
    ): Result<com.example.iurankomplek.data.api.models.PaymentResponse> =
        Result.failure(UnsupportedOperationException("not used in this test"))

    override suspend fun processPayment(request: com.example.iurankomplek.payment.PaymentRequest):
        Result<com.example.iurankomplek.transaction.Transaction> =
        Result.failure(UnsupportedOperationException("not used in this test"))

    override suspend fun getTransactionById(id: String) = null

    override fun getTransactionsByUserId(userId: String) = MutableStateFlow(emptyList<com.example.iurankomplek.transaction.Transaction>())

    override fun getTransactionsByStatus(status: com.example.iurankomplek.payment.PaymentStatus) =
        MutableStateFlow(emptyList<com.example.iurankomplek.transaction.Transaction>())

    override suspend fun updateTransaction(transaction: com.example.iurankomplek.transaction.Transaction) = Unit

    override suspend fun refundPayment(transactionId: String, reason: String?):
        Result<com.example.iurankomplek.payment.RefundResponse> =
        Result.failure(UnsupportedOperationException("not used in this test"))

    override suspend fun deleteTransaction(transaction: com.example.iurankomplek.transaction.Transaction) = Unit
}

@Singleton
class FakePemanfaatanRepository @Inject constructor() : PemanfaatanRepository {
    val response = MutableStateFlow<Result<PemanfaatanResponse>>(
        Result.success(PemanfaatanResponse(data = emptyList()))
    )

    override suspend fun getPemanfaatan(): Result<PemanfaatanResponse> = response.value
}

/**
 * Mirrors AppModule so only [UserRepository] is swapped; the remaining bindings keep the
 * production implementations because Hilt's ViewModel map needs all of them resolvable.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AppModule::class])
abstract class TestAppModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(repository: FakeUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindPemanfaatanRepository(repository: FakePemanfaatanRepository): PemanfaatanRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        repository: FakeTransactionRepository
    ): TransactionRepositoryInterface

    companion object {
        @Provides
        @Singleton
        fun provideApiService(): ApiService = ApiConfig.getApiService()

        @Provides
        @Singleton
        fun provideSessionManager(@ApplicationContext context: Context): UserSessionManager =
            UserSessionManager(context)

        @Provides
        @Singleton
        fun provideVendorRepository(apiService: ApiService): VendorRepository =
            VendorRepositoryImpl(apiService)

        @Provides
        @Singleton
        fun providePaymentGateway(apiService: ApiService): PaymentGateway = RealPaymentGateway(apiService)

        @Provides
        @Singleton
        fun provideContext(@ApplicationContext context: Context): Context = context
    }
}
