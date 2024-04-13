package app.vibecast.data.local_data.db.music.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spotify.protocol.types.ImageUri

@Entity(tableName = "songs")
data class SongEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "uri") val uri: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "album") val album: String,
    @ColumnInfo(name = "imageUri") val imageUri: ImageUri,
    @ColumnInfo(name = "url") val url : String,
    @ColumnInfo(name = "previewUrl") val previewUrl : String?,
)