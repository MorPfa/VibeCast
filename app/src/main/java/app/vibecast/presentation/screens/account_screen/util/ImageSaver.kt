package app.vibecast.presentation.screens.account_screen.util

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object ImageSaver {

    private val firebase: FirebaseAuth = Firebase.auth

    private val currentUser = firebase.currentUser
    fun saveImageToInternalStorage(bitmap: Bitmap, context: Context): Uri? {
        val wrapper = ContextWrapper(context)
        val file =
            File(wrapper.getDir("profile_images", Context.MODE_PRIVATE), "${currentUser?.uid}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            Timber.tag("profile_picture").d("Error when saving image $e")
            return null
        }

        return Uri.parse(file.absolutePath)
    }

    fun loadImageFromInternalStorage(context: Context): Bitmap? {
        val wrapper = ContextWrapper(context)
        val file =
            File(wrapper.getDir("profile_images", Context.MODE_PRIVATE), "${currentUser?.uid}.jpg")

        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }
}