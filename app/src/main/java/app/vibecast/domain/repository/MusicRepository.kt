package app.vibecast.domain.repository

import app.vibecast.data.remote.network.music.PlaylistApiModel
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    fun getPlaylist(category : String, accessCode : String) : Flow<PlaylistApiModel>
}