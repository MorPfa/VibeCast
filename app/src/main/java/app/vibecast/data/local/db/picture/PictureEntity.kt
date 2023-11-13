package app.vibecast.data.local.db.picture

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pictures")
data class PictureEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")val id : Int
)
//Fill with real db schema for picture api response