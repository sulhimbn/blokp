package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.model.CommunityPost
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

class CreateCommunityPostUseCaseTest {

    @Mock
    private lateinit var mockRepository: CommunityPostRepository

    private lateinit var useCase: CreateCommunityPostUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = CreateCommunityPostUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val expectedPost = CommunityPost(
            id = "post1",
            authorId = "user1",
            title = "Community Meeting",
            content = "Join us for the monthly community meeting.",
            category = "announcement",
            createdAt = "2024-01-15T10:00:00Z",
            updatedAt = "2024-01-15T10:00:00Z"
        )
        doReturn(expectedPost).`when`(mockRepository).createCommunityPost(
            authorId = "user1",
            title = "Community Meeting",
            content = "Join us for the monthly community meeting.",
            category = "announcement"
        )

        val result = useCase(
            authorId = "user1",
            title = "Community Meeting",
            content = "Join us for the monthly community meeting.",
            category = "announcement"
        )

        assertTrue(result.isSuccess)
        assertEquals(expectedPost, result.getOrNull())
        verify(mockRepository).createCommunityPost(
            authorId = "user1",
            title = "Community Meeting",
            content = "Join us for the monthly community meeting.",
            category = "announcement"
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Network error")
        doThrow(exception).`when`(mockRepository).createCommunityPost(
            authorId = "user1",
            title = "Community Meeting",
            content = "Join us for the monthly community meeting.",
            category = "announcement"
        )

        val result = useCase(
            authorId = "user1",
            title = "Community Meeting",
            content = "Join us for the monthly community meeting.",
            category = "announcement"
        )

        assertTrue(result.isError)
        val error = result.errorOrNull()
        assertNotNull(error)
        assertEquals(exception, error?.exception)
        assertEquals("Network error", error?.message)
        verify(mockRepository).createCommunityPost(
            authorId = "user1",
            title = "Community Meeting",
            content = "Join us for the monthly community meeting.",
            category = "announcement"
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with default message when repository throws exception without message`() = runTest {
        val exception = RuntimeException()
        doThrow(exception).`when`(mockRepository).createCommunityPost(
            authorId = "user1",
            title = "Community Meeting",
            content = "Join us for the monthly community meeting.",
            category = "announcement"
        )

        val result = useCase(
            authorId = "user1",
            title = "Community Meeting",
            content = "Join us for the monthly community meeting.",
            category = "announcement"
        )

        assertTrue(result.isError)
        val error = result.errorOrNull()
        assertNotNull(error)
        assertEquals(exception, error?.exception)
        assertEquals("Failed to create post", error?.message)
        verify(mockRepository).createCommunityPost(
            authorId = "user1",
            title = "Community Meeting",
            content = "Join us for the monthly community meeting.",
            category = "announcement"
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with all fields preserved`() = runTest {
        val expectedPost = CommunityPost(
            id = "post2",
            authorId = "user2",
            title = "Event: Summer Festival",
            content = "Annual summer festival at the community center. Everyone is welcome!",
            category = "event",
            createdAt = "2024-06-01T14:30:00Z",
            updatedAt = "2024-06-01T14:30:00Z",
            likes = 0,
            comments = emptyList()
        )
        doReturn(expectedPost).`when`(mockRepository).createCommunityPost(
            authorId = "user2",
            title = "Event: Summer Festival",
            content = "Annual summer festival at the community center. Everyone is welcome!",
            category = "event"
        )

        val result = useCase(
            authorId = "user2",
            title = "Event: Summer Festival",
            content = "Annual summer festival at the community center. Everyone is welcome!",
            category = "event"
        )

        assertTrue(result.isSuccess)
        val post = result.getOrNull()
        assertNotNull(post)
        assertEquals("post2", post?.id)
        assertEquals("user2", post?.authorId)
        assertEquals("Event: Summer Festival", post?.title)
        assertEquals("Annual summer festival at the community center. Everyone is welcome!", post?.content)
        assertEquals("event", post?.category)
        assertEquals("2024-06-01T14:30:00Z", post?.createdAt)
        assertEquals("2024-06-01T14:30:00Z", post?.updatedAt)
    }

    @Test
    fun `invoke returns success with different categories`() = runTest {
        val categories = listOf("announcement", "event", "discussion", "poll", "other")

        categories.forEach { category ->
            val expectedPost = CommunityPost(
                id = "post_$category",
                authorId = "user1",
                title = "Test Post",
                content = "Test content",
                category = category
            )
            doReturn(expectedPost).`when`(mockRepository).createCommunityPost(
                authorId = "user1",
                title = "Test Post",
                content = "Test content",
                category = category
            )

            val result = useCase(
                authorId = "user1",
                title = "Test Post",
                content = "Test content",
                category = category
            )

            assertTrue(result.isSuccess)
            assertEquals(category, result.getOrNull()?.category)
        }
    }

    @Test
    fun `invoke returns success with empty content`() = runTest {
        val expectedPost = CommunityPost(
            id = "post3",
            authorId = "user3",
            title = "Quick Update",
            content = "",
            category = "announcement"
        )
        doReturn(expectedPost).`when`(mockRepository).createCommunityPost(
            authorId = "user3",
            title = "Quick Update",
            content = "",
            category = "announcement"
        )

        val result = useCase(
            authorId = "user3",
            title = "Quick Update",
            content = "",
            category = "announcement"
        )

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull()?.content)
    }

    @Test
    fun `invoke returns success with long content`() = runTest {
        val longContent = "A".repeat(5000)
        val expectedPost = CommunityPost(
            id = "post4",
            authorId = "user4",
            title = "Long Post",
            content = longContent,
            category = "discussion"
        )
        doReturn(expectedPost).`when`(mockRepository).createCommunityPost(
            authorId = "user4",
            title = "Long Post",
            content = longContent,
            category = "discussion"
        )

        val result = useCase(
            authorId = "user4",
            title = "Long Post",
            content = longContent,
            category = "discussion"
        )

        assertTrue(result.isSuccess)
        assertEquals(longContent, result.getOrNull()?.content)
    }

    @Test
    fun `invoke returns success with special characters in content`() = runTest {
        val specialContent = "Hello! @user #tag\nNew line & symbols <test> \"quotes\""
        val expectedPost = CommunityPost(
            id = "post5",
            authorId = "user5",
            title = "Special Chars",
            content = specialContent,
            category = "discussion"
        )
        doReturn(expectedPost).`when`(mockRepository).createCommunityPost(
            authorId = "user5",
            title = "Special Chars",
            content = specialContent,
            category = "discussion"
        )

        val result = useCase(
            authorId = "user5",
            title = "Special Chars",
            content = specialContent,
            category = "discussion"
        )

        assertTrue(result.isSuccess)
        assertEquals(specialContent, result.getOrNull()?.content)
    }

    @Test
    fun `invoke returns success with unicode content`() = runTest {
        val unicodeContent = "مرحبا! 你好! こんにちは! 안녕하세요! Привет! 🎉🌟🚀"
        val expectedPost = CommunityPost(
            id = "post6",
            authorId = "user6",
            title = "Unicode Test",
            content = unicodeContent,
            category = "discussion"
        )
        doReturn(expectedPost).`when`(mockRepository).createCommunityPost(
            authorId = "user6",
            title = "Unicode Test",
            content = unicodeContent,
            category = "discussion"
        )

        val result = useCase(
            authorId = "user6",
            title = "Unicode Test",
            content = unicodeContent,
            category = "discussion"
        )

        assertTrue(result.isSuccess)
        assertEquals(unicodeContent, result.getOrNull()?.content)
    }

    @Test
    fun `invoke returns success with emoji in title and content`() = runTest {
        val title = "Party Time! 🎉🎊"
        val content = "Let's celebrate! 🥳🎈🎁"
        val expectedPost = CommunityPost(
            id = "post7",
            authorId = "user7",
            title = title,
            content = content,
            category = "event"
        )
        doReturn(expectedPost).`when`(mockRepository).createCommunityPost(
            authorId = "user7",
            title = title,
            content = content,
            category = "event"
        )

        val result = useCase(
            authorId = "user7",
            title = title,
            content = content,
            category = "event"
        )

        assertTrue(result.isSuccess)
        assertEquals(title, result.getOrNull()?.title)
        assertEquals(content, result.getOrNull()?.content)
    }
}
