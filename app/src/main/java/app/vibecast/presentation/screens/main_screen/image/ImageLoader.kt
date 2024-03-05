package app.vibecast.presentation.screens.main_screen.image

import android.content.Context
import android.widget.ImageView
import androidx.core.content.ContextCompat
import app.vibecast.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import javax.inject.Inject

class ImageLoader @Inject constructor(@ApplicationContext private val context: Context) {

    fun loadUrlIntoImageView(url: String, imageView: ImageView, applyFilter : Boolean) {
        if(applyFilter) {
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.gallery_image_placeholder)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .into(imageView)
        }
        else {
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.gallery_image_placeholder)
                .into(imageView)
        }

    }
}