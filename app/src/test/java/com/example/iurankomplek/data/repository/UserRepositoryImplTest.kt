package com.example.iurankomplek.data.repository

import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var sessionManager: UserSessionManager
    private lateinit var repository: UserRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        apiService = mock(ApiService::class.java)
        sessionManager = mock(UserSessionManager::class.java)
        CacheManager.getInstance().clearSync()
        repository = UserRepositoryImpl(apiService, sessionManager)
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
        Dispatchers.resetMain()
    }

    private fun errorResponse(code: Int): Response<UserResponse> =
        Response.error(code, "{}".toResponseBody("application/json".toMediaType()))

    @Test
    fun `getUsers returns the payload the API returned`() = runTest(testDispatcher) {
        val payload = UserResponse(listOf(TestFixtures.dataItem()))
        `when`(apiService.getUsers()).thenReturn(Response.success(payload))

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        assertEquals(payload, result.getOrNull())
        assertEquals("John", result.getOrNull()?.data?.single()?.first_name)
    }

    @Test
    fun `getUsers maps a 404 and does not retry it`() = runTest(testDispatcher) {
        `when`(apiService.getUsers()).thenReturn(errorResponse(404))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        assertEquals("Resource not found", result.exceptionOrNull()?.message)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `getUsers maps a 401 to Unauthorized access`() = runTest(testDispatcher) {
        `when`(apiService.getUsers()).thenReturn(errorResponse(401))

        val result = repository.getUsers()

        assertEquals("Unauthorized access", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUsers retries a 500 up to maxRetries then reports Server error`() = runTest(testDispatcher) {
        `when`(apiService.getUsers()).thenReturn(errorResponse(500))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        assertEquals("Server error", result.exceptionOrNull()?.message)
        verify(apiService, times(4)).getUsers()
    }

    @Test
    fun `getUsers retries UnknownHostException and maps it to No internet connection`() =
        runTest(testDispatcher) {
            `when`(apiService.getUsers()).thenAnswer { throw UnknownHostException("dns") }

            val result = repository.getUsers()

            assertEquals("No internet connection", result.exceptionOrNull()?.message)
            verify(apiService, times(4)).getUsers()
        }

    @Test
    fun `getUsers retries SocketTimeoutException and maps it to Connection timeout`() =
        runTest(testDispatcher) {
            `when`(apiService.getUsers()).thenAnswer { throw SocketTimeoutException("slow") }

            val result = repository.getUsers()

            assertEquals("Connection timeout", result.exceptionOrNull()?.message)
        }

    @Test
    fun `getUsers does not retry a non-retryable exception`() = runTest(testDispatcher) {
        `when`(apiService.getUsers()).thenAnswer { throw IllegalStateException("bug") }

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        assertEquals("An error occurred: bug", result.exceptionOrNull()?.message)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `second getUsers call is served from cache without hitting the API again`() =
        runTest(testDispatcher) {
            val payload = UserResponse(listOf(TestFixtures.dataItem()))
            `when`(apiService.getUsers()).thenReturn(Response.success(payload))

            val first = repository.getUsers()
            val second = repository.getUsers()

            assertEquals(payload, first.getOrNull())
            assertEquals(payload, second.getOrNull())
            verify(apiService, times(1)).getUsers()
        }

    @Test
    fun `logout clears the session and drops the cached user list`() = runTest(testDispatcher) {
        val payload = UserResponse(listOf(TestFixtures.dataItem()))
        `when`(apiService.getUsers()).thenReturn(Response.success(payload))
        repository.getUsers()

        val logout = repository.logout()

        assertTrue(logout.isSuccess)
        verify(sessionManager).clearSession()
        assertTrue(!CacheManager.getInstance().contains("user_list"))

        `when`(apiService.getUsers()).thenReturn(Response.success(payload))
        repository.getUsers()
        verify(apiService, times(2)).getUsers()
    }

    @Test
    fun `login signs in a resident that exists in the API response`() = runTest(testDispatcher) {
        val payload = UserResponse(listOf(TestFixtures.dataItem(email = "john@example.com")))
        `when`(apiService.getUsers()).thenReturn(Response.success(payload))

        val result = repository.login("john@example.com", "irrelevant")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("john@example.com", user!!.email)
        assertEquals("John Doe", user.fullName)
        verify(sessionManager).setCurrentUser(user)
    }

    @Test
    fun `login fails for an email that is not in the resident list`() = runTest(testDispatcher) {
        val payload = UserResponse(listOf(TestFixtures.dataItem(email = "john@example.com")))
        `when`(apiService.getUsers()).thenReturn(Response.success(payload))

        val result = repository.login("nobody@example.com", "irrelevant")

        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login propagates a network failure instead of reporting bad credentials`() =
        runTest(testDispatcher) {
            `when`(apiService.getUsers()).thenAnswer { throw UnknownHostException("dns") }

            val result = repository.login("john@example.com", "irrelevant")

            assertTrue(result.isFailure)
            assertEquals("No internet connection", result.exceptionOrNull()?.message)
        }

    @Test
    fun `getCurrentUserFlow delegates to the session manager`() = runTest(testDispatcher) {
        val user: User = TestFixtures.user()
        `when`(sessionManager.currentUser).thenReturn(MutableStateFlow(user))

        assertEquals(user, repository.getCurrentUserFlow().first())
    }
}
