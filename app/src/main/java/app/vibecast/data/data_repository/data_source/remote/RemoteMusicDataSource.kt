package app.vibecast.data.data_repository.data_source.remote

import app.vibecast.data.remote.network.music.PlaylistApiModel
import kotlinx.coroutines.flow.Flow

interface RemoteMusicDataSource {

    fun getPlaylist(category : String, accessCode : String) : Flow<PlaylistApiModel>
}