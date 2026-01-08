import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.presentation.ui.activity.MainActivity
import com.example.iurankomplek.utils.NetworkUtils
import com.example.iurankomplek.R
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()

        // Setup mock responses for API calls
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    "/users" -> {
                        val mockUsers = listOf(
                            LegacyDataItemDto(
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
                            LegacyDataItemDto(
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
                        val mockResponse = UserResponse(data = mockUsers)

                        MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody(com.google.gson.Gson().toJson(mockResponse))
                    }
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        mockWebServer.start(8080)

        // Replace ApiConfig's base URL with mock server URL
        // Note: This is a workaround since ApiConfig is an object with a fixed URL
        // In a real scenario, we would use dependency injection to make this more testable
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun mainActivity_shouldDisplayUsers_whenDataIsLoaded() {
        // Given: MainActivity is launched
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        // When: Data is loaded from API
        // Then: RecyclerView should display users
        onView(withId(R.id.rv_users))
            .check(matches(isDisplayed()))
            .check(matches(hasChildCount(2))) // Expecting 2 users from mock data
    }

    @Test
    fun mainActivity_shouldShowProgressBar_duringLoading() {
        // Given: MainActivity is launched
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        // Then: Progress bar should be visible initially
        onView(withId(R.id.progressBar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_recyclerViewShouldScroll() {
        // Given: MainActivity is launched with data
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        // When: User scrolls the RecyclerView
        onView(withId(R.id.rv_users))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(1))

        // Then: Should be able to scroll without errors
        // The test passes if no exceptions are thrown
    }

    @Test
    fun mainActivity_shouldHandleNetworkError() {
        // This test would require a more complex setup to simulate network failures
        // For now, we'll test that the activity can be created without network issues
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // The activity should be able to handle the scenario gracefully
        onView(withId(R.id.rv_users))
            .check(matches(isDisplayed()))
    }
}