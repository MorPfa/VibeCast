package app.vibecast.data.local_data.db.music.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_playlists")
data class UserPlaylistEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "playlist_id") val playlistID: String,
    @ColumnInfo(name = "playlist_name") val playlistName: String,


)