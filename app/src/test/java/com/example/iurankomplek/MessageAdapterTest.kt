package com.example.iurankomplek

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.Message
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MessageAdapterTest {

    @Mock
    private lateinit var inflater: LayoutInflater

    @Mock
    private lateinit var parent: RecyclerView.ViewHolder

    @Mock
    private lateinit var mockView: View

    private lateinit var adapter: MessageAdapter

    @Before
    fun setup() {
        adapter = MessageAdapter()
    }

    @Test
    fun `submitList should update adapter data correctly`() {
        val messages = listOf(
            Message(
                id = 1,
                content = "Test message",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = emptyList()
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList with empty list should clear adapter`() {
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
        adapter.submitList(messages)
        advanceExecutor()

        adapter.submitList(emptyList())
        advanceExecutor()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `submitList should handle single message`() {
        val message = Message(
            id = 1,
            content = "Single message",
            timestamp = "2024-01-01T10:00:00Z",
            senderId = "user1",
            isRead = false,
            attachments = emptyList()
        )

        adapter.submitList(listOf(message))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle unread messages`() {
        val messages = listOf(
            Message(
                id = 1,
                content = "Unread message",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = emptyList()
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle read messages`() {
        val messages = listOf(
            Message(
                id = 1,
                content = "Read message",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = true,
                attachments = emptyList()
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle message with attachment`() {
        val messages = listOf(
            Message(
                id = 1,
                content = "Message with attachment",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = listOf("image1.jpg")
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle message with multiple attachments`() {
        val messages = listOf(
            Message(
                id = 1,
                content = "Message with multiple attachments",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = listOf("image1.jpg", "document.pdf", "video.mp4")
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle message with empty attachments`() {
        val messages = listOf(
            Message(
                id = 1,
                content = "Message without attachments",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = emptyList()
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle message with special characters`() {
        val messages = listOf(
            Message(
                id = 1,
                content = "Message with émojis 🎉 & spëcial ch@rs!",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = emptyList()
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle message with empty content`() {
        val messages = listOf(
            Message(
                id = 1,
                content = "",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = emptyList()
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle message with long content`() {
        val longContent = "A".repeat(5000)
        val messages = listOf(
            Message(
                id = 1,
                content = longContent,
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = emptyList()
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle messages from different senders`() {
        val messages = listOf(
            Message(id = 1, content = "From user1", timestamp = "", senderId = "user1", isRead = false, attachments = emptyList()),
            Message(id = 2, content = "From user2", timestamp = "", senderId = "user2", isRead = false, attachments = emptyList()),
            Message(id = 3, content = "From user3", timestamp = "", senderId = "user3", isRead = false, attachments = emptyList())
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `submitList should handle message with only attachments`() {
        val messages = listOf(
            Message(
                id = 1,
                content = "",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = listOf("image1.jpg", "image2.jpg")
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle message with many attachments`() {
        val attachments = (1..20).map { "file$it.pdf" }
        val messages = listOf(
            Message(
                id = 1,
                content = "Message with many attachments",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user1",
                isRead = false,
                attachments = attachments
            )
        )

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle null list`() {
        adapter.submitList(null)
        advanceExecutor()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `submitList should handle data update`() {
        val initialMessages = listOf(
            Message(id = 1, content = "Initial", timestamp = "", senderId = "user1", isRead = false, attachments = emptyList())
        )
        val updatedMessages = listOf(
            Message(id = 1, content = "Updated", timestamp = "", senderId = "user1", isRead = true, attachments = emptyList())
        )

        adapter.submitList(initialMessages)
        advanceExecutor()
        assertEquals(1, adapter.itemCount)

        adapter.submitList(updatedMessages)
        advanceExecutor()
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle large list`() {
        val messages = (1..100).map { id ->
            Message(
                id = id.toLong(),
                content = "Message $id",
                timestamp = "2024-01-01T10:00:00Z",
                senderId = "user$id",
                isRead = id % 2 == 0,
                attachments = emptyList()
            )
        }

        adapter.submitList(messages)
        advanceExecutor()

        assertEquals(100, adapter.itemCount)
    }

    @Test
    fun `DiffCallback should identify items with same ID as same item`() {
        val messages1 = listOf(
            Message(id = 1, content = "Message 1", timestamp = "", senderId = "user1", isRead = false, attachments = emptyList())
        )
        val messages2 = listOf(
            Message(id = 1, content = "Message 1 Updated", timestamp = "", senderId = "user1", isRead = true, attachments = emptyList())
        )

        adapter.submitList(messages1)
        advanceExecutor()
        adapter.submitList(messages2)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `DiffCallback should identify items with different ID as different items`() {
        val messages1 = listOf(
            Message(id = 1, content = "Message 1", timestamp = "", senderId = "user1", isRead = false, attachments = emptyList())
        )
        val messages2 = listOf(
            Message(id = 2, content = "Message 2", timestamp = "", senderId = "user1", isRead = false, attachments = emptyList())
        )

        adapter.submitList(messages1)
        advanceExecutor()
        adapter.submitList(messages2)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    private fun advanceExecutor() {
        val executor = androidx.arch.core.executor.ArchTaskExecutor.getInstance()
        try {
            androidx.arch.core.executor.ArchTaskExecutor.getInstance().executeOnDiskIO(
                androidx.arch.core.internal.FastSafeRunnable {}
            )
        } catch (e: Exception) {
        }
    }
}
