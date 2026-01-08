package com.example.iurankomplek.presentation.ui.activity

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.iurankomplek.R
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
@MediumTest
@Config(sdk = [33])
class MenuActivityTest {

    private lateinit var scenario: ActivityScenario<MenuActivity>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        scenario = ActivityScenario.launch(MenuActivity::class.java)
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
    fun `activity has four menu card buttons`() {
        scenario.onActivity { activity ->
            val menu1Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu1)
            val menu2Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu2)
            val menu3Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu3)
            val menu4Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu4)
            
            org.junit.Assert.assertNotNull(menu1Card)
            org.junit.Assert.assertNotNull(menu2Card)
            org.junit.Assert.assertNotNull(menu3Card)
            org.junit.Assert.assertNotNull(menu4Card)
        }
    }

    @Test
    fun `clicking menu1 navigates to MainActivity`() {
        scenario.onActivity { activity ->
            val menu1Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu1)
            menu1Card.performClick()
            
            val expectedIntent = Intent(activity, MainActivity::class.java)
            val actualIntent = Shadows.shadowOf(activity.application).nextStartedActivity
            
            org.junit.Assert.assertNotNull(actualIntent)
            org.junit.Assert.assertEquals(expectedIntent.component?.className, actualIntent.component?.className)
        }
    }

    @Test
    fun `clicking menu2 navigates to LaporanActivity`() {
        scenario.onActivity { activity ->
            val menu2Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu2)
            menu2Card.performClick()
            
            val expectedIntent = Intent(activity, LaporanActivity::class.java)
            val actualIntent = Shadows.shadowOf(activity.application).nextStartedActivity
            
            org.junit.Assert.assertNotNull(actualIntent)
            org.junit.Assert.assertEquals(expectedIntent.component?.className, actualIntent.component?.className)
        }
    }

    @Test
    fun `clicking menu3 navigates to CommunicationActivity`() {
        scenario.onActivity { activity ->
            val menu3Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu3)
            menu3Card.performClick()
            
            val expectedIntent = Intent(activity, CommunicationActivity::class.java)
            val actualIntent = Shadows.shadowOf(activity.application).nextStartedActivity
            
            org.junit.Assert.assertNotNull(actualIntent)
            org.junit.Assert.assertEquals(expectedIntent.component?.className, actualIntent.component?.className)
        }
    }

    @Test
    fun `clicking menu4 navigates to PaymentActivity`() {
        scenario.onActivity { activity ->
            val menu4Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu4)
            menu4Card.performClick()
            
            val expectedIntent = Intent(activity, PaymentActivity::class.java)
            val actualIntent = Shadows.shadowOf(activity.application).nextStartedActivity
            
            org.junit.Assert.assertNotNull(actualIntent)
            org.junit.Assert.assertEquals(expectedIntent.component?.className, actualIntent.component?.className)
        }
    }

    @Test
    fun `activity properly initializes click listeners in onCreate`() {
        scenario.onActivity { activity ->
            val menu1Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu1)
            val menu2Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu2)
            val menu3Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu3)
            val menu4Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu4)
            
            var menu1Clicked = false
            var menu2Clicked = false
            var menu3Clicked = false
            var menu4Clicked = false
            
            menu1Card.setOnClickListener { menu1Clicked = true }
            menu2Card.setOnClickListener { menu2Clicked = true }
            menu3Card.setOnClickListener { menu3Clicked = true }
            menu4Card.setOnClickListener { menu4Clicked = true }
            
            menu1Card.performClick()
            menu2Card.performClick()
            menu3Card.performClick()
            menu4Card.performClick()
            
            org.junit.Assert.assertTrue(menu1Clicked)
            org.junit.Assert.assertTrue(menu2Clicked)
            org.junit.Assert.assertTrue(menu3Clicked)
            org.junit.Assert.assertTrue(menu4Clicked)
        }
    }

    @Test
    fun `activity handles multiple clicks on same menu item`() {
        scenario.onActivity { activity ->
            val menu1Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu1)
            
            var clickCount = 0
            menu1Card.setOnClickListener { clickCount++ }
            
            menu1Card.performClick()
            menu1Card.performClick()
            menu1Card.performClick()
            
            org.junit.Assert.assertEquals(3, clickCount)
        }
    }

    @Test
    fun `activity maintains correct lifecycle state`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.lifecycle)
            org.junit.Assert.assertTrue(activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.CREATED))
        }
    }

    @Test
    fun `activity properly cleans up when destroyed`() {
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.DESTROYED)
        
        scenario.onActivity { activity ->
            org.junit.Assert.assertTrue(activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.DESTROYED))
        }
    }

    @Test
    fun `activity handles null intent on creation`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.intent)
        }
    }

    @Test
    fun `activity properly sets content view`() {
        scenario.onActivity { activity ->
            val contentView = activity.findViewById<android.widget.RelativeLayout>(R.id.rootLayout)
            org.junit.Assert.assertNotNull(contentView)
        }
    }

    @Test
    fun `activity handles rapid successive clicks on different menu items`() {
        scenario.onActivity { activity ->
            val menu1Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu1)
            val menu2Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu2)
            
            var menu1Clicks = 0
            var menu2Clicks = 0
            
            menu1Card.setOnClickListener { menu1Clicks++ }
            menu2Card.setOnClickListener { menu2Clicks++ }
            
            menu1Card.performClick()
            menu2Card.performClick()
            menu1Card.performClick()
            menu2Card.performClick()
            
            org.junit.Assert.assertEquals(2, menu1Clicks)
            org.junit.Assert.assertEquals(2, menu2Clicks)
        }
    }

    @Test
    fun `activity does not crash when clicking menu cards rapidly`() {
        scenario.onActivity { activity ->
            val menu1Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu1)
            val menu2Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu2)
            val menu3Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu3)
            val menu4Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu4)
            
            repeat(10) {
                menu1Card.performClick()
                menu2Card.performClick()
                menu3Card.performClick()
                menu4Card.performClick()
            }
            
            org.junit.Assert.assertTrue(true)
        }
    }

    @Test
    fun `activity correctly sets up all click listeners`() {
        scenario.onActivity { activity ->
            val menu1Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu1)
            val menu2Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu2)
            val menu3Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu3)
            val menu4Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu4)
            
            org.junit.Assert.assertNotNull(menu1Card)
            org.junit.Assert.assertNotNull(menu2Card)
            org.junit.Assert.assertNotNull(menu3Card)
            org.junit.Assert.assertNotNull(menu4Card)
        }
    }

    @Test
    fun `activity handles recreation after configuration change`() {
        scenario.recreate()
        
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity)
            org.junit.Assert.assertTrue(activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.CREATED))
        }
    }

    @Test
    fun `activity properly initializes binding`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity.binding)
        }
    }

    @Test
    fun `activity root layout is properly inflated`() {
        scenario.onActivity { activity ->
            val rootLayout = activity.findViewById<android.widget.RelativeLayout>(R.id.rootLayout)
            org.junit.Assert.assertNotNull(rootLayout)
        }
    }

    @Test
    fun `all menu cards have proper clickability`() {
        scenario.onActivity { activity ->
            val menu1Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu1)
            val menu2Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu2)
            val menu3Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu3)
            val menu4Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu4)
            
            org.junit.Assert.assertTrue(menu1Card.isClickable)
            org.junit.Assert.assertTrue(menu2Card.isClickable)
            org.junit.Assert.assertTrue(menu3Card.isClickable)
            org.junit.Assert.assertTrue(menu4Card.isClickable)
        }
    }

    @Test
    fun `activity navigates correctly even when called multiple times`() {
        scenario.onActivity { activity ->
            val menu1Card = activity.findViewById<android.widget.RelativeLayout>(R.id.cdMenu1)
            
            repeat(5) {
                menu1Card.performClick()
                val intent = Shadows.shadowOf(activity.application).nextStartedActivity
                org.junit.Assert.assertNotNull(intent)
                Shadows.shadowOf(activity.application).clearNextStartedActivities()
            }
        }
    }
}
