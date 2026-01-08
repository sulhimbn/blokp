package com.example.iurankomplek.presentation.ui.helper

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RecyclerViewHelperTest {

    private lateinit var recyclerView: RecyclerView
    private lateinit var testAdapter: TestAdapter

    @Before
    fun setup() {
        recyclerView = RecyclerView(ApplicationProvider.getApplicationContext())
        testAdapter = TestAdapter()
    }

    @Test
    fun `configureRecyclerView with phone portrait sets single column layout`() {
        val screenWidthDp = 390 // Phone portrait
        val orientation = Configuration.ORIENTATION_PORTRAIT

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            adapter = testAdapter,
            orientation = orientation,
            screenWidthDp = screenWidthDp
        )

        assertTrue(recyclerView.layoutManager is LinearLayoutManager)
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        assertEquals(LinearLayoutManager.VERTICAL, layoutManager.orientation)
        assertEquals(1, layoutManager.spanCount)
    }

    @Test
    fun `configureRecyclerView with phone landscape sets two column layout`() {
        val screenWidthDp = 390 // Phone portrait width, but in landscape
        val orientation = Configuration.ORIENTATION_LANDSCAPE

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            adapter = testAdapter,
            orientation = orientation,
            screenWidthDp = screenWidthDp
        )

        assertTrue(recyclerView.layoutManager is GridLayoutManager)
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        assertEquals(2, layoutManager.spanCount)
    }

    @Test
    fun `configureRecyclerView with tablet portrait sets two column layout`() {
        val screenWidthDp = 800 // Tablet portrait
        val orientation = Configuration.ORIENTATION_PORTRAIT

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            adapter = testAdapter,
            orientation = orientation,
            screenWidthDp = screenWidthDp
        )

        assertTrue(recyclerView.layoutManager is GridLayoutManager)
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        assertEquals(2, layoutManager.spanCount)
    }

    @Test
    fun `configureRecyclerView with tablet landscape sets three column layout`() {
        val screenWidthDp = 1280 // Tablet landscape
        val orientation = Configuration.ORIENTATION_LANDSCAPE

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            adapter = testAdapter,
            orientation = orientation,
            screenWidthDp = screenWidthDp
        )

        assertTrue(recyclerView.layoutManager is GridLayoutManager)
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        assertEquals(3, layoutManager.spanCount)
    }

    @Test
    fun `configureRecyclerView sets hasFixedSize to true`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        assertTrue(recyclerView.hasFixedSize())
    }

    @Test
    fun `configureRecyclerView sets item view cache size`() {
        val itemCount = 30

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            itemCount = itemCount,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        assertEquals(itemCount, recyclerView.itemViewCacheSize)
    }

    @Test
    fun `configureRecyclerView sets default item cache size of 20`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        assertEquals(20, recyclerView.itemViewCacheSize)
    }

    @Test
    fun `configureRecyclerView sets adapter`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        assertEquals(testAdapter, recyclerView.adapter)
    }

    @Test
    fun `configureRecyclerView with keyboard nav enabled sets focusable properties`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        assertTrue(recyclerView.isFocusable)
        assertTrue(recyclerView.isFocusableInTouchMode)
        assertNotNull(recyclerView.keyListener)
    }

    @Test
    fun `configureRecyclerView with keyboard nav disabled does not set key listener`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = false,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        assertTrue(recyclerView.isFocusable)
        assertFalse(recyclerView.isFocusableInTouchMode)
        assertNull(recyclerView.keyListener)
    }

    @Test
    fun `DPAD_DOWN scrolls down in LinearLayoutManager`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        testAdapter.itemCount = 10
        testAdapter.notifyDataSetChanged()

        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)
        val handled = recyclerView.keyListener?.onKey(recyclerView, downEvent.keyCode, downEvent)

        assertTrue(handled ?: false)
    }

    @Test
    fun `DPAD_UP scrolls up in LinearLayoutManager`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        testAdapter.itemCount = 10
        testAdapter.notifyDataSetChanged()

        val upEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP)
        val handled = recyclerView.keyListener?.onKey(recyclerView, upEvent.keyCode, upEvent)

        assertTrue(handled ?: false)
    }

    @Test
    fun `DPAD_RIGHT in LinearLayoutManager returns false`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        val rightEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)
        val handled = recyclerView.keyListener?.onKey(recyclerView, rightEvent.keyCode, rightEvent)

        assertFalse(handled ?: false)
    }

    @Test
    fun `DPAD_LEFT in LinearLayoutManager returns false`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        val leftEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT)
        val handled = recyclerView.keyListener?.onKey(recyclerView, leftEvent.keyCode, leftEvent)

        assertFalse(handled ?: false)
    }

    @Test
    fun `DPAD_DOWN scrolls by columnCount in GridLayoutManager`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_LANDSCAPE,
            screenWidthDp = 800 // Tablet
        )

        testAdapter.itemCount = 20
        testAdapter.notifyDataSetChanged()

        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)
        val handled = recyclerView.keyListener?.onKey(recyclerView, downEvent.keyCode, downEvent)

        assertTrue(handled ?: false)
    }

    @Test
    fun `DPAD_UP scrolls by columnCount in GridLayoutManager`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_LANDSCAPE,
            screenWidthDp = 800 // Tablet
        )

        testAdapter.itemCount = 20
        testAdapter.notifyDataSetChanged()

        val upEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP)
        val handled = recyclerView.keyListener?.onKey(recyclerView, upEvent.keyCode, upEvent)

        assertTrue(handled ?: false)
    }

    @Test
    fun `DPAD_RIGHT scrolls right in GridLayoutManager`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_LANDSCAPE,
            screenWidthDp = 800 // Tablet
        )

        testAdapter.itemCount = 20
        testAdapter.notifyDataSetChanged()

        val rightEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)
        val handled = recyclerView.keyListener?.onKey(recyclerView, rightEvent.keyCode, rightEvent)

        assertTrue(handled ?: false)
    }

    @Test
    fun `DPAD_LEFT scrolls left in GridLayoutManager`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_LANDSCAPE,
            screenWidthDp = 800 // Tablet
        )

        testAdapter.itemCount = 20
        testAdapter.notifyDataSetChanged()

        val leftEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT)
        val handled = recyclerView.keyListener?.onKey(recyclerView, leftEvent.keyCode, leftEvent)

        assertTrue(handled ?: false)
    }

    @Test
    fun `DPAD_DOWN at end of list returns false`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        testAdapter.itemCount = 5
        testAdapter.notifyDataSetChanged()

        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)
        var handled = recyclerView.keyListener?.onKey(recyclerView, downEvent.keyCode, downEvent)

        assertTrue(handled ?: false)

        downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)
        handled = recyclerView.keyListener?.onKey(recyclerView, downEvent.keyCode, downEvent)

        assertFalse(handled ?: false)
    }

    @Test
    fun `DPAD_UP at top of list returns false`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        testAdapter.itemCount = 5
        testAdapter.notifyDataSetChanged()

        val upEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP)
        val handled = recyclerView.keyListener?.onKey(recyclerView, upEvent.keyCode, upEvent)

        assertFalse(handled ?: false)
    }

    @Test
    fun `ACTION_UP events return false`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        val upActionEvent = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN)
        val handled = recyclerView.keyListener?.onKey(recyclerView, upActionEvent.keyCode, upActionEvent)

        assertFalse(handled ?: false)
    }

    @Test
    fun `non-DPAD key events return false`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            enableKeyboardNav = true,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        val enterEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        val handled = recyclerView.keyListener?.onKey(recyclerView, enterEvent.keyCode, enterEvent)

        assertFalse(handled ?: false)
    }

    @Test
    fun `boundary at exact tablet width uses tablet layout`() {
        val screenWidthDp = 600 // Exact tablet threshold
        val orientation = Configuration.ORIENTATION_PORTRAIT

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            adapter = testAdapter,
            orientation = orientation,
            screenWidthDp = screenWidthDp
        )

        assertTrue(recyclerView.layoutManager is GridLayoutManager)
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        assertEquals(2, layoutManager.spanCount)
    }

    @Test
    fun `boundary below tablet width uses phone layout`() {
        val screenWidthDp = 599 // Just below tablet threshold
        val orientation = Configuration.ORIENTATION_PORTRAIT

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            adapter = testAdapter,
            orientation = orientation,
            screenWidthDp = screenWidthDp
        )

        assertTrue(recyclerView.layoutManager is LinearLayoutManager)
    }

    @Test
    fun `configureRecyclerView handles zero itemCount`() {
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            itemCount = 0,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        assertEquals(0, recyclerView.itemViewCacheSize)
    }

    @Test
    fun `configureRecyclerView handles large itemCount`() {
        val largeItemCount = 100

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            itemCount = largeItemCount,
            adapter = testAdapter,
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 390
        )

        assertEquals(largeItemCount, recyclerView.itemViewCacheSize)
    }

    @Test
    fun `configureRecyclerView with landscape orientation and tablet uses three columns`() {
        val screenWidthDp = 800
        val orientation = Configuration.ORIENTATION_LANDSCAPE

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = recyclerView,
            adapter = testAdapter,
            orientation = orientation,
            screenWidthDp = screenWidthDp
        )

        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        assertEquals(3, layoutManager.spanCount)
    }

    private class TestAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var itemCount = 10

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = android.widget.TextView(parent.context)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        }

        override fun getItemCount(): Int = itemCount
    }
}
