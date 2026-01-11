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

class SendMessageUseCaseTest {

    @Mock
    private lateinit var mockRepository: MessageRepository

    private lateinit var useCase: SendMessageUseCase

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
        useCase = SendMessageUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        doReturn(OperationResult.Success(testMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", "Hello!"
        )

        val result = useCase("user1", "user2", "Hello!")

        assertTrue(result.isSuccess)
        assertEquals(testMessage, result.getOrNull())
        verify(mockRepository).sendMessage("user1", "user2", "Hello!")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with message containing all fields`() = runTest {
        val expectedMessage = testMessage.copy(
            content = "Test message content",
            timestamp = Date()
        )
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "sender1", "receiver1", "Test message content"
        )

        val result = useCase("sender1", "receiver1", "Test message content")

        assertTrue(result.isSuccess)
        val message = result.getOrNull()
        assertEquals("sender1", message?.senderId)
        assertEquals("receiver1", message?.receiverId)
        assertEquals("Test message content", message?.content)
        verify(mockRepository).sendMessage("sender1", "receiver1", "Test message content")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        val exception = RuntimeException("Network error")
        doReturn(OperationResult.Error(exception, "Network error")).`when`(mockRepository).sendMessage(
            "user1", "user2", "Hello!"
        )

        val result = useCase("user1", "user2", "Hello!")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).sendMessage("user1", "user2", "Hello!")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws exception`() = runTest {
        val exception = IllegalStateException("Repository not initialized")
        doThrow(exception).`when`(mockRepository).sendMessage(
            "user1", "user2", "Hello!"
        )

        val result = useCase("user1", "user2", "Hello!")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalStateException)
        assertEquals("Repository not initialized", error?.message)
        verify(mockRepository).sendMessage("user1", "user2", "Hello!")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Connection lost")
        doThrow(exception).`when`(mockRepository).sendMessage(
            "user1", "user2", "Hello!"
        )

        val result = useCase("user1", "user2", "Hello!")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is java.io.IOException)
        assertEquals("Connection lost", error?.message)
        verify(mockRepository).sendMessage("user1", "user2", "Hello!")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with empty content`() = runTest {
        val expectedMessage = testMessage.copy(content = "")
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", ""
        )

        val result = useCase("user1", "user2", "")

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull()?.content)
        verify(mockRepository).sendMessage("user1", "user2", "")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with long content`() = runTest {
        val longContent = "a".repeat(1000)
        val expectedMessage = testMessage.copy(content = longContent)
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", longContent
        )

        val result = useCase("user1", "user2", longContent)

        assertTrue(result.isSuccess)
        assertEquals(longContent, result.getOrNull()?.content)
        verify(mockRepository).sendMessage("user1", "user2", longContent)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with special characters in content`() = runTest {
        val specialContent = "Hello! @#$%^&*()_+-=[]{}|;':\",./<>?"
        val expectedMessage = testMessage.copy(content = specialContent)
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", specialContent
        )

        val result = useCase("user1", "user2", specialContent)

        assertTrue(result.isSuccess)
        assertEquals(specialContent, result.getOrNull()?.content)
        verify(mockRepository).sendMessage("user1", "user2", specialContent)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with unicode characters in content`() = runTest {
        val unicodeContent = "Hello 你好 مرحبا 안녕하세요"
        val expectedMessage = testMessage.copy(content = unicodeContent)
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", unicodeContent
        )

        val result = useCase("user1", "user2", unicodeContent)

        assertTrue(result.isSuccess)
        assertEquals(unicodeContent, result.getOrNull()?.content)
        verify(mockRepository).sendMessage("user1", "user2", unicodeContent)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with whitespace in content`() = runTest {
        val whitespaceContent = "  Hello World  "
        val expectedMessage = testMessage.copy(content = whitespaceContent)
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", whitespaceContent
        )

        val result = useCase("user1", "user2", whitespaceContent)

        assertTrue(result.isSuccess)
        assertEquals(whitespaceContent, result.getOrNull()?.content)
        verify(mockRepository).sendMessage("user1", "user2", whitespaceContent)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with numeric content`() = runTest {
        val numericContent = "1234567890"
        val expectedMessage = testMessage.copy(content = numericContent)
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", numericContent
        )

        val result = useCase("user1", "user2", numericContent)

        assertTrue(result.isSuccess)
        assertEquals(numericContent, result.getOrNull()?.content)
        verify(mockRepository).sendMessage("user1", "user2", numericContent)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with same sender and receiver`() = runTest {
        val expectedMessage = testMessage.copy(senderId = "user1", receiverId = "user1")
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user1", "Self message"
        )

        val result = useCase("user1", "user1", "Self message")

        assertTrue(result.isSuccess)
        assertEquals("user1", result.getOrNull()?.senderId)
        assertEquals("user1", result.getOrNull()?.receiverId)
        verify(mockRepository).sendMessage("user1", "user1", "Self message")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with numeric user IDs`() = runTest {
        val expectedMessage = testMessage.copy(senderId = "123", receiverId = "456")
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "123", "456", "Numeric IDs"
        )

        val result = useCase("123", "456", "Numeric IDs")

        assertTrue(result.isSuccess)
        assertEquals("123", result.getOrNull()?.senderId)
        assertEquals("456", result.getOrNull()?.receiverId)
        verify(mockRepository).sendMessage("123", "456", "Numeric IDs")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with alphanumeric user IDs`() = runTest {
        val expectedMessage = testMessage.copy(senderId = "user_123", receiverId = "user_456")
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user_123", "user_456", "Alphanumeric IDs"
        )

        val result = useCase("user_123", "user_456", "Alphanumeric IDs")

        assertTrue(result.isSuccess)
        assertEquals("user_123", result.getOrNull()?.senderId)
        assertEquals("user_456", result.getOrNull()?.receiverId)
        verify(mockRepository).sendMessage("user_123", "user_456", "Alphanumeric IDs")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with descriptive message`() = runTest {
        val exception = RuntimeException("Message sending failed: timeout")
        doReturn(OperationResult.Error(exception, "Message sending failed: timeout")).`when`(
            mockRepository
        ).sendMessage("user1", "user2", "Hello!")

        val result = useCase("user1", "user2", "Hello!")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals("Message sending failed: timeout", result.exceptionOrNull()?.message)
        verify(mockRepository).sendMessage("user1", "user2", "Hello!")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles null repository response gracefully`() = runTest {
        val exception = NullPointerException("Repository returned null")
        doReturn(OperationResult.Error(exception, "Repository returned null")).`when`(
            mockRepository
        ).sendMessage("user1", "user2", "Hello!")

        val result = useCase("user1", "user2", "Hello!")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NullPointerException)
        verify(mockRepository).sendMessage("user1", "user2", "Hello!")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with single character content`() = runTest {
        val expectedMessage = testMessage.copy(content = "x")
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", "x"
        )

        val result = useCase("user1", "user2", "x")

        assertTrue(result.isSuccess)
        assertEquals("x", result.getOrNull()?.content)
        verify(mockRepository).sendMessage("user1", "user2", "x")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with multiline content`() = runTest {
        val multilineContent = "Line 1\nLine 2\nLine 3"
        val expectedMessage = testMessage.copy(content = multilineContent)
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", multilineContent
        )

        val result = useCase("user1", "user2", multilineContent)

        assertTrue(result.isSuccess)
        assertEquals(multilineContent, result.getOrNull()?.content)
        verify(mockRepository).sendMessage("user1", "user2", multilineContent)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with emoji content`() = runTest {
        val emojiContent = "Hello! 👋 😊 🎉"
        val expectedMessage = testMessage.copy(content = emojiContent)
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", emojiContent
        )

        val result = useCase("user1", "user2", emojiContent)

        assertTrue(result.isSuccess)
        assertEquals(emojiContent, result.getOrNull()?.content)
        verify(mockRepository).sendMessage("user1", "user2", emojiContent)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with URL content`() = runTest {
        val urlContent = "Check out https://example.com"
        val expectedMessage = testMessage.copy(content = urlContent)
        doReturn(OperationResult.Success(expectedMessage)).`when`(mockRepository).sendMessage(
            "user1", "user2", urlContent
        )

        val result = useCase("user1", "user2", urlContent)

        assertTrue(result.isSuccess)
        assertEquals(urlContent, result.getOrNull()?.content)
        verify(mockRepository).sendMessage("user1", "user2", urlContent)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws validation exception`() = runTest {
        val exception = IllegalArgumentException("Invalid user ID")
        doThrow(exception).`when`(mockRepository).sendMessage(
            "", "user2", "Hello!"
        )

        val result = useCase("", "user2", "Hello!")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertEquals("Invalid user ID", error?.message)
        verify(mockRepository).sendMessage("", "user2", "Hello!")
        verifyNoMoreInteractions(mockRepository)
    }
}
