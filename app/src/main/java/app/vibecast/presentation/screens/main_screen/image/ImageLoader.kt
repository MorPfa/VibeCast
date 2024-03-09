package app.vibecast.presentation.screens.main_screen.image

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import app.vibecast.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.wasabeef.glide.transformations.BlurTransformation
import javax.inject.Inject

class ImageLoader @Inject constructor(@ApplicationContext private val context: Context) {

    @SuppressLint("CheckResult")
    fun loadUrlIntoImageView(
        url: String,
        imageView: ImageView,
        applyFilter: Boolean,
        cornerRadius: Int,
    ) {
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.gallery_image_placeholder)

        if (applyFilter) {
            if (cornerRadius != 0) {
                requestOptions.transform(RoundedCorners(cornerRadius))
            } else {
                // Apply blur transformation
                requestOptions.transform(BlurTransformation(2, 3))
            }
        }

        Glide.with(context)
            .load(url)
            .apply(requestOptions)
            .into(imageView)
    }
}