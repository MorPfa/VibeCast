package app.vibecast.presentation.util

data class SaveResult(
    val success: Boolean,
    val playlistName : String? = null,
    val error : String? = null
)
