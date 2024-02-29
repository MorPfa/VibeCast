package app.vibecast.domain.repository

import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    fun getPlaylist(category : String, accessCode : String) : Flow<PlaylistApiModel>
}