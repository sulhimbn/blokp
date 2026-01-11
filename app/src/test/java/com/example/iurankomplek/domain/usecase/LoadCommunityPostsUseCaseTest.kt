package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.model.Comment
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

class LoadCommunityPostsUseCaseTest {

    @Mock
    private lateinit var mockRepository: CommunityPostRepository

    private lateinit var useCase: LoadCommunityPostsUseCase

    private val testComment = Comment(
        id = "comment1",
        authorId = "user2",
        content = "Great post!",
        timestamp = "2024-01-15T11:00:00Z"
    )

    private val testPost = CommunityPost(
        id = "post1",
        authorId = "user1",
        title = "Community Meeting Tomorrow",
        content = "Don't forget about the community meeting tomorrow at 7 PM",
        category = "announcements",
        likes = 5,
        comments = listOf(testComment),
        createdAt = "2024-01-15T10:00:00Z"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadCommunityPostsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val expectedPosts = listOf(testPost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedPosts, result.getOrNull())
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with empty list`() = runTest {
        val expectedPosts = emptyList<CommunityPost>()
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedPosts, result.getOrNull())
        assertEquals(0, result.getOrNull()?.size)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with multiple posts`() = runTest {
        val post2 = testPost.copy(
            id = "post2",
            title = "Lost Keys",
            content = "Has anyone seen my keys?",
            category = "lost_found"
        )
        val post3 = testPost.copy(
            id = "post3",
            title = "Event This Weekend",
            content = "Join us for the event!",
            category = "events"
        )
        val expectedPosts = listOf(testPost, post2, post3)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh true calls repository`() = runTest {
        val expectedPosts = listOf(testPost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(true)

        val result = useCase(forceRefresh = true)

        assertTrue(result.isSuccess)
        assertEquals(expectedPosts, result.getOrNull())
        verify(mockRepository).getCommunityPosts(true)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh false calls repository`() = runTest {
        val expectedPosts = listOf(testPost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase(forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(expectedPosts, result.getOrNull())
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Network error", result.errorMessage)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Connection timeout")
        doThrow(exception).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Connection timeout", result.errorMessage)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with default message when exception has no message`() = runTest {
        val exception = RuntimeException()
        doThrow(exception).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()

        assertTrue(result.isError)
        assertEquals("Failed to load posts", result.errorMessage)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke preserves all post fields`() = runTest {
        val expectedPosts = listOf(testPost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val post = result.getOrNull()?.first()

        assertEquals("post1", post?.id)
        assertEquals("user1", post?.authorId)
        assertEquals("Community Meeting Tomorrow", post?.title)
        assertEquals("Don't forget about the community meeting tomorrow at 7 PM", post?.content)
        assertEquals("announcements", post?.category)
        assertEquals(5, post?.likes)
        assertEquals(1, post?.comments?.size)
        assertEquals("2024-01-15T10:00:00Z", post?.createdAt)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with different categories`() = runTest {
        val announcementPost = testPost.copy(id = "p1", category = "announcements")
        val lostFoundPost = testPost.copy(id = "p2", category = "lost_found")
        val eventsPost = testPost.copy(id = "p3", category = "events")
        val discussionPost = testPost.copy(id = "p4", category = "discussion")
        val expectedPosts = listOf(announcementPost, lostFoundPost, eventsPost, discussionPost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val posts = result.getOrNull()

        assertEquals(4, posts?.size)
        assertEquals("announcements", posts?.get(0)?.category)
        assertEquals("lost_found", posts?.get(1)?.category)
        assertEquals("events", posts?.get(2)?.category)
        assertEquals("discussion", posts?.get(3)?.category)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with multiple comments`() = runTest {
        val comment1 = testComment.copy(id = "c1", content = "First comment")
        val comment2 = testComment.copy(id = "c2", content = "Second comment")
        val comment3 = testComment.copy(id = "c3", content = "Third comment")
        val postWithComments = testPost.copy(
            id = "post2",
            comments = listOf(comment1, comment2, comment3)
        )
        val expectedPosts = listOf(testPost, postWithComments)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val posts = result.getOrNull()

        assertEquals(2, posts?.size)
        assertEquals(1, posts?.get(0)?.comments?.size)
        assertEquals(3, posts?.get(1)?.comments?.size)
        assertEquals("First comment", posts?.get(1)?.comments?.get(0)?.content)
        assertEquals("Second comment", posts?.get(1)?.comments?.get(1)?.content)
        assertEquals("Third comment", posts?.get(1)?.comments?.get(2)?.content)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with different like counts`() = runTest {
        val post0Likes = testPost.copy(id = "p1", likes = 0)
        val post5Likes = testPost.copy(id = "p2", likes = 5)
        val post10Likes = testPost.copy(id = "p3", likes = 10)
        val post100Likes = testPost.copy(id = "p4", likes = 100)
        val expectedPosts = listOf(post0Likes, post5Likes, post10Likes, post100Likes)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val posts = result.getOrNull()

        assertEquals(4, posts?.size)
        assertEquals(0, posts?.get(0)?.likes)
        assertEquals(5, posts?.get(1)?.likes)
        assertEquals(10, posts?.get(2)?.likes)
        assertEquals(100, posts?.get(3)?.likes)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with empty comments`() = runTest {
        val postNoComments = testPost.copy(
            id = "post2",
            comments = emptyList()
        )
        val expectedPosts = listOf(testPost, postNoComments)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val posts = result.getOrNull()

        assertEquals(2, posts?.size)
        assertEquals(1, posts?.get(0)?.comments?.size)
        assertEquals(0, posts?.get(1)?.comments?.size)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with long content`() = runTest {
        val longContent = "This is a very long post content that goes on and on and on and on " +
                "and contains a lot of information about various community topics and events " +
                "that are happening in the neighborhood and surrounding areas."
        val longContentPost = testPost.copy(
            id = "post2",
            content = longContent
        )
        val expectedPosts = listOf(testPost, longContentPost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val posts = result.getOrNull()

        assertEquals(2, posts?.size)
        assertTrue(posts?.get(1)?.content?.length ?: 0 > 100)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with special characters in content`() = runTest {
        val specialContentPost = testPost.copy(
            id = "post2",
            content = "Post with émojis 😊 and spëcial çharacters!",
            title = "Tëst Tïtle with spëcial çharacters"
        )
        val expectedPosts = listOf(testPost, specialContentPost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val posts = result.getOrNull()

        assertEquals(2, posts?.size)
        assertEquals("Post with émojis 😊 and spëcial çharacters!", posts?.get(1)?.content)
        assertEquals("Tëst Tïtle with spëcial çharacters", posts?.get(1)?.title)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with unicode content`() = runTest {
        val unicodePost = testPost.copy(
            id = "post2",
            content = "Post with Arabic العربية Chinese 中文 Japanese 日本語",
            title = "Unicode 测试 العربية"
        )
        val expectedPosts = listOf(testPost, unicodePost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val posts = result.getOrNull()

        assertEquals(2, posts?.size)
        assertEquals("Post with Arabic العربية Chinese 中文 Japanese 日本語", posts?.get(1)?.content)
        assertEquals("Unicode 测试 العربية", posts?.get(1)?.title)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with multiline content`() = runTest {
        val multilineContentPost = testPost.copy(
            id = "post2",
            content = "Line 1\nLine 2\nLine 3\n\nThis is a paragraph.\n\nAnother paragraph."
        )
        val expectedPosts = listOf(testPost, multilineContentPost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val posts = result.getOrNull()

        assertEquals(2, posts?.size)
        assertTrue(posts?.get(1)?.content?.contains("\n") == true)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with HTML content`() = runTest {
        val htmlContentPost = testPost.copy(
            id = "post2",
            content = "<p>This is <strong>bold</strong> and <em>italic</em> text.</p>"
        )
        val expectedPosts = listOf(testPost, htmlContentPost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val posts = result.getOrNull()

        assertEquals(2, posts?.size)
        assertEquals("<p>This is <strong>bold</strong> and <em>italic</em> text.</p>", posts?.get(1)?.content)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with URL in content`() = runTest {
        val urlPost = testPost.copy(
            id = "post2",
            content = "Check out this link: https://example.com/more-info"
        )
        val expectedPosts = listOf(testPost, urlPost)
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()
        val posts = result.getOrNull()

        assertEquals(2, posts?.size)
        assertTrue(posts?.get(1)?.content?.contains("https://example.com/more-info") == true)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles large post list efficiently`() = runTest {
        val largePostList = (1..100).map { i ->
            testPost.copy(
                id = "post$i",
                title = "Post $i",
                likes = i % 10
            )
        }
        val expectedPosts = largePostList
        doReturn(OperationResult.Success(expectedPosts)).`when`(mockRepository).getCommunityPosts(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.size)
        verify(mockRepository).getCommunityPosts(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles posts with forceRefresh and exception`() = runTest {
        val exception = RuntimeException("Cache refresh failed")
        doThrow(exception).`when`(mockRepository).getCommunityPosts(true)

        val result = useCase(forceRefresh = true)

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Cache refresh failed", result.errorMessage)
        verify(mockRepository).getCommunityPosts(true)
        verifyNoMoreInteractions(mockRepository)
    }
}
