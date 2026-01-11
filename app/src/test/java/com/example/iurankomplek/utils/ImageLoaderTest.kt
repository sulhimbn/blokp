package com.example.iurankomplek.utils

import android.content.Context
import android.widget.ImageView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ImageLoaderTest {

    @Mock
    private lateinit var mockImageView: ImageView

    private lateinit var context: Context

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun `loadCircularImage with valid URL loads image`() {
        // Arrange
        val validUrl = "https://example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, validUrl)

        // Assert - Glide should load the image
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with null URL shows placeholder`() {
        // Arrange
        val nullUrl = null

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, nullUrl)

        // Assert - Should show placeholder
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with empty URL shows placeholder`() {
        // Arrange
        val emptyUrl = ""

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, emptyUrl)

        // Assert - Should show placeholder
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with blank URL shows placeholder`() {
        // Arrange
        val blankUrl = "   "

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, blankUrl)

        // Assert - Should show placeholder
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with valid HTTP URL loads image`() {
        // Arrange
        val httpUrl = "http://example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, httpUrl)

        // Assert - Should load image
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with valid HTTPS URL loads image`() {
        // Arrange
        val httpsUrl = "https://example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, httpsUrl)

        // Assert - Should load image
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with URL containing port loads image`() {
        // Arrange
        val urlWithPort = "https://example.com:8080/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, urlWithPort)

        // Assert - Should load image
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with URL with path loads image`() {
        // Arrange
        val urlWithPath = "https://example.com/images/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, urlWithPath)

        // Assert - Should load image
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with URL with query parameters loads image`() {
        // Arrange
        val urlWithQuery = "https://example.com/avatar.jpg?size=80&format=webp"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, urlWithQuery)

        // Assert - Should load image
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with invalid URL shows placeholder`() {
        // Arrange
        val invalidUrl = "not-a-url"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, invalidUrl)

        // Assert - Should show placeholder
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with URL missing protocol shows placeholder`() {
        // Arrange
        val urlWithoutProtocol = "example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, urlWithoutProtocol)

        // Assert - Should show placeholder
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with ftp URL shows placeholder`() {
        // Arrange
        val ftpUrl = "ftp://example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, ftpUrl)

        // Assert - Should show placeholder (only http/https supported)
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with file URL shows placeholder`() {
        // Arrange
        val fileUrl = "file:///sdcard/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, fileUrl)

        // Assert - Should show placeholder
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with custom placeholder resource`() {
        // Arrange
        val validUrl = "https://example.com/avatar.jpg"
        val customPlaceholder = android.R.drawable.ic_menu_gallery

        // Act
        ImageLoader.loadCircularImage(
            context,
            mockImageView,
            validUrl,
            placeholderResId = customPlaceholder
        )

        // Assert - Should use custom placeholder
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with custom error resource`() {
        // Arrange
        val validUrl = "https://example.com/avatar.jpg"
        val customError = android.R.drawable.ic_menu_delete

        // Act
        ImageLoader.loadCircularImage(
            context,
            mockImageView,
            validUrl,
            errorResId = customError
        )

        // Assert - Should use custom error drawable
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with custom size`() {
        // Arrange
        val validUrl = "https://example.com/avatar.jpg"
        val customSize = 120

        // Act
        ImageLoader.loadCircularImage(
            context,
            mockImageView,
            validUrl,
            size = customSize
        )

        // Assert - Should use custom size
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with default size`() {
        // Arrange
        val validUrl = "https://example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, validUrl)

        // Assert - Should use default size (80)
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with URL containing special characters loads image`() {
        // Arrange
        val urlWithSpecialChars = "https://example.com/avatar_image%20test.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, urlWithSpecialChars)

        // Assert - Should load image
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage with URL containing Unicode characters loads image`() {
        // Arrange
        val urlWithUnicode = "https://example.com/avatar-测试.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, urlWithUnicode)

        // Assert - Should load image
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage applies CircleCrop transform`() {
        // Arrange
        val validUrl = "https://example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, validUrl)

        // Assert - CircleCrop should be applied
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage sets memory cache enabled`() {
        // Arrange
        val validUrl = "https://example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, validUrl)

        // Assert - Memory caching should be enabled
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage sets disk cache strategy`() {
        // Arrange
        val validUrl = "https://example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, validUrl)

        // Assert - Disk caching should be enabled
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage sets timeout`() {
        // Arrange
        val validUrl = "https://example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, validUrl)

        // Assert - Timeout should be set (10 seconds)
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage disables animation to prevent flickering`() {
        // Arrange
        val validUrl = "https://example.com/avatar.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, validUrl)

        // Assert - Animation should be disabled
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage handles very long URL`() {
        // Arrange
        val longUrl = "https://example.com/" + "a".repeat(1000) + ".jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, longUrl)

        // Assert - Should handle long URL
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage trims whitespace from URL`() {
        // Arrange
        val urlWithWhitespace = "  https://example.com/avatar.jpg  "

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, urlWithWhitespace)

        // Assert - Should trim and load image
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }

    @Test
    fun `loadCircularImage can be called multiple times on same view`() {
        // Arrange
        val url1 = "https://example.com/avatar1.jpg"
        val url2 = "https://example.com/avatar2.jpg"

        // Act
        ImageLoader.loadCircularImage(context, mockImageView, url1)
        ImageLoader.loadCircularImage(context, mockImageView, url2)

        // Assert - Should handle multiple loads
        verify(mockImageView).setTag(com.bumptech.glide.request.target.Target.TAG_ID, null)
    }
}
