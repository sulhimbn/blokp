package com.example.iurankomplek

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.CacheManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * End-to-end functional test of the resident list: real HTTP over MockWebServer, real
 * Retrofit/Gson deserialisation, the real [UserRepositoryImpl], the real [UserAdapter],
 * real ViewBinding and real inflated [TextView]s.
 *
 * It replaces the previous MainActivityTest, which could never work: MainActivity obtains
 * its ViewModel through `by viewModels()` with Hilt's factory, so a Mockito `@Mock`
 * ViewModel is never injected, and the test additionally reached into a private
 * `viewModel` field that does not exist (the delegate compiles to `viewModel$delegate`).
 * Exercising the Activity itself needs `hilt-android-testing` plus a HiltTestApplication,
 * which this project does not have; this test covers the same data path instead.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ResidentListFunctionalTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService
    private lateinit var repository: UserRepositoryImpl
    private lateinit var items: MutableList<DataItem>
    private lateinit var adapter: UserAdapter
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        CacheManager.getInstance().clearSync()

        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        repository = UserRepositoryImpl(api, Mockito.mock(UserSessionManager::class.java))

        items = mutableListOf()
        adapter = UserAdapter(items, kotlinx.coroutines.CoroutineScope(testDispatcher))
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun enqueueUsers(vararg users: DataItem) {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(UserResponse(data = users.toList())))
        )
    }

    @Test
    fun `api payload flows all the way to the rendered row text`() = runTest {
        enqueueUsers(
            TestFixtures.dataItem(
                firstName = "Budi", lastName = "Santoso",
                email = "budi@blokp.test", alamat = "Blok A No 12",
                iuranPerwarga = 150000
            ),
            TestFixtures.dataItem(
                firstName = "Siti", lastName = "Nurhaliza",
                email = "siti@blokp.test", alamat = "Blok B No 7",
                iuranPerwarga = 90000
            )
        )

        val result = repository.getUsers()
        assertTrue(result.isSuccess)
        adapter.setUsers(result.getOrNull()!!.data)

        assertEquals(2, adapter.itemCount)
        assertEquals("/users", server.takeRequest().path)

        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val rv = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        rv.adapter = adapter

        val rows = (0 until 2).map { position ->
            val holder = rv.adapter!!.createViewHolder(rv, 0)
            rv.adapter!!.bindViewHolder(holder, position)
            holder.itemView
        }

        val first = rows[0].findViewById<TextView>(R.id.itemName).text.toString()
        val firstEmail = rows[0].findViewById<TextView>(R.id.itemEmail).text.toString()
        val firstAddress = rows[0].findViewById<TextView>(R.id.itemAddress).text.toString()

        assertTrue("expected the API's first name in the row, got: $first", first.contains("Budi"))
        assertTrue("expected the API's email in the row, got: $firstEmail", firstEmail.contains("budi@blokp.test"))
        assertTrue("expected the API's address in the row, got: $firstAddress", firstAddress.contains("Blok A No 12"))

        val second = rows[1].findViewById<TextView>(R.id.itemName).text.toString()
        assertTrue("expected the API's second name in the row, got: $second", second.contains("Siti"))
    }

    @Test
    fun `a rejected request leaves the list empty instead of rendering junk`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(404).setBody("Not Found")
        )

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `an unknown name renders the fallback label rather than an empty row`() = runTest {
        enqueueUsers(TestFixtures.dataItem(firstName = "", lastName = ""))

        val result = repository.getUsers()
        adapter.setUsers(result.getOrNull()!!.data)

        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val rv = RecyclerView(context).apply { layoutManager = LinearLayoutManager(context) }
        rv.adapter = adapter
        val holder = rv.adapter!!.createViewHolder(rv, 0)
        rv.adapter!!.bindViewHolder(holder, 0)

        val name = holder.itemView.findViewById<TextView>(R.id.itemName).text.toString()
        val email = holder.itemView.findViewById<TextView>(R.id.itemEmail).text.toString()

        assertTrue("expected a non-empty fallback name, got: '$name'", name.isNotBlank())
        assertTrue("expected a non-empty fallback email, got: '$email'", email.isNotBlank())
    }
}