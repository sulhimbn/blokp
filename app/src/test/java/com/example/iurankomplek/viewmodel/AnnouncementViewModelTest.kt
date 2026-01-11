package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.AnnouncementRepository
import com.example.iurankomplek.model.Announcement
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
class AnnouncementViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var announcementRepository: AnnouncementRepository

    private lateinit var viewModel: AnnouncementViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AnnouncementViewModel(announcementRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAnnouncements should emit Loading state initially`() = runTest {
        val announcements = listOf(
            Announcement(
                id = 1,
                title = "Maintenance Notice",
                content = "Scheduled maintenance tomorrow",
                timestamp = "2024-01-01T10:00:00Z",
                priority = "high"
            )
        )
        Mockito.`when`(announcementRepository.getAnnouncements(false))
            .thenReturn(Result.success(announcements))

        viewModel.loadAnnouncements()

        val loadingState = viewModel.announcementsState.value
        assertTrue(loadingState is UiState.Loading)
    }

    @Test
    fun `loadAnnouncements should emit Success state when repository returns data`() = runTest {
        val announcements = listOf(
            Announcement(
                id = 1,
                title = "Maintenance Notice",
                content = "Scheduled maintenance tomorrow",
                timestamp = "2024-01-01T10:00:00Z",
                priority = "high"
            ),
            Announcement(
                id = 2,
                title = "Pool Closure",
                content = "Pool closed for cleaning",
                timestamp = "2024-01-02T14:00:00Z",
                priority = "medium"
            )
        )
        Mockito.`when`(announcementRepository.getAnnouncements(false))
            .thenReturn(Result.success(announcements))

        viewModel.loadAnnouncements()
        advanceUntilIdle()

        val state = viewModel.announcementsState.value
        assertTrue(state is UiState.Success)
        assertEquals(announcements, (state as UiState.Success).data)
    }

    @Test
    fun `loadAnnouncements should emit Error state when repository returns error`() = runTest {
        val errorMessage = "Network error occurred"
        Mockito.`when`(announcementRepository.getAnnouncements(false))
            .thenReturn(Result.failure(IOException(errorMessage)))

        viewModel.loadAnnouncements()
        advanceUntilIdle()

        val state = viewModel.announcementsState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).error)
    }

    @Test
    fun `loadAnnouncements should not make duplicate calls when already loading`() = runTest {
        val announcements = listOf(
            Announcement(
                id = 1,
                title = "Test",
                content = "Test content",
                timestamp = "2024-01-01T10:00:00Z",
                priority = "low"
            )
        )
        Mockito.`when`(announcementRepository.getAnnouncements(false))
            .thenReturn(Result.success(announcements))

        viewModel.loadAnnouncements()
        viewModel.loadAnnouncements()

        advanceUntilIdle()
        Mockito.verify(announcementRepository).getAnnouncements(false)
    }

    @Test
    fun `loadAnnouncements should handle empty data correctly`() = runTest {
        Mockito.`when`(announcementRepository.getAnnouncements(false))
            .thenReturn(Result.success(emptyList()))

        viewModel.loadAnnouncements()
        advanceUntilIdle()

        val state = viewModel.announcementsState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.isEmpty())
    }

    @Test
    fun `loadAnnouncements should emit error with unknown message on null exception`() = runTest {
        Mockito.`when`(announcementRepository.getAnnouncements(false))
            .thenReturn(Result.failure(Exception()))

        viewModel.loadAnnouncements()
        advanceUntilIdle()

        val state = viewModel.announcementsState.value
        assertTrue(state is UiState.Error)
        assertEquals("Unknown error occurred", (state as UiState.Error).error)
    }

    @Test
    fun `refreshAnnouncements should call loadAnnouncements with forceRefresh true`() = runTest {
        val announcements = listOf(
            Announcement(
                id = 1,
                title = "Test",
                content = "Test",
                timestamp = "2024-01-01T10:00:00Z",
                priority = "low"
            )
        )
        Mockito.`when`(announcementRepository.getAnnouncements(true))
            .thenReturn(Result.success(announcements))

        viewModel.refreshAnnouncements()
        advanceUntilIdle()

        Mockito.verify(announcementRepository).getAnnouncements(true)
    }

    @Test
    fun `refreshAnnouncements should update state correctly`() = runTest {
        val announcements = listOf(
            Announcement(
                id = 1,
                title = "Updated Announcement",
                content = "Updated content",
                timestamp = "2024-01-02T10:00:00Z",
                priority = "high"
            )
        )
        Mockito.`when`(announcementRepository.getAnnouncements(true))
            .thenReturn(Result.success(announcements))

        viewModel.refreshAnnouncements()
        advanceUntilIdle()

        val state = viewModel.announcementsState.value
        assertTrue(state is UiState.Success)
        assertEquals(announcements, (state as UiState.Success).data)
    }

    @Test
    fun `loadAnnouncements should handle high priority announcements`() = runTest {
        val announcements = listOf(
            Announcement(
                id = 1,
                title = "Emergency Notice",
                content = "Water outage",
                timestamp = "2024-01-01T10:00:00Z",
                priority = "urgent"
            ),
            Announcement(
                id = 2,
                title = "Regular Notice",
                content = "General information",
                timestamp = "2024-01-01T11:00:00Z",
                priority = "low"
            )
        )
        Mockito.`when`(announcementRepository.getAnnouncements(false))
            .thenReturn(Result.success(announcements))

        viewModel.loadAnnouncements()
        advanceUntilIdle()

        val state = viewModel.announcementsState.value
        assertTrue(state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.size)
    }

    @Test
    fun `loadAnnouncements should preserve order of announcements from repository`() = runTest {
        val announcements = listOf(
            Announcement(id = 1, title = "First", content = "", timestamp = "", priority = ""),
            Announcement(id = 2, title = "Second", content = "", timestamp = "", priority = ""),
            Announcement(id = 3, title = "Third", content = "", timestamp = "", priority = "")
        )
        Mockito.`when`(announcementRepository.getAnnouncements(false))
            .thenReturn(Result.success(announcements))

        viewModel.loadAnnouncements()
        advanceUntilIdle()

        val state = viewModel.announcementsState.value
        assertTrue(state is UiState.Success)
        val result = (state as UiState.Success).data
        assertEquals("First", result[0].title)
        assertEquals("Second", result[1].title)
        assertEquals("Third", result[2].title)
    }
}
