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
    
    private val urlPattern = Regex("^https?://[\\w.-]+(:\\d+)?(/.*)?$")

    private fun isValidUrl(url: String): Boolean {
        return urlPattern.matches(url)
    }
}