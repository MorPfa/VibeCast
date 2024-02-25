package app.vibecast.data.data_repository.repository

import android.util.Log
import app.vibecast.data.TAGS.MUSIC_ERROR
import app.vibecast.data.data_repository.data_source.remote.RemoteMusicDataSource
import app.vibecast.data.remote.network.music.PlaylistApiModel
import app.vibecast.domain.repository.MusicRepository
import app.vibecast.presentation.TAG
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