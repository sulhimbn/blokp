package com.example.iurankomplek.presentation.ui.fragment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.example.iurankomplek.R
import com.example.iurankomplek.data.repository.MessageRepository
import com.example.iurankomplek.presentation.adapter.MessageAdapter
import com.example.iurankomplek.presentation.viewmodel.MessageViewModel
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
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class MessagesFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: MessageRepository

    private lateinit var mockViewModel: TestMessageViewModel

    private val testMessage = com.example.iurankomplek.data.dto.MessageDto(
        id = 1,
        senderId = 1,
        receiverId = 2,
        message = "Test message",
        timestamp = System.currentTimeMillis(),
        isRead = false
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockViewModel = TestMessageViewModel()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `onCreateView initializes RecyclerView with adapter`() {
        launchFragmentInContainer<MessagesFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.rvMessages))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `onCreateView sets LinearLayoutManager on RecyclerView`() {
        launchFragmentInContainer<MessagesFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.rvMessages))
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
    fun `observeMessagesState with Idle state shows no change`() {
        mockViewModel.setState(UiState.Idle)

        launchFragmentInContainer<MessagesFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `observeMessagesState with Loading state shows progressBar`() {
        mockViewModel.setState(UiState.Loading)

        launchFragmentInContainer<MessagesFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `observeMessagesState with Success with data hides progressBar and submits list`() {
        mockViewModel.setState(UiState.Success(listOf(testMessage)))

        launchFragmentInContainer<MessagesFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `observeMessagesState with Success with empty data hides progressBar`() {
        mockViewModel.setState(UiState.Success(emptyList()))

        launchFragmentInContainer<MessagesFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `observeMessagesState with Error state hides progressBar`() {
        mockViewModel.setState(UiState.Error("Network error"))

        launchFragmentInContainer<MessagesFragment>(
            factory = TestFragmentFactory(mockViewModel)
        )

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun `onDestroyView nullifies binding`() {
        val scenario = launchFragmentInContainer<MessagesFragment>(
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

    class TestMessageViewModel : ViewModel() {
        private val _messagesState = MutableStateFlow<UiState<List<com.example.iurankomplek.data.dto.MessageDto>>>(UiState.Idle)
        val messagesState = _messagesState

        fun setState(state: UiState<List<com.example.iurankomplek.data.dto.MessageDto>>) {
            _messagesState.value = state
        }

        fun loadMessages(userId: Long) {
        }
    }

    class TestFragmentFactory(private val viewModel: ViewModel) : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): android.app.Fragment {
            return when (className) {
                MessagesFragment::class.java.name -> {
                    val fragment = MessagesFragment()
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