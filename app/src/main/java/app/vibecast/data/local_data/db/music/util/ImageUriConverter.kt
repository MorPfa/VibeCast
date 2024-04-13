package app.vibecast.data.local_data.db.music.util

import androidx.room.TypeConverter
import com.spotify.protocol.types.ImageUri

class ImageUriConverter {

    @TypeConverter
    fun fromImageUri(imageUri: ImageUri?): String? {
        return imageUri?.raw
    }

    @TypeConverter
    fun toImageUri(raw: String?): ImageUri? {
        return if (raw != null) ImageUri(raw) else null
    }
}
