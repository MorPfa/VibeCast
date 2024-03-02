package app.vibecast.domain.repository.music

import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.domain.model.TokenDto
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    fun getPlaylist(category : String, accessCode : String) : Flow<PlaylistApiModel>


}