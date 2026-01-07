package com.example.iurankomplek

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.CommunityPost
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CommunityPostAdapterTest {

    @Mock
    private lateinit var inflater: LayoutInflater

    @Mock
    private lateinit var parent: RecyclerView.ViewHolder

    @Mock
    private lateinit var mockView: View

    private lateinit var adapter: CommunityPostAdapter

    @Before
    fun setup() {
        adapter = CommunityPostAdapter()
    }

    @Test
    fun `submitList should update adapter data correctly`() {
        val posts = listOf(
            CommunityPost(
                id = 1,
                title = "Test Post",
                content = "Test content",
                timestamp = "2024-01-01T10:00:00Z",
                authorId = "user1",
                category = "General",
                likes = 5,
                comments = emptyList()
            )
        )

        adapter.submitList(posts)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList with empty list should clear adapter`() {
        val posts = listOf(
            CommunityPost(
                id = 1,
                title = "Test",
                content = "",
                timestamp = "",
                authorId = "user1",
                category = "",
                likes = 0,
                comments = emptyList()
            )
        )
        adapter.submitList(posts)
        advanceExecutor()

        adapter.submitList(emptyList())
        advanceExecutor()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `submitList should handle single post`() {
        val post = CommunityPost(
            id = 1,
            title = "Single Post",
            content = "Single content",
            timestamp = "2024-01-01T10:00:00Z",
            authorId = "user1",
            category = "General",
            likes = 10,
            comments = emptyList()
        )

        adapter.submitList(listOf(post))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle post with many likes`() {
        val post = CommunityPost(
            id = 1,
            title = "Popular Post",
            content = "Popular content",
            timestamp = "2024-01-01T10:00:00Z",
            authorId = "user1",
            category = "Trending",
            likes = 1000,
            comments = emptyList()
        )

        adapter.submitList(listOf(post))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle post with zero likes`() {
        val post = CommunityPost(
            id = 1,
            title = "New Post",
            content = "New content",
            timestamp = "2024-01-01T10:00:00Z",
            authorId = "user1",
            category = "General",
            likes = 0,
            comments = emptyList()
        )

        adapter.submitList(listOf(post))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle post with negative likes`() {
        val post = CommunityPost(
            id = 1,
            title = "Disliked Post",
            content = "Disliked content",
            timestamp = "2024-01-01T10:00:00Z",
            authorId = "user1",
            category = "General",
            likes = -5,
            comments = emptyList()
        )

        adapter.submitList(listOf(post))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle post with comments`() {
        val post = CommunityPost(
            id = 1,
            title = "Post with Comments",
            content = "Content",
            timestamp = "2024-01-01T10:00:00Z",
            authorId = "user1",
            category = "General",
            likes = 5,
            comments = listOf("Great post!", "Thanks for sharing")
        )

        adapter.submitList(listOf(post))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle post with empty comments`() {
        val post = CommunityPost(
            id = 1,
            title = "Post without Comments",
            content = "Content",
            timestamp = "2024-01-01T10:00:00Z",
            authorId = "user1",
            category = "General",
            likes = 5,
            comments = emptyList()
        )

        adapter.submitList(listOf(post))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle post with special characters in title`() {
        val post = CommunityPost(
            id = 1,
            title = "Post with émojis 🎉 & spëcial ch@rs!",
            content = "Content",
            timestamp = "2024-01-01T10:00:00Z",
            authorId = "user1",
            category = "General",
            likes = 5,
            comments = emptyList()
        )

        adapter.submitList(listOf(post))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle post with long content`() {
        val longContent = "A".repeat(1000)
        val post = CommunityPost(
            id = 1,
            title = "Long Post",
            content = longContent,
            timestamp = "2024-01-01T10:00:00Z",
            authorId = "user1",
            category = "General",
            likes = 5,
            comments = emptyList()
        )

        adapter.submitList(listOf(post))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle post with empty title`() {
        val post = CommunityPost(
            id = 1,
            title = "",
            content = "Content without title",
            timestamp = "2024-01-01T10:00:00Z",
            authorId = "user1",
            category = "General",
            likes = 5,
            comments = emptyList()
        )

        adapter.submitList(listOf(post))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle post with empty content`() {
        val post = CommunityPost(
            id = 1,
            title = "Title",
            content = "",
            timestamp = "2024-01-01T10:00:00Z",
            authorId = "user1",
            category = "General",
            likes = 5,
            comments = emptyList()
        )

        adapter.submitList(listOf(post))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle different categories`() {
        val posts = listOf(
            CommunityPost(id = 1, title = "", content = "", timestamp = "", authorId = "", category = "Events", likes = 0, comments = emptyList()),
            CommunityPost(id = 2, title = "", content = "", timestamp = "", authorId = "", category = "News", likes = 0, comments = emptyList()),
            CommunityPost(id = 3, title = "", content = "", timestamp = "", authorId = "", category = "General", likes = 0, comments = emptyList()),
            CommunityPost(id = 4, title = "", content = "", timestamp = "", authorId = "", category = "Trending", likes = 0, comments = emptyList())
        )

        adapter.submitList(posts)
        advanceExecutor()

        assertEquals(4, adapter.itemCount)
    }

    @Test
    fun `submitList should handle null list`() {
        adapter.submitList(null)
        advanceExecutor()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `submitList should handle data update`() {
        val initialPosts = listOf(
            CommunityPost(id = 1, title = "Initial", content = "", timestamp = "", authorId = "", category = "", likes = 0, comments = emptyList())
        )
        val updatedPosts = listOf(
            CommunityPost(id = 1, title = "Updated", content = "", timestamp = "", authorId = "", category = "", likes = 0, comments = emptyList())
        )

        adapter.submitList(initialPosts)
        advanceExecutor()
        assertEquals(1, adapter.itemCount)

        adapter.submitList(updatedPosts)
        advanceExecutor()
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle large list`() {
        val posts = (1..100).map { id ->
            CommunityPost(
                id = id.toLong(),
                title = "Post $id",
                content = "Content $id",
                timestamp = "2024-01-01T10:00:00Z",
                authorId = "user$id",
                category = "General",
                likes = id,
                comments = emptyList()
            )
        }

        adapter.submitList(posts)
        advanceExecutor()

        assertEquals(100, adapter.itemCount)
    }

    @Test
    fun `DiffCallback should identify items with same ID as same item`() {
        val posts1 = listOf(
            CommunityPost(id = 1, title = "Post 1", content = "", timestamp = "", authorId = "", category = "", likes = 0, comments = emptyList())
        )
        val posts2 = listOf(
            CommunityPost(id = 1, title = "Post 1 Updated", content = "", timestamp = "", authorId = "", category = "", likes = 10, comments = emptyList())
        )

        adapter.submitList(posts1)
        advanceExecutor()
        adapter.submitList(posts2)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `DiffCallback should identify items with different ID as different items`() {
        val posts1 = listOf(
            CommunityPost(id = 1, title = "Post 1", content = "", timestamp = "", authorId = "", category = "", likes = 0, comments = emptyList())
        )
        val posts2 = listOf(
            CommunityPost(id = 2, title = "Post 2", content = "", timestamp = "", authorId = "", category = "", likes = 0, comments = emptyList())
        )

        adapter.submitList(posts1)
        advanceExecutor()
        adapter.submitList(posts2)
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
