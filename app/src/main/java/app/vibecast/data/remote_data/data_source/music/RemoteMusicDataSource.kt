package app.vibecast.data.remote_data.data_source.music

import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.data.remote_data.network.music.model.SearchModel
import kotlinx.coroutines.flow.Flow

interface RemoteMusicDataSource {

    fun getPlaylist(category : String, accessCode : String) : Flow<PlaylistApiModel>

    fun getCurrentSong(song: String, artist: String, accessCode : String): Flow<SearchModel>
}