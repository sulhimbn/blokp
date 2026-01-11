package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.MessageRepository
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.util.Date

class LoadMessagesUseCaseTest {

    @Mock
    private lateinit var mockRepository: MessageRepository

    private lateinit var useCase: LoadMessagesUseCase

    private val testMessage = Message(
        id = "msg123",
        senderId = "user1",
        receiverId = "user2",
        content = "Hello!",
        timestamp = Date()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadMessagesUseCase(mockRepository)
    }

    @Test
    fun `invoke with userId returns success when repository succeeds`() = runTest {
        val expectedMessages = listOf(testMessage)
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isSuccess)
        assertEquals(expectedMessages, result.getOrNull())
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId returns success with empty list`() = runTest {
        val expectedMessages = emptyList<Message>()
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isSuccess)
        assertEquals(expectedMessages, result.getOrNull())
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId returns success with multiple messages`() = runTest {
        val expectedMessages = listOf(
            testMessage,
            testMessage.copy(id = "msg456", content = "Second message"),
            testMessage.copy(id = "msg789", content = "Third message")
        )
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId returns error when repository fails`() = runTest {
        val exception = RuntimeException("Database error")
        doReturn(OperationResult.Error(exception, "Database error")).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId returns error when repository throws exception`() = runTest {
        val exception = IllegalStateException("Repository not initialized")
        doThrow(exception).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalStateException)
        assertEquals("Repository not initialized", error?.message)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Network error")
        doThrow(exception).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is java.io.IOException)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with receiverId and senderId returns success when repository succeeds`() = runTest {
        val expectedMessages = listOf(testMessage)
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessagesWithUser(
            "user1", "user2"
        )

        val result = useCase("user1", "user2")

        assertTrue(result.isSuccess)
        assertEquals(expectedMessages, result.getOrNull())
        verify(mockRepository).getMessagesWithUser("user1", "user2")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with receiverId and senderId returns success with empty list`() = runTest {
        val expectedMessages = emptyList<Message>()
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessagesWithUser(
            "user1", "user2"
        )

        val result = useCase("user1", "user2")

        assertTrue(result.isSuccess)
        assertEquals(expectedMessages, result.getOrNull())
        verify(mockRepository).getMessagesWithUser("user1", "user2")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with receiverId and senderId returns success with conversation`() = runTest {
        val expectedMessages = listOf(
            testMessage.copy(id = "msg1", senderId = "user1", receiverId = "user2", content = "Hi"),
            testMessage.copy(id = "msg2", senderId = "user2", receiverId = "user1", content = "Hello"),
            testMessage.copy(id = "msg3", senderId = "user1", receiverId = "user2", content = "How are you?")
        )
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessagesWithUser(
            "user1", "user2"
        )

        val result = useCase("user1", "user2")

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        verify(mockRepository).getMessagesWithUser("user1", "user2")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with receiverId and senderId returns error when repository fails`() = runTest {
        val exception = RuntimeException("Query failed")
        doReturn(OperationResult.Error(exception, "Query failed")).`when`(mockRepository).getMessagesWithUser(
            "user1", "user2"
        )

        val result = useCase("user1", "user2")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getMessagesWithUser("user1", "user2")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with receiverId and senderId returns error when repository throws exception`() = runTest {
        val exception = IllegalArgumentException("Invalid user ID")
        doThrow(exception).`when`(mockRepository).getMessagesWithUser("", "user2")

        val result = useCase("", "user2")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertEquals("Invalid user ID", error?.message)
        verify(mockRepository).getMessagesWithUser("", "user2")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId handles large message list efficiently`() = runTest {
        val largeList = (1..100).map { i ->
            testMessage.copy(id = "msg$i", content = "Message $i")
        }
        doReturn(OperationResult.Success(largeList)).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.size)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId returns messages in correct order`() = runTest {
        val date1 = Date(1000)
        val date2 = Date(2000)
        val date3 = Date(3000)
        val expectedMessages = listOf(
            testMessage.copy(id = "msg1", timestamp = date1),
            testMessage.copy(id = "msg2", timestamp = date2),
            testMessage.copy(id = "msg3", timestamp = date3)
        )
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isSuccess)
        val messages = result.getOrNull()
        assertEquals(3, messages?.size)
        assertEquals("msg1", messages?.get(0)?.id)
        assertEquals("msg2", messages?.get(1)?.id)
        assertEquals("msg3", messages?.get(2)?.id)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId returns messages with all fields`() = runTest {
        val expectedMessages = listOf(testMessage)
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isSuccess)
        val message = result.getOrNull()?.first()
        assertEquals("msg123", message?.id)
        assertEquals("user1", message?.senderId)
        assertEquals("user2", message?.receiverId)
        assertEquals("Hello!", message?.content)
        assertNotNull(message?.timestamp)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with receiverId and senderId handles conversation correctly`() = runTest {
        val conversationMessages = listOf(
            testMessage.copy(id = "msg1", senderId = "user1", receiverId = "user2"),
            testMessage.copy(id = "msg2", senderId = "user2", receiverId = "user1")
        )
        doReturn(OperationResult.Success(conversationMessages)).`when`(mockRepository).getMessagesWithUser(
            "user1", "user2"
        )

        val result = useCase("user1", "user2")

        assertTrue(result.isSuccess)
        val messages = result.getOrNull()
        assertEquals(2, messages?.size)
        assertEquals("user1", messages?.get(0)?.senderId)
        assertEquals("user2", messages?.get(0)?.receiverId)
        assertEquals("user2", messages?.get(1)?.senderId)
        assertEquals("user1", messages?.get(1)?.receiverId)
        verify(mockRepository).getMessagesWithUser("user1", "user2")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId returns error with descriptive message`() = runTest {
        val exception = RuntimeException("Failed to load messages: timeout")
        doReturn(OperationResult.Error(exception, "Failed to load messages: timeout")).`when`(
            mockRepository
        ).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals("Failed to load messages: timeout", result.exceptionOrNull()?.message)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with receiverId and senderId returns error with descriptive message`() = runTest {
        val exception = RuntimeException("Failed to load conversation: database error")
        doReturn(OperationResult.Error(exception, "Failed to load conversation: database error")).`when`(
            mockRepository
        ).getMessagesWithUser("user1", "user2")

        val result = useCase("user1", "user2")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals("Failed to load conversation: database error", result.exceptionOrNull()?.message)
        verify(mockRepository).getMessagesWithUser("user1", "user2")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId handles messages with special characters`() = runTest {
        val specialMessage = testMessage.copy(content = "Special! @#$%^&*()")
        val expectedMessages = listOf(specialMessage)
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isSuccess)
        assertEquals("Special! @#$%^&*()", result.getOrNull()?.first()?.content)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId handles messages with unicode characters`() = runTest {
        val unicodeMessage = testMessage.copy(content = "你好 مرحبا 안녕하세요")
        val expectedMessages = listOf(unicodeMessage)
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isSuccess)
        assertEquals("你好 مرحبا 안녕하세요", result.getOrNull()?.first()?.content)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId handles empty content messages`() = runTest {
        val emptyContentMessage = testMessage.copy(content = "")
        val expectedMessages = listOf(emptyContentMessage)
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull()?.first()?.content)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with userId handles long content messages`() = runTest {
        val longContent = "a".repeat(1000)
        val longContentMessage = testMessage.copy(content = longContent)
        val expectedMessages = listOf(longContentMessage)
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessages("user1")

        val result = useCase("user1")

        assertTrue(result.isSuccess)
        assertEquals(longContent, result.getOrNull()?.first()?.content)
        verify(mockRepository).getMessages("user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with receiverId and senderId returns success for self conversation`() = runTest {
        val selfMessages = listOf(
            testMessage.copy(id = "msg1", senderId = "user1", receiverId = "user1", content = "Note 1"),
            testMessage.copy(id = "msg2", senderId = "user1", receiverId = "user1", content = "Note 2")
        )
        doReturn(OperationResult.Success(selfMessages)).`when`(mockRepository).getMessagesWithUser(
            "user1", "user1"
        )

        val result = useCase("user1", "user1")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        verify(mockRepository).getMessagesWithUser("user1", "user1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with receiverId and senderId handles same sender and receiver`() = runTest {
        val expectedMessages = listOf(testMessage.copy(senderId = "user1", receiverId = "user1"))
        doReturn(OperationResult.Success(expectedMessages)).`when`(mockRepository).getMessagesWithUser(
            "user1", "user1"
        )

        val result = useCase("user1", "user1")

        assertTrue(result.isSuccess)
        verify(mockRepository).getMessagesWithUser("user1", "user1")
        verifyNoMoreInteractions(mockRepository)
    }
}
