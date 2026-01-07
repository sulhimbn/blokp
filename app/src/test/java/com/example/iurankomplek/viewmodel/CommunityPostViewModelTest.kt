package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.model.CommunityPost
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
class CommunityPostViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var communityPostRepository: CommunityPostRepository

    private lateinit var viewModel: CommunityPostViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CommunityPostViewModel(communityPostRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPosts should emit Loading state initially`() = runTest {
        val posts = listOf(
            CommunityPost(
                id = 1,
                title = "Community Event",
                content = "Join us for the annual gathering",
                timestamp = "2024-01-01T10:00:00Z",
                authorId = "user1",
                category = "Events",
                likes = 5,
                comments = emptyList()
            )
        )
        Mockito.`when`(communityPostRepository.getPosts())
            .thenReturn(Result.success(posts))

        viewModel.loadPosts()

        val loadingState = viewModel.postsState.value
        assertTrue(loadingState is UiState.Loading)
    }

    @Test
    fun `loadPosts should emit Success state when repository returns data`() = runTest {
        val posts = listOf(
            CommunityPost(
                id = 1,
                title = "First Post",
                content = "Content of first post",
                timestamp = "2024-01-01T10:00:00Z",
                authorId = "user1",
                category = "General",
                likes = 10,
                comments = emptyList()
            ),
            CommunityPost(
                id = 2,
                title = "Second Post",
                content = "Content of second post",
                timestamp = "2024-01-01T11:00:00Z",
                authorId = "user2",
                category = "News",
                likes = 20,
                comments = emptyList()
            )
        )
        Mockito.`when`(communityPostRepository.getPosts())
            .thenReturn(Result.success(posts))

        viewModel.loadPosts()
        advanceUntilIdle()

        val state = viewModel.postsState.value
        assertTrue(state is UiState.Success)
        assertEquals(posts, (state as UiState.Success).data)
    }

    @Test
    fun `loadPosts should emit Error state when repository returns error`() = runTest {
        val errorMessage = "Network error occurred"
        Mockito.`when`(communityPostRepository.getPosts())
            .thenReturn(Result.failure(IOException(errorMessage)))

        viewModel.loadPosts()
        advanceUntilIdle()

        val state = viewModel.postsState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).error)
    }

    @Test
    fun `loadPosts should not make duplicate calls when already loading`() = runTest {
        val posts = listOf(
            CommunityPost(
                id = 1,
                title = "Test",
                content = "Test content",
                timestamp = "2024-01-01T10:00:00Z",
                authorId = "user1",
                category = "Test",
                likes = 0,
                comments = emptyList()
            )
        )
        Mockito.`when`(communityPostRepository.getPosts())
            .thenReturn(Result.success(posts))

        viewModel.loadPosts()
        viewModel.loadPosts()

        advanceUntilIdle()
        Mockito.verify(communityPostRepository).getPosts()
    }

    @Test
    fun `loadPosts should handle empty data correctly`() = runTest {
        Mockito.`when`(communityPostRepository.getPosts())
            .thenReturn(Result.success(emptyList()))

        viewModel.loadPosts()
        advanceUntilIdle()

        val state = viewModel.postsState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.isEmpty())
    }

    @Test
    fun `loadPosts should emit error with unknown message on null exception`() = runTest {
        Mockito.`when`(communityPostRepository.getPosts())
            .thenReturn(Result.failure(Exception()))

        viewModel.loadPosts()
        advanceUntilIdle()

        val state = viewModel.postsState.value
        assertTrue(state is UiState.Error)
        assertEquals("Unknown error occurred", (state as UiState.Error).error)
    }

    @Test
    fun `loadPosts should handle posts with many likes`() = runTest {
        val posts = listOf(
            CommunityPost(
                id = 1,
                title = "Popular Post",
                content = "This post is popular",
                timestamp = "2024-01-01T10:00:00Z",
                authorId = "user1",
                category = "Trending",
                likes = 1000,
                comments = emptyList()
            )
        )
        Mockito.`when`(communityPostRepository.getPosts())
            .thenReturn(Result.success(posts))

        viewModel.loadPosts()
        advanceUntilIdle()

        val state = viewModel.postsState.value
        assertTrue(state is UiState.Success)
        assertEquals(1000, (state as UiState.Success).data[0].likes)
    }

    @Test
    fun `loadPosts should handle posts with zero likes`() = runTest {
        val posts = listOf(
            CommunityPost(
                id = 1,
                title = "New Post",
                content = "No likes yet",
                timestamp = "2024-01-01T10:00:00Z",
                authorId = "user1",
                category = "General",
                likes = 0,
                comments = emptyList()
            )
        )
        Mockito.`when`(communityPostRepository.getPosts())
            .thenReturn(Result.success(posts))

        viewModel.loadPosts()
        advanceUntilIdle()

        val state = viewModel.postsState.value
        assertTrue(state is UiState.Success)
        assertEquals(0, (state as UiState.Success).data[0].likes)
    }

    @Test
    fun `loadPosts should preserve post categories`() = runTest {
        val posts = listOf(
            CommunityPost(
                id = 1,
                title = "Event",
                content = "",
                timestamp = "",
                authorId = "user1",
                category = "Events",
                likes = 5,
                comments = emptyList()
            ),
            CommunityPost(
                id = 2,
                title = "News",
                content = "",
                timestamp = "",
                authorId = "user2",
                category = "News",
                likes = 10,
                comments = emptyList()
            )
        )
        Mockito.`when`(communityPostRepository.getPosts())
            .thenReturn(Result.success(posts))

        viewModel.loadPosts()
        advanceUntilIdle()

        val state = viewModel.postsState.value
        assertTrue(state is UiState.Success)
        val result = (state as UiState.Success).data
        assertEquals("Events", result[0].category)
        assertEquals("News", result[1].category)
    }
}
