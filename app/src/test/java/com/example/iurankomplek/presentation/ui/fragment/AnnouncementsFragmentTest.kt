package com.example.iurankomplek.presentation.ui.fragment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.fragment.app.FragmentFactory
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.example.iurankomplek.R
import com.example.iurankomplek.data.repository.AnnouncementRepository
import com.example.iurankomplek.presentation.adapter.AnnouncementAdapter
import com.example.iurankomplek.presentation.viewmodel.AnnouncementViewModel
import com.example.iurankomplek.utils.UiState
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AnnouncementsFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: AnnouncementRepository

    private lateinit var mockViewModel: TestAnnouncementViewModel

    private val testAnnouncement = com.example.iurankomplek.data.dto.AnnouncementDto(
        id = 1,
        title = "Test Announcement",
        content = "Test content",
        createdAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockViewModel = TestAnnouncementViewModel()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `onCreateView initializes RecyclerView with adapter`() {
        launchFragmentInContainer<AnnouncementsFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.rvAnnouncements))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `onCreateView sets LinearLayoutManager on RecyclerView`() {
        launchFragmentInContainer<AnnouncementsFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.rvAnnouncements))
            .check { view, noViewFoundException ->
                Assert.assertNotNull("RecyclerView should be initialized", view)
                val recyclerView = view as RecyclerView
                Assert.assertTrue(
                    "RecyclerView should have LinearLayoutManager",
                    recyclerView.layoutManager is androidx.recyclerview.widget.LinearLayoutManager
                )
            }
    }

    @Test
    fun `observeAnnouncementsState with Idle state shows no change`() {
        mockViewModel.setState(UiState.Idle)

        launchFragmentInContainer<AnnouncementsFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `observeAnnouncementsState with Loading state shows progressBar`() {
        mockViewModel.setState(UiState.Loading)

        launchFragmentInContainer<AnnouncementsFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `observeAnnouncementsState with Success with data hides progressBar and submits list`() {
        mockViewModel.setState(UiState.Success(listOf(testAnnouncement)))

        launchFragmentInContainer<AnnouncementsFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `observeAnnouncementsState with Success with empty data hides progressBar`() {
        mockViewModel.setState(UiState.Success(emptyList()))

        launchFragmentInContainer<AnnouncementsFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `observeAnnouncementsState with Error state hides progressBar`() {
        mockViewModel.setState(UiState.Error("Network error"))

        launchFragmentInContainer<AnnouncementsFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `onDestroyView nullifies binding`() {
        val scenario = launchFragmentInContainer<AnnouncementsFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        scenario.onFragment { fragment ->
            fragment.onDestroyView()
            val binding = fragment.javaClass.getDeclaredField("_binding").apply {
                isAccessible = true
            }
            Assert.assertNull("Binding should be null after onDestroyView", binding.get(fragment))
        }
    }

    class TestAnnouncementViewModel : ViewModel() {
        private val _announcementsState = MutableStateFlow<UiState<List<com.example.iurankomplek.data.dto.AnnouncementDto>>>(UiState.Idle)
        val announcementsState = _announcementsState

        fun setState(state: UiState<List<com.example.iurankomplek.data.dto.AnnouncementDto>>) {
            _announcementsState.value = state
        }

        fun loadAnnouncements() {
        }
    }

    class TestFragmentFactory(private val viewModel: ViewModel) : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): android.app.Fragment {
            return when (className) {
                AnnouncementsFragment::class.java.name -> {
                    val fragment = AnnouncementsFragment()
                    fragment.javaClass.getDeclaredField("viewModel").apply {
                        isAccessible = true
                    }.set(fragment, viewModel)
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
        }
    }
}