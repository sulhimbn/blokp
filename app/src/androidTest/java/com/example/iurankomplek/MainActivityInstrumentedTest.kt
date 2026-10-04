package com.example.iurankomplek

import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.presentation.adapter.UserAdapter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @Test
    fun createsSuccessfully() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull("Activity should be created", activity)
                assertTrue("Activity should not be finishing", !activity.isFinishing)
            }
        }
    }

    @Test
    fun initializesUserList() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(
                    "RecyclerView should be initialized",
                    activity.findViewById<RecyclerView>(R.id.rv_users)
                )
            }
        }
    }

    @Test
    fun initializesProgressIndicator() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(
                    "ProgressBar should be initialized",
                    activity.findViewById<ProgressBar>(R.id.progressBar)
                )
            }
        }
    }

    @Test
    fun attachesUserAdapter() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
                assertNotNull("Adapter should be set", recyclerView.adapter)
                assertTrue("Adapter should be UserAdapter", recyclerView.adapter is UserAdapter)
            }
        }
    }
}
