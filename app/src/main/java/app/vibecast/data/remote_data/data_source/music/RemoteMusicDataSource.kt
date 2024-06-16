package app.vibecast.data.remote_data.data_source.music


import app.vibecast.domain.model.PlaylistDto
import app.vibecast.domain.model.TracksDto
import app.vibecast.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface RemoteMusicDataSource {

    suspend fun getPlaylist(category: String, accessCode: String): Resource<PlaylistDto>

    suspend fun getCurrentSong(song: String, artist: String, accessCode: String): Resource<TracksDto>
}