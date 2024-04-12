package app.vibecast.data.local_data.data_source.music

import app.vibecast.data.local_data.db.music.dao.SongDao
import app.vibecast.data.local_data.db.music.model.SongEntity
import app.vibecast.data.remote_data.network.music.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LocalMusicDataSourceImpl @Inject constructor(private val songDao: SongDao) :
    LocalMusicDataSource {

    override suspend fun saveSong(song: Song) {
        try {
            songDao.saveSong(SongEntity(url = song.url, uri = song.uri, album = song.album, previewUrl = song.previewUrl))
        } catch (e : Exception){

        }

    }

    override suspend fun deleteSong(song: Song) {
        try {
            songDao.deleteSong(SongEntity(url = song.url, uri = song.uri, album = song.album, previewUrl = song.previewUrl))
        } catch (e : Exception){

        }
    }

    override fun getSavedSong(song: Song): Flow<Song> = flow {
        try {
            songDao.getSavedSong(song.uri).collect {
            }
        } catch (e: Exception) {

        }
    }

    override fun getAllSavedSongs(): Flow<List<Song>> = flow {
        try {
            songDao.getAllSongs().collect {
            }
        } catch (e: Exception) {

        }
    }


}