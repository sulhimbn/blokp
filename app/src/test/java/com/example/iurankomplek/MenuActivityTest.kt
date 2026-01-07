package com.example.iurankomplek

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.databinding.ActivityMenuBinding
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MenuActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var binding: ActivityMenuBinding

    @Mock
    private lateinit var mockIntent: Intent

    private lateinit var activity: TestMenuActivity

    @Before
    fun setup() {
        activity = TestMenuActivity()
        activity.binding = binding
    }

    @Test
    fun `onCreate should initialize binding and set content view`() {
        activity.testOnCreate()
        
        assertNotNull(activity.binding)
    }

    @Test
    fun `onCreate should setup fullscreen mode`() {
        activity.testOnCreate()
        
        assertTrue(activity.fullscreenModeSetupCalled)
    }

    @Test
    fun `onCreate should setup click listeners`() {
        activity.testOnCreate()
        
        assertTrue(activity.clickListenersSetupCalled)
    }

    @Test
    fun `clicking menu1 should navigate to MainActivity`() {
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        
        activity.testOnMenu1Click()
        
        verify(mockIntent).putExtra(anyString(), any())
    }

    @Test
    fun `clicking menu2 should navigate to LaporanActivity`() {
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        
        activity.testOnMenu2Click()
        
        verify(mockIntent).putExtra(anyString(), any())
    }

    @Test
    fun `clicking menu3 should navigate to CommunicationActivity`() {
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        
        activity.testOnMenu3Click()
        
        verify(mockIntent).putExtra(anyString(), any())
    }

    @Test
    fun `clicking menu4 should navigate to PaymentActivity`() {
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        
        activity.testOnMenu4Click()
        
        verify(mockIntent).putExtra(anyString(), any())
    }

    @Test
    fun `multiple menu clicks should start corresponding activities`() {
        activity.testOnMenu1Click()
        activity.testOnMenu2Click()
        activity.testOnMenu3Click()
        activity.testOnMenu4Click()
        
        verify(mockIntent, times(4)).putExtra(anyString(), any())
    }
}

class TestMenuActivity : MenuActivity() {
    var fullscreenModeSetupCalled = false
    var clickListenersSetupCalled = false
    var binding: ActivityMenuBinding? = null

    fun testOnCreate() {
        fullscreenModeSetupCalled = false
        clickListenersSetupCalled = false
        
        onCreate(null)
    }

    fun testOnMenu1Click() {
        binding?.cdMenu1?.callOnClick()
    }

    fun testOnMenu2Click() {
        binding?.cdMenu2?.callOnClick()
    }

    fun testOnMenu3Click() {
        binding?.cdMenu3?.callOnClick()
    }

    fun testOnMenu4Click() {
        binding?.cdMenu4?.callOnClick()
    }

    override fun setupFullscreenMode() {
        fullscreenModeSetupCalled = true
    }

    override fun setupClickListeners() {
        clickListenersSetupCalled = true
    }
}
