package app.vibecast.data.local.db.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "alt_description") val altDescription : String?,
    @ColumnInfo(name = "regular_url") val regularUrl: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "portfolio_url") val portfolioUrl: String?,
    @ColumnInfo(name = "user_link") val userLink: String,
    @ColumnInfo(name = "download_location") val downloadLink: String,
    @ColumnInfo(name = "timestamp") val timestamp : Long
)