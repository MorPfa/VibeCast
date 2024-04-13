package app.vibecast.data.remote_data.network.music.model

import app.vibecast.domain.model.SongDto

data class SongModel(
    val album: String,
    val name : String,
    val url: String,
    val uri : String,
    val previewUrl: String?
) {
    fun toSongDto(): SongDto {
        return SongDto(
            album = this.album,
            name = this.name,
            imageUri = null,
            url = this.url,
            uri = this.uri,
            previewUrl = this.previewUrl
        )
    }
}
