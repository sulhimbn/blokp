package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.Message
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
class MessageRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: MessageRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        repository = MessageRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getMessages should return success when API returns valid response`() = runTest {
        val userId = "user123"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = "user456",
                receiverId = userId,
                content = "Hello there!",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            ),
            Message(
                id = "msg2",
                senderId = userId,
                receiverId = "user456",
                content = "Hi back!",
                timestamp = "2024-01-01T01:00:00Z",
                readStatus = true,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessages(userId)).thenReturn(Response.success(mockData))

        val result = repository.getMessages(userId)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertNotNull(responseBody)
        assertEquals(2, responseBody?.size)
        assertEquals("Hello there!", responseBody?.get(0)?.content)
    }

    @Test
    fun `getMessages should cache messages for user`() = runTest {
        val userId = "user123"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = "user456",
                receiverId = userId,
                content = "Cached message",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessages(userId)).thenReturn(Response.success(mockData))

        repository.getMessages(userId)
        val cachedResult = repository.getMessages(userId)

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertEquals(1, responseBody?.size)
        verify(apiService, times(1)).getMessages(userId)
    }

    @Test
    fun `getMessages should handle empty message list`() = runTest {
        val userId = "user123"
        val emptyData = emptyList<Message>()

        `when`(apiService.getMessages(userId)).thenReturn(Response.success(emptyData))

        val result = repository.getMessages(userId)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(0, responseBody?.size)
    }

    @Test
    fun `getMessages should return failure when response body is null`() = runTest {
        val userId = "user123"

        `when`(apiService.getMessages(userId)).thenReturn(Response.success(null))

        val result = repository.getMessages(userId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Response body is null") == true)
    }

    @Test
    fun `getMessagesWithUser should return success when API returns valid response`() = runTest {
        val receiverId = "user123"
        val senderId = "user456"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = senderId,
                receiverId = receiverId,
                content = "Conversation message 1",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            ),
            Message(
                id = "msg2",
                senderId = receiverId,
                receiverId = senderId,
                content = "Conversation message 2",
                timestamp = "2024-01-01T01:00:00Z",
                readStatus = true,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessagesWithUser(receiverId, senderId))
            .thenReturn(Response.success(mockData))

        val result = repository.getMessagesWithUser(receiverId, senderId)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(2, responseBody?.size)
    }

    @Test
    fun `getMessagesWithUser should not cache results`() = runTest {
        val receiverId = "user123"
        val senderId = "user456"
        val firstData = listOf(
            Message(
                id = "msg1",
                senderId = senderId,
                receiverId = receiverId,
                content = "First message",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        val secondData = listOf(
            Message(
                id = "msg2",
                senderId = senderId,
                receiverId = receiverId,
                content = "Second message",
                timestamp = "2024-01-01T01:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessagesWithUser(receiverId, senderId))
            .thenReturn(Response.success(firstData))
            .thenReturn(Response.success(secondData))

        val firstResult = repository.getMessagesWithUser(receiverId, senderId)
        val secondResult = repository.getMessagesWithUser(receiverId, senderId)

        assertTrue(firstResult.isSuccess)
        assertTrue(secondResult.isSuccess)
        assertNotEquals(
            firstResult.getOrNull()?.get(0)?.content,
            secondResult.getOrNull()?.get(0)?.content
        )
        verify(apiService, times(2)).getMessagesWithUser(receiverId, senderId)
    }

    @Test
    fun `sendMessage should return success when API accepts message`() = runTest {
        val senderId = "user123"
        val receiverId = "user456"
        val content = "Test message"
        val mockMessage = Message(
            id = "msg1",
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            timestamp = "2024-01-01T00:00:00Z",
            readStatus = false,
            attachments = emptyList()
        )

        `when`(apiService.sendMessage(senderId, receiverId, content))
            .thenReturn(Response.success(mockMessage))

        val result = repository.sendMessage(senderId, receiverId, content)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals("msg1", responseBody?.id)
        assertEquals(content, responseBody?.content)
    }

    @Test
    fun `sendMessage should return failure when response body is null`() = runTest {
        val senderId = "user123"
        val receiverId = "user456"
        val content = "Test message"

        `when`(apiService.sendMessage(senderId, receiverId, content))
            .thenReturn(Response.success(null))

        val result = repository.sendMessage(senderId, receiverId, content)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Response body is null") == true)
    }

    @Test
    fun `getCachedMessages should return cached messages for user`() = runTest {
        val userId = "user123"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = "user456",
                receiverId = userId,
                content = "Cached message",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessages(userId)).thenReturn(Response.success(mockData))

        repository.getMessages(userId)
        val cachedResult = repository.getCachedMessages(userId)

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertEquals(1, responseBody?.size)
        assertEquals("Cached message", responseBody?.get(0)?.content)
    }

    @Test
    fun `getCachedMessages should return empty list when no cache exists for user`() = runTest {
        val userId = "user123"

        val cachedResult = repository.getCachedMessages(userId)

        assertTrue(cachedResult.isSuccess)
        val responseBody = cachedResult.getOrNull()
        assertNotNull(responseBody)
        assertEquals(0, responseBody?.size)
    }

    @Test
    fun `clearCache should successfully clear cache`() = runTest {
        val userId = "user123"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = "user456",
                receiverId = userId,
                content = "Cached message",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessages(userId)).thenReturn(Response.success(mockData))

        repository.getMessages(userId)
        val clearResult = repository.clearCache()
        val cachedAfterClear = repository.getCachedMessages(userId)

        assertTrue(clearResult.isSuccess)
        assertEquals(0, cachedAfterClear.getOrNull()?.size)
    }

    @Test
    fun `getMessages should retry on SocketTimeoutException`() = runTest {
        val userId = "user123"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = "user456",
                receiverId = userId,
                content = "Message after retry",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessages(userId))
            .thenThrow(SocketTimeoutException())
            .thenThrow(SocketTimeoutException())
            .thenReturn(Response.success(mockData))

        val result = repository.getMessages(userId)

        assertTrue(result.isSuccess)
        verify(apiService, atLeast(2)).getMessages(userId)
    }

    @Test
    fun `getMessages should retry on UnknownHostException`() = runTest {
        val userId = "user123"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = "user456",
                receiverId = userId,
                content = "Message after retry",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessages(userId))
            .thenThrow(UnknownHostException())
            .thenReturn(Response.success(mockData))

        val result = repository.getMessages(userId)

        assertTrue(result.isSuccess)
        verify(apiService, atLeast(2)).getMessages(userId)
    }

    @Test
    fun `getMessages should retry on SSLException`() = runTest {
        val userId = "user123"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = "user456",
                receiverId = userId,
                content = "Message after retry",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessages(userId))
            .thenThrow(SSLException("SSL error"))
            .thenReturn(Response.success(mockData))

        val result = repository.getMessages(userId)

        assertTrue(result.isSuccess)
        verify(apiService, atLeast(2)).getMessages(userId)
    }

    @Test
    fun `getMessages should handle messages with attachments`() = runTest {
        val userId = "user123"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = "user456",
                receiverId = userId,
                content = "Message with attachments",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = listOf("image1.jpg", "document.pdf")
            )
        )

        `when`(apiService.getMessages(userId)).thenReturn(Response.success(mockData))

        val result = repository.getMessages(userId)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(2, responseBody?.get(0)?.attachments?.size)
    }

    @Test
    fun `getMessages should handle both read and unread messages`() = runTest {
        val userId = "user123"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = "user456",
                receiverId = userId,
                content = "Unread message",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            ),
            Message(
                id = "msg2",
                senderId = "user456",
                receiverId = userId,
                content = "Read message",
                timestamp = "2024-01-01T01:00:00Z",
                readStatus = true,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessages(userId)).thenReturn(Response.success(mockData))

        val result = repository.getMessages(userId)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertEquals(2, responseBody?.size)
        assertFalse(responseBody?.get(0)?.readStatus ?: true)
        assertTrue(responseBody?.get(1)?.readStatus ?: false)
    }

    @Test
    fun `sendMessage should handle long content`() = runTest {
        val senderId = "user123"
        val receiverId = "user456"
        val longContent = "A".repeat(5000)
        val mockMessage = Message(
            id = "msg1",
            senderId = senderId,
            receiverId = receiverId,
            content = longContent,
            timestamp = "2024-01-01T00:00:00Z",
            readStatus = false,
            attachments = emptyList()
        )

        `when`(apiService.sendMessage(senderId, receiverId, longContent))
            .thenReturn(Response.success(mockMessage))

        val result = repository.sendMessage(senderId, receiverId, longContent)

        assertTrue(result.isSuccess)
        assertEquals(5000, result.getOrNull()?.content?.length)
    }

    @Test
    fun `getMessages should handle messages with special characters`() = runTest {
        val userId = "user123"
        val mockData = listOf(
            Message(
                id = "msg1",
                senderId = "user456",
                receiverId = userId,
                content = "Message with émojis 🎉 and spëcial çharacters",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        `when`(apiService.getMessages(userId)).thenReturn(Response.success(mockData))

        val result = repository.getMessages(userId)

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertTrue(responseBody?.get(0)?.content?.contains("🎉") ?: false)
    }
}
