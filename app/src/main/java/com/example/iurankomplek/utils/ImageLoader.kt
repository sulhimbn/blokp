package com.example.iurankomplek.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
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
                    .timeout(Constants.Image.LOAD_TIMEOUT_MS.toInt()) // 10 second timeout for image loading
            )
            .transform(CircleCrop())
            .into(imageView)
    }
    
    private val urlPattern = Regex("^https?://[\\w.-]+(:\\d+)?(/.*)?$")

    private fun isValidUrl(url: String): Boolean {
        return urlPattern.matches(url)
    }
}