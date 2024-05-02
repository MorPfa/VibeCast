package app.vibecast.domain.model


data class FirebaseSong(
    val name: String = "",
    val album: String = "",
    val artist: String = "",
    val imageUri: String = "",
    val trackUri: String = "",
    val artistUri: String = "",
    val albumUri: String = "",
    val url: String = "",
    val previewUrl: String? = null,
)