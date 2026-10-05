package com.example.iurankomplek

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val gson = Gson()
    private var dispatcher: Dispatcher = residentsDispatcher()

    private val mockWebServer = MockWebServer().apply {
        this.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                dispatcher.dispatch(request)
        }
        start()
    }

    @BindValue
    @JvmField
    val boundApiService: BoundApiService = BoundApiService(
        Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    )

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun mainActivity_rendersOneRowPerResident_fromTheApi() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users))
                .check(matches(isDisplayed()))
                .check(matches(hasChildCount(3)))
        }
    }

    @Test
    fun mainActivity_rendersResidentNameAndEmail() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        click()
                    )
                )
            onView(withText("John Doe")).check(matches(isDisplayed()))
            onView(withText("john.doe@example.com")).check(matches(isDisplayed()))
        }
    }

    @Test
    fun mainActivity_hidesTheProgressIndicator_onceDataArrives() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun mainActivity_queriesTheResidentsEndpoint() {
        ActivityScenario.launch(MainActivity::class.java).use {
            assertEquals("/users", mockWebServer.takeRequest().path)
        }
    }

    @Test
    fun mainActivity_reloadIsRequestedOnPullToRefresh() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.swipeRefreshLayout)).perform(swipeDown())
            assertEquals("/users", mockWebServer.takeRequest().path)
            assertEquals("/users", mockWebServer.takeRequest().path)
        }
    }

    @Test
    fun mainActivity_survivesAnEmptyResidentList() {
        dispatcher = jsonDispatcher(UserResponse(data = emptyList()))

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users)).check(matches(hasChildCount(0)))
        }
    }

    @Test
    fun mainActivity_survivesAServerError() {
        dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                MockResponse().setResponseCode(500).setBody("boom")
        }

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users)).check(matches(hasChildCount(0)))
        }
    }

    @Test
    fun mainActivity_rendersAFallbackName_whenTheResidentHasNoName() {
        dispatcher = jsonDispatcher(
            UserResponse(
                data = listOf(DataItem(email = "anon@example.com", iuran_perwarga = 10_000))
            )
        )

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users)).check(matches(hasChildCount(1)))
        }
    }

    @Test
    fun mainActivity_listScrollsAcrossEveryResident() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users))
                .perform(
                    RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(2)
                )
        }
    }

    private fun residentsDispatcher(): Dispatcher =
        jsonDispatcher(UserResponse(data = residents()))

    private fun jsonDispatcher(body: Any): Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(gson.toJson(body))
    }

    private fun residents() = listOf(
        resident("John", "Doe", "john.doe@example.com", "123 Main St", 150_000),
        resident("Jane", "Smith", "jane.smith@example.com", "456 Oak Ave", 200_000),
        resident("Budi", "Santoso", "budi.santoso@example.com", "789 Pine Rd", 175_000)
    )

    private fun resident(
        firstName: String,
        lastName: String,
        email: String,
        address: String,
        dues: Int
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = address,
        iuran_perwarga = dues,
        total_iuran_rekap = dues * 12,
        jumlah_iuran_bulanan = dues,
        total_iuran_individu = dues,
        pengeluaran_iuran_warga = dues / 3,
        pemanfaatan_iuran = "Perbaikan jalan",
        avatar = "https://example.com/avatar.jpg"
    )
}