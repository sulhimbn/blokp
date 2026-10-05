package com.example.iurankomplek

import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.di.UserRepositoryModule
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.presentation.adapter.UserAdapter
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import java.io.IOException

/**
 * Test double for the user repository. The result it returns is configurable so the
 * activity can be driven through every UiState branch.
 */
class FakeUserRepository : UserRepository {

    @Volatile
    var result: Result<UserResponse> = Result.success(UserResponse(emptyList()))

    @Volatile
    var callCount: Int = 0

    override suspend fun getUsers(): Result<UserResponse> {
        callCount++
        return result
    }

    override suspend fun login(email: String, password: String): Result<User> =
        Result.failure(IOException("login is out of scope for this test"))

    override suspend fun logout(): Result<Unit> = Result.success(Unit)

    override fun getCurrentUserFlow() = MutableStateFlow<User?>(null)
}

@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
@HiltAndroidTest
@UninstallModules(UserRepositoryModule::class)
class MainActivityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val repository: UserRepository = FakeUserRepository()

    private val fakeRepository: FakeUserRepository
        get() = repository as FakeUserRepository

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun `the user list screen wires up its views and adapter`() {
        val activity = startActivity()

        assertNotNull(activity.findViewById<RecyclerView>(R.id.rv_users))
        assertNotNull(activity.findViewById<ProgressBar>(R.id.progressBar))
        assertTrue(activity.findViewById<RecyclerView>(R.id.rv_users).adapter is UserAdapter)
        assertTrue(!activity.isFinishing)
    }

    @Test
    fun `creating the screen loads users`() {
        fakeRepository.result = Result.success(UserResponse(listOf(TestFixtures.dataItem())))

        startActivity()
        drain()

        assertEquals(1, fakeRepository.callCount)
    }

    @Test
    fun `a successful load hides the progress indicator`() {
        fakeRepository.result = Result.success(UserResponse(listOf(TestFixtures.dataItem())))

        val activity = startActivity()
        drain()

        assertEquals(View.GONE, activity.findViewById<ProgressBar>(R.id.progressBar).visibility)
    }

    @Test
    fun `an empty successful load finishes without an error`() {
        fakeRepository.result = Result.success(UserResponse(emptyList()))

        val activity = startActivity()
        drain()

        assertEquals(1, fakeRepository.callCount)
        assertEquals(View.GONE, activity.findViewById<ProgressBar>(R.id.progressBar).visibility)
    }

    @Test
    fun `a failed load hides the progress indicator and reports the error`() {
        fakeRepository.result = Result.failure(IOException("Network error occurred"))

        val activity = startActivity()
        drain()

        assertEquals(View.GONE, activity.findViewById<ProgressBar>(R.id.progressBar).visibility)
        assertEquals("Network error occurred", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `rows without an email are filtered out of the list`() {
        fakeRepository.result = Result.success(
            UserResponse(
                listOf(
                    TestFixtures.dataItem(),
                    TestFixtures.dataItem(firstName = "NoMail", lastName = "User", email = "")
                )
            )
        )

        val activity = startActivity()
        drain()

        val adapter = activity.findViewById<RecyclerView>(R.id.rv_users).adapter as UserAdapter
        assertEquals(1, adapter.itemCount)
    }

    private fun startActivity(): MainActivity =
        Robolectric.buildActivity(MainActivity::class.java).setup().get()

    private fun drain() {
        shadowOf(Looper.getMainLooper()).idle()
    }
}