package com.zandeveloper.tiktokly.utils.uiHandler

import android.widget.ImageView
import android.widget.TextView
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.engine.DiskCacheStrategy

class UiHandler(
private val input: TextView,
    private val title: TextView,
    private val thumbnail: ImageView
    ) {

    fun showThumbnail(imageView: ImageView, url: String) {
    
        Glide.with(imageView.context)
          .load(url)
          .thumbnail(0.25f) 
          .override(600, 600)
          .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
          .transform(
        CenterCrop(),
        RoundedCorners(20))
        .into(imageView)
    }

    fun clearThumbnail(imageView: ImageView) {
        Glide.with(imageView.context).clear(imageView)
        imageView.setImageDrawable(null)
    }

    fun clearText(vararg views: View) {
        views.forEach {
            when(it) {
                is android.widget.TextView -> it.text = ""
                is android.widget.EditText -> it.text?.clear()
            }
        }
    }

    fun showShimmer(shimmer: View, content: View) {
        shimmer.visibility = View.VISIBLE
        content.visibility = View.GONE
    }

    fun hideShimmer(shimmer: View, content: View) {
        shimmer.visibility = View.GONE
        content.visibility = View.VISIBLE
    }
}