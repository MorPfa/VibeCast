package app.vibecast.data.remote_data.network.music.model

import app.vibecast.domain.model.SongDto
import com.spotify.protocol.types.ImageUri

data class SongModel(
    val name : String,
    val album: String,
    val artist: String,
    val imageUri : ImageUri?,
    val trackUri: String,
    val artistUri: String,
    val albumUri: String,
    val url: String,
    val previewUrl: String?
) {
    fun toSongDto(): SongDto {
        return SongDto(
            album = this.album,
            name = this.name,
            imageUri = this.imageUri,
            url = this.url,
            trackUri = this.trackUri,
            previewUrl = this.previewUrl,
            artistUri = this.artistUri,
            albumUri = this.albumUri,
            artist = this.artist
        )
    }
}
