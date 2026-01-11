package com.example.iurankomplek.accessibility

import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.example.iurankomplek.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ListItemAccessibilityDelegateTest {

    private lateinit var mockView: View
    private lateinit var mockEvent: AccessibilityEvent

    @Before
    fun setup() {
        mockView = RuntimeEnvironment.getApplication().applicationContext.getSystemService(View::class.java)
        mockEvent = AccessibilityEvent.obtain()
    }

    // ========== onInitializeAccessibilityNodeInfo Tests ==========

    @Test
    fun `onInitializeAccessibilityNodeInfo sets description with all fields`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "John Doe",
            email = "john@example.com",
            address = "123 Main St",
            iuranPerwarga = "50000",
            totalIuranIndividu = "150000"
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Name: John Doe, Email: john@example.com, Address: 123 Main St, Monthly fee: 50000, Total individual fee: 150000"
        assertEquals(expectedDescription, info.text?.toString())
        assertEquals(expectedDescription, info.contentDescription?.toString())
    }

    @Test
    fun `onInitializeAccessibilityNodeInfo sets description with only name`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Jane Doe",
            email = null,
            address = null,
            iuranPerwarga = null,
            totalIuranIndividu = null
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Name: Jane Doe"
        assertEquals(expectedDescription, info.text?.toString())
        assertEquals(expectedDescription, info.contentDescription?.toString())
    }

    @Test
    fun `onInitializeAccessibilityNodeInfo sets description with name and email`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Bob Smith",
            email = "bob@example.com",
            address = null,
            iuranPerwarga = null,
            totalIuranIndividu = null
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Name: Bob Smith, Email: bob@example.com"
        assertEquals(expectedDescription, info.text?.toString())
        assertEquals(expectedDescription, info.contentDescription?.toString())
    }

    @Test
    fun `onInitializeAccessibilityNodeInfo sets description with all null fields`() {
        val delegate = ListItemAccessibilityDelegate(
            name = null,
            email = null,
            address = null,
            iuranPerwarga = null,
            totalIuranIndividu = null
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        assertNull(info.text?.toString())
        assertNull(info.contentDescription?.toString())
    }

    @Test
    fun `onInitializeAccessibilityNodeInfo sets className`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Test User",
            email = null,
            address = null,
            iuranPerwarga = null,
            totalIuranIndividu = null
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        assertEquals("android.widget.ListView", info.className)
    }

    @Test
    fun `onInitializeAccessibilityNodeInfo handles blank strings`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "   ",
            email = "   ",
            address = "   ",
            iuranPerwarga = "   ",
            totalIuranIndividu = "   "
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Name:    , Email:    , Address:    , Monthly fee:    , Total individual fee:    "
        assertEquals(expectedDescription, info.text?.toString())
    }

    // ========== onPopulateAccessibilityEvent Tests ==========

    @Test
    fun `onPopulateAccessibilityEvent sets event text with all fields`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "John Doe",
            email = "john@example.com",
            address = "123 Main St",
            iuranPerwarga = "50000",
            totalIuranIndividu = "150000"
        )

        delegate.onPopulateAccessibilityEvent(mockView, mockEvent)

        val expectedDescription = "Name: John Doe, Email: john@example.com, Address: 123 Main St, Monthly fee: 50000, Total individual fee: 150000"
        assertFalse(mockEvent.text.isEmpty())
        assertEquals(expectedDescription, mockEvent.text[0]?.toString())
    }

    @Test
    fun `onPopulateAccessibilityEvent sets event text with only name`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Jane Doe",
            email = null,
            address = null,
            iuranPerwarga = null,
            totalIuranIndividu = null
        )

        delegate.onPopulateAccessibilityEvent(mockView, mockEvent)

        val expectedDescription = "Name: Jane Doe"
        assertEquals(expectedDescription, mockEvent.text[0]?.toString())
    }

    @Test
    fun `onPopulateAccessibilityEvent sets event text with all null fields`() {
        val delegate = ListItemAccessibilityDelegate(
            name = null,
            email = null,
            address = null,
            iuranPerwarga = null,
            totalIuranIndividu = null
        )

        delegate.onPopulateAccessibilityEvent(mockView, mockEvent)

        assertTrue(mockEvent.text.isEmpty())
    }

    // ========== buildDescription Edge Cases ==========

    @Test
    fun `buildDescription handles special characters in name`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "José O'Connor-Johnson",
            email = null,
            address = null,
            iuranPerwarga = null,
            totalIuranIndividu = null
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        assertEquals("Name: José O'Connor-Johnson", info.text?.toString())
    }

    @Test
    fun `buildDescription handles unicode characters`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "山田 太郎",
            email = "test@例え.jp",
            address = "東京都渋谷区",
            iuranPerwarga = "50000",
            totalIuranIndividu = "150000"
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Name: 山田 太郎, Email: test@例え.jp, Address: 東京都渋谷区, Monthly fee: 50000, Total individual fee: 150000"
        assertEquals(expectedDescription, info.text?.toString())
    }

    @Test
    fun `buildDescription handles empty iuran values`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Test User",
            email = "test@example.com",
            address = "Test Address",
            iuranPerwarga = "0",
            totalIuranIndividu = "0"
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Name: Test User, Email: test@example.com, Address: Test Address, Monthly fee: 0, Total individual fee: 0"
        assertEquals(expectedDescription, info.text?.toString())
    }

    @Test
    fun `buildDescription handles large numeric values`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Test User",
            email = null,
            address = null,
            iuranPerwarga = "999999999",
            totalIuranIndividu = "999999999"
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Name: Test User, Monthly fee: 999999999, Total individual fee: 999999999"
        assertEquals(expectedDescription, info.text?.toString())
    }

    @Test
    fun `buildDescription formats iuran values correctly with commas`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Test User",
            email = null,
            address = null,
            iuranPerwarga = "1,000,000",
            totalIuranIndividu = "3,000,000"
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Name: Test User, Monthly fee: 1,000,000, Total individual fee: 3,000,000"
        assertEquals(expectedDescription, info.text?.toString())
    }

    // ========== Integration Tests ==========

    @Test
    fun `onInitializeAccessibilityNodeInfo and onPopulateAccessibilityEvent produce same description`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Integration Test",
            email = "integration@example.com",
            address = "123 Test St",
            iuranPerwarga = "50000",
            totalIuranIndividu = "150000"
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)
        val infoDescription = info.text?.toString()

        val event = AccessibilityEvent.obtain()
        delegate.onPopulateAccessibilityEvent(mockView, event)
        val eventDescription = event.text[0]?.toString()

        assertEquals(infoDescription, eventDescription)
    }

    @Test
    fun `multiple calls to onInitializeAccessibilityNodeInfo are consistent`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Consistency Test",
            email = "consistency@example.com",
            address = "456 Consistency Ave",
            iuranPerwarga = "75000",
            totalIuranIndividu = "225000"
        )

        val info1 = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info1)

        val info2 = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info2)

        assertEquals(info1.text?.toString(), info2.text?.toString())
        assertEquals(info1.contentDescription?.toString(), info2.contentDescription?.toString())
    }

    // ========== Field Combination Tests ==========

    @Test
    fun `buildDescription handles name and address only`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Test User",
            email = null,
            address = "Test Address",
            iuranPerwarga = null,
            totalIuranIndividu = null
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Name: Test User, Address: Test Address"
        assertEquals(expectedDescription, info.text?.toString())
    }

    @Test
    fun `buildDescription handles name and iuranPerwarga only`() {
        val delegate = ListItemAccessibilityDelegate(
            name = "Test User",
            email = null,
            address = null,
            iuranPerwarga = "50000",
            totalIuranIndividu = null
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Name: Test User, Monthly fee: 50000"
        assertEquals(expectedDescription, info.text?.toString())
    }

    @Test
    fun `buildDescription handles email and totalIuranIndividu only`() {
        val delegate = ListItemAccessibilityDelegate(
            name = null,
            email = "test@example.com",
            address = null,
            iuranPerwarga = null,
            totalIuranIndividu = "150000"
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Email: test@example.com, Total individual fee: 150000"
        assertEquals(expectedDescription, info.text?.toString())
    }

    @Test
    fun `buildDescription handles all financial fields without name`() {
        val delegate = ListItemAccessibilityDelegate(
            name = null,
            email = "test@example.com",
            address = "Test Address",
            iuranPerwarga = "50000",
            totalIuranIndividu = "150000"
        )

        val info = AccessibilityNodeInfoCompat.obtain()
        delegate.onInitializeAccessibilityNodeInfo(mockView, info)

        val expectedDescription = "Email: test@example.com, Address: Test Address, Monthly fee: 50000, Total individual fee: 150000"
        assertEquals(expectedDescription, info.text?.toString())
    }
}
