package com.example.iurankomplek

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.presentation.adapter.AnnouncementAdapter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import androidx.recyclerview.widget.RecyclerView

@RunWith(RobolectricTestRunner::class)
class AnnouncementAdapterTest {

    private lateinit var adapter: AnnouncementAdapter

    @Before
    fun setup() {
        adapter = AnnouncementAdapter()
    }

    @Test
    fun `AnnouncementAdapter should have correct initial item count`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should set items correctly`() {
        val announcements = createMockAnnouncements(count = 3)

        adapter.submitList(announcements)

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should create ViewHolder correctly`() {
        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)

        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(viewHolder)
        assertNotNull(viewHolder.itemView)
    }

    @Test
    fun `AnnouncementAdapter should bind ViewHolder correctly`() {
        val announcement = createMockAnnouncement(
            id = "ann_1",
            title = "Important Announcement",
            content = "This is the content",
            category = "General",
            priority = "high",
            createdAt = "2024-01-07T10:00:00Z",
            readBy = emptyList()
        )

        adapter.submitList(listOf(announcement))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.bind(announcement)

        assertEquals("Important Announcement", viewHolder.binding.announcementTitle.text.toString())
        assertEquals("This is the content", viewHolder.binding.announcementContent.text.toString())
        assertEquals("General", viewHolder.binding.announcementCategory.text.toString())
        assertEquals("2024-01-07T10:00:00Z", viewHolder.binding.announcementCreatedAt.text.toString())
    }

    @Test
    fun `AnnouncementAdapter should handle empty list`() {
        adapter.submitList(emptyList())

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should handle single item`() {
        val announcement = createMockAnnouncement()

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should handle large number of items`() {
        val announcements = createMockAnnouncements(count = 100)

        adapter.submitList(announcements)

        assertEquals(100, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should handle items with empty strings`() {
        val announcement = createMockAnnouncement(
            title = "",
            content = "",
            category = "",
            createdAt = ""
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.bind(announcement)

        assertEquals("", viewHolder.binding.announcementTitle.text.toString())
        assertEquals("", viewHolder.binding.announcementContent.text.toString())
        assertEquals("", viewHolder.binding.announcementCategory.text.toString())
        assertEquals("", viewHolder.binding.announcementCreatedAt.text.toString())
    }

    @Test
    fun `AnnouncementAdapter should handle items with special characters`() {
        val announcement = createMockAnnouncement(
            title = "Important: Meeting Tomorrow @ 10am!",
            content = "Don't forget to bring your laptop & charger. Also, check email.",
            category = "Events (Monthly)",
            createdAt = "2024-01-07T10:00:00Z"
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should handle items with unicode characters`() {
        val announcement = createMockAnnouncement(
            title = "📢 Maintenance Scheduled",
            content = "System maintenance will occur on 🗓️ 2024-01-10",
            category = "Maintenance 🛠️",
            createdAt = "2024-01-07T10:00:00Z"
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.bind(announcement)

        assertEquals("📢 Maintenance Scheduled", viewHolder.binding.announcementTitle.text.toString())
        assertTrue(viewHolder.binding.announcementContent.text.toString().contains("🗓️"))
    }

    @Test
    fun `AnnouncementAdapter should handle items with very long strings`() {
        val longTitle = "A".repeat(200)
        val longContent = "B".repeat(1000)
        val announcement = createMockAnnouncement(
            title = longTitle,
            content = longContent
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.bind(announcement)

        assertEquals(longTitle, viewHolder.binding.announcementTitle.text.toString())
        assertEquals(longContent, viewHolder.binding.announcementContent.text.toString())
    }

    @Test
    fun `AnnouncementAdapter should handle different priorities`() {
        val priorities = listOf("low", "medium", "high", "urgent", "critical")

        val announcements = priorities.mapIndexed { index, priority ->
            createMockAnnouncement(
                id = "ann_$index",
                priority = priority
            )
        }

        adapter.submitList(announcements)

        assertEquals(priorities.size, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)

        for (i in announcements.indices) {
            val viewHolder = adapter.onCreateViewHolder(parent, 0)
            viewHolder.bind(announcements[i])
        }
    }

    @Test
    fun `AnnouncementAdapter should handle different categories`() {
        val categories = listOf(
            "General",
            "Maintenance",
            "Events",
            "Important",
            "Urgent",
            "Information"
        )

        val announcements = categories.mapIndexed { index, category ->
            createMockAnnouncement(
                id = "ann_$index",
                category = category
            )
        }

        adapter.submitList(announcements)

        assertEquals(categories.size, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)

        for (i in announcements.indices) {
            val viewHolder = adapter.onCreateViewHolder(parent, 0)
            viewHolder.bind(announcements[i])
            assertEquals(categories[i], viewHolder.binding.announcementCategory.text.toString())
        }
    }

    @Test
    fun `AnnouncementAdapter should handle readBy list correctly`() {
        val announcement = createMockAnnouncement(
            readBy = listOf("user1", "user2", "user3")
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should handle empty readBy list`() {
        val announcement = createMockAnnouncement(
            readBy = emptyList()
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should handle large readBy list`() {
        val largeReadBy = (1..100).map { "user$it" }
        val announcement = createMockAnnouncement(
            readBy = largeReadBy
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter DiffCallback should identify same items by id`() {
        val oldAnnouncements = listOf(
            createMockAnnouncement(id = "ann_123", title = "Old Title")
        )
        val newAnnouncements = listOf(
            createMockAnnouncement(id = "ann_123", title = "New Title") // Same id, different title
        )

        adapter.submitList(oldAnnouncements)
        assertEquals(1, adapter.itemCount)

        adapter.submitList(newAnnouncements)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should update items incrementally`() {
        val initialAnnouncements = createMockAnnouncements(count = 1)

        adapter.submitList(initialAnnouncements)
        assertEquals(1, adapter.itemCount)

        val updatedAnnouncements = createMockAnnouncements(count = 5)

        adapter.submitList(updatedAnnouncements)
        assertEquals(5, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should handle items with HTML-like content`() {
        val announcement = createMockAnnouncement(
            content = "<p>This is a paragraph</p><strong>Important</strong>"
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.bind(announcement)

        assertEquals("<p>This is a paragraph</p><strong>Important</strong>", viewHolder.binding.announcementContent.text.toString())
    }

    @Test
    fun `AnnouncementAdapter should handle items with multiline content`() {
        val multilineContent = """
            Line 1
            Line 2
            Line 3
        """.trimIndent()

        val announcement = createMockAnnouncement(
            content = multilineContent
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.bind(announcement)

        assertEquals(multilineContent, viewHolder.binding.announcementContent.text.toString())
    }

    @Test
    fun `AnnouncementAdapter should handle items with newlines in title`() {
        val announcement = createMockAnnouncement(
            title = "Line 1\nLine 2\nLine 3"
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.bind(announcement)

        assertEquals("Line 1\nLine 2\nLine 3", viewHolder.binding.announcementTitle.text.toString())
    }

    @Test
    fun `AnnouncementAdapter ViewHolder should have correct view references`() {
        val announcement = createMockAnnouncement()

        adapter.submitList(listOf(announcement))

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.bind(announcement)

        assertNotNull(viewHolder.binding.announcementTitle)
        assertNotNull(viewHolder.binding.announcementContent)
        assertNotNull(viewHolder.binding.announcementCategory)
        assertNotNull(viewHolder.binding.announcementCreatedAt)
    }

    @Test
    fun `AnnouncementAdapter should handle items with timestamps`() {
        val announcement = createMockAnnouncement(
            createdAt = "2024-01-07T10:00:00.000Z"
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.bind(announcement)

        assertEquals("2024-01-07T10:00:00.000Z", viewHolder.binding.announcementCreatedAt.text.toString())
    }

    @Test
    fun `AnnouncementAdapter should handle items with different date formats`() {
        val dateFormats = listOf(
            "2024-01-07T10:00:00Z",
            "07/01/2024 10:00 AM",
            "January 7, 2024",
            "2024-01-07",
            "20240107"
        )

        val announcements = dateFormats.mapIndexed { index, date ->
            createMockAnnouncement(
                id = "ann_$index",
                createdAt = date
            )
        }

        adapter.submitList(announcements)

        assertEquals(dateFormats.size, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = RecyclerView(context)

        for (i in announcements.indices) {
            val viewHolder = adapter.onCreateViewHolder(parent, 0)
            viewHolder.bind(announcements[i])
            assertEquals(dateFormats[i], viewHolder.binding.announcementCreatedAt.text.toString())
        }
    }

    @Test
    fun `AnnouncementAdapter should handle items with null-like values in readBy`() {
        val announcement = createMockAnnouncement(
            readBy = listOf("", "  ", "\t")
        )

        adapter.submitList(listOf(announcement))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `AnnouncementAdapter should replace list correctly`() {
        val initialAnnouncements = createMockAnnouncements(count = 3)
        adapter.submitList(initialAnnouncements)
        assertEquals(3, adapter.itemCount)

        val newAnnouncements = createMockAnnouncements(
            count = 2,
            idPrefix = "new_"
        )
        adapter.submitList(newAnnouncements)
        assertEquals(2, adapter.itemCount)
    }

    private fun createMockAnnouncements(
        count: Int,
        idPrefix: String = ""
    ): List<Announcement> {
        return (1..count).map { index ->
            createMockAnnouncement(
                id = "${idPrefix}ann_$index",
                title = "Announcement $index",
                content = "Content for announcement $index"
            )
        }
    }

    private fun createMockAnnouncement(
        id: String = "ann_1",
        title: String = "Sample Announcement",
        content: String = "This is the content of the announcement",
        category: String = "General",
        priority: String = "medium",
        createdAt: String = "2024-01-07T10:00:00Z",
        readBy: List<String> = emptyList()
    ): Announcement {
        return Announcement(
            id = id,
            title = title,
            content = content,
            category = category,
            priority = priority,
            createdAt = createdAt,
            readBy = readBy
        )
    }
}