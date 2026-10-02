package com.example.iurankomplek

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.google.gson.Gson
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    private lateinit var mockWebServer: MockWebServer

    private val roster = listOf(
        DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john.doe@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 150,
            pengeluaran_iuran_warga = 50,
            pemanfaatan_iuran = "Maintenance",
            avatar = "https://example.com/avatar.jpg"
        ),
        DataItem(
            first_name = "Jane",
            last_name = "Smith",
            email = "jane.smith@example.com",
            alamat = "456 Oak Ave",
            iuran_perwarga = 200,
            total_iuran_rekap = 600,
            jumlah_iuran_bulanan = 300,
            total_iuran_individu = 200,
            pengeluaran_iuran_warga = 75,
            pemanfaatan_iuran = "Repairs",
            avatar = "https://example.com/avatar2.jpg"
        )
    )

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        val gson = Gson()
        val usersBody = gson.toJson(UserResponse(data = roster))
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                if (request.path?.endsWith("/users") == true) {
                    MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setBody(usersBody)
                } else {
                    MockResponse().setResponseCode(404)
                }
        }
        mockWebServer.start(8080)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun mainActivity_shouldDisplayUsers_whenDataIsLoaded() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users))
                .check(matches(isDisplayed()))
                .check(matches(hasChildCount(roster.size)))
        }
    }

    @Test
    fun mainActivity_shouldShowProgressBar_duringLoading() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.progressBar)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun mainActivity_recyclerViewShouldScroll() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(1))
        }
    }

    @Test
    fun mainActivity_shouldHandleNetworkError() {
        mockWebServer.shutdown()
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users)).check(matches(isDisplayed()))
        }
    }
}
