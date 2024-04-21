package app.vibecast.data.local_data.data_source.music

import app.vibecast.data.local_data.db.music.dao.SongDao
import app.vibecast.data.local_data.db.music.model.SongEntity
import app.vibecast.domain.model.SongDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class LocalMusicDataSourceImpl @Inject constructor(private val songDao: SongDao) :
    LocalMusicDataSource {

    override suspend fun saveSong(song: SongDto) {
        try {
            songDao.saveSong(
                SongEntity(
                    url = song.url,
                    uri = song.trackUri,
                    album = song.album,
                    imageUri = song.imageUri!!,
                    name = song.name,
                    previewUrl = song.previewUrl,
                    artist = song.artist,
                    albumUri = song.albumUri,
                    artistUri = song.artistUri
                )
            )
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't save song $e")
        }

    }

    override suspend fun deleteSong(song: SongDto) {
        try {
            songDao.deleteSong(
                SongEntity(
                    url = song.url,
                    uri = song.trackUri,
                    album = song.album,
                    imageUri = song.imageUri!!,
                    name = song.name,
                    previewUrl = song.previewUrl,
                    artist = song.artist,
                    albumUri = song.albumUri,
                    artistUri = song.artistUri
                )
            )
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't delete song $e")
        }
    }

    override fun getSavedSong(song: SongDto): Flow<SongDto> = flow {
        try {
            songDao.getSavedSong(song.trackUri).collect {
                emit(SongDto(
                    album = it.album,
                    name = it.name,
                    url = it.url,
                    trackUri = it.uri,
                    imageUri = song.imageUri,
                    previewUrl = it.previewUrl,
                    artist = it.artist,
                    artistUri = it.artist,
                    albumUri = song.albumUri,
                ))
            }
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't get song")
        }
    }

    override fun getAllSavedSongs(): Flow<List<SongDto>> = flow {
        try {
            songDao.getAllSongs().collect { songEntityList ->
                val songList = songEntityList.map { songEntity ->
                    SongDto(
                        album = songEntity.album,
                        name = songEntity.name,
                        imageUri = songEntity.imageUri,
                        url = songEntity.url,
                        trackUri = songEntity.uri,
                        previewUrl = songEntity.previewUrl,
                        artist = songEntity.artist,
                        albumUri = songEntity.albumUri,
                        artistUri = songEntity.artistUri
                    )
                }
                emit(songList)
            }
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't get songs")
        }
    }
}