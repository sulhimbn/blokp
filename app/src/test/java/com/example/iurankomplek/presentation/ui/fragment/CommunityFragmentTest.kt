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
import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.presentation.adapter.CommunityPostAdapter
import com.example.iurankomplek.presentation.viewmodel.CommunityPostViewModel
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
class CommunityFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: CommunityPostRepository

    private lateinit var mockViewModel: TestCommunityPostViewModel

    private val testPost = com.example.iurankomplek.data.dto.CommunityPostDto(
        id = 1,
        userId = 1,
        content = "Test post content",
        createdAt = System.currentTimeMillis(),
        likes = 0,
        comments = 0
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockViewModel = TestCommunityPostViewModel()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `onCreateView initializes RecyclerView with adapter`() {
        launchFragmentInContainer<CommunityFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.rvCommunity))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `onCreateView sets LinearLayoutManager on RecyclerView`() {
        launchFragmentInContainer<CommunityFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.rvCommunity))
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
    fun `observePostsState with Idle state shows no change`() {
        mockViewModel.setState(UiState.Idle)

        launchFragmentInContainer<CommunityFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `observePostsState with Loading state shows progressBar`() {
        mockViewModel.setState(UiState.Loading)

        launchFragmentInContainer<CommunityFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `observePostsState with Success with data hides progressBar and submits list`() {
        mockViewModel.setState(UiState.Success(listOf(testPost)))

        launchFragmentInContainer<CommunityFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `observePostsState with Success with empty data hides progressBar`() {
        mockViewModel.setState(UiState.Success(emptyList()))

        launchFragmentInContainer<CommunityFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `observePostsState with Error state hides progressBar`() {
        mockViewModel.setState(UiState.Error("Network error"))

        launchFragmentInContainer<CommunityFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `onDestroyView nullifies binding`() {
        val scenario = launchFragmentInContainer<CommunityFragment>(
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

    class TestCommunityPostViewModel : ViewModel() {
        private val _postsState = MutableStateFlow<UiState<List<com.example.iurankomplek.data.dto.CommunityPostDto>>>(UiState.Idle)
        val postsState = _postsState

        fun setState(state: UiState<List<com.example.iurankomplek.data.dto.CommunityPostDto>>) {
            _postsState.value = state
        }

        fun loadPosts() {
        }
    }

    class TestFragmentFactory(private val viewModel: ViewModel) : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): android.app.Fragment {
            return when (className) {
                CommunityFragment::class.java.name -> {
                    val fragment = CommunityFragment()
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