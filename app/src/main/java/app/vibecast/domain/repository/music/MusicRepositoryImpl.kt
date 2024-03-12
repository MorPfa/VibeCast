package app.vibecast.domain.repository.music

import app.vibecast.data.remote_data.data_source.music.RemoteMusicDataSource
import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val musicDataSource: RemoteMusicDataSource,
) : MusicRepository {
    override fun getPlaylist(category: String, accessCode : String): Flow<PlaylistApiModel> = flow {
        try {
            musicDataSource.getPlaylist(category, accessCode).collect {
                emit(it)
            }
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }

    }


}