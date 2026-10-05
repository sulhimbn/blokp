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

/**
 * On-device smoke tests for the resident list.
 *
 * The file previously had no `package` declaration, used the removed
 * `MockWebServer.setDispatcher` API, and still built the retired
 * `UserResponse(success, message, data)` envelope, so the androidTest source set had
 * never compiled.
 *
 * These require a connected device or emulator (`connectedAndroidTest`); the app is a
 * Hilt `@AndroidEntryPoint` host, so running them also needs a Hilt test application.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    private lateinit var mockWebServer: MockWebServer

    private fun user(name: String, email: String) = DataItem(
        first_name = name,
        last_name = "Resident",
        email = email,
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                "/users" -> MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(
                        Gson().toJson(
                            UserResponse(
                                data = listOf(
                                    user("John", "john.doe@example.com"),
                                    user("Jane", "jane.smith@example.com")
                                )
                            )
                        )
                    )
                else -> MockResponse().setResponseCode(404)
            }
        }
        mockWebServer.start()
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
                .check(matches(hasChildCount(2)))
        }
    }

    @Test
    fun mainActivity_shouldShowProgressBar_duringLoading() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.progressBar))
                .check(matches(isDisplayed()))
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
    fun mainActivity_shouldSurviveNetworkError() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = MockResponse().setResponseCode(500)
        }
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users))
                .check(matches(isDisplayed()))
        }
    }
}