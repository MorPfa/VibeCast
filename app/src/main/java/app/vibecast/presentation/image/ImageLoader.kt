package app.vibecast.presentation.image

import android.content.Context
import android.util.Log
import android.widget.ImageView
import app.vibecast.R
import app.vibecast.presentation.TAG
import com.bumptech.glide.Glide
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ImageLoader @Inject constructor(@ApplicationContext private val context: Context) {

    fun loadUrlIntoImageView(url: String, imageView: ImageView) {
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.gallery_image_placeholder)
            .into(imageView)

    }
}