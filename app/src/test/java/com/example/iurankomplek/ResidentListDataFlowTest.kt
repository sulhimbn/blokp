package com.example.iurankomplek

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.CacheManager
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.UserViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ResidentListDataFlowTest {

    private lateinit var server: MockWebServer
    private lateinit var viewModel: UserViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val gson = Gson()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        CacheManager.getInstance().clearSync()
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        val repository = UserRepositoryImpl(
            api,
            Mockito.mock(UserSessionManager::class.java)
        )
        viewModel = UserViewModel(repository, EventBus())
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun enqueue(body: Any) = server.enqueue(
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(gson.toJson(body))
    )

    private fun enqueueError(code: Int, body: String, times: Int = 1) = repeat(times) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    private fun resident(name: String, email: String) = DataItem(
        first_name = name,
        last_name = "Doe",
        email = email,
        alamat = "Jl. Contoh No. 1",
        iuran_perwarga = 150_000,
        total_iuran_rekap = 1_800_000,
        jumlah_iuran_bulanan = 150_000,
        total_iuran_individu = 150_000,
        pengeluaran_iuran_warga = 50_000,
        pemanfaatan_iuran = "Perbaikan jalan komplek",
        avatar = "https://example.com/avatar.jpg"
    )

    @Test
    fun `resident payload fetched over HTTP reaches the UI state as success`() =
        runTest(testDispatcher) {
            enqueue(
                UserResponse(
                    data = listOf(
                        resident("John", "john@example.com"),
                        resident("Jane", "jane@example.com")
                    )
                )
            )

            viewModel.loadUsers()

            val state = viewModel.usersState.first { it !is UiState.Loading }
            assertTrue(state is UiState.Success)
            assertEquals(2, (state as UiState.Success).data.data.size)
        }

    @Test
    fun `successful payload renders one row per resident with their dues`() =
        runTest(testDispatcher) {
            enqueue(
                UserResponse(
                    data = listOf(
                        resident("John", "john@example.com"),
                        resident("Jane", "jane@example.com")
                    )
                )
            )

            viewModel.loadUsers()
            val state = viewModel.usersState.first { it !is UiState.Loading } as UiState.Success

            val adapter = UserAdapter(state.data.data.toMutableList(), this)
            val parent: ViewGroup = FrameLayout(RuntimeEnvironment.getApplication())
            assertEquals(2, adapter.itemCount)

            val holder = adapter.onCreateViewHolder(parent, 0)
            adapter.onBindViewHolder(holder as UserAdapter.ListViewHolder, 1)
            val rendered = holder.itemView.findViewById<TextView>(R.id.itemName).text.toString()
            val email = holder.itemView.findViewById<TextView>(R.id.itemEmail).text.toString()

            assertEquals("Jane Doe", rendered)
            assertEquals("jane@example.com", email)
        }

    private fun awaitTerminalState(timeoutMs: Long = 20_000): UiState<UserResponse> {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = viewModel.usersState.value
            if (state !is UiState.Loading) return state
            Thread.sleep(20)
        }
        return viewModel.usersState.value
    }

    @Test
    fun `server error surfaces as an error state carrying the mapped message`() = runBlocking {
        Dispatchers.setMain(testDispatcher)
        CacheManager.getInstance().clearSync()
        enqueueError(500, "boom", times = 4)

        viewModel.loadUsers()

        val state = awaitTerminalState()
        assertTrue("expected an error state but got $state", state is UiState.Error)
    }

    @Test
    fun `the residents endpoint is the one the repository calls`() = runTest(testDispatcher) {
        enqueue(UserResponse(data = emptyList()))

        viewModel.loadUsers()
        viewModel.usersState.first { it !is UiState.Loading }

        assertEquals("/users", server.takeRequest().path)
    }

    @Test
    fun `a repeat load is served from cache without a second HTTP call`() =
        runTest(testDispatcher) {
            enqueue(UserResponse(data = listOf(resident("John", "john@example.com"))))

            viewModel.loadUsers()
            viewModel.usersState.first { it !is UiState.Loading }

            val direct = UserRepositoryImpl(
                Retrofit.Builder()
                    .baseUrl(server.url("/"))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java),
                Mockito.mock(UserSessionManager::class.java)
            )
            direct.getUsers()

            assertEquals(1, server.requestCount)
        }

    @Test
    fun `an empty payload yields a success state with no rows`() = runTest(testDispatcher) {
        enqueue(UserResponse(data = emptyList()))

        viewModel.loadUsers()

        val state = viewModel.usersState.first { it !is UiState.Loading }
        assertTrue(state is UiState.Success)
        assertEquals(0, (state as UiState.Success).data.data.size)
    }

    @Test
    fun `a row bound for a resident with a missing name renders the unknown user fallback`() =
        runTest(testDispatcher) {
            enqueue(
                UserResponse(
                    data = listOf(
                        DataItem(
                            first_name = null,
                            last_name = null,
                            email = "anon@example.com",
                            iuran_perwarga = 10_000,
                            total_iuran_individu = 20_000
                        )
                    )
                )
            )

            viewModel.loadUsers()
            val state = viewModel.usersState.first { it !is UiState.Loading } as UiState.Success

            val adapter = UserAdapter(state.data.data.toMutableList(), this)
            val holder = adapter.onCreateViewHolder(
                FrameLayout(RuntimeEnvironment.getApplication()),
                0
            )
            adapter.onBindViewHolder(holder as UserAdapter.ListViewHolder, 0)

            assertEquals(
                "Unknown User",
                holder.itemView.findViewById<TextView>(R.id.itemName).text.toString()
            )
        }

    @Test
    fun `recycler view receives the rows through its adapter`() {
        val adapter = UserAdapter(mutableListOf(), CoroutineScope(Dispatchers.Unconfined))
        val recyclerView = RecyclerView(RuntimeEnvironment.getApplication())
        recyclerView.adapter = adapter

        adapter.addUser(resident("John", "john@example.com"))

        assertEquals(1, adapter.itemCount)
        assertEquals(1, recyclerView.adapter!!.itemCount)
    }
}