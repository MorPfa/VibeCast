package app.vibecast.data.local_data.db.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "userId") val id : Long,
    @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "userName") val userName : String,
    @ColumnInfo(name = "email") val email : String,
    @ColumnInfo(name = "profilePicture") val profilePicture : String

    )