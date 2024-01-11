package app.vibecast.presentation.image



import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import app.vibecast.presentation.TAG
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ImageSaver {

    fun saveImageFromUrlToGallery(imageUrl: String, context: Context) {
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                    saveImageToGallery(resource, context)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Not needed in this case
                }
            })
    }

    private fun saveImageToGallery(imageBitmap: Bitmap, context: Context) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"

        val imageFile = createImageFile(context, imageFileName)

        if (imageFile != null) {
            saveImageToFile(imageBitmap, imageFile)
            addImageToGallery(context, imageFile)
        }
    }

    private fun createImageFile(context: Context, imageFileName: String): File? {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImageToFile(imageBitmap: Bitmap, imageFile: File) {
        try {
            val fos = FileOutputStream(imageFile)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun addImageToGallery(context: Context, imageFile: File) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val resolver = context.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            try {
                val outputStream = resolver.openOutputStream(it)
                outputStream?.use { stream ->
                    val bitmapStream = imageFile.inputStream()
                    bitmapStream.use { input ->
                        input.copyTo(stream)
                    }
                }
                Toast.makeText(context, "Image saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
