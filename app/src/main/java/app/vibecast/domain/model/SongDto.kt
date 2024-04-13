package app.vibecast.domain.model

import com.spotify.protocol.types.ImageUri

data class SongDto(
    val album: String,
    val name : String,
    val imageUri : ImageUri?,
    val url: String,
    val uri: String,
    val previewUrl: String?
)
