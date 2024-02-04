package app.vibecast.presentation.image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageSaver {

    /**
     * Saves an image from the specified URL to the device's gallery using Glide library.
     *
     * @param imageUrl The URL of the image to be saved.
     * @param context The application or activity context.
     */
    fun saveImageFromUrlToGallery(imageUrl: String, context: Context) {
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // When the image is loaded successfully, save it to the gallery
                    saveImageToGallery(resource, context)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Not needed in this case
                }
            })
    }

    /**
     * Saves the provided Bitmap image to the device's gallery.
     *
     * @param imageBitmap The Bitmap image to be saved.
     * @param context The application or activity context.
     */
    private fun saveImageToGallery(imageBitmap: Bitmap, context: Context) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"

        // Create an image file and save it to the gallery
        val imageFile = createImageFile(context, imageFileName)

        if (imageFile != null) {
            saveImageToFile(imageBitmap, imageFile)
            addImageToGallery(context, imageFile)
        }
    }

    /**
     * Creates a temporary image file with a unique name.
     *
     * @param context The application or activity context.
     * @param imageFileName The name to be given to the image file.
     * @return A File object representing the created image file.
     */
    private fun createImageFile(context: Context, imageFileName: String): File? {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (e: IOException) {
            // Handle IOException, if any
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves the provided Bitmap image to a file on the device.
     *
     * @param imageBitmap The Bitmap image to be saved.
     * @param imageFile The File object representing the destination file.
     */
    private fun saveImageToFile(imageBitmap: Bitmap, imageFile: File) {
        try {
            val fos = FileOutputStream(imageFile)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            // Handle IOException, if any
            e.printStackTrace()
        }
    }

    /**
     * Adds the saved image to the device's gallery.
     *
     * @param context The application or activity context.
     * @param imageFile The File object representing the saved image file.
     */
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
                // Show a success message
                Toast.makeText(context, "Image saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                // Handle IOException, if any
                e.printStackTrace()
                // Show an error message
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
