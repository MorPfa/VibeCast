package app.vibecast.data.remote_data.data_source.music


import app.vibecast.domain.model.PlaylistDto
import app.vibecast.domain.model.TracksDto
import kotlinx.coroutines.flow.Flow

interface RemoteMusicDataSource {

    fun getPlaylist(category: String, accessCode: String): Flow<PlaylistDto>

    fun getCurrentSong(song: String, artist: String, accessCode: String): Flow<TracksDto>
}