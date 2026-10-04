package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.CacheManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var sessionManager: UserSessionManager

    private lateinit var repository: UserRepositoryImpl

    private fun dataItem(
        email: String = "john@example.com",
        firstName: String = "John",
        lastName: String = "Doe"
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = "Jl. Contoh No. 1",
        iuran_perwarga = 150000,
        total_iuran_rekap = 1800000,
        jumlah_iuran_bulanan = 150000,
        total_iuran_individu = 150000,
        pengeluaran_iuran_warga = 50000,
        pemanfaatan_iuran = "Perbaikan jalan komplek",
        avatar = "https://example.com/avatar1.jpg"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        CacheManager.getInstance().clearSync()
        repository = UserRepositoryImpl(apiService, sessionManager)
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
    }

    @Test
    fun `getUsers returns success when API returns valid response`() = runTest {
        `when`(apiService.getUsers()).thenReturn(
            Response.success(UserResponse(data = listOf(dataItem())))
        )

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.data?.size)
        assertEquals("john@example.com", result.getOrNull()?.data?.first()?.email)
    }

    @Test
    fun `getUsers returns failure when every response is unsuccessful`() = runTest {
        `when`(apiService.getUsers()).thenReturn(
            Response.error<UserResponse>(500, "Server Error".toResponseBody())
        )

        val result = repository.getUsers()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getUsers returns failure on IOException`() = runTest {
        `when`(apiService.getUsers()).thenAnswer { throw RuntimeException("Network error") }

        val result = repository.getUsers()

        assertTrue("expected failure but got $result", result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("Network error"))
    }

    @Test
    fun `getUsers retries a retryable server error and then succeeds`() = runTest {
        `when`(apiService.getUsers())
            .thenReturn(Response.error<UserResponse>(503, "Unavailable".toResponseBody()))
            .thenReturn(Response.success(UserResponse(data = listOf(dataItem()))))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `getUsers does not retry a non-retryable client error`() = runTest {
        `when`(apiService.getUsers()).thenReturn(
            Response.error<UserResponse>(400, "Bad Request".toResponseBody())
        )

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers serves the second call from cache without a second network call`() = runTest {
        `when`(apiService.getUsers()).thenReturn(
            Response.success(UserResponse(data = listOf(dataItem())))
        )

        repository.getUsers()
        val second = repository.getUsers()

        assertTrue(second.isSuccess)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `login authenticates and persists the session when the email matches`() = runTest {
        `when`(apiService.getUsers()).thenReturn(
            Response.success(UserResponse(data = listOf(dataItem(email = "john@example.com"))))
        )
        val authenticated = User(
            id = "john@example.com",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe",
            avatar = "https://example.com/avatar1.jpg"
        )

        val result = repository.login("john@example.com", "any-password")

        assertTrue(result.isSuccess)
        assertEquals("john@example.com", result.getOrNull()?.email)
        assertEquals("John Doe", result.getOrNull()?.fullName)
        verify(sessionManager).setCurrentUser(authenticated)
    }

    @Test
    fun `login fails without persisting a session when no user matches`() = runTest {
        `when`(apiService.getUsers()).thenReturn(
            Response.success(UserResponse(data = listOf(dataItem(email = "john@example.com"))))
        )

        val result = repository.login("nobody@example.com", "bad-password")

        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
        verify(sessionManager, never()).setCurrentUser(any())
    }

    @Test
    fun `login propagates an upstream network failure`() = runTest {
        `when`(apiService.getUsers()).thenAnswer { throw RuntimeException("Network error") }

        val result = repository.login("john@example.com", "any-password")

        assertTrue("expected failure but got $result", result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("Network error"))
    }

    @Test
    fun `logout clears the session and the cached user list`() = runTest {
        `when`(apiService.getUsers()).thenReturn(
            Response.success(UserResponse(data = listOf(dataItem())))
        )
        repository.getUsers()
        assertTrue(CacheManager.getInstance().contains("user_list"))

        val result = repository.logout()

        assertTrue(result.isSuccess)
        verify(sessionManager).clearSession()
        assertTrue(!CacheManager.getInstance().contains("user_list"))
    }

    @Test
    fun `getCurrentUserFlow emits the signed-in user`() = runTest {
        val user = User(
            id = "john@example.com",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe",
            avatar = "https://example.com/avatar1.jpg"
        )
        `when`(sessionManager.currentUser).thenReturn(MutableStateFlow(user))

        assertEquals(user, repository.getCurrentUserFlow().first())
    }

    @Test
    fun `getCurrentUserFlow emits null when nobody is signed in`() = runTest {
        `when`(sessionManager.currentUser).thenReturn(MutableStateFlow(null))

        assertNull(repository.getCurrentUserFlow().first())
    }
}