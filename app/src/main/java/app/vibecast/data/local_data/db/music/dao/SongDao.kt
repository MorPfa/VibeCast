package app.vibecast.data.local_data.db.music.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import app.vibecast.data.local_data.db.music.model.SongEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface SongDao {

    @Upsert
    suspend fun saveSong(song: SongEntity)

    @Delete
    suspend fun deleteSong(song: SongEntity)
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Query("SELECT * FROM songs WHERE uri == :uri")
    fun getSavedSong(uri : String) : Flow<SongEntity>

    @Query("SELECT * FROM songs")
    fun getAllSongs() : Flow<List<SongEntity>>
}