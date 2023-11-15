package app.vibecast.presentation.image

import android.content.Context
import android.widget.ImageView
import app.vibecast.R
import com.bumptech.glide.Glide

class ImageLoader(private val context: Context) {

    fun loadUrlIntoImageView(url: String, imageView: ImageView) {
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.gallery_image_placeholder)
            .into(imageView)

        //TODO figure out DiskCacheStrategy
    }
}