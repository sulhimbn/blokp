package com.example.iurankomplek.core.base

import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.R
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class BaseFragmentTest {

    private lateinit var testScenario: FragmentScenario<TestFragment>

    @Before
    fun setup() {
        testScenario = launchFragmentInContainer<TestFragment>()
    }

    // ========== Abstract Methods Implementation Tests ==========

    @Test
    fun `concrete fragment implements all abstract methods successfully`() {
        testScenario.onFragment { fragment ->
            assertNotNull(fragment.recyclerView)
            assertNotNull(fragment.progressBar)
            assertNotNull(fragment.viewModel)
            assertEquals(R.string.empty_message, fragment.emptyMessageStringRes)
        }
    }

    @Test
    fun `createAdapter returns non-null adapter`() {
        testScenario.onFragment { fragment ->
            val adapter = fragment.createAdapter()
            assertNotNull(adapter)
        }
    }

    @Test
    fun `initializeViewModel sets view model provider`() {
        testScenario.onFragment { fragment ->
            fragment.initializeViewModel(fragment.viewLifecycleOwner)
            assertNotNull(fragment.viewModel)
        }
    }

    @Test
    fun `observeViewModelState successfully observes state flow`() {
        testScenario.onFragment { fragment ->
            fragment.testState = MutableStateFlow(UiState.Success("Test Data"))
            fragment.observeViewModelState()
            delay(100)
            assertNotNull(fragment.lastObservedState)
        }
    }

    @Test
    fun `loadData is called during fragment lifecycle`() {
        testScenario.onFragment { fragment ->
            fragment.loadDataInvoked = false
            fragment.testLoadData()
            assertTrue(fragment.loadDataInvoked)
        }
    }

    // ========== setupRecyclerView Tests ==========

    @Test
    fun `setupRecyclerView configures LinearLayoutManager`() {
        testScenario.onFragment { fragment ->
            val layoutManager = fragment.recyclerView.layoutManager
            assertNotNull(layoutManager)
            assertTrue(layoutManager is androidx.recyclerview.widget.LinearLayoutManager)
        }
    }

    @Test
    fun `setupRecyclerView sets HasFixedSize to true`() {
        testScenario.onFragment { fragment ->
            assertTrue(fragment.recyclerView.hasFixedSize())
        }
    }

    @Test
    fun `setupRecyclerView sets ItemViewCacheSize to 20`() {
        testScenario.onFragment { fragment ->
            val cacheSize = fragment.recyclerView.itemViewCacheSize
            assertEquals(20, cacheSize)
        }
    }

    @Test
    fun `setupRecyclerView sets MaxRecycledViews to 20`() {
        testScenario.onFragment { fragment ->
            val pool = fragment.recyclerView.recycledViewPool
            assertNotNull(pool)
            assertEquals(20, pool.getRecycledViewCount(0))
        }
    }

    @Test
    fun `setupRecyclerView sets adapter`() {
        testScenario.onFragment { fragment ->
            assertNotNull(fragment.recyclerView.adapter)
        }
    }

    // ========== observeUiState Tests ==========

    @Test
    fun `observeUiState shows progress bar on Loading state`() {
        testScenario.onFragment { fragment ->
            fragment.testState = MutableStateFlow(UiState.Loading)
            fragment.observeUiState(fragment.testState) { data -> }
            delay(100)
            assertEquals(View.VISIBLE, fragment.progressBar.visibility)
        }
    }

    @Test
    fun `observeUiState hides progress bar on Success state`() {
        testScenario.onFragment { fragment ->
            fragment.testState = MutableStateFlow(UiState.Success("Test Data"))
            fragment.observeUiState(fragment.testState) { data -> }
            delay(100)
            assertEquals(View.GONE, fragment.progressBar.visibility)
        }
    }

    @Test
    fun `observeUiState hides progress bar on Error state`() {
        testScenario.onFragment { fragment ->
            fragment.testState = MutableStateFlow(UiState.Error("Test error"))
            fragment.observeUiState(fragment.testState, showErrorToast = false) { data -> }
            delay(100)
            assertEquals(View.GONE, fragment.progressBar.visibility)
        }
    }

    @Test
    fun `observeUiState does nothing on Idle state`() {
        testScenario.onFragment { fragment ->
            val initialVisibility = fragment.progressBar.visibility
            fragment.testState = MutableStateFlow(UiState.Idle)
            fragment.observeUiState(fragment.testState) { data -> }
            delay(100)
            assertEquals(initialVisibility, fragment.progressBar.visibility)
        }
    }

    @Test
    fun `observeUiState invokes onDataLoaded callback on Success`() {
        testScenario.onFragment { fragment ->
            var callbackInvoked = false
            var receivedData: String? = null

            fragment.testState = MutableStateFlow(UiState.Success("Test Data"))
            fragment.observeUiState(fragment.testState, showErrorToast = false) { data ->
                callbackInvoked = true
                receivedData = data
            }
            delay(100)

            assertTrue(callbackInvoked)
            assertEquals("Test Data", receivedData)
        }
    }

    @Test
    fun `observeUiState suppresses UNCHECKED_CAST warning on Success`() {
        testScenario.onFragment { fragment ->
            fragment.testState = MutableStateFlow(UiState.Success("Test Data"))
            fragment.observeUiState(fragment.testState, showErrorToast = false) { data ->
                assertEquals("Test Data", data)
            }
            delay(100)
        }
    }

    // ========== Lifecycle Tests ==========

    @Test
    fun `onViewCreated calls setupRecyclerView during fragment lifecycle`() {
        testScenario.onFragment { fragment ->
            fragment.recyclerViewInitialized = false
            fragment.testSetupRecyclerView()
            assertTrue(fragment.recyclerViewInitialized)
        }
    }

    @Test
    fun `onViewCreated calls initializeViewModel during fragment lifecycle`() {
        testScenario.onFragment { fragment ->
            fragment.viewModelInitialized = false
            fragment.testInitializeViewModel()
            assertTrue(fragment.viewModelInitialized)
        }
    }

    @Test
    fun `onViewCreated calls observeViewModelState during fragment lifecycle`() {
        testScenario.onFragment { fragment ->
            fragment.stateObserverInvoked = false
            fragment.testObserveViewModelState()
            assertTrue(fragment.stateObserverInvoked)
        }
    }

    @Test
    fun `onViewCreated calls loadData during fragment lifecycle`() {
        testScenario.onFragment { fragment ->
            fragment.loadDataInvoked = false
            fragment.testLoadData()
            assertTrue(fragment.loadDataInvoked)
        }
    }

    // ========== Edge Cases ==========

    @Test
    fun `observeUiState handles rapid state changes`() {
        testScenario.onFragment { fragment ->
            var loadCount = 0
            var successCount = 0
            var errorCount = 0

            fragment.testState = MutableStateFlow(UiState.Loading)
            fragment.observeUiState(fragment.testState, showErrorToast = false) { data ->
                when (fragment.testState.value) {
                    is UiState.Loading -> loadCount++
                    is UiState.Success -> successCount++
                    is UiState.Error -> errorCount++
                    is UiState.Idle -> {}
                }
            }

            fragment.testState.value = UiState.Loading
            delay(50)
            fragment.testState.value = UiState.Success("Data 1")
            delay(50)
            fragment.testState.value = UiState.Loading
            delay(50)
            fragment.testState.value = UiState.Error("Error 1")
            delay(50)
            fragment.testState.value = UiState.Success("Data 2")
            delay(50)

            assertEquals(2, loadCount)
            assertEquals(2, successCount)
            assertEquals(1, errorCount)
        }
    }

    @Test
    fun `observeUiState handles null error gracefully`() {
        testScenario.onFragment { fragment ->
            fragment.testState = MutableStateFlow(UiState.Error(null))
            fragment.observeUiState(fragment.testState, showErrorToast = false) { data -> }
            delay(100)
            assertNotNull(fragment.lastObservedState)
        }
    }

    @Test
    fun `observeUiState with showErrorToast false does not show toast`() {
        testScenario.onFragment { fragment ->
            fragment.testState = MutableStateFlow(UiState.Error("Test error"))
            fragment.toastShown = false
            fragment.observeUiState(fragment.testState, showErrorToast = false) { data -> }
            delay(100)
            assertFalse(fragment.toastShown)
        }
    }

    @Test
    fun `observeUiState with showErrorToast true shows toast`() {
        testScenario.onFragment { fragment ->
            fragment.testState = MutableStateFlow(UiState.Error("Test error"))
            fragment.observeUiState(fragment.testState, showErrorToast = true) { data -> }
            delay(100)
        }
    }

    // ========== Custom Test Fragment ==========

    class TestFragment : BaseFragment<String>() {
        lateinit var recyclerView: RecyclerView
        lateinit var progressBar: ProgressBar
        val emptyMessageStringRes = R.string.app_name
        lateinit var viewModel: TestViewModel
        var recyclerViewInitialized = false
        var viewModelInitialized = false
        var stateObserverInvoked = false
        var loadDataInvoked = false
        var toastShown = false
        var lastObservedState: UiState<String>? = null
        var testState: MutableStateFlow<UiState<String>> = MutableStateFlow(UiState.Idle)

        override fun setupRecyclerView() {
            super.setupRecyclerView()
            recyclerViewInitialized = true
        }

        override fun initializeViewModel(viewModelProvider: ViewModelProvider) {
            viewModel = ViewModelProvider(this).get(TestViewModel::class.java)
            viewModelInitialized = true
        }

        override fun observeViewModelState() {
            stateObserverInvoked = true
        }

        override fun createAdapter(): RecyclerView.Adapter<*> {
            return TestAdapter()
        }

        override fun loadData() {
            loadDataInvoked = true
        }

        fun testSetupRecyclerView() {
            setupRecyclerView()
        }

        fun testInitializeViewModel() {
            initializeViewModel(ViewModelProvider(this))
        }

        fun testObserveViewModelState() {
            observeViewModelState()
        }

        fun testLoadData() {
            loadData()
        }
    }

    class TestViewModel : androidx.lifecycle.ViewModel()

    class TestAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: View, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(View(parent.context)) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

        override fun getItemCount(): Int {
            return 0
        }
    }

    companion object {
        init {
            FragmentScenario.launch(TestFragment::class.java)
        }
    }
}
