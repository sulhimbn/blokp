package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

@OptIn(ExperimentalCoroutinesApi::class)
class AnnouncementRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: AnnouncementRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        repository = AnnouncementRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAnnouncements should return success when API returns valid response`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "Maintenance Notice",
                content = "Scheduled maintenance tomorrow",
                category = "maintenance",
                priority = "high",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = listOf("user1", "user2")
            ),
            Announcement(
                id = "2",
                title = "New Policy",
                content = "Updated community guidelines",
                category = "policy",
                priority = "medium",
                createdAt = "2024-01-02T00:00:00Z",
                readBy = emptyList()
            )
        )

        `when`(apiService.getAnnouncements()).thenReturn(Response.success(mockData))

        val result = repository.getAnnouncements(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertNotNull(responseBody)
        assertEquals(2, responseBody?.size)
        assertEquals("Maintenance Notice", responseBody?.get(0)?.title)
    }

    @Test
    fun `getAnnouncements should return cached data when forceRefresh is false`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "Cached Announcement",
                content = "This should come from cache",
                category = "general",
                priority = "low",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )

        `when`(apiService.getAnnouncements()).thenReturn(Response.success(mockData))

        repository.getAnnouncements(forceRefresh = true)
        val cachedResult = repository.getAnnouncements(forceRefresh = false)

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertEquals(1, responseBody?.size)
        assertEquals("Cached Announcement", responseBody?.get(0)?.title)
        verify(apiService, times(1)).getAnnouncements()
    }

    @Test
    fun `getAnnouncements should clear cache and fetch new data when forceRefresh is true`() = runTest {
        val firstData = listOf(
            Announcement(
                id = "1",
                title = "Old Announcement",
                content = "Old content",
                category = "general",
                priority = "low",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )

        val newData = listOf(
            Announcement(
                id = "2",
                title = "New Announcement",
                content = "New content",
                category = "urgent",
                priority = "high",
                createdAt = "2024-01-02T00:00:00Z",
                readBy = emptyList()
            )
        )

        `when`(apiService.getAnnouncements())
            .thenReturn(Response.success(firstData))
            .thenReturn(Response.success(newData))

        repository.getAnnouncements(forceRefresh = true)
        val refreshedResult = repository.getAnnouncements(forceRefresh = true)

        assertTrue(refreshedResult.isSuccess)
        val responseBody = refreshedResult.getOrNull()
        assertEquals(1, responseBody?.size)
        assertEquals("New Announcement", responseBody?.get(0)?.title)
        verify(apiService, times(2)).getAnnouncements()
    }

    @Test
    fun `getAnnouncements should return failure when response body is null`() = runTest {
        `when`(apiService.getAnnouncements()).thenReturn(Response.success(null))

        val result = repository.getAnnouncements(forceRefresh = true)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Response body is null") == true)
    }

    @Test
    fun `getAnnouncements should retry on SocketTimeoutException`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "Announcement after retry",
                content = "Content",
                category = "general",
                priority = "low",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )

        `when`(apiService.getAnnouncements())
            .thenThrow(SocketTimeoutException())
            .thenThrow(SocketTimeoutException())
            .thenReturn(Response.success(mockData))

        val result = repository.getAnnouncements(forceRefresh = true)

        assertTrue(result.isSuccess)
        verify(apiService, atLeast(2)).getAnnouncements()
    }

    @Test
    fun `getAnnouncements should retry on UnknownHostException`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "Announcement after retry",
                content = "Content",
                category = "general",
                priority = "low",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )

        `when`(apiService.getAnnouncements())
            .thenThrow(UnknownHostException())
            .thenReturn(Response.success(mockData))

        val result = repository.getAnnouncements(forceRefresh = true)

        assertTrue(result.isSuccess)
        verify(apiService, atLeast(2)).getAnnouncements()
    }

    @Test
    fun `getAnnouncements should retry on SSLException`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "Announcement after retry",
                content = "Content",
                category = "general",
                priority = "low",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )

        `when`(apiService.getAnnouncements())
            .thenThrow(SSLException("SSL error"))
            .thenReturn(Response.success(mockData))

        val result = repository.getAnnouncements(forceRefresh = true)

        assertTrue(result.isSuccess)
        verify(apiService, atLeast(2)).getAnnouncements()
    }

    @Test
    fun `getCachedAnnouncements should return cached announcements`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "Cached Announcement",
                content = "Cached content",
                category = "general",
                priority = "low",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )

        `when`(apiService.getAnnouncements()).thenReturn(Response.success(mockData))

        repository.getAnnouncements(forceRefresh = true)
        val cachedResult = repository.getCachedAnnouncements()

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertEquals(1, responseBody?.size)
        assertEquals("Cached Announcement", responseBody?.get(0)?.title)
    }

    @Test
    fun `getCachedAnnouncements should return empty list when cache is empty`() = runTest {
        val cachedResult = repository.getCachedAnnouncements()

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertNotNull(responseBody)
        assertEquals(0, responseBody?.size)
    }

    @Test
    fun `clearCache should successfully clear cache`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "Cached Announcement",
                content = "Cached content",
                category = "general",
                priority = "low",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )

        `when`(apiService.getAnnouncements()).thenReturn(Response.success(mockData))

        repository.getAnnouncements(forceRefresh = true)
        val clearResult = repository.clearCache()
        val cachedAfterClear = repository.getCachedAnnouncements()

        assertTrue(clearResult.isSuccess)
        assertEquals(0, cachedAfterClear.getOrNull()?.size)
    }

    @Test
    fun `getAnnouncements should cache announcements successfully`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "First Announcement",
                content = "First content",
                category = "general",
                priority = "low",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = emptyList()
            ),
            Announcement(
                id = "2",
                title = "Second Announcement",
                content = "Second content",
                category = "urgent",
                priority = "high",
                createdAt = "2024-01-02T00:00:00Z",
                readBy = listOf("user1")
            )
        )

        `when`(apiService.getAnnouncements()).thenReturn(Response.success(mockData))

        repository.getAnnouncements(forceRefresh = true)
        val cachedResult = repository.getCachedAnnouncements()

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertEquals(2, responseBody?.size)
    }

    @Test
    fun `getAnnouncements should handle empty announcement list`() = runTest {
        val emptyData = emptyList<Announcement>()

        `when`(apiService.getAnnouncements()).thenReturn(Response.success(emptyData))

        val result = repository.getAnnouncements(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(0, responseBody?.size)
    }

    @Test
    fun `getAnnouncements should handle announcement with empty readBy list`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "Unread Announcement",
                content = "Content",
                category = "general",
                priority = "medium",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = emptyList()
            )
        )

        `when`(apiService.getAnnouncements()).thenReturn(Response.success(mockData))

        val result = repository.getAnnouncements(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(emptyList<String>(), responseBody?.get(0)?.readBy)
    }

    @Test
    fun `getAnnouncements should handle announcement with multiple readers`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "Read Announcement",
                content = "Content",
                category = "general",
                priority = "low",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = listOf("user1", "user2", "user3", "user4", "user5")
            )
        )

        `when`(apiService.getAnnouncements()).thenReturn(Response.success(mockData))

        val result = repository.getAnnouncements(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(5, responseBody?.get(0)?.readBy?.size)
    }

    @Test
    fun `getAnnouncements should handle different priority levels`() = runTest {
        val mockData = listOf(
            Announcement(
                id = "1",
                title = "Low Priority",
                content = "Content",
                category = "general",
                priority = "low",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = emptyList()
            ),
            Announcement(
                id = "2",
                title = "Medium Priority",
                content = "Content",
                category = "general",
                priority = "medium",
                createdAt = "2024-01-02T00:00:00Z",
                readBy = emptyList()
            ),
            Announcement(
                id = "3",
                title = "High Priority",
                content = "Content",
                category = "urgent",
                priority = "high",
                createdAt = "2024-01-03T00:00:00Z",
                readBy = emptyList()
            )
        )

        `when`(apiService.getAnnouncements()).thenReturn(Response.success(mockData))

        val result = repository.getAnnouncements(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(3, responseBody?.size)
        assertEquals("low", responseBody?.get(0)?.priority)
        assertEquals("medium", responseBody?.get(1)?.priority)
        assertEquals("high", responseBody?.get(2)?.priority)
    }
}
