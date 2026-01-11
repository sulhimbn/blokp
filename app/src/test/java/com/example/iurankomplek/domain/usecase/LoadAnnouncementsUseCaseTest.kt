package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.AnnouncementRepository
import com.example.iurankomplek.model.Announcement
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

class LoadAnnouncementsUseCaseTest {

    @Mock
    private lateinit var mockRepository: AnnouncementRepository

    private lateinit var useCase: LoadAnnouncementsUseCase

    private val testAnnouncement = Announcement(
        id = "ann1",
        title = "Test Announcement",
        content = "This is a test announcement",
        createdAt = Date()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadAnnouncementsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val expectedAnnouncements = listOf(testAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedAnnouncements, result.getOrNull())
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with empty list`() = runTest {
        val expectedAnnouncements = emptyList<Announcement>()
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedAnnouncements, result.getOrNull())
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with multiple announcements`() = runTest {
        val expectedAnnouncements = listOf(
            testAnnouncement,
            testAnnouncement.copy(id = "ann2", title = "Second Announcement"),
            testAnnouncement.copy(id = "ann3", title = "Third Announcement")
        )
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        val exception = RuntimeException("Network error")
        doReturn(OperationResult.Error(exception, "Network error")).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertEquals(exception, error)
        assertEquals("Network error", error?.message)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws exception`() = runTest {
        val exception = IllegalStateException("Repository not initialized")
        doThrow(exception).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalStateException)
        assertEquals("Repository not initialized", error?.message)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh true calls repository with true`() = runTest {
        val expectedAnnouncements = listOf(testAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(true)

        val result = useCase(forceRefresh = true)

        assertTrue(result.isSuccess)
        assertEquals(expectedAnnouncements, result.getOrNull())
        verify(mockRepository).getAnnouncements(true)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh false calls repository with false`() = runTest {
        val expectedAnnouncements = listOf(testAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase(forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(expectedAnnouncements, result.getOrNull())
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh true handles repository exception`() = runTest {
        val exception = java.io.IOException("Connection timeout")
        doThrow(exception).`when`(mockRepository).getAnnouncements(true)

        val result = useCase(forceRefresh = true)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is java.io.IOException)
        assertEquals("Connection timeout", error?.message)
        verify(mockRepository).getAnnouncements(true)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh false handles repository exception`() = runTest {
        val exception = java.net.SocketTimeoutException("Read timeout")
        doThrow(exception).`when`(mockRepository).getAnnouncements(false)

        val result = useCase(forceRefresh = false)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is java.net.SocketTimeoutException)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns announcements in correct order`() = runTest {
        val date1 = Date(1000)
        val date2 = Date(2000)
        val date3 = Date(3000)
        val expectedAnnouncements = listOf(
            testAnnouncement.copy(id = "ann1", createdAt = date1),
            testAnnouncement.copy(id = "ann2", createdAt = date2),
            testAnnouncement.copy(id = "ann3", createdAt = date3)
        )
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        val announcements = result.getOrNull()
        assertEquals(3, announcements?.size)
        assertEquals("ann1", announcements?.get(0)?.id)
        assertEquals("ann2", announcements?.get(1)?.id)
        assertEquals("ann3", announcements?.get(2)?.id)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with announcements containing all fields`() = runTest {
        val expectedAnnouncements = listOf(testAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        val announcement = result.getOrNull()?.first()
        assertEquals("ann1", announcement?.id)
        assertEquals("Test Announcement", announcement?.title)
        assertEquals("This is a test announcement", announcement?.content)
        assertNotNull(announcement?.createdAt)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles large announcement list efficiently`() = runTest {
        val largeList = (1..100).map { i ->
            testAnnouncement.copy(id = "ann$i", title = "Announcement $i")
        }
        doReturn(OperationResult.Success(largeList)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.size)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with descriptive message`() = runTest {
        val exception = RuntimeException("Failed to fetch announcements: Server error 500")
        doReturn(OperationResult.Error(exception, "Failed to fetch announcements: Server error 500")).`when`(
            mockRepository
        ).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals("Failed to fetch announcements: Server error 500", result.exceptionOrNull()?.message)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles announcements with special characters in title`() = runTest {
        val specialAnnouncement = testAnnouncement.copy(title = "Special! @#$%^&*()")
        val expectedAnnouncements = listOf(specialAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals("Special! @#$%^&*()", result.getOrNull()?.first()?.title)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles announcements with unicode characters`() = runTest {
        val unicodeAnnouncement = testAnnouncement.copy(
            title = "公告",
            content = "这是一条中文公告"
        )
        val expectedAnnouncements = listOf(unicodeAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals("公告", result.getOrNull()?.first()?.title)
        assertEquals("这是一条中文公告", result.getOrNull()?.first()?.content)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles announcements with empty content`() = runTest {
        val emptyContentAnnouncement = testAnnouncement.copy(content = "")
        val expectedAnnouncements = listOf(emptyContentAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull()?.first()?.content)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles announcements with long content`() = runTest {
        val longContent = "a".repeat(10000)
        val longContentAnnouncement = testAnnouncement.copy(content = longContent)
        val expectedAnnouncements = listOf(longContentAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(longContent, result.getOrNull()?.first()?.content)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles announcements with multiline content`() = runTest {
        val multilineContent = "Line 1\nLine 2\nLine 3"
        val multilineAnnouncement = testAnnouncement.copy(content = multilineContent)
        val expectedAnnouncements = listOf(multilineAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(multilineContent, result.getOrNull()?.first()?.content)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success when forceRefresh bypasses cache`() = runTest {
        val expectedAnnouncements = listOf(testAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(true)

        val result = useCase(forceRefresh = true)

        assertTrue(result.isSuccess)
        verify(mockRepository).getAnnouncements(true)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success when forceRefresh false uses cache`() = runTest {
        val expectedAnnouncements = listOf(testAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase(forceRefresh = false)

        assertTrue(result.isSuccess)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles null repository response gracefully`() = runTest {
        val exception = NullPointerException("Repository returned null")
        doReturn(OperationResult.Error(exception, "Repository returned null")).`when`(
            mockRepository
        ).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NullPointerException)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles announcements with HTML content`() = runTest {
        val htmlContent = "<p>This is <strong>bold</strong> text</p>"
        val htmlAnnouncement = testAnnouncement.copy(content = htmlContent)
        val expectedAnnouncements = listOf(htmlAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(htmlContent, result.getOrNull()?.first()?.content)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles announcements with emoji in title and content`() = runTest {
        val emojiAnnouncement = testAnnouncement.copy(
            title = "Important! 📢",
            content = "Please read this announcement! 👀"
        )
        val expectedAnnouncements = listOf(emojiAnnouncement)
        doReturn(OperationResult.Success(expectedAnnouncements)).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals("Important! 📢", result.getOrNull()?.first()?.title)
        assertEquals("Please read this announcement! 👀", result.getOrNull()?.first()?.content)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws timeout exception`() = runTest {
        val exception = java.util.concurrent.TimeoutException("Request timeout")
        doThrow(exception).`when`(mockRepository).getAnnouncements(false)

        val result = useCase()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is java.util.concurrent.TimeoutException)
        assertEquals("Request timeout", error?.message)
        verify(mockRepository).getAnnouncements(false)
        verifyNoMoreInteractions(mockRepository)
    }
}
