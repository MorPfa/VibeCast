package app.vibecast.data.local.db.image

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Query("SELECT * FROM images")
    fun getAllImages() : Flow<List<ImageEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImage(imageEntity: ImageEntity)

    @Delete
    suspend fun deleteImage(imageEntity: ImageEntity)
}