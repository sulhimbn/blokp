package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.Comment
import com.example.iurankomplek.model.CommunityPost
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
class CommunityPostRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: CommunityPostRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        repository = CommunityPostRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCommunityPosts should return success when API returns valid response`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "First Post",
                content = "This is my first community post",
                category = "general",
                likes = 10,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            ),
            CommunityPost(
                id = "post2",
                authorId = "user456",
                title = "Second Post",
                content = "Another post here",
                category = "discussion",
                likes = 5,
                comments = emptyList(),
                createdAt = "2024-01-02T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(mockData))

        val result = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertNotNull(responseBody)
        assertEquals(2, responseBody?.size)
        assertEquals("First Post", responseBody?.get(0)?.title)
    }

    @Test
    fun `getCommunityPosts should return cached data when forceRefresh is false`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "Cached Post",
                content = "This should come from cache",
                category = "general",
                likes = 10,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(mockData))

        repository.getCommunityPosts(forceRefresh = true)
        val cachedResult = repository.getCommunityPosts(forceRefresh = false)

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertEquals(1, responseBody?.size)
        assertEquals("Cached Post", responseBody?.get(0)?.title)
        verify(apiService, times(1)).getCommunityPosts()
    }

    @Test
    fun `getCommunityPosts should clear cache and fetch new data when forceRefresh is true`() = runTest {
        val firstData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "Old Post",
                content = "Old content",
                category = "general",
                likes = 10,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        val newData = listOf(
            CommunityPost(
                id = "post2",
                authorId = "user456",
                title = "New Post",
                content = "New content",
                category = "urgent",
                likes = 20,
                comments = emptyList(),
                createdAt = "2024-01-02T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts())
            .thenReturn(Response.success(firstData))
            .thenReturn(Response.success(newData))

        repository.getCommunityPosts(forceRefresh = true)
        val refreshedResult = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(refreshedResult.isSuccess)
        val responseBody = refreshedResult.getOrNull()
        assertEquals(1, responseBody?.size)
        assertEquals("New Post", responseBody?.get(0)?.title)
        verify(apiService, times(2)).getCommunityPosts()
    }

    @Test
    fun `getCommunityPosts should return failure when response body is null`() = runTest {
        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(null))

        val result = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Response body is null") == true)
    }

    @Test
    fun `getCommunityPosts should retry on SocketTimeoutException`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "Post after retry",
                content = "Content",
                category = "general",
                likes = 10,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts())
            .thenThrow(SocketTimeoutException())
            .thenThrow(SocketTimeoutException())
            .thenReturn(Response.success(mockData))

        val result = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(result.isSuccess)
        verify(apiService, atLeast(2)).getCommunityPosts()
    }

    @Test
    fun `getCommunityPosts should retry on UnknownHostException`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "Post after retry",
                content = "Content",
                category = "general",
                likes = 10,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts())
            .thenThrow(UnknownHostException())
            .thenReturn(Response.success(mockData))

        val result = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(result.isSuccess)
        verify(apiService, atLeast(2)).getCommunityPosts()
    }

    @Test
    fun `getCommunityPosts should retry on SSLException`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "Post after retry",
                content = "Content",
                category = "general",
                likes = 10,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts())
            .thenThrow(SSLException("SSL error"))
            .thenReturn(Response.success(mockData))

        val result = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(result.isSuccess)
        verify(apiService, atLeast(2)).getCommunityPosts()
    }

    @Test
    fun `createCommunityPost should return success when API accepts post`() = runTest {
        val authorId = "user123"
        val title = "New Community Post"
        val content = "This is a new post"
        val category = "general"
        val mockPost = CommunityPost(
            id = "post1",
            authorId = authorId,
            title = title,
            content = content,
            category = category,
            likes = 0,
            comments = emptyList(),
            createdAt = "2024-01-01T00:00:00Z"
        )

        `when`(apiService.createCommunityPost(authorId, title, content, category))
            .thenReturn(Response.success(mockPost))

        val result = repository.createCommunityPost(authorId, title, content, category)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals("post1", responseBody?.id)
        assertEquals(title, responseBody?.title)
        assertEquals(content, responseBody?.content)
    }

    @Test
    fun `createCommunityPost should return failure when response body is null`() = runTest {
        val authorId = "user123"
        val title = "New Post"
        val content = "Content"
        val category = "general"

        `when`(apiService.createCommunityPost(authorId, title, content, category))
            .thenReturn(Response.success(null))

        val result = repository.createCommunityPost(authorId, title, content, category)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Response body is null") == true)
    }

    @Test
    fun `getCachedCommunityPosts should return cached posts`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "Cached Post",
                content = "Cached content",
                category = "general",
                likes = 10,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(mockData))

        repository.getCommunityPosts(forceRefresh = true)
        val cachedResult = repository.getCachedCommunityPosts()

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertEquals(1, responseBody?.size)
        assertEquals("Cached Post", responseBody?.get(0)?.title)
    }

    @Test
    fun `getCachedCommunityPosts should return empty list when cache is empty`() = runTest {
        val cachedResult = repository.getCachedCommunityPosts()

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertNotNull(responseBody)
        assertEquals(0, responseBody?.size)
    }

    @Test
    fun `clearCache should successfully clear cache`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "Cached Post",
                content = "Cached content",
                category = "general",
                likes = 10,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(mockData))

        repository.getCommunityPosts(forceRefresh = true)
        val clearResult = repository.clearCache()
        val cachedAfterClear = repository.getCachedCommunityPosts()

        assertTrue(clearResult.isSuccess)
        assertEquals(0, cachedAfterClear.getOrNull()?.size)
    }

    @Test
    fun `getCommunityPosts should handle empty post list`() = runTest {
        val emptyData = emptyList<CommunityPost>()

        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(emptyData))

        val result = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(0, responseBody?.size)
    }

    @Test
    fun `getCommunityPosts should cache posts successfully`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "First Post",
                content = "First content",
                category = "general",
                likes = 10,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            ),
            CommunityPost(
                id = "post2",
                authorId = "user456",
                title = "Second Post",
                content = "Second content",
                category = "urgent",
                likes = 20,
                comments = emptyList(),
                createdAt = "2024-01-02T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(mockData))

        repository.getCommunityPosts(forceRefresh = true)
        val cachedResult = repository.getCachedCommunityPosts()

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertEquals(2, responseBody?.size)
    }

    @Test
    fun `getCommunityPosts should handle posts with comments`() = runTest {
        val comments = listOf(
            Comment(
                id = "comment1",
                authorId = "user789",
                content = "Great post!",
                timestamp = "2024-01-01T01:00:00Z"
            ),
            Comment(
                id = "comment2",
                authorId = "user999",
                content = "I agree",
                timestamp = "2024-01-01T02:00:00Z"
            )
        )

        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "Post with comments",
                content = "Content",
                category = "discussion",
                likes = 15,
                comments = comments,
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(mockData))

        val result = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(2, responseBody?.get(0)?.comments?.size)
    }

    @Test
    fun `getCommunityPosts should handle posts with zero likes`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "Unpopular Post",
                content = "Content",
                category = "general",
                likes = 0,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(mockData))

        val result = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(0, responseBody?.get(0)?.likes)
    }

    @Test
    fun `getCommunityPosts should handle posts with many likes`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "Popular Post",
                content = "Content",
                category = "trending",
                likes = 1000,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(mockData))

        val result = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(1000, responseBody?.get(0)?.likes)
    }

    @Test
    fun `createCommunityPost should handle long content`() = runTest {
        val authorId = "user123"
        val title = "Post with long content"
        val longContent = "A".repeat(5000)
        val category = "general"
        val mockPost = CommunityPost(
            id = "post1",
            authorId = authorId,
            title = title,
            content = longContent,
            category = category,
            likes = 0,
            comments = emptyList(),
            createdAt = "2024-01-01T00:00:00Z"
        )

        `when`(apiService.createCommunityPost(authorId, title, longContent, category))
            .thenReturn(Response.success(mockPost))

        val result = repository.createCommunityPost(authorId, title, longContent, category)

        assertTrue(result.isSuccess)
        assertEquals(5000, result.getOrNull()?.content?.length)
    }

    @Test
    fun `createCommunityPost should handle special characters`() = runTest {
        val authorId = "user123"
        val title = "Post with spëcial çharacters"
        val content = "Content with émojis 🎉 and spëcial çharacters"
        val category = "general"
        val mockPost = CommunityPost(
            id = "post1",
            authorId = authorId,
            title = title,
            content = content,
            category = category,
            likes = 0,
            comments = emptyList(),
            createdAt = "2024-01-01T00:00:00Z"
        )

        `when`(apiService.createCommunityPost(authorId, title, content, category))
            .thenReturn(Response.success(mockPost))

        val result = repository.createCommunityPost(authorId, title, content, category)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.content?.contains("🎉") ?: false)
    }

    @Test
    fun `getCommunityPosts should handle different categories`() = runTest {
        val mockData = listOf(
            CommunityPost(
                id = "post1",
                authorId = "user123",
                title = "General Post",
                content = "Content",
                category = "general",
                likes = 10,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            ),
            CommunityPost(
                id = "post2",
                authorId = "user456",
                title = "Discussion Post",
                content = "Content",
                category = "discussion",
                likes = 15,
                comments = emptyList(),
                createdAt = "2024-01-02T00:00:00Z"
            ),
            CommunityPost(
                id = "post3",
                authorId = "user789",
                title = "Trending Post",
                content = "Content",
                category = "trending",
                likes = 20,
                comments = emptyList(),
                createdAt = "2024-01-03T00:00:00Z"
            )
        )

        `when`(apiService.getCommunityPosts()).thenReturn(Response.success(mockData))

        val result = repository.getCommunityPosts(forceRefresh = true)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(3, responseBody?.size)
        assertEquals("general", responseBody?.get(0)?.category)
        assertEquals("discussion", responseBody?.get(1)?.category)
        assertEquals("trending", responseBody?.get(2)?.category)
    }
}
