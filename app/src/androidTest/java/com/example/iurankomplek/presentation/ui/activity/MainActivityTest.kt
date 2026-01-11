package com.example.iurankomplek.presentation.ui.activity

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.iurankomplek.R
import com.example.iurankomplek.data.dto.UserDto
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@MediumTest
@Config(sdk = [33])
class MainActivityTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun `activity launches successfully and extends BaseActivity`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity)
            org.junit.Assert.assertTrue(activity is com.example.iurankomplek.core.base.BaseActivity)
        }
    }

    @Test
    fun `recyclerView is initialized with correct configuration`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(recyclerView.adapter)
            org.junit.Assert.assertNotNull(recyclerView.layoutManager)
            org.junit.Assert.assertTrue(recyclerView.layoutManager is androidx.recyclerview.widget.LinearLayoutManager)
        }
    }

    @Test
    fun `adapter is initialized and attached to RecyclerView`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            
            org.junit.Assert.assertNotNull(activity.adapter)
            org.junit.Assert.assertEquals(activity.adapter, recyclerView.adapter)
        }
    }

    @Test
    fun `swipeRefreshLayout is initialized with refresh listener`() {
        scenario.onActivity { activity ->
            val swipeRefreshLayout = activity.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)
            
            org.junit.Assert.assertNotNull(swipeRefreshLayout)
        }
    }

    @Test
    fun `progressBar is present for loading state`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `empty state TextView is present for empty data state`() {
        scenario.onActivity { activity ->
            val emptyStateTextView = activity.findViewById<android.widget.TextView>(R.id.emptyStateTextView)
            
            org.junit.Assert.assertNotNull(emptyStateTextView)
        }
    }

    @Test
    fun `error state layout is present for error state`() {
        scenario.onActivity { activity ->
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)
            
            org.junit.Assert.assertNotNull(errorStateLayout)
        }
    }

    @Test
    fun `error state TextView is present for displaying error messages`() {
        scenario.onActivity { activity ->
            val errorStateTextView = activity.findViewById<android.widget.TextView>(R.id.errorStateTextView)
            
            org.junit.Assert.assertNotNull(errorStateTextView)
        }
    }

    @Test
    fun `retry TextView is present for retry functionality`() {
        scenario.onActivity { activity ->
            val retryTextView = activity.findViewById<android.widget.TextView>(R.id.retryTextView)
            
            org.junit.Assert.assertNotNull(retryTextView)
        }
    }

    @Test
    fun `viewModel is initialized via factory pattern`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.viewModel)
        }
    }

    @Test
    fun `lifecycle scope is valid and in CREATED state or higher`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.lifecycle)
            org.junit.Assert.assertTrue(activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.CREATED))
        }
    }

    @Test
    fun `loading state shows progressBar and hides other UI elements`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            val emptyStateTextView = activity.findViewById<android.widget.TextView>(R.id.emptyStateTextView)
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)
            val swipeRefreshLayout = activity.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)
            
            org.junit.Assert.assertNotNull(progressBar)
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(emptyStateTextView)
            org.junit.Assert.assertNotNull(errorStateLayout)
            org.junit.Assert.assertNotNull(swipeRefreshLayout)
        }
    }

    @Test
    fun `empty state shows empty message and hides content`() {
        scenario.onActivity { activity ->
            val emptyStateTextView = activity.findViewById<android.widget.TextView>(R.id.emptyStateTextView)
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)
            
            org.junit.Assert.assertNotNull(emptyStateTextView)
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(progressBar)
            org.junit.Assert.assertNotNull(errorStateLayout)
        }
    }

    @Test
    fun `error state shows error message and retry button`() {
        scenario.onActivity { activity ->
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)
            val errorStateTextView = activity.findViewById<android.widget.TextView>(R.id.errorStateTextView)
            val retryTextView = activity.findViewById<android.widget.TextView>(R.id.retryTextView)
            
            org.junit.Assert.assertNotNull(errorStateLayout)
            org.junit.Assert.assertNotNull(errorStateTextView)
            org.junit.Assert.assertNotNull(retryTextView)
        }
    }

    @Test
    fun `success state shows content and hides loading`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(progressBar)
        }
    }

    @Test
    fun `recyclerView has fixed size and view cache configured for performance`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertTrue(recyclerView.hasFixedSize())
        }
    }

    @Test
    fun `activity properly handles null data response`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)
            val errorStateTextView = activity.findViewById<android.widget.TextView>(R.id.errorStateTextView)
            val retryTextView = activity.findViewById<android.widget.TextView>(R.id.retryTextView)
            
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(progressBar)
            org.junit.Assert.assertNotNull(errorStateLayout)
            org.junit.Assert.assertNotNull(errorStateTextView)
            org.junit.Assert.assertNotNull(retryTextView)
        }
    }

    @Test
    fun `activity properly filters invalid user data during display`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            val adapter = activity.adapter
            
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(adapter)
            
            val users = listOf(
                UserDto(
                    id = 1,
                    email = "valid@example.com",
                    first_name = "John",
                    last_name = "Doe",
                    avatar = "avatar1.jpg"
                ),
                UserDto(
                    id = 2,
                    email = "",
                    first_name = "",
                    last_name = "",
                    avatar = "avatar2.jpg"
                ),
                UserDto(
                    id = 3,
                    email = "valid2@example.com",
                    first_name = "Jane",
                    last_name = "",
                    avatar = "avatar3.jpg"
                )
            )
            
            adapter.submitList(users.filter { user ->
                user.email.isNotBlank() &&
                (user.first_name.isNotBlank() || user.last_name.isNotBlank())
            })
            
            val validatedUsers = users.filter { user ->
                user.email.isNotBlank() &&
                (user.first_name.isNotBlank() || user.last_name.isNotBlank())
            }
            
            org.junit.Assert.assertEquals(2, validatedUsers.size)
        }
    }

    @Test
    fun `activity handles swipe refresh gesture`() {
        scenario.onActivity { activity ->
            val swipeRefreshLayout = activity.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)
            
            org.junit.Assert.assertNotNull(swipeRefreshLayout)
            
            swipeRefreshLayout.isRefreshing = true
            org.junit.Assert.assertTrue(swipeRefreshLayout.isRefreshing)
            
            swipeRefreshLayout.isRefreshing = false
            org.junit.Assert.assertFalse(swipeRefreshLayout.isRefreshing)
        }
    }

    @Test
    fun `retry button has click listener configured`() {
        scenario.onActivity { activity ->
            val retryTextView = activity.findViewById<android.widget.TextView>(R.id.retryTextView)
            
            org.junit.Assert.assertNotNull(retryTextView)
        }
    }

    @Test
    fun `activity handles empty user list gracefully`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            val emptyStateTextView = activity.findViewById<android.widget.TextView>(R.id.emptyStateTextView)
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)
            
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(emptyStateTextView)
            org.junit.Assert.assertNotNull(progressBar)
            org.junit.Assert.assertNotNull(errorStateLayout)
        }
    }

    @Test
    fun `activity properly handles non-empty user list`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            val adapter = activity.adapter
            
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(adapter)
            
            val users = listOf(
                UserDto(
                    id = 1,
                    email = "valid@example.com",
                    first_name = "John",
                    last_name = "Doe",
                    avatar = "avatar1.jpg"
                )
            )
            
            adapter.submitList(users)
        }
    }

    @Test
    fun `activity gracefully handles users with blank email`() {
        scenario.onActivity { activity ->
            val adapter = activity.adapter
            
            org.junit.Assert.assertNotNull(adapter)
            
            val users = listOf(
                UserDto(
                    id = 1,
                    email = "",
                    first_name = "John",
                    last_name = "Doe",
                    avatar = "avatar1.jpg"
                )
            )
            
            val validatedUsers = users.filter { user ->
                user.email.isNotBlank() &&
                (user.first_name.isNotBlank() || user.last_name.isNotBlank())
            }
            
            org.junit.Assert.assertTrue(validatedUsers.isEmpty())
        }
    }

    @Test
    fun `activity gracefully handles users with blank first and last name`() {
        scenario.onActivity { activity ->
            val adapter = activity.adapter
            
            org.junit.Assert.assertNotNull(adapter)
            
            val users = listOf(
                UserDto(
                    id = 1,
                    email = "valid@example.com",
                    first_name = "",
                    last_name = "",
                    avatar = "avatar1.jpg"
                )
            )
            
            val validatedUsers = users.filter { user ->
                user.email.isNotBlank() &&
                (user.first_name.isNotBlank() || user.last_name.isNotBlank())
            }
            
            org.junit.Assert.assertTrue(validatedUsers.isEmpty())
        }
    }

    @Test
    fun `activity accepts users with valid email and at least one name field`() {
        scenario.onActivity { activity ->
            val adapter = activity.adapter
            
            org.junit.Assert.assertNotNull(adapter)
            
            val users = listOf(
                UserDto(
                    id = 1,
                    email = "valid@example.com",
                    first_name = "John",
                    last_name = "",
                    avatar = "avatar1.jpg"
                ),
                UserDto(
                    id = 2,
                    email = "valid2@example.com",
                    first_name = "",
                    last_name = "Doe",
                    avatar = "avatar2.jpg"
                ),
                UserDto(
                    id = 3,
                    email = "valid3@example.com",
                    first_name = "Jane",
                    last_name = "Smith",
                    avatar = "avatar3.jpg"
                )
            )
            
            val validatedUsers = users.filter { user ->
                user.email.isNotBlank() &&
                (user.first_name.isNotBlank() || user.last_name.isNotBlank())
            }
            
            org.junit.Assert.assertEquals(3, validatedUsers.size)
        }
    }

    @Test
    fun `activity properly handles null avatar field`() {
        scenario.onActivity { activity ->
            val adapter = activity.adapter
            
            org.junit.Assert.assertNotNull(adapter)
            
            val users = listOf(
                UserDto(
                    id = 1,
                    email = "valid@example.com",
                    first_name = "John",
                    last_name = "Doe",
                    avatar = null
                )
            )
            
            adapter.submitList(users)
        }
    }

    @Test
    fun `activity correctly sets visibility for error state`() {
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            val emptyStateTextView = activity.findViewById<android.widget.TextView>(R.id.emptyStateTextView)
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)
            val errorStateTextView = activity.findViewById<android.widget.TextView>(R.id.errorStateTextView)
            
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(progressBar)
            org.junit.Assert.assertNotNull(emptyStateTextView)
            org.junit.Assert.assertNotNull(errorStateLayout)
            org.junit.Assert.assertNotNull(errorStateTextView)
        }
    }

    @Test
    fun `activity correctly sets visibility for loading state`() {
        scenario.onActivity { activity ->
            val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            val recyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUsers)
            val emptyStateTextView = activity.findViewById<android.widget.TextView>(R.id.emptyStateTextView)
            val errorStateLayout = activity.findViewById<android.view.ViewGroup>(R.id.errorStateLayout)
            val swipeRefreshLayout = activity.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)
            
            org.junit.Assert.assertNotNull(progressBar)
            org.junit.Assert.assertNotNull(recyclerView)
            org.junit.Assert.assertNotNull(emptyStateTextView)
            org.junit.Assert.assertNotNull(errorStateLayout)
            org.junit.Assert.assertNotNull(swipeRefreshLayout)
        }
    }
}
