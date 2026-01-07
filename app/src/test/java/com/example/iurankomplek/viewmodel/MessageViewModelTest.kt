package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.MessageRepository
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class MessageViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var messageRepository: MessageRepository

    private lateinit var viewModel: MessageViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MessageViewModel(messageRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMessages should emit Loading state initially`() = runTest {
        val messages = listOf(
            Message(
                id = 1,
                content = "Hello",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = emptyList()
            )
        )
        Mockito.`when`(messageRepository.getMessages("user1"))
            .thenReturn(Result.success(messages))

        viewModel.loadMessages("user1")

        val loadingState = viewModel.messagesState.value
        assertTrue(loadingState is UiState.Loading)
    }

    @Test
    fun `loadMessages should emit Success state when repository returns data`() = runTest {
        val messages = listOf(
            Message(
                id = 1,
                content = "First message",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = emptyList()
            ),
            Message(
                id = 2,
                content = "Second message",
                timestamp = "2024-01-01T11:00:00Z",
                senderId = "user1",
                isRead = true,
                attachments = emptyList()
            )
        )
        Mockito.`when`(messageRepository.getMessages("user1"))
            .thenReturn(Result.success(messages))

        viewModel.loadMessages("user1")
        advanceUntilIdle()

        val state = viewModel.messagesState.value
        assertTrue(state is UiState.Success)
        assertEquals(messages, (state as UiState.Success).data)
    }

    @Test
    fun `loadMessages should emit Error state when repository returns error`() = runTest {
        val errorMessage = "Network error occurred"
        Mockito.`when`(messageRepository.getMessages("user1"))
            .thenReturn(Result.failure(IOException(errorMessage)))

        viewModel.loadMessages("user1")
        advanceUntilIdle()

        val state = viewModel.messagesState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).error)
    }

    @Test
    fun `loadMessages should not make duplicate calls when already loading`() = runTest {
        val messages = listOf(
            Message(
                id = 1,
                content = "Test",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = emptyList()
            )
        )
        Mockito.`when`(messageRepository.getMessages("user1"))
            .thenReturn(Result.success(messages))

        viewModel.loadMessages("user1")
        viewModel.loadMessages("user1")

        advanceUntilIdle()
        Mockito.verify(messageRepository).getMessages("user1")
    }

    @Test
    fun `loadMessages should handle empty data correctly`() = runTest {
        Mockito.`when`(messageRepository.getMessages("user1"))
            .thenReturn(Result.success(emptyList()))

        viewModel.loadMessages("user1")
        advanceUntilIdle()

        val state = viewModel.messagesState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.isEmpty())
    }

    @Test
    fun `loadMessages should emit error with unknown message on null exception`() = runTest {
        Mockito.`when`(messageRepository.getMessages("user1"))
            .thenReturn(Result.failure(Exception()))

        viewModel.loadMessages("user1")
        advanceUntilIdle()

        val state = viewModel.messagesState.value
        assertTrue(state is UiState.Error)
        assertEquals("Unknown error occurred", (state as UiState.Error).error)
    }

    @Test
    fun `loadMessages should pass userId to repository`() = runTest {
        val userId = "user123"
        val messages = emptyList<Message>()
        Mockito.`when`(messageRepository.getMessages(userId))
            .thenReturn(Result.success(messages))

        viewModel.loadMessages(userId)
        advanceUntilIdle()

        Mockito.verify(messageRepository).getMessages(userId)
    }

    @Test
    fun `loadMessages should handle messages with attachments`() = runTest {
        val messages = listOf(
            Message(
                id = 1,
                content = "Message with attachment",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = listOf("image1.jpg", "document.pdf")
            )
        )
        Mockito.`when`(messageRepository.getMessages("user1"))
            .thenReturn(Result.success(messages))

        viewModel.loadMessages("user1")
        advanceUntilIdle()

        val state = viewModel.messagesState.value
        assertTrue(state is UiState.Success)
        val result = (state as UiState.Success).data
        assertEquals(2, result[0].attachments.size)
        assertTrue(result[0].attachments.contains("image1.jpg"))
    }

    @Test
    fun `loadMessages should preserve read status of messages`() = runTest {
        val messages = listOf(
            Message(
                id = 1,
                content = "Unread",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = emptyList()
            ),
            Message(
                id = 2,
                content = "Read",
                timestamp = "2024-01-01T11:00:00Z",
                senderId = "user1",
                isRead = true,
                attachments = emptyList()
            )
        )
        Mockito.`when`(messageRepository.getMessages("user1"))
            .thenReturn(Result.success(messages))

        viewModel.loadMessages("user1")
        advanceUntilIdle()

        val state = viewModel.messagesState.value
        assertTrue(state is UiState.Success)
        val result = (state as UiState.Success).data
        assertFalse(result[0].isRead)
        assertTrue(result[1].isRead)
    }
}
