package com.example.iurankomplek.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import java.net.URL

object ImageLoader {
    fun loadCircularImage(
        context: Context,
        imageView: ImageView,
        url: String?,
        placeholderResId: Int = com.example.iurankomplek.R.drawable.icon_avatar,
        errorResId: Int = com.example.iurankomplek.R.drawable.icon_avatar,
        size: Int = 80
    ) {
        val validUrl = url.takeIf { it.isNotBlank() && isValidUrl(it) }
        
        Glide.with(context)
            .load(validUrl)
            .apply(
                RequestOptions()
                    .override(size, size)
                    .placeholder(placeholderResId)
                    .error(errorResId)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
            )
            .transform(CircleCrop())
            .into(imageView)
    }
    
    private fun isValidUrl(url: String): Boolean {
        return try {
            URL(url).toURI()
            true
        } catch (e: Exception) {
            false
        }
    }
}