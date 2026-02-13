package com.example.iurankomplek.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.iurankomplek.R

object ImageLoader {
    fun loadCircularImage(
        context: Context,
        imageView: ImageView,
        url: String?,
        placeholderResId: Int = R.drawable.icon_avatar,
        errorResId: Int = R.drawable.icon_avatar,
        size: Int = 80
    ) {
        // More robust URL validation and handling
        val validUrl = url?.trim()?.takeIf { 
            it.isNotBlank() && isValidUrl(it)
        }
        
        Glide.with(context)
            .load(validUrl)
            .apply(
                RequestOptions()
                    .override(size, size)
                    .placeholder(placeholderResId)
                    .error(errorResId)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache both original and resized images
                    .skipMemoryCache(false)  // Enable memory caching
                    .dontAnimate() // Prevent flickering during loading
                    .timeout(10000) // 10 second timeout for image loading
            )
            .transform(CircleCrop())
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Log the error for debugging purposes
                    e?.let {
                        android.util.Log.e("ImageLoader", "Error loading image: $url", it)
                    }
                    return false // Allow Glide to show error placeholder
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false // Allow Glide to handle the resource normally
                }
            })
            .into(imageView)
    }
    
    /**
     * Regex for validating HTTPS URLs for avatar images.
     * Security requirements:
     * - Only HTTPS (no HTTP for security)
     * - No username/password in URL (prevents credential leaks)
     * - Valid domain format with at least one dot
     * - Optional port number
     * - Optional path
     */
    private val urlPattern = Regex(
        "^https://" +                    // Only HTTPS, no HTTP
        "(?!.*@)" +                     // No username/password (no @ symbol)
        "[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]" +  // Domain name (subdomain)
        "(\\.[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9])*" +  // Additional subdomains
        "\\.[a-zA-Z]{2,}" +            // TLD (at least 2 chars)
        "(:\\d{1,5})?" +               // Optional port (1-5 digits)
        "(/[^\\s]*)?$"                 // Optional path
    )

    private fun isValidUrl(url: String): Boolean {
        // Additional security checks beyond regex
        if (url.isBlank()) return false
        
        // Reject URLs with embedded credentials (defense in depth)
        if (url.contains("@")) {
            android.util.Log.w("ImageLoader", "Rejected URL with embedded credentials")
            return false
        }
        
        // Reject HTTP URLs explicitly (security requirement for avatars)
        if (url.startsWith("http://", ignoreCase = true)) {
            android.util.Log.w("ImageLoader", "Rejected HTTP URL (HTTPS required): $url")
            return false
        }
        
        return urlPattern.matches(url)
    }
}